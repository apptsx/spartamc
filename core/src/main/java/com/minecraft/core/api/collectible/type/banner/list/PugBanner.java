package com.minecraft.core.api.collectible.type.banner.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.banner.BannerCollectible;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import java.util.ArrayList;

public class PugBanner extends BannerCollectible {

    public PugBanner() {
        super("Pug", CollectibleRarity.EPIC, DyeColor.ORANGE, new ArrayList<>(), 1726526700284L);

        setPatterns(new Pattern(DyeColor.WHITE, PatternType.BRICKS),
                new Pattern(DyeColor.BROWN, PatternType.STRIPE_BOTTOM),
                new Pattern(DyeColor.ORANGE, PatternType.BORDER),
                new Pattern(DyeColor.BROWN, PatternType.TRIANGLE_BOTTOM),
                new Pattern(DyeColor.PINK, PatternType.RHOMBUS_MIDDLE),
                new Pattern(DyeColor.BLACK, PatternType.CREEPER),
                new Pattern(DyeColor.BLACK, PatternType.FLOWER),
                new Pattern(DyeColor.BROWN, PatternType.SKULL),
                new Pattern(DyeColor.ORANGE, PatternType.STRIPE_TOP),
                new Pattern(DyeColor.ORANGE, PatternType.TRIANGLE_TOP),
                new Pattern(DyeColor.BROWN, PatternType.CIRCLE_MIDDLE));
    }
}
