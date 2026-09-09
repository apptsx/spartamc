package com.minecraft.core.api.collectible.type.companion;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionAnimation;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionFrames;
import com.minecraft.core.api.item.Item;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

@Getter
public abstract class CompanionCollectible extends Collectible {

    protected CompanionFrames frames;

    public CompanionCollectible(String name, CollectibleRarity rarity, List<RankType> ranks, int quality, long releasedAt, String headValue) {
        super(name, CollectibleCategory.COMPANION, rarity, ranks, releasedAt);

        this.frames = new CompanionFrames(quality);

        setIcon(Item.of(Material.SKULL_ITEM, 3));
        setHeadValue(headValue);

        Core.getCollectibleController().save(this);
    }

    public Map<Integer, List<CompanionAnimation>> getFrames() {
        return this.frames.getFrames();
    }

    public Map<Integer, List<CompanionAnimation>> getIdleFrames() {
        return this.frames.getIdleFrames();
    }

    public Map<Integer, List<CompanionAnimation>> getKeyFrames() {
        return this.frames.getKeyFrames();
    }

    public Map<Integer, List<CompanionAnimation>> getIdleKeyFrames() {
        return this.frames.getIdleKeyFrames();
    }

    public boolean hasIdleAnimation() {
        return !this.frames.getIdleFrames().isEmpty();
    }
}
