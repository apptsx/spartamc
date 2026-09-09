package com.minecraft.core.api.collectible.type.banner.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.banner.BannerCollectible;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import java.util.ArrayList;

public class HalloweenBanner extends BannerCollectible {

    public HalloweenBanner() {
        super("Halloween", CollectibleRarity.EPIC, DyeColor.BLACK, new ArrayList<>(), 1726526700284L);

        setPatterns(new Pattern(DyeColor.ORANGE, PatternType.FLOWER),
                new Pattern(DyeColor.ORANGE, PatternType.FLOWER),
                new Pattern(DyeColor.ORANGE, PatternType.CIRCLE_MIDDLE),
                new Pattern(DyeColor.ORANGE, PatternType.TRIANGLES_BOTTOM),
                new Pattern(DyeColor.BLACK, PatternType.HALF_HORIZONTAL),
                new Pattern(DyeColor.ORANGE, PatternType.TRIANGLE_TOP),
                new Pattern(DyeColor.ORANGE, PatternType.STRIPE_MIDDLE));
    }
}
