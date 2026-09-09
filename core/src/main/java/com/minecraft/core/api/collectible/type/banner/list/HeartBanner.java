package com.minecraft.core.api.collectible.type.banner.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.banner.BannerCollectible;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import java.util.ArrayList;

public class HeartBanner extends BannerCollectible {

    public HeartBanner() {
        super("Coração", CollectibleRarity.RARE, DyeColor.WHITE, new ArrayList<>(), 1726526700284L);

        setPatterns(new Pattern(DyeColor.RED, PatternType.RHOMBUS_MIDDLE),
                new Pattern(DyeColor.RED, PatternType.RHOMBUS_MIDDLE),
                new Pattern(DyeColor.WHITE, PatternType.TRIANGLE_TOP),
                new Pattern(DyeColor.RED, PatternType.GRADIENT_UP),
                new Pattern(DyeColor.RED, PatternType.GRADIENT));
    }
}
