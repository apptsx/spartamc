package com.minecraft.core.api.collectible.type.companion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionAnimation;
import com.minecraft.core.api.collectible.util.math.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class PerryThePlatypusCompanion extends CompanionCollectible {

    public PerryThePlatypusCompanion() {
        super("Perry o Ornitorrinco", CollectibleRarity.EPIC, new ArrayList<>(), 8, 1726517599029L,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2U3YzE4MWExOWYzNjdmMWFjOTQ5NWEyYmU3NjFhZDBkOTE1NzRiNzVlZDc5OGY3NjVhMWVlNmJlNDFhODcxIn19fQ==");

        this.frames.createKeyFrame(0, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(180, 500, 25), "front_Left", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(180, 500, 25), "back_Left", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(180, 500, 25), "front_Right", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(180, 500, 25), "back_Right", CompanionAnimation.MovementType.ARM_RIGHT)));
        this.frames.createKeyFrame(1, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(180, 500, 25), "front_Left", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(205, 525, 25), "back_Left", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(205, 525, 25), "front_Right", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(180, 500, 25), "back_Right", CompanionAnimation.MovementType.ARM_RIGHT)));
        this.frames.createKeyFrame(2, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(205, 525, 25), "front_Left", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(180, 500, 25), "back_Left", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(180, 500, 25), "front_Right", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(205, 525, 25), "back_Right", CompanionAnimation.MovementType.ARM_RIGHT)));

        this.frames.addFrames(0, 0, 1, 2, 2, 1, 0);
        this.frames.addFrames(1, 0, 1, 2, 2, 1, 0);
        this.frames.addFrames(2, 0, 1, 2, 2, 1, 0);
        this.frames.addFrames(3, 0, 1, 2, 2, 1, 0);
    }
}
