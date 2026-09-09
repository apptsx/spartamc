package com.minecraft.core.member.context.elo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@Getter
@AllArgsConstructor
public enum Elo {

    NONE("Starter", "-", ChatColor.DARK_GRAY, 0),

    KNIGHT("Knight", "⭑", ChatColor.GREEN, 5000),
    HERO("Hero", "✶", ChatColor.DARK_GREEN, 12000),
    GUARDIAN("Guardian", "✷", ChatColor.DARK_AQUA, 21000),
    LEGEND("Legend", "✦", ChatColor.YELLOW, 32000),
    SUPREME("Supreme", "✸", ChatColor.GOLD, 45000),
    INVINCIBLE("Invincible", "❁", ChatColor.LIGHT_PURPLE, 52000),
    MASTER("Master", "❋", ChatColor.DARK_PURPLE, 64000),
    TITAN("Titan", "✵", ChatColor.RED, 75000),
    IMMORTAL("Immortal", "⚜", ChatColor.DARK_RED, 90000);

    private final String name, symbol;
    private final ChatColor color;
    private final int minXp;

    public boolean isHighest() {
        return this == IMMORTAL;
    }

    public String getColoredName() {
        return getColor() + getName();
    }

    public String getColoredSymbol() {
        return getColor() + getSymbol();
    }

    public String getChat() {
        return "§7[" + getColoredSymbol() + "§7]";
    }

    public String getFullName() {
        return getColoredSymbol() + " " + getName();
    }
}
