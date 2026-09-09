package com.minecraft.core.member.list.skywars;

import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.member.Member;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import com.minecraft.core.member.list.skywars.objects.metadata.SkyMetadata;
import com.minecraft.core.member.list.skywars.objects.metadata.objects.stats.SkyStats;
import com.minecraft.core.server.type.ServerType;
import lombok.Getter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class SkyMember extends Member {

    private SkyMetadata metadata;

    public SkyMember(UUID id, String name) {
        super(id, name);

        SkyMetadata metadata = new SkyMetadata();

        /* Adicionar estatísticas */
        ArcadeCategory.of(ServerType.SKYWARS).forEach(arcade -> {
            if (!arcade.name().contains("VERSUS")) {
                for (Type type : Type.values())
                    metadata.getStatsList().add(new SkyStats(arcade, type));
            } else
                metadata.getStatsList().add(new SkyStats(arcade, Type.CASUAL));
        });

        this.metadata = metadata;
    }

    @Override
    public void save(String... fields) {
    }

    @Override
    public int getMinLevelXp() {
        return 10000;
    }

    /* Metadata */
    public void saveMetadata(SkyMetadata metadata) {
        this.metadata = metadata;
        save("metadata");
    }

    /* Estatísticas */
    public List<SkyStats> getStatsList() {
        return metadata.getStatsList();
    }

    public List<SkyStats> getStatsByArcade(ArcadeCategory arcade) {
        return getStatsList().stream()
                .filter(stats -> stats.getArcade().equals(arcade))
                .collect(Collectors.toList());
    }

    public SkyStats getStats(ArcadeCategory arcade, Type type) {
        Stream<SkyStats> stream = getStatsList().stream()
                .filter(search -> search.getArcade().equals(arcade));

        if (!arcade.name().contains("VERSUS"))
            stream = stream.filter(search -> search.getType().equals(type));

        SkyStats stats = stream.findFirst().orElse(null);

        if (stats == null) {
            stats = new SkyStats(arcade, type);

            getStatsList().add(stats);
            saveMetadata(metadata);
        }

        return stats;
    }

    public void updateStats(SkyStats stats) {
        int indexOf = getStatsList().indexOf(stats);

        if (indexOf >= 0) {
            getStatsList().set(indexOf, stats);

            saveMetadata(metadata);
        }
    }

    public int getTotalMatches() {
        return getStatsList().stream().mapToInt(SkyStats::getMatches).sum();
    }

    public int getTotalWins() {
        return getStatsList().stream().mapToInt(SkyStats::getWins).sum();
    }

    public int getTotalWins(ArcadeCategory arcade) {
        return getStatsList().stream().filter(stats -> stats.getArcade().equals(arcade)).mapToInt(SkyStats::getWins).sum();
    }

    public int getTotalWinstreak(ArcadeCategory arcade) {
        return getStatsList().stream().filter(stats -> stats.getArcade().equals(arcade)).mapToInt(SkyStats::getWinStreak).sum();
    }

    public int getTotalDefeats() {
        return getStatsList().stream().mapToInt(SkyStats::getDefeats).sum();
    }

    public int getTotalKills() {
        return getStatsList().stream().mapToInt(SkyStats::getKills).sum();
    }

    public int getTotalKills(ArcadeCategory arcade) {
        return getStatsList().stream().filter(stats -> stats.getArcade().equals(arcade)).mapToInt(SkyStats::getKills).sum();
    }

    public int getTotalWinstreak() {
        return getStatsList().stream().mapToInt(SkyStats::getWinStreak).sum();
    }

    public int getTotalHighWinstreak() {
        return getStatsList().stream().mapToInt(SkyStats::getHighWinStreak).sum();
    }

    public void setProfile(ProfileType profile) {
        metadata.setProfile(profile);
        saveMetadata(metadata);
    }

    public boolean isProfile(ProfileType profile) {
        return metadata.getProfile().equals(profile);
    }
}

