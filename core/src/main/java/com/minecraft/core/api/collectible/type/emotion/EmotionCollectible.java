package com.minecraft.core.api.collectible.type.emotion;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class EmotionCollectible extends Collectible {

    private List<String> frames;

    private final int ticksPerFrame;

    public EmotionCollectible(String name, CollectibleRarity rarity, List<RankType> ranks, int ticksPerFrame, long releasedAt) {
        super(name, CollectibleCategory.EMOTION, rarity, ranks, releasedAt);

        this.frames = new ArrayList<>();
        this.ticksPerFrame = ticksPerFrame;

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

    @Getter
    @RequiredArgsConstructor
    public static class EmotionAnimation {

        private final EmotionCollectible cosmetic;

        private int ticks, intervalTick, currentFrame;
        private boolean up;

        public String next() {
            String next = null;

            if (this.ticks < cosmetic.getTicksPerFrame()) {
                this.ticks++;
            } else {
                this.ticks = 0;
                next = cosmetic.getFrames().get(currentFrame);

                if (this.up) {
                    if (this.currentFrame >= cosmetic.getFrames().size() - 1) {
                        this.up = false;
                    } else {
                        this.currentFrame++;
                    }
                } else {
                    if (this.currentFrame == 0) {
                        if (this.intervalTick >= 20 / this.cosmetic.getTicksPerFrame()) {
                            this.up = true;
                            this.intervalTick = 0;
                        } else {
                            this.intervalTick++;
                        }
                    } else {
                        this.currentFrame--;
                    }
                }
            }

            return next;
        }
    }
}
