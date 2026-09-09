package com.minecraft.core.api.collectible.type.trail;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.util.particle.ParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TrailCollectible extends Collectible {

    private static final Map<UUID, TrailData> activeTrails = new HashMap<>();
    private static final Map<UUID, WoolColor> savedColors = new HashMap<>();

    public static final WoolColor[] WOOL_COLORS = {
            new WoolColor(14, "Vermelho", (short) 14),
            new WoolColor(1, "Laranja", (short) 1),
            new WoolColor(4, "Amarelo", (short) 4),
            new WoolColor(5, "Verde", (short) 5),
            new WoolColor(3, "Ciano", (short) 3),
            new WoolColor(11, "Azul", (short) 11),
            new WoolColor(10, "Roxo", (short) 10),
            new WoolColor(9, "Rosa", (short) 9),
    };

    private static final int BLOCK_EXPIRE_TICKS = 60;

    public static class WoolColor {
        public final int dyeData;
        public final String name;
        public final short woolData;

        WoolColor(int dyeData, String name, short woolData) {
            this.dyeData = dyeData;
            this.name = name;
            this.woolData = woolData;
        }

        public int getRed() {
            switch (dyeData) {
                case 14: return 255;
                case 1: return 255;
                case 4: return 255;
                case 5: return 50;
                case 3: return 0;
                case 11: return 50;
                case 10: return 200;
                case 9: return 255;
                default: return 255;
            }
        }

        public int getGreen() {
            switch (dyeData) {
                case 14: return 0;
                case 1: return 128;
                case 4: return 255;
                case 5: return 255;
                case 3: return 255;
                case 11: return 0;
                case 10: return 0;
                case 9: return 105;
                default: return 255;
            }
        }

        public int getBlue() {
            switch (dyeData) {
                case 14: return 0;
                case 1: return 0;
                case 4: return 0;
                case 5: return 50;
                case 3: return 255;
                case 11: return 255;
                case 10: return 255;
                case 9: return 180;
                default: return 255;
            }
        }
    }

    private static class TrailData {
        final WoolColor selectedColor;
        final Queue<PlacedBlock> placedBlocks;
        org.bukkit.scheduler.BukkitRunnable trailTask;
        Location[] lastLocation = new Location[1];

        TrailData(WoolColor color) {
            this.selectedColor = color;
            this.placedBlocks = new LinkedList<>();
        }

        void cleanup() {
            if (trailTask != null) {
                trailTask.cancel();
            }
            for (PlacedBlock block : placedBlocks) {
                block.revert();
            }
            placedBlocks.clear();
        }
    }

    private static class PlacedBlock {
        final Block block;
        final int originalTypeId;
        final byte originalData;
        final long placedAt;

        PlacedBlock(Block block, int originalTypeId, byte originalData) {
            this.block = block;
            this.originalTypeId = originalTypeId;
            this.originalData = originalData;
            this.placedAt = System.currentTimeMillis();
        }

        void revert() {
            if (block != null && block.getWorld().isChunkLoaded(block.getChunk())) {
                block.setTypeIdAndData(originalTypeId, originalData, true);
            }
        }

        boolean shouldRemove() {
            return System.currentTimeMillis() - placedAt >= BLOCK_EXPIRE_TICKS * 50;
        }
    }

    private final WoolColor defaultColor;

    public TrailCollectible(WoolColor color) {
        super("Rastro " + color.name, CollectibleCategory.ARTIFACT, CollectibleRarity.EPIC, new ArrayList<>(), System.currentTimeMillis());
        this.defaultColor = color;
    }

    public WoolColor getColor() {
        return defaultColor;
    }

    public void handle(Player player) {
        UUID uuid = player.getUniqueId();

        TrailData existing = activeTrails.get(uuid);
        if (existing != null) {
            existing.cleanup();
            activeTrails.remove(uuid);
            savedColors.remove(uuid);
            removeTrailArrow(player);
            player.sendMessage("§c✦ Rastro desativado!");
            return;
        }

        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account != null) {
            account.activateCollectible(this);
        }

        player.sendMessage("§e✦ Rastro §" + getColorChar(defaultColor.dyeData) + defaultColor.name + " §eativado!");
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 2.0f);

        startTrail(player, defaultColor);
        savedColors.put(uuid, defaultColor);
        giveTrailArrow(player);
    }

    public static void openColorMenu(Player player) {
        int size = 18;
        org.bukkit.inventory.Inventory inv = Bukkit.createInventory(null, size, "§6§lEscolha a cor do rastro");

        for (int i = 0; i < Math.min(WOOL_COLORS.length, size); i++) {
            WoolColor color = WOOL_COLORS[i];
            ItemStack wool = new ItemStack(Material.WOOL, 1, color.woolData);
            org.bukkit.inventory.meta.ItemMeta meta = wool.getItemMeta();
            meta.setDisplayName("§" + getColorChar(color.dyeData) + color.name);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Clique para selecionar");
            lore.add("§7este rastro!");
            meta.setLore(lore);
            wool.setItemMeta(meta);
            inv.setItem(i, wool);
        }

        ItemStack info = new ItemStack(Material.BOOK);
        org.bukkit.inventory.meta.ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§e§lInformação");
        List<String> infoLore = new ArrayList<>();
        infoLore.add("");
        infoLore.add("§7Escolha a cor do rastro");
        infoLore.add("§7que ficará no chão");
        infoLore.add("§7enquanto você anda!");
        infoLore.add("");
        infoLore.add("§7Os blocos somem após");
        infoLore.add("§73 segundos cada.");
        infoMeta.setLore(infoLore);
        info.setItemMeta(infoMeta);
        inv.setItem(17, info);

        player.openInventory(inv);
    }

    public static void handleColorSelection(Player player, ItemStack clicked) {
        if (clicked == null || clicked.getType() != Material.WOOL) {
            return;
        }

        short woolData = clicked.getDurability();

        for (WoolColor color : WOOL_COLORS) {
            if (color.woolData == woolData) {
                player.closeInventory();
                selectColorAndStart(player, color);
                return;
            }
        }
    }

    public static void selectColorAndStart(Player player, WoolColor color) {
        UUID uuid = player.getUniqueId();

        TrailCollectible trail = new TrailCollectible(color);
        player.sendMessage("§e✦ Rastro §" + getColorChar(color.dyeData) + color.name + " §eativado!");
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 2.0f);

        TrailData existing = activeTrails.get(uuid);
        if (existing != null) {
            existing.cleanup();
        }

        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account != null) {
            for (String id : new ArrayList<>(account.getActiveCollectibles())) {
                if (id.startsWith("TRAIL:")) {
                    account.removeActiveCollectible(Core.getCollectibleController().of(id));
                }
            }
            account.setCollectible(trail.getIdentifier());
            account.activateCollectible(trail);
        }

        startTrail(player, color);
        savedColors.put(uuid, color);
        giveTrailArrow(player);
    }

    public static void giveTrailArrow(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack arrow = new ItemStack(Material.ARROW, 1);
        player.getInventory().setItem(0, arrow);
    }

    public static void removeTrailArrow(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack item = player.getInventory().getItem(0);
        if (item != null && item.getType() == Material.ARROW) {
            player.getInventory().setItem(0, null);
        }
    }

    public static void checkAndRemoveTrailArrow(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack item = player.getInventory().getItem(0);
        if (item != null && item.getType() == Material.ARROW) {
            removeTrailArrow(player);
        }
    }

    private static char getColorChar(int dyeData) {
        switch (dyeData) {
            case 14: return 'c';
            case 1: return '6';
            case 4: return 'e';
            case 5: return 'a';
            case 3: return 'b';
            case 11: return '9';
            case 10: return '5';
            case 9: return 'd';
            default: return 'f';
        }
    }

    public static void startTrail(Player host, WoolColor color) {
        UUID uuid = host.getUniqueId();

        if (activeTrails.containsKey(uuid)) {
            return;
        }

        TrailData data = new TrailData(color);
        data.lastLocation[0] = host.getLocation().clone();

        data.trailTask = new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (!host.isOnline() || host.isDead()) {
                    data.cleanup();
                    activeTrails.remove(uuid);
                    return;
                }

                Location currentLoc = host.getLocation();

                if (data.lastLocation[0] != null && currentLoc.distance(data.lastLocation[0]) < 0.5) {
                    checkExpiredBlocks(data);
                    return;
                }

                Block belowBlock = currentLoc.getBlock().getRelative(BlockFace.DOWN);
                
                if (belowBlock.getType() == Material.AIR || belowBlock.getType() == Material.WATER) {
                    checkExpiredBlocks(data);
                    return;
                }

                data.lastLocation[0] = currentLoc.clone();

                @SuppressWarnings("deprecation")
                int originalTypeId = belowBlock.getTypeId();
                @SuppressWarnings("deprecation")
                byte originalData = belowBlock.getData();

                belowBlock.setTypeIdAndData(35, (byte) color.woolData, true);

                PlacedBlock placedBlock = new PlacedBlock(belowBlock, originalTypeId, originalData);
                data.placedBlocks.offer(placedBlock);

                checkExpiredBlocks(data);
            }
        };
        data.trailTask.runTaskTimer(Core.getJavaPlugin(), 2L, 2L);

        activeTrails.put(uuid, data);
    }

    private static void checkExpiredBlocks(TrailData data) {
        while (!data.placedBlocks.isEmpty()) {
            PlacedBlock oldest = data.placedBlocks.peek();
            if (oldest.shouldRemove()) {
                oldest.revert();
                data.placedBlocks.poll();
            } else {
                break;
            }
        }
    }

    public static boolean wasActiveOnLogout(UUID uuid) {
        return savedColors.containsKey(uuid);
    }

    public static void restoreTrailWithSavedColor(Player player) {
        UUID uuid = player.getUniqueId();
        WoolColor color = savedColors.get(uuid);
        if (color != null) {
            startTrail(player, color);
            giveTrailArrow(player);
        }
    }

    public static void cleanupAll() {
        for (TrailData data : new ArrayList<>(activeTrails.values())) {
            data.cleanup();
        }
        activeTrails.clear();
        savedColors.clear();
    }
}
