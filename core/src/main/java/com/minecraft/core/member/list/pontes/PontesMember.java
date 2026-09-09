package com.minecraft.core.member.list.pontes;

import com.minecraft.core.Core;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.member.Member;
import com.minecraft.core.member.context.stats.ArcadeStats;
import com.minecraft.core.server.type.ServerType;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PontesMember extends Member {

    public PontesMember(UUID id, String name) {
        super(id, name);

        ArcadeCategory.of(ServerType.PONTES).forEach(arcade -> this.addMultipleStats(new ArcadeStats(arcade)));
    }

    @Override
    public void save(String... fields) {
        // Pontes stats saving - implement if needed
    }

    public List<PontesStats> getStatsByArcade(ArcadeCategory arcade) {
        return getStats().values().stream()
                .filter(stats -> stats.getArcade().equals(arcade))
                .map(stats -> (PontesStats) stats)
                .collect(Collectors.toList());
    }

    public PontesStats getStats(ArcadeCategory arcade) {
        return (PontesStats) getStats(arcade);
    }

    public int getTotalWins(ArcadeCategory arcade) {
        return getStatsByArcade(arcade).stream().mapToInt(PontesStats::getWins).sum();
    }

    public int getTotalWinstreak(ArcadeCategory arcade) {
        return getStatsByArcade(arcade).stream().mapToInt(PontesStats::getCurrentWinstreak).max().orElse(0);
    }

    public int getTotalKills(ArcadeCategory arcade) {
        return getStatsByArcade(arcade).stream().mapToInt(PontesStats::getKills).sum();
    }

    public int getTotalDeaths(ArcadeCategory arcade) {
        return getStatsByArcade(arcade).stream().mapToInt(PontesStats::getDeaths).sum();
    }
}
