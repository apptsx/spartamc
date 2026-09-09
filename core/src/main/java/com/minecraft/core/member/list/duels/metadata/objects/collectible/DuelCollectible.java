package com.minecraft.core.member.list.duels.metadata.objects.collectible;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.objects.type.DuelCollectibleType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class DuelCollectible {

    private final DuelCollectibleType type;
    private final CollectibleRarity rarity;

    private final String name;
    private final Item icon;

    private final List<String> lore = new ArrayList<>();
    private final List<RankType> ranks = new ArrayList<>();

    private final long createdAt;

    public void setLore(String... lore) {
        this.lore.addAll(Arrays.asList(lore));
    }

    public void setRanks(RankType... ranks) {
        this.ranks.addAll(Arrays.asList(ranks));
    }

    public boolean equals(Class<? extends DuelCollectible> collectibleClass) {
        return collectibleClass.isAssignableFrom(getClass());
    }

    public boolean isFree() {
        return ranks.isEmpty() || ranks.stream().allMatch(rank -> rank.equals(RankType.MEMBER));
    }

    public boolean hasPermission(Account account) {
        if (isFree()) return true;

        if (!ranks.isEmpty() && ranks.stream().anyMatch(rank -> account.getRank().getType().ordinal() >= rank.ordinal()))
            return true;

        return account.hasPermission(getPermission());
    }

    public String getPermission() {
        return "arcade.duel.collectible." + type.getName().toLowerCase() + "." + name.toLowerCase();
    }
}
