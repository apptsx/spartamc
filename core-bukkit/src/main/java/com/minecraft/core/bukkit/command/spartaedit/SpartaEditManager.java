package com.minecraft.core.bukkit.command.spartaedit;

import com.minecraft.core.bukkit.BukkitCore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpartaEditManager implements Listener {

    private static final Map<UUID, Location> firstPosition = new HashMap<>();
    private static final Map<UUID, Location> secondPosition = new HashMap<>();
    private static final Map<UUID, Boolean> toolEnabled = new HashMap<>();

    public SpartaEditManager() {
        BukkitCore.getInstance().getServer().getPluginManager().registerEvents(this, BukkitCore.getInstance());
    }

    public void setPosition(Player player, boolean first, Location location) {
        if (first) {
            firstPosition.put(player.getUniqueId(), location);
            player.sendMessage("§6[SPARTAEDIT] §ePosição 1 setada em: §b" + formatLocation(location));
        } else {
            secondPosition.put(player.getUniqueId(), location);
            player.sendMessage("§6[SPARTAEDIT] §ePosição 2 setada em: §b" + formatLocation(location));
        }
    }

    public Location getFirstPosition(Player player) {
        return firstPosition.get(player.getUniqueId());
    }

    public Location getSecondPosition(Player player) {
        return secondPosition.get(player.getUniqueId());
    }

    public void clearSelection(Player player) {
        firstPosition.remove(player.getUniqueId());
        secondPosition.remove(player.getUniqueId());
        player.sendMessage("§6[SPARTAEDIT] §eSeleção limpa.");
    }

    public void toggleTool(Player player) {
        UUID uuid = player.getUniqueId();
        boolean enabled = toolEnabled.getOrDefault(uuid, false);
        toolEnabled.put(uuid, !enabled);
        if (!enabled) {
            player.sendMessage("§6[SPARTAEDIT] §eFerramenta ativada. Use um §bMachado de Madeira§e para selecionar.");
            // Give wood axe
            player.getInventory().addItem(new ItemStack(Material.WOOD_AXE));
        } else {
            player.sendMessage("§6[SPARTAEDIT] §eFerramenta desativada.");
        }
    }

    public void setBlocks(Player player, Material material) {
        Location pos1 = firstPosition.get(player.getUniqueId());
        Location pos2 = secondPosition.get(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
            player.sendMessage("§6[SPARTAEDIT] §cSelecione as duas posições primeiro!");
            return;
        }

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int count = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = player.getWorld().getBlockAt(x, y, z);
                    block.setType(material);
                    count++;
                }
            }
        }

        player.sendMessage("§6[SPARTAEDIT] §e" + count + " blocos alterados para " + material.name() + ".");
    }

    public void replaceBlocks(Player player, Material from, Material to) {
        Location pos1 = firstPosition.get(player.getUniqueId());
        Location pos2 = secondPosition.get(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
            player.sendMessage("§6[SPARTAEDIT] §cSelecione as duas posições primeiro!");
            return;
        }

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int count = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = player.getWorld().getBlockAt(x, y, z);
                    if (block.getType() == from) {
                        block.setType(to);
                        count++;
                    }
                }
            }
        }

        player.sendMessage("§6[SPARTAEDIT] §e" + count + " blocos substituídos de " + from.name() + " para " + to.name() + ".");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!toolEnabled.getOrDefault(player.getUniqueId(), false)) return;

        // Check if player is holding a wood axe
        if (player.getItemInHand().getType() != Material.WOOD_AXE) return;

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getClickedBlock() != null) {
                setPosition(player, true, event.getClickedBlock().getLocation());
                event.setCancelled(true);
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() != null) {
                setPosition(player, false, event.getClickedBlock().getLocation());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        firstPosition.remove(uuid);
        secondPosition.remove(uuid);
        toolEnabled.remove(uuid);
    }

    private String formatLocation(Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }
}
