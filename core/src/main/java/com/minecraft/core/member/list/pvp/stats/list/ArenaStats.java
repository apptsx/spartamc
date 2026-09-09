package com.minecraft.core.member.list.pvp.stats.list;

import lombok.Getter;

@Getter
public class ArenaStats {

    private int kills, deaths, killStreak, bestKillStreak;

    private int weekKills, weekDeaths, weekKillStreak;
    private int monthKills, monthDeaths, monthKillStreak;

    public void setKills() {
        this.kills += 1;

        this.weekKills += 1;
        this.monthKills += 1;

        this.killStreak += 1;

        if (killStreak > bestKillStreak)
            this.bestKillStreak = killStreak;

        this.weekKillStreak += 1;
        this.monthKillStreak += 1;
    }

    public void setDeaths() {
        this.deaths += 1;

        this.weekDeaths += 1;
        this.monthDeaths += 1;

        if (killStreak > 0)
            this.killStreak = 0;
    }
}
