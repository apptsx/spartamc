package com.minecraft.core.api.clan.metadata.emblem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ClanEmblem {

    DEFAULT("Padrão", "", ChatColor.GRAY, 0, false),
    PRIMARY("Primários", "⛏", ChatColor.YELLOW, 2000, false),
    TROGLODYTES("Trogloditas", "☪", ChatColor.RED, 9000, false),
    AMETHYSTS("Ametistas", "✦", ChatColor.DARK_PURPLE, 20000, false);

    private final String name;
    private final String symbol;

    private final ChatColor color;

    private final int minPower;
    private final boolean exclusive;

    public static ClanEmblem of(String name) {
        return Arrays.stream(values())
                .filter(emblem -> emblem.name().equalsIgnoreCase(name) || emblem.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public boolean isAchievable() {
        return minPower > 0 && !exclusive;
    }
}
