package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import org.bukkit.entity.Player;

public class TitleOperator extends CollectibleOperator {

    private TitleCollectible title;

    public TitleOperator(Player host, TitleCollectible title) {
        super(host, CollectibleCategory.TITLE);
        
        this.title = title;
        handle(title);
    }

    @Override
    public void handle(Collectible collectible) {
        this.title = (TitleCollectible) collectible;
        
        if (getHost() != null && getHost().isOnline()) {
            title.showTitle(getHost());
        }
        
        setCollectible(title);
    }

    @Override
    public void cancel() {
        if (title != null && getHost() != null) {
            TitleCollectible.removeTitle(getHost());
        }
        title = null;
    }
}
