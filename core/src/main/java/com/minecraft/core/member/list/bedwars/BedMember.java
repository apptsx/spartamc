package com.minecraft.core.member.list.bedwars;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.member.Member;
import com.minecraft.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import com.minecraft.core.member.list.bedwars.objects.menu.item.BedItem;
import com.minecraft.core.member.list.bedwars.objects.metadata.BedMetadata;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.BedCollectible;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.BedCollectibleController;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.share.ShareType;
import com.minecraft.core.member.list.bedwars.objects.menu.BedMenuMetadata;
import com.minecraft.core.member.list.bedwars.objects.menu.type.BedMenuType;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.stats.BedStats;
import com.minecraft.core.server.type.ServerType;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class BedMember extends Member {

    private BedMetadata metadata;

    public BedMember(UUID id, String name) {
        super(id, name);

        BedMetadata metadata = new BedMetadata();

        /* Adicionar estatísticas */
        ArcadeCategory.of(ServerType.BEDWARS).forEach(arcade -> {
            if (!arcade.name().contains("VERSUS")) {
                for (Type type : Type.values())
                    metadata.getStatsList().add(new BedStats(arcade, type));
            } else
                metadata.getStatsList().add(new BedStats(arcade, Type.CASUAL));

        });

        /* Adicionar inventários */
        BedMenuType.list().forEach(type -> metadata.getMenus().add(new BedMenuMetadata(type)));

        /* Setar o "Nenhuma" como padrão */
        BedCollectibleType.list().forEach(type -> {
            BedCollectible none = BedCollectibleController.of("Nenhuma", type);

            if (none != null)
                metadata.getCollectibles().put(type, none.getName().toLowerCase());
        });

        this.metadata = metadata;
    }

    @Override
    public void save(String... fields) {
        for (String field : fields)
            Core.getBedWarsData().update(this, field);
    }

    @Override
    public int getMinLevelXp() {
        // XP necessário = (nível atual + 1) * 400
        // Nível 0 → 1: 400 XP
        // Nível 1 → 2: 800 XP
        // Nível 2 → 3: 1200 XP
        // E assim por diante
        return (getLevel() + 1) * 400;
    }

    /* Metadata */
    public void saveMetadata(BedMetadata metadata) {
        this.metadata = metadata;
        save("metadata");
    }

    /* Estatísticas */
    public List<BedStats> getStatsList() {
        return metadata.getStatsList();
    }

    public List<BedStats> getStatsByArcade(ArcadeCategory arcade) {
        return getStatsList().stream()
                .filter(stats -> stats.getArcade().equals(arcade))
                .collect(Collectors.toList());
    }

    public BedStats getStats(ArcadeCategory arcade, Type type) {
        Stream<BedStats> stream = getStatsList().stream()
                .filter(search -> search.getArcade().equals(arcade));

        if (!arcade.name().contains("VERSUS"))
            stream = stream.filter(search -> search.getType().equals(type));

        BedStats stats = stream.findFirst().orElse(null);

        if (stats == null) {
            stats = new BedStats(arcade, type);

            getStatsList().add(stats);
            saveMetadata(metadata);
        }

        return stats;
    }

    public void updateStats(BedStats stats) {
        int indexOf = getStatsList().indexOf(stats);

        if (indexOf >= 0) {
            getStatsList().set(indexOf, stats);

            saveMetadata(metadata);
        }
    }

    public int getTotalMatches() {
        return getStatsList().stream().mapToInt(BedStats::getMatches).sum();
    }

    public int getTotalWins() {
        return getStatsList().stream().mapToInt(BedStats::getWins).sum();
    }

    public int getTotalWins(ArcadeCategory arcade) {
        return getStatsList().stream().filter(stats -> stats.getArcade().equals(arcade)).mapToInt(BedStats::getWins).sum();
    }

    public int getTotalWinstreak(ArcadeCategory arcade) {
        return getStatsList().stream().filter(stats -> stats.getArcade().equals(arcade)).mapToInt(BedStats::getWinStreak).sum();
    }

    public int getTotalDefeats() {
        return getStatsList().stream().mapToInt(BedStats::getDefeats).sum();
    }

    public int getTotalAssists() {
        return getStatsList().stream().mapToInt(BedStats::getAssists).sum();
    }

    public int getTotalKills() {
        return getStatsList().stream().mapToInt(BedStats::getKills).sum();
    }

    public int getTotalFinalKills() {
        return getStatsList().stream().mapToInt(BedStats::getFinalKills).sum();
    }

    public int getTotalFinalKills(ArcadeCategory arcade) {
        return getStatsList().stream()
                .filter(stats -> stats.getArcade().equals(arcade))
                .mapToInt(BedStats::getFinalKills)
                .sum();
    }
    
    public int getTotalKills(ArcadeCategory arcade) {
        return getStatsList().stream()
                .filter(stats -> stats.getArcade().equals(arcade))
                .mapToInt(BedStats::getKills)
                .sum();
    }

    public int getTotalBedDestruction() {
        return getStatsList().stream().mapToInt(BedStats::getBedDestruction).sum();
    }

    public int getTotalWinstreak() {
        return getStatsList().stream().mapToInt(BedStats::getWinStreak).sum();
    }

    public int getTotalHighWinstreak() {
        return getStatsList().stream().mapToInt(BedStats::getHighWinStreak).sum();
    }

    public void setProfile(ProfileType profile) {
        metadata.setProfile(profile);
        saveMetadata(metadata);
    }

    public boolean isProfile(ProfileType profile) {
        return metadata.getProfile().equals(profile);
    }

    public void setShare(ShareType share) {
        metadata.setShare(share);
        saveMetadata(metadata);
    }

    public boolean isShare(ShareType share) {
        return metadata.getShare().equals(share);
    }

    public boolean checkShare(Account target) {
        Account account = getAccount();

        if (account == null) return false;

        switch (metadata.getShare()) {
            case MEDIUM: {
                // Amigos, membros da party, ou do clan.

                return account.isFriend(target)
                        || account.hasParty() && account.getParty().isMember(target.getId())
                        || account.hasClan() && account.getClan().isMember(target.getId());
            }
            case HARD: {
                // Somente os amigos podem compartilhar
                return account.isFriend(target);
            }
            case MAX:
                // Ninguém pode compartilhar
                return false;
            default:
                // Todos podem compartilhar
                return true;
        }
    }

    /* Custom Menu */
    public List<BedMenuMetadata> getMetadataMenus() {
        return metadata.getMenus();
    }

    public BedMenuMetadata getMenu(BedMenuType type) {
        return getMetadataMenus().stream().filter(menu -> menu.getType().equals(type)).findFirst().orElse(null);
    }

    public void updateMenu(BedMenuMetadata menu) {
        int indexOf = getMetadataMenus().indexOf(menu);

        if (indexOf >= 0) {
            getMetadataMenus().set(getMetadataMenus().indexOf(menu), menu);
            saveMetadata(metadata);
        }
    }

    public void removeFavoriteItem(BedWarsItem item) {
        BedMenuMetadata menu = getMenu(BedMenuType.FAVORITE);

        if (menu != null) {
            menu.getItemList().removeIf(search -> search.getName().equalsIgnoreCase(item.getName()));
            updateMenu(menu);
        }
    }

    public void setFavoriteItem(int slot, BedWarsItem item) {
        BedMenuMetadata menu = getMenu(BedMenuType.FAVORITE);

        if (menu != null) {
            menu.getItemList().add(new BedItem(slot, item.getName().toLowerCase()));
            updateMenu(menu);
        }
    }

    public boolean isFavoriteItem(BedWarsItem item) {
        return getMenu(BedMenuType.FAVORITE).getItemList().stream().anyMatch(search -> search.getName().equalsIgnoreCase(item.getName()));
    }

    /* Collectibles */
    public List<BedCollectible> getCollectibles() {
        return BedCollectibleController.list(collectible -> collectible.hasPermission(getAccount()));
    }

    public BedCollectible getCollectible(BedCollectibleType type, String name) {
        return getCollectibles(type).stream()
                .filter(collectible -> collectible.getClass().getSimpleName().equalsIgnoreCase(name) || collectible.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public BedCollectible getRandomCollectible(BedCollectibleType type) {
        List<BedCollectible> list = getCollectibles(type)
                .stream()
                .filter(collectible -> !(collectible.getName().equalsIgnoreCase("Nenhuma")
                        || collectible.getName().equalsIgnoreCase("Padrão")
                        || collectible.getName().equalsIgnoreCase("Aleatório")))
                .collect(Collectors.toList());

        return list.get(Core.RANDOM.nextInt(list.size()));
    }

    public List<BedCollectible> getCollectibles(BedCollectibleType type) {
        return getCollectibles().stream().filter(collectible -> collectible.getRarity() == null || collectible.getType().equals(type)).collect(Collectors.toList());
    }

    public int getCollectibleCount(BedCollectibleType type) {
        return (int) getCollectibles(type).stream().filter(collectible -> collectible.getRarity() != null).count();
    }

    public List<BedCollectible> getActiveCollectibles() {
        List<BedCollectible> list = new ArrayList<>();

        metadata.getCollectibles().forEach((type, name) -> {
            BedCollectible collectible = BedCollectibleController.of(name, type);

            if (collectible != null)
                list.add(collectible);
        });

        return list;
    }

    public BedCollectible getActiveCollectible(BedCollectibleType type) {
        return getActiveCollectibles().stream().filter(collectible -> collectible.getType().equals(type)).findFirst().orElse(null);
    }

    public boolean isActivateCollectible(BedCollectible collectible) {
        return getActiveCollectibles().stream().anyMatch(search -> search.getType().equals(collectible.getType()) && search.getName().equalsIgnoreCase(collectible.getName()));
    }

    public void setCollectible(BedCollectible collectible) {
        getActiveCollectibles().removeIf(search -> search.getType().equals(collectible.getType()));

        metadata.getCollectibles().put(collectible.getType(), collectible.getName().toLowerCase());
        saveMetadata(metadata);
    }

    /* Abilities System */
    public boolean hasPurchasedAbility(String abilityName) {
        return metadata.getPurchasedAbilities().contains(abilityName.toLowerCase());
    }

    public void purchaseAbility(String abilityName) {
        metadata.getPurchasedAbilities().add(abilityName.toLowerCase());
        saveMetadata(metadata);
    }

    public Set<String> getPurchasedAbilities() {
        return new HashSet<>(metadata.getPurchasedAbilities());
    }

    public boolean hasSelectedAbility(String abilityName) {
        return metadata.getSelectedAbilities().contains(abilityName.toLowerCase());
    }

    public void selectAbility(String abilityName) {
        metadata.getSelectedAbilities().add(abilityName.toLowerCase());
        saveMetadata(metadata);
    }

    public void deselectAbility(String abilityName) {
        metadata.getSelectedAbilities().remove(abilityName.toLowerCase());
        saveMetadata(metadata);
    }

    public Set<String> getSelectedAbilities() {
        return new HashSet<>(metadata.getSelectedAbilities());
    }

    public void clearSelectedAbilities() {
        metadata.getSelectedAbilities().clear();
        saveMetadata(metadata);
    }

    /* Coins System */
    public int getCoins() {
        return metadata.getCoins();
    }

    public void setCoins(int coins) {
        metadata.setCoins(coins);
        saveMetadata(metadata);
    }

    public void addCoins(int amount) {
        setCoins(getCoins() + amount);
    }

    public void removeCoins(int amount) {
        setCoins(Math.max(0, getCoins() - amount));
    }

    public boolean hasCoins(int amount) {
        return getCoins() >= amount;
    }

}
