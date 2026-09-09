package com.minecraft.core.bukkit.util.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemFactory {

    public static ItemStack create(Material material) {
        return new ItemStack(material);
    }

    public static ItemStack create(Material material, int amount) {
        return new ItemStack(material, amount);
    }

    public static ItemStack create(Material material, int amount, short durability) {
        return new ItemStack(material, amount, durability);
    }

    public static ItemStack createItemStack(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (displayName != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            }
            if (lore != null) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(coloredLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
