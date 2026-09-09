package com.minecraft.core.member.context.stats;

import com.minecraft.core.arcade.category.ArcadeCategory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ArcadeStats {

    private final ArcadeCategory arcade;

    private int wins, defeats, matches;
    private int kills, deaths, assists;

    private int winStreak, highWinStreak, killStreak, highKillStreak;

    private int weekWins, weekDefeats, weekMatches, weekKills, weekAssists, weekWinStreak, weekKillStreak;
    private int monthWins, monthDefeats, monthMatches, monthKills, monthAssists, monthWinStreak, monthKillStreak;

    public double getKDR() {
        return deaths == 0 ? kills : (double) kills / deaths;
    }

    public String getWinAverage() {
        if (matches == 0) return "0";

        double winPercentage = ((double) wins / matches) * 100.0;

        String result = winPercentage % 1 == 0 ? String.valueOf((int) winPercentage) : String.valueOf(Math.round(winPercentage * 10.0) / 10.0);

        return result.replace(".", ",");
    }

    /* --------------------------------
          Aplicando Week e Month
    -------------------------------- */

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

    public void setAssists() {
        this.assists += 1;

        this.weekAssists += 1;
        this.monthAssists += 1;
    }

    public void setWinStreak() {
        this.winStreak += 1;

        if (this.winStreak > highWinStreak)
            this.highWinStreak = this.winStreak;

        this.weekWinStreak += 1;
        this.monthWinStreak += 1;
    }

    public void setKillStreak() {
        this.killStreak += 1;

        this.weekKillStreak += 1;
        this.monthKillStreak += 1;
    }
}
