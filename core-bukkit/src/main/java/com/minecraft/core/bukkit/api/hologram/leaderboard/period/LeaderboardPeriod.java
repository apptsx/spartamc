package com.minecraft.core.bukkit.api.hologram.leaderboard.period;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum LeaderboardPeriod {

    DAY("Diário"),
    WEEK("Semanal"),
    MONTH("Mensal");

    private final String name;

    public LeaderboardPeriod next() {
        return this != MONTH ? values()[ordinal() + 1] : DAY;
    }

    public static List<LeaderboardPeriod> list() {
        return new ArrayList<>(Arrays.asList(values()));
    }

    public static LeaderboardPeriod of(String name) {
        return list().stream()
                .filter(period -> period.name().equalsIgnoreCase(name) || period.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(DAY);
    }
}
