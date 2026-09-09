package com.minecraft.core.member.list.pvp.stats.list;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;

@Getter
public class LavaStats {

    private final int[] levels = new int[4];

    @Getter
    @AllArgsConstructor
    public enum LavaLevel {

        EASY("Fácil", ChatColor.GREEN),
        MEDIUM("Médio", ChatColor.YELLOW),
        HARD("Difícil", ChatColor.RED),
        EXTREME("Extremo", ChatColor.DARK_RED);

        private final String name;
        private final ChatColor color;

        public String getColoredName() {
            return color + name;
        }

        public LavaLevel next() {
            return this != EXTREME ? values()[ordinal() + 1] : EASY;
        }
    }

    public void incrementLevel(LavaLevel level) {
        levels[level.ordinal()]++;
    }

    public int getLevel(LavaLevel level) {
        return levels[level.ordinal()];
    }

    public int getTotalHits() {
        return Arrays.stream(levels).sum();
    }
}
