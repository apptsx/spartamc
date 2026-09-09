package com.minecraft.core.member.list.eggwars;

import com.minecraft.core.Core;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.member.Member;
import com.minecraft.core.member.list.eggwars.objects.enums.item.EggWarsItem;
import com.minecraft.core.member.list.eggwars.objects.menu.item.EggItem;
import com.minecraft.core.member.list.eggwars.objects.metadata.EggMetadata;
import com.minecraft.core.member.list.eggwars.objects.menu.EggMenuMetadata;
import com.minecraft.core.member.list.eggwars.objects.menu.type.EggMenuType;
import com.minecraft.core.member.list.eggwars.objects.metadata.objects.stats.EggStats;
import com.minecraft.core.server.type.ServerType;
import lombok.Getter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class EggMember extends Member {

    private EggMetadata metadata;

    public EggMember(UUID id, String name) {
        super(id, name);

        EggMetadata metadata = new EggMetadata();

        /* Adicionar estatísticas */
        ArcadeCategory.of(ServerType.EGGWARS).forEach(arcade -> {
            if (!arcade.name().contains("VERSUS")) {
                for (Type type : Type.values())
                    metadata.getStatsList().add(new EggStats(arcade, type));
            } else
                metadata.getStatsList().add(new EggStats(arcade, Type.CASUAL));

        });

        /* Adicionar inventários */
        EggMenuType.list().forEach(type -> metadata.getMenus().add(new EggMenuMetadata(type)));

        this.metadata = metadata;
    }

    @Override
    public void save(String... fields) {
        for (String field : fields)
            Core.getEggWarsData().update(this, field);
    }

    @Override
    public int getMinLevelXp() {
        return 10000;
    }

    /* Metadata */
    public void saveMetadata(EggMetadata metadata) {
        this.metadata = metadata;
        save("metadata");
    }

    /* Estatísticas */
    public List<EggStats> getStatsList() {
        return metadata.getStatsList();
    }

    public List<EggStats> getStatsByArcade(ArcadeCategory arcade) {
        return getStatsList().stream()
                .filter(stats -> stats.getArcade().equals(arcade))
                .collect(Collectors.toList());
    }

    public EggStats getStats(ArcadeCategory arcade, Type type) {
        Stream<EggStats> stream = getStatsList().stream()
                .filter(search -> search.getArcade().equals(arcade));

        if (!arcade.name().contains("VERSUS"))
            stream = stream.filter(search -> search.getType().equals(type));

        EggStats stats = stream.findFirst().orElse(null);

        if (stats == null) {
            stats = new EggStats(arcade, type);

            getStatsList().add(stats);
            saveMetadata(metadata);
        }

        return stats;
    }

    public void updateStats(EggStats stats) {
        int indexOf = getStatsList().indexOf(stats);

        if (indexOf >= 0) {
            getStatsList().set(indexOf, stats);

            saveMetadata(metadata);
        }
    }

    public int getTotalMatches() {
        return getStatsList().stream().mapToInt(EggStats::getMatches).sum();
    }

    public int getTotalWins() {
        return getStatsList().stream().mapToInt(EggStats::getWins).sum();
    }

    public int getTotalWins(ArcadeCategory arcade) {
        return getStatsList().stream().filter(stats -> stats.getArcade().equals(arcade)).mapToInt(EggStats::getWins).sum();
    }

    public int getTotalWinstreak(ArcadeCategory arcade) {
        return getStatsList().stream().filter(stats -> stats.getArcade().equals(arcade)).mapToInt(EggStats::getWinStreak).sum();
    }

    public int getTotalDefeats() {
        return getStatsList().stream().mapToInt(EggStats::getDefeats).sum();
    }

    public int getTotalAssists() {
        return getStatsList().stream().mapToInt(EggStats::getAssists).sum();
    }

    public int getTotalKills() {
        return getStatsList().stream().mapToInt(EggStats::getKills).sum();
    }

    public int getTotalFinalKills() {
        return getStatsList().stream().mapToInt(EggStats::getFinalKills).sum();
    }

    public int getTotalFinalKills(ArcadeCategory arcade) {
        return getStatsList().stream()
                .filter(stats -> stats.getArcade().equals(arcade))
                .mapToInt(EggStats::getFinalKills)
                .sum();
    }
    
    public int getTotalKills(ArcadeCategory arcade) {
        return getStatsList().stream()
                .filter(stats -> stats.getArcade().equals(arcade))
                .mapToInt(EggStats::getKills)
                .sum();
    }

    public int getTotalEggDestruction() {
        return getStatsList().stream().mapToInt(EggStats::getEggDestruction).sum();
    }

    public int getTotalWinstreak() {
        return getStatsList().stream().mapToInt(EggStats::getWinStreak).sum();
    }

    public int getTotalHighWinstreak() {
        return getStatsList().stream().mapToInt(EggStats::getHighWinStreak).sum();
    }

    /* Custom Menu */
    public List<EggMenuMetadata> getMetadataMenus() {
        return metadata.getMenus();
    }

    public EggMenuMetadata getMenu(EggMenuType type) {
        return getMetadataMenus().stream().filter(menu -> menu.getType().equals(type)).findFirst().orElse(null);
    }

    public void updateMenu(EggMenuMetadata menu) {
        int indexOf = getMetadataMenus().indexOf(menu);

        if (indexOf >= 0) {
            getMetadataMenus().set(getMetadataMenus().indexOf(menu), menu);
            saveMetadata(metadata);
        }
    }

    public void removeFavoriteItem(EggWarsItem item) {
        EggMenuMetadata menu = getMenu(EggMenuType.FAVORITE);

        if (menu != null) {
            menu.getItemList().removeIf(search -> search.getName().equalsIgnoreCase(item.getName()));
            updateMenu(menu);
        }
    }

    public void setFavoriteItem(int slot, EggWarsItem item) {
        EggMenuMetadata menu = getMenu(EggMenuType.FAVORITE);

        if (menu != null) {
            menu.getItemList().add(new EggItem(slot, item.getName().toLowerCase()));
            updateMenu(menu);
        }
    }

    public boolean isFavoriteItem(EggWarsItem item) {
        return getMenu(EggMenuType.FAVORITE).getItemList().stream().anyMatch(search -> search.getName().equalsIgnoreCase(item.getName()));
    }

    public void setProfile(com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType profile) {
        metadata.setProfile(profile);
        saveMetadata(metadata);
    }

    public boolean isProfile(com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType profile) {
        return metadata.getProfile().equals(profile);
    }
}

