package com.minecraft.core.api.collectible.type.cape;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

@Getter
public abstract class CapeCollectible extends Collectible {

    private final String resourceName;

    private final float ratio;

    public CapeCollectible(String name, CollectibleRarity rarity, List<RankType> ranks, String resourceName, float ratio, long releasedAt) {
        super(name, CollectibleCategory.CAPES, rarity, ranks, releasedAt);

        this.resourceName = resourceName;
        this.ratio = ratio;

        setIcon(Item.of(Material.LEATHER_CHESTPLATE)
                .name(rarity.getColor() + name));

        Core.getCollectibleController().save(this);
    }
}
