package com.minecraft.core.api.collectible.type.title;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomTitle {

    private static final Map<UUID, ArmorStand> activeCustomTitles = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> positionTasks = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> textTasks = new HashMap<>();

    public static void showTitle(Player player) {
        TitleCollectible.removeTitle(player);
        removeTitle(player);

        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        String customTitle = account.getCustomTitle();
        if (customTitle == null || customTitle.isEmpty()) return;

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
        armorStand.setCustomName(customTitle);
        armorStand.setMarker(true);
        armorStand.setSmall(true);
        armorStand.setGravity(false);

        activeCustomTitles.put(player.getUniqueId(), armorStand);

        BukkitRunnable positionTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }
                ArmorStand as = activeCustomTitles.get(player.getUniqueId());
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
                ArmorStand as = activeCustomTitles.get(player.getUniqueId());
                if (as == null || as.isDead()) {
                    this.cancel();
                    return;
                }

                Account acc = Core.getAccountController().of(player.getUniqueId());
                String newTitle = acc != null ? acc.getCustomTitle() : "";

                String currentName = as.getCustomName();
                if (currentName == null || !currentName.equals(newTitle)) {
                    as.setCustomName(newTitle);
                }

                if (player.hasMetadata("vanished") && player.getMetadata("vanished").get(0).asBoolean()) {
                    as.setCustomNameVisible(false);
                } else {
                    as.setCustomNameVisible(true);
                }
            }
        };
        textTask.runTaskTimer(Bukkit.getPluginManager().getPlugin("Lobby"), 0L, 20L);
        textTasks.put(player.getUniqueId(), textTask);
    }

    public static void removeTitle(Player player) {
        ArmorStand armorStand = activeCustomTitles.remove(player.getUniqueId());
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
    }

    public static void reapplyTitle(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        String customTitle = account.getCustomTitle();
        if (customTitle != null && !customTitle.isEmpty() && player.isOnline()) {
            showTitle(player);
        }
    }

    public static boolean hasCustomTitle(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return false;

        String customTitle = account.getCustomTitle();
        return customTitle != null && !customTitle.isEmpty();
    }

    public static void updateTitleVisibility(Player player, boolean visible) {
        ArmorStand armorStand = activeCustomTitles.get(player.getUniqueId());
        if (armorStand != null && !armorStand.isDead()) {
            armorStand.setCustomNameVisible(visible);
        }
    }
}
