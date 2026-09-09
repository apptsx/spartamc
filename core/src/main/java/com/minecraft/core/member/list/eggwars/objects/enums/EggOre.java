package com.minecraft.core.member.list.eggwars.objects.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public enum EggOre {

    IRON("Ferros", ChatColor.WHITE, Material.IRON_INGOT),
    GOLD("Ouros", ChatColor.GOLD, Material.GOLD_INGOT),
    DIAMOND("Diamantes", ChatColor.AQUA, Material.DIAMOND);

    private final String name;

    private final ChatColor color;
    private final Material material;

    public String getOreName() {
        return name.substring(0, name.length() - 1);
    }

    public String getDisplay() {
        return color + name;
    }

    public String getValue(int price) {
        return color.toString() + price + " " + name;
    }
}

