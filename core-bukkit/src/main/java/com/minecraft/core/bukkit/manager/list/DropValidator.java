package com.minecraft.core.bukkit.manager.list;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class DropValidator {

    public static boolean canDropItem(Player player) {
        Block blockBelow = player.getLocation().subtract(0, 1, 0).getBlock();
        return blockBelow.getType() != Material.AIR;
    }

    public static boolean isResource(Material material) {
        return material == Material.IRON_INGOT || 
               material == Material.GOLD_INGOT || 
               material == Material.DIAMOND || 
               material == Material.EMERALD;
    }
}