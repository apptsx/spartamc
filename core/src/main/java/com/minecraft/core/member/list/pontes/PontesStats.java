package com.minecraft.core.member.list.pontes;

import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.member.context.stats.ArcadeStats;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PontesStats extends ArcadeStats {

    private int kills = 0;
    private int deaths = 0;
    private int wins = 0;
    private int losses = 0;
    private int currentWinstreak = 0;
    private int bestWinstreak = 0;

    public PontesStats(ArcadeCategory arcade) {
        super(arcade);
    }

    public void addKill() {
        this.kills++;
    }

    public void addDeath() {
        this.deaths++;
    }

    public void addWin(int wins) {
        this.wins += wins;
        this.currentWinstreak++;
        if (currentWinstreak > bestWinstreak)
            bestWinstreak = currentWinstreak;
    }

    public void addLoss(int losses) {
        this.losses += losses;
        this.currentWinstreak = 0;
    }

}