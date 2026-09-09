package com.minecraft.core.api.collectible.type.companion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionAnimation;
import com.minecraft.core.api.collectible.util.math.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class MiniMeCompanion extends CompanionCollectible {

    public MiniMeCompanion() {
        super("Seu filho", CollectibleRarity.EPIC, new ArrayList<>(), 5, 1726514967370L,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDRhZjgwYmMxZWVjZGI3ZjQ1MTQwMmIzOTI0MDVkNjg1YTM1ZmZmY2E4MDA3MjViOWI4MzNjZGM2N2VlMzVjYSJ9fX0=");

        this.frames.createKeyFrame(0, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "body", CompanionAnimation.MovementType.LEG_LEFT),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "body", CompanionAnimation.MovementType.LEG_RIGHT),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "body", CompanionAnimation.MovementType.ARM_LEFT),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "body", CompanionAnimation.MovementType.ARM_RIGHT)));
        this.frames.createKeyFrame(1, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(45, 0, 0), "body", CompanionAnimation.MovementType.LEG_LEFT),
                new CompanionAnimation(MathUtils.angle(-45, 0, 0), "body", CompanionAnimation.MovementType.LEG_RIGHT),
                new CompanionAnimation(MathUtils.angle(-45, 0, 0), "body", CompanionAnimation.MovementType.ARM_LEFT),
                new CompanionAnimation(MathUtils.angle(45, 0, 0), "body", CompanionAnimation.MovementType.ARM_RIGHT)));
        this.frames.createKeyFrame(2, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(-45, 0, 0), "body", CompanionAnimation.MovementType.LEG_LEFT),
                new CompanionAnimation(MathUtils.angle(45, 0, 0), "body", CompanionAnimation.MovementType.LEG_RIGHT),
                new CompanionAnimation(MathUtils.angle(45, 0, 0), "body", CompanionAnimation.MovementType.ARM_LEFT),
                new CompanionAnimation(MathUtils.angle(-45, 0, 0), "body", CompanionAnimation.MovementType.ARM_RIGHT)));

        this.frames.addFrames(0, 0, 1, 2, 2, 1, 0);
        this.frames.addFrames(1, 0, 1, 2, 2, 1, 0);
        this.frames.addFrames(2, 0, 1, 2, 2, 1, 0);
        this.frames.addFrames(3, 0, 1, 2, 2, 1, 0);
    }
}
