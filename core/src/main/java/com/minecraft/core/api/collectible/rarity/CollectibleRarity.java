package com.minecraft.core.api.collectible.rarity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@Getter
@AllArgsConstructor
public enum CollectibleRarity {

    COMUM("Comum", ChatColor.GREEN),
    RARE("Raro", ChatColor.YELLOW),
    EPIC("Épico", ChatColor.AQUA),
    MYTHICAL("Mítico", ChatColor.RED);

    private final String name;
    private final ChatColor color;

    public String getColoredName() {
        return color + name;
    }
}
