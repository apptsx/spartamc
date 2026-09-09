package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.hat.HatCollectible;
import org.bukkit.entity.Player;

import java.util.Collections;

public class HatOperator extends CollectibleOperator {

    public HatOperator(Player host, HatCollectible collectible) {
        super(host, CollectibleCategory.HAT);

        setCategoriesWithConflict(Collections.singletonList(CollectibleCategory.EMOTION));

        handle(collectible);
    }

    public void handle(Collectible collectible) {
        if (getHost() != null) {
            HatCollectible hat = (HatCollectible) collectible;

            getHost().getInventory().setHelmet(hat.component());
            getHost().updateInventory();

            setCollectible(collectible);
        }
    }

    @Override
    public void cancel() {
        if (getHost() != null) {
            getHost().getInventory().setHelmet(null);
            getHost().updateInventory();
        }
    }
}
