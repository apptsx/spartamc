package com.minecraft.core.member.list.pvp.stats.list;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class MLGStats {

    /* Níveis: Fácil, Médio, Difícil, Extremo */
    private final int[] hits = new int[4];
    private final int[] fails = new int[4];

    private final int[] streaks = new int[4];
    private final int[] bestStreaks = new int[4];

    private final int[] weekStreaks = new int[4];
    private final int[] monthStreaks = new int[4];

    public enum MLGLevel {
        EASY, MEDIUM, HARD, EXTREME
    }

    public void incrementHit(MLGLevel level) {
        int id = level.ordinal();

        hits[id]++;
        streaks[id]++;
        weekStreaks[id]++;
        monthStreaks[id]++;

        if (streaks[id] > bestStreaks[id])
            bestStreaks[id] = streaks[id];
    }

    public void incrementFail(MLGLevel level) {
        int id = level.ordinal();

        fails[id]++;
        streaks[id] = 0;
    }

    public int getTotalHits() {
        return Arrays.stream(hits).sum();
    }

    public int getTotalFails() {
        return Arrays.stream(fails).sum();
    }

    /* Lidando com sequências */
    public int getHits(MLGLevel level) {
        return hits[level.ordinal()];
    }

    public int getFails(MLGLevel level) {
        return fails[level.ordinal()];
    }

    public int getWeekStreak(MLGLevel level) {
        return weekStreaks[level.ordinal()];
    }

    public int getMonthStreak(MLGLevel level) {
        return monthStreaks[level.ordinal()];
    }

    public int getBestStreak(MLGLevel level) {
        return bestStreaks[level.ordinal()];
    }

    public int getCurrentStreak(MLGLevel level) {
        return streaks[level.ordinal()];
    }
}