package com.minecraft.core.api.collectible.type.title;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCategory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class TitleCollectible extends Collectible {

    private static final Map<UUID, ArmorStand> activeTitles = new HashMap<>();
    private static final Map<UUID, TitleCollectible> playerTitles = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> positionTasks = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> textTasks = new HashMap<>();
    
    private static final int MAX_TITLE_LENGTH = 128; // Minecraft limit for entity custom names

    private final TitleCategory titleCategory;

    public TitleCollectible(String name, TitleCategory titleCategory, CollectibleRarity rarity, List<RankType> ranks, long releasedAt) {
        super(name, CollectibleCategory.TITLE, rarity, ranks, releasedAt);
        this.titleCategory = titleCategory;
    }

    public TitleCategory getTitleCategory() {
        return titleCategory;
    }

    public abstract String getTitle();
    
    private static String truncateTitle(String title) {
        if (title == null) return "";
        if (title.length() <= MAX_TITLE_LENGTH) return title;
        return title.substring(0, MAX_TITLE_LENGTH - 3) + "...";
    }

    public String getSubtitle() {
        return null;
    }

    public String getDynamicTitle(Player player) {
        return getTitle();
    }

    public void showTitle(Player player) {
        CustomTitle.removeTitle(player);
        removeTitle(player);

        double altura = 2.3;

        Location loc = player.getLocation().clone();
        loc.setYaw(0);
        loc.setPitch(0);
        loc.add(0, altura, 0);

        ArmorStand armorStand = player.getWorld().spawn(loc, ArmorStand.class);

        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCanPickupItems(false);
        armorStand.setCustomNameVisible(true);
        armorStand.setCustomName(truncateTitle(getDynamicTitle(player)));
        armorStand.setMarker(true);
        armorStand.setSmall(true);
        armorStand.setGravity(false);

        activeTitles.put(player.getUniqueId(), armorStand);
        playerTitles.put(player.getUniqueId(), this);

        BukkitRunnable positionTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }
                ArmorStand as = activeTitles.get(player.getUniqueId());
                if (as == null || as.isDead()) {
                    this.cancel();
                    return;
                }
                Location newLoc = player.getLocation().clone();
                newLoc.setYaw(0);
                newLoc.setPitch(0);
                newLoc.add(0, altura, 0);
                as.teleport(newLoc);
            }
        };
        positionTask.runTaskTimer(Bukkit.getPluginManager().getPlugin("Lobby"), 0L, 1L);
        positionTasks.put(player.getUniqueId(), positionTask);

        BukkitRunnable textTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }
                ArmorStand as = activeTitles.get(player.getUniqueId());
                if (as == null || as.isDead()) {
                    this.cancel();
                    return;
                }

                String currentName = as.getCustomName();
                String newTitle = truncateTitle(getDynamicTitle(player));

                if (currentName == null || !currentName.equals(newTitle)) {
                    as.setCustomName(newTitle);
                }

                Account account = Core.getAccountController().of(player.getUniqueId());
                boolean canSee = account == null || (account.getToggle().isShowTitle() && account.getToggle().isShowOwnTitle());

                if (player.hasMetadata("vanished") && player.getMetadata("vanished").get(0).asBoolean()) {
                    as.setCustomNameVisible(false);
                } else {
                    as.setCustomNameVisible(canSee);
                }
            }
        };
        textTask.runTaskTimer(Bukkit.getPluginManager().getPlugin("Lobby"), 0L, 20L);
        textTasks.put(player.getUniqueId(), textTask);
    }

    public static void removeTitle(Player player) {
        ArmorStand armorStand = activeTitles.remove(player.getUniqueId());
        if (armorStand != null && !armorStand.isDead()) {
            armorStand.remove();
        }
        BukkitRunnable positionTask = positionTasks.remove(player.getUniqueId());
        if (positionTask != null) {
            positionTask.cancel();
        }
        BukkitRunnable textTask = textTasks.remove(player.getUniqueId());
        if (textTask != null) {
            textTask.cancel();
        }
        playerTitles.remove(player.getUniqueId());
    }

    public static void reapplyTitle(Player player) {
        TitleCollectible title = playerTitles.get(player.getUniqueId());
        if (title != null && player.isOnline()) {
            title.showTitle(player);
            return;
        }

        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        List<String> activeTitlesList = account.getActiveCollectibles();
        for (String id : activeTitlesList) {
            if (id.startsWith("TITLE:")) {
                Collectible collectible = Core.getCollectibleController().of(id);
                if (collectible != null && collectible instanceof TitleCollectible && player.isOnline()) {
                    ((TitleCollectible) collectible).showTitle(player);
                    break;
                }
            }
        }
    }

    public static void removeAllTitles() {
        for (ArmorStand armorStand : activeTitles.values()) {
            if (armorStand != null && !armorStand.isDead()) {
                armorStand.remove();
            }
        }
        for (BukkitRunnable task : positionTasks.values()) {
            task.cancel();
        }
        for (BukkitRunnable task : textTasks.values()) {
            task.cancel();
        }
        activeTitles.clear();
        positionTasks.clear();
        textTasks.clear();
        playerTitles.clear();
    }

    public static void updateTitleVisibility(Player player, boolean visible) {
        ArmorStand armorStand = activeTitles.get(player.getUniqueId());
        if (armorStand != null && !armorStand.isDead()) {
            armorStand.setCustomNameVisible(visible);
        }
    }
}
