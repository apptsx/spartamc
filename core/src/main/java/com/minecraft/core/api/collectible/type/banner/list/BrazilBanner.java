package com.minecraft.core.api.collectible.type.banner.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.banner.BannerCollectible;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import java.util.ArrayList;

public class BrazilBanner extends BannerCollectible {

    public BrazilBanner() {
        super("Brasil", CollectibleRarity.COMUM, DyeColor.LIME, new ArrayList<>(), 1726526700284L);

        setPatterns(new Pattern(DyeColor.YELLOW, PatternType.RHOMBUS_MIDDLE),
                new Pattern(DyeColor.BLUE, PatternType.CIRCLE_MIDDLE));
    }
}
