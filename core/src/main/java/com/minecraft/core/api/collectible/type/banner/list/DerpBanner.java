package com.minecraft.core.api.collectible.type.banner.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.banner.BannerCollectible;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import java.util.ArrayList;

public class DerpBanner extends BannerCollectible {

    public DerpBanner() {
        super("Derp", CollectibleRarity.EPIC, DyeColor.BLACK, new ArrayList<>(), 1726526700284L);

        setPatterns(new Pattern(DyeColor.WHITE, PatternType.RHOMBUS_MIDDLE),
                new Pattern(DyeColor.BLACK, PatternType.FLOWER),
                new Pattern(DyeColor.BLACK, PatternType.BRICKS),
                new Pattern(DyeColor.BLACK, PatternType.MOJANG),
                new Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM),
                new Pattern(DyeColor.BLACK, PatternType.MOJANG),
                new Pattern(DyeColor.BLACK, PatternType.BRICKS),
                new Pattern(DyeColor.BLACK, PatternType.FLOWER),
                new Pattern(DyeColor.BLACK, PatternType.FLOWER),
                new Pattern(DyeColor.BLACK, PatternType.MOJANG),
                new Pattern(DyeColor.BLACK, PatternType.BRICKS),
                new Pattern(DyeColor.BLACK, PatternType.TRIANGLE_TOP));
    }
}
