package com.minecraft.core.bukkit.util.vanish;

import org.bukkit.entity.Player;

public class Vanish {

    public static boolean isVanished(Player player) {
        return player.hasMetadata("vanished");
    }

    public static void setVanished(Player player, boolean vanished) {
        player.setMetadata("vanished", new org.bukkit.metadata.FixedMetadataValue(
            player.getServer().getPluginManager().getPlugin("Core"), vanished));
    }

    public static void hidePlayer(Player viewer, Player target) {
        if (isVanished(target)) {
            viewer.hidePlayer(target);
        }
    }

    public static void showPlayer(Player viewer, Player target) {
        viewer.showPlayer(target);
    }

    public static boolean has(Player player) {
        return isVanished(player);
    }
}
