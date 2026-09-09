package com.minecraft.core.api.collectible.type.banner.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.banner.BannerCollectible;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import java.util.ArrayList;

public class SkyBlockBanner extends BannerCollectible {

    public SkyBlockBanner() {
        super("Sky Block", CollectibleRarity.COMUM, DyeColor.CYAN, new ArrayList<>(), 1726526700284L);

        setPatterns(new Pattern(DyeColor.BROWN, PatternType.STRAIGHT_CROSS),
                new Pattern(DyeColor.LIME, PatternType.STRIPE_MIDDLE),
                new Pattern(DyeColor.BROWN, PatternType.HALF_HORIZONTAL_MIRROR),
                new Pattern(DyeColor.SILVER, PatternType.STRIPE_BOTTOM),
                new Pattern(DyeColor.GREEN, PatternType.STRIPE_TOP),
                new Pattern(DyeColor.CYAN, PatternType.BORDER));
    }
}
