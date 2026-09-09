package com.minecraft.core.api.collectible.type.companion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionAnimation;
import com.minecraft.core.api.collectible.util.math.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class LionCompanion extends CompanionCollectible {

    public LionCompanion() {
        super("Leão", CollectibleRarity.RARE, new ArrayList<>(), 5, 1726517599029L,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODU1YjIyZDUxYjFjYjQ4NDljZjNiZWRlMjg2MDk0ZTMxZWE2MTgzMmExNTM2NWY4YTA2OGZjNTVmNGMzNjg5ZCJ9fX0=");

        this.frames.createKeyFrame(0, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "front_Left", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "back_Left", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "front_Right", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "back_Right", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(183, 500, 14), "tail", CompanionAnimation.MovementType.ARM_RIGHT)));

        this.frames.createKeyFrame(1, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "front_Left", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-35, 0, 0), "back_Left", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-35, 0, 0), "front_Right", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "back_Right", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(183, 500, 0), "tail", CompanionAnimation.MovementType.ARM_RIGHT)));

        this.frames.createKeyFrame(2, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(-35, 0, 0), "front_Left", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "back_Left", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "front_Right", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-35, 0, 0), "back_Right", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(183, 500, 29), "tail", CompanionAnimation.MovementType.ARM_RIGHT)));

        this.frames.addFrames(0, 0, 1, 2, 2, 1, 0);
        this.frames.addFrames(1, 0, 1, 2, 2, 1, 0);
        this.frames.addFrames(2, 0, 1, 2, 2, 1, 0);
        this.frames.addFrames(3, 0, 1, 2, 2, 1, 0);
        this.frames.addFrames(4, 0, 1, 2, 2, 1, 0);
    }
}
