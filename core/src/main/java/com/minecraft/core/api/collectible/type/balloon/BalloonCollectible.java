package com.minecraft.core.api.collectible.type.balloon;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class BalloonCollectible extends Collectible {

    private List<String> frames;

    private int ticksPerFrame;

    public BalloonCollectible(String name, CollectibleRarity rarity, List<RankType> ranks, long releasedAt) {
        super(name, CollectibleCategory.BALLOON, rarity, ranks, releasedAt);

        this.frames = new ArrayList<>();

        setIcon(Item.of(Material.SKULL_ITEM, 3));

        Core.getCollectibleController().save(this);
    }

    public void setFrames(List<String> frames) {
        this.frames = frames;

        if (getHeadValue() == null)
            setHeadValue(this.frames.get(0));
    }

    public void addFrames(String... frames) {
        this.frames.addAll(Arrays.asList(frames));

        if (getHeadValue() == null)
            setHeadValue(this.frames.get(0));
    }
}
