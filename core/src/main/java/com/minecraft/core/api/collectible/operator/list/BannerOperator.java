package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.banner.BannerCollectible;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class BannerOperator extends CollectibleOperator {

    public BannerOperator(Player host, BannerCollectible banner) {
        super(host, CollectibleCategory.BANNER);

        setCategoriesWithConflict(Arrays.asList(CollectibleCategory.HAT, CollectibleCategory.EMOTION));

        handle(banner);
    }

    @Override
    public void handle(Collectible collectible) {
        BannerCollectible banner = (BannerCollectible) collectible;

        getHost().getInventory().setHelmet(banner.component());
        getHost().updateInventory();

        setCollectible(banner);
    }

    @Override
    public void cancel() {
        if (getHost() != null) {
            getHost().getInventory().setHelmet(null);
            getHost().updateInventory();
        }
    }
}
