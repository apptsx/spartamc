package com.minecraft.core.member.list.skywars.objects.metadata.objects.stats;

import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.type.Type;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class SkyStats {

    private final ArcadeCategory arcade;
    private final Type type;

    private int wins, defeats, matches;
    private int kills, deaths;

    private int winStreak, highWinStreak;

    private int weekWins, weekDefeats, weekMatches, weekKills, weekWinStreak;
    private int monthWins, monthDefeats, monthMatches, monthKills, monthWinStreak;

    public String getWinAverage() {
        if (matches == 0) return "0";

        double winPercentage = ((double) wins / matches) * 100.0;

        String result = winPercentage % 1 == 0 ? String.valueOf((int) winPercentage) : String.valueOf(Math.round(winPercentage * 10.0) / 10.0);

        return result.replace(".", ",");
    }

    public void setWins() {
        this.wins += 1;

        this.weekWins += 1;
        this.monthWins += 1;
    }

    public void setDefeats() {
        this.defeats += 1;

        this.weekDefeats += 1;
        this.monthDefeats += 1;
    }

    public void setMatches() {
        this.matches += 1;

        this.weekMatches += 1;
        this.monthMatches += 1;
    }

    public void setKills() {
        this.kills += 1;

        this.weekKills += 1;
        this.monthKills += 1;
    }

    public void addKills(int amount) {
        this.kills += amount;

        this.weekKills += amount;
        this.monthKills += amount;
    }

    public void setDeaths() {
        this.deaths += 1;
    }

    public void setWinStreak() {
        this.winStreak += 1;

        if (this.winStreak > highWinStreak)
            this.highWinStreak = winStreak;

        this.weekWinStreak += 1;
        this.monthWinStreak += 1;
    }

    public void resetWinStreak() {
        this.winStreak = 0;
    }
}

