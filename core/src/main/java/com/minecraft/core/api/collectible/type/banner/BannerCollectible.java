package com.minecraft.core.api.collectible.type.banner;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import lombok.Getter;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class BannerCollectible extends Collectible {

    private final DyeColor color;
    private final List<Pattern> patterns;

    public BannerCollectible(String name, CollectibleRarity rarity, DyeColor color, List<RankType> ranks, long releasedAt) {
        super(name, CollectibleCategory.BANNER, rarity, ranks, releasedAt);

        this.color = color;
        this.patterns = new ArrayList<>();

        setIcon(component());

        Core.getCollectibleController().save(this);
    }

    public static Item getFirst() {
        BannerCollectible collectible = (BannerCollectible) Core.getCollectibleController()
                .list(CollectibleCategory.BANNER)
                .stream().findFirst().orElse(null);

        return collectible != null ? collectible.component() : Item.of(Material.BANNER, 1);
    }

    public Item component() {
        Item item = Item.of(Material.BANNER)
                .name(getRarity().getColor() + getName())
                .flags(ItemFlag.values());

        BannerMeta meta = (BannerMeta) item.getItemMeta();

        meta.setBaseColor(color);
        meta.setPatterns(patterns);

        item.updateMeta(meta);

        return item;
    }

    public void setPatterns(Pattern... patterns) {
        this.patterns.addAll(Arrays.asList(patterns));
    }
}
