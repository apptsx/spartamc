package com.minecraft.core.api.collectible.type.companion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionAnimation;
import com.minecraft.core.api.collectible.util.math.MathUtils;

import java.util.ArrayList;
import java.util.Collections;

public class BB8Companion extends CompanionCollectible {

    public BB8Companion() {
        super("BB-8", CollectibleRarity.MYTHICAL, new ArrayList<>(), 3, 1726514967370L,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZjNjhlODJhZDMyMmU1OWZhNDUwN2M0YTM4MWIxNmEwMzRmMDI2ZmRkNmQ3OTNiYTZkMGFkNDFlNjUwZGYyMCJ9fX0=");

        this.frames.createKeyFrame(0, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0, 0, 269.863122), "body", CompanionAnimation.MovementType.HEAD)));
        this.frames.createKeyFrame(1, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0, 44.9771869, 269.863122), "body", CompanionAnimation.MovementType.HEAD)));
        this.frames.createKeyFrame(2, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0, 74.4845134, 269.863122), "body", CompanionAnimation.MovementType.HEAD)));
        this.frames.createKeyFrame(3, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0, 91.6732472, 269.863122), "body", CompanionAnimation.MovementType.HEAD)));

        this.frames.addFrames(0, 1, 2, 3);
    }
}
