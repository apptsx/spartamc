package com.minecraft.core.api.collectible.type.companion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionAnimation;
import com.minecraft.core.api.collectible.util.math.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class PandaCompanion extends CompanionCollectible {

    public PandaCompanion() {
        super("Panda", CollectibleRarity.COMUM, new ArrayList<>(), 5, 1726514967370L,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTQ3YjY4ZWQwMjE2MzJmNDA4ZmMyMjNlZjc5NTdjMjQ3ODZhZTUwOWE4NGU2ZjE4YTM3MWE1NWMzZDhjZjkwOSJ9fX0=");

        this.frames.createKeyFrame(0, Arrays
                .asList(new CompanionAnimation(MathUtils.angle(190, 500, 13), "frontLeft", CompanionAnimation.MovementType.ARM_RIGHT),
                        new CompanionAnimation(MathUtils.angle(190, 500, 13), "backLeft", CompanionAnimation.MovementType.ARM_RIGHT),
                        new CompanionAnimation(MathUtils.angle(190, 500, 13), "frontRight", CompanionAnimation.MovementType.ARM_RIGHT),
                        new CompanionAnimation(MathUtils.angle(190, 500, 13), "backRight", CompanionAnimation.MovementType.ARM_RIGHT),
                        new CompanionAnimation(MathUtils.angle(183, 500, 13), "tail", CompanionAnimation.MovementType.ARM_RIGHT)));

        this.frames.createKeyFrame(1, Arrays
                .asList(new CompanionAnimation(MathUtils.angle(215, 525, 13), "frontLeft", CompanionAnimation.MovementType.ARM_RIGHT),
                        new CompanionAnimation(MathUtils.angle(190, 500, 13), "backLeft", CompanionAnimation.MovementType.ARM_RIGHT),
                        new CompanionAnimation(MathUtils.angle(190, 500, 13), "frontRight", CompanionAnimation.MovementType.ARM_RIGHT),
                        new CompanionAnimation(MathUtils.angle(215, 525, 13), "backRight", CompanionAnimation.MovementType.ARM_RIGHT),
                        new CompanionAnimation(MathUtils.angle(183, 500, 29), "tail", CompanionAnimation.MovementType.ARM_RIGHT)));

        this.frames.createKeyFrame(2, Arrays
                .asList(new CompanionAnimation(MathUtils.angle(190, 500, 13), "frontLeft", CompanionAnimation.MovementType.ARM_RIGHT),
                        new CompanionAnimation(MathUtils.angle(215, 525, 13), "backLeft", CompanionAnimation.MovementType.ARM_RIGHT),
                        new CompanionAnimation(MathUtils.angle(215, 525, 13), "frontRight", CompanionAnimation.MovementType.ARM_RIGHT),
                        new CompanionAnimation(MathUtils.angle(190, 500, 13), "backRight", CompanionAnimation.MovementType.ARM_RIGHT),
                        new CompanionAnimation(MathUtils.angle(183, 500, 0), "tail", CompanionAnimation.MovementType.ARM_RIGHT)));

        this.frames.createKeyFrame(3, Arrays
                .asList(new CompanionAnimation(MathUtils.angle(190, 500, 13), "frontLeft", CompanionAnimation.MovementType.ARM_RIGHT),
                        new CompanionAnimation(MathUtils.angle(190, 500, 13), "backLeft", CompanionAnimation.MovementType.ARM_RIGHT),
                        new CompanionAnimation(MathUtils.angle(190, 500, 13), "frontRight", CompanionAnimation.MovementType.ARM_RIGHT),
                        new CompanionAnimation(MathUtils.angle(190, 500, 13), "backRight", CompanionAnimation.MovementType.ARM_RIGHT),
                        new CompanionAnimation(MathUtils.angle(183, 500, 13), "tail", CompanionAnimation.MovementType.ARM_RIGHT)));

        this.frames.addFrames(0, 1, 2, 3);
        this.frames.addFrames(1, 1, 2, 3);
        this.frames.addFrames(2, 1, 2, 3);
        this.frames.addFrames(3, 1, 2, 3);
        this.frames.addFrames(4, 1, 2, 3);
    }
}
