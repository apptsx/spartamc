package com.minecraft.core.api.collectible.type.hat;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
public class HatCollectible extends Collectible {

    private final String value;

    public HatCollectible(String name, CollectibleRarity rarity, List<RankType> ranks, String value, long releasedAt) {
        super(name, CollectibleCategory.HAT, rarity, ranks, releasedAt);

        this.value = value;

        setIcon(Item.of(Material.SKULL_ITEM, 3));
        setHeadValue(value);
    }

    public static void handle() {
        for (HatList hat : HatList.values())
            Core.getCollectibleController().save(hat.getCosmetic());
    }

    public ItemStack component() {
        return Item.of(Material.SKULL_ITEM, 3, getRarity().getColor() + getName())
                .skullByBase64(value);
    }
}
