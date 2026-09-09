package com.minecraft.core.api.collectible.type.title;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public enum TitleCategory {
    GERAL("Geral", "§7Títulos gerais como Ausente, Beta, Cargo, etc."),
    BEDWARS("BedWars", "§7Títulos relacionados ao modo BedWars"),
    SKYWARS("SkyWars", "§7Títulos relacionados ao modo SkyWars"),
    THE_BRIDGE("The Bridge", "§7Títulos relacionados ao modo The Bridge"),
    DUELS("Duels", "§7Títulos relacionados ao modo Duels"),
    PVP("PvP", "§7Títulos relacionados ao modo PvP"),
    HUNGER_GAMES("Hunger Games", "§7Títulos relacionados ao modo Hunger Games"),
    SPECIAL("Especial", "§7Títulos especiais e únicos"),
    STAFF("Staff", "§7Títulos exclusivos da staff");

    @Getter
    private final String displayName;
    private final String description;

    TitleCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public static List<TitleCategory> getServerCategories() {
        return Arrays.asList(
                BEDWARS, SKYWARS, THE_BRIDGE, DUELS, PVP, HUNGER_GAMES
        );
    }
}
