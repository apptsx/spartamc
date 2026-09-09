package com.minecraft.core.api.collectible;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class Collectible {

    private final String name;

    private final CollectibleCategory category;
    private final CollectibleRarity rarity;

    private final List<RankType> ranks;

    private final long releasedAt;

    private List<String> lore = new ArrayList<>();

    private String headValue;
    private Item icon;

    public Collectible(String name, CollectibleCategory category, CollectibleRarity rarity, List<RankType> ranks, long releasedAt) {
        this.name = name;

        this.category = category;
        this.rarity = rarity;

        this.ranks = Collections.singletonList(RankType.ADMIN);
        this.releasedAt = releasedAt;
    }

    @Override
    public boolean equals(Object collectibleObj) {
        if (collectibleObj == null || getClass() != collectibleObj.getClass()) return false;

        Collectible collectible = (Collectible) collectibleObj;

        return collectible.getName().equalsIgnoreCase(getName()) && collectible.getCategory().equals(getCategory());
    }

    public boolean isFree() {
        return ranks != null && (ranks.isEmpty() || ranks.stream().allMatch(rank -> rank.equals(RankType.MEMBER)));
    }

    public boolean hasAccess(RankType rank) {
        return isFree() || ranks.stream().anyMatch(type -> rank.ordinal() >= type.ordinal());
    }

    public String getIdentifier() {
        return category.name().toLowerCase() + ":" + rarity.name().toLowerCase() + ":" + name; // emotion:comum:happy
    }
}
