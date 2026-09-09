package com.minecraft.core.api.collectible.type.companion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionAnimation;
import com.minecraft.core.api.collectible.util.math.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class GorillaCompanion extends CompanionCollectible {

    public GorillaCompanion() {
        super("Gorila", CollectibleRarity.EPIC, new ArrayList<>(), 5, 1726514967370L,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZmMGI5Y2VhNzhiY2FiN2JlN2QxMTk4ODdjNTNjN2FlYjBiMjgyZDFmMmM5ZjdiZmY2ZjdkMzkyMjU5Y2MifX19");

        this.frames.createKeyFrame(0, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "back_Left", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "back_Right", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "front_Rightup", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "front_Leftup", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "front_Right", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "front_Left", CompanionAnimation.MovementType.HEAD)));

        this.frames.createKeyFrame(1, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "back_Left", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-35, 0, 0), "back_Right", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "front_Rightup", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-35, 0, 0), "front_Leftup", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "front_Right", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-35, 0, 0), "front_Left", CompanionAnimation.MovementType.HEAD)));

        this.frames.createKeyFrame(2, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(-35, 0, 0), "back_Left", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "back_Right", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(35, 0, 0), "front_Rightup", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "front_Leftup", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-35, 0, 0), "front_Right", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "front_Left", CompanionAnimation.MovementType.HEAD)));


        this.frames.addFrames(0, 0, 1, 2, 2, 1, 0);
        this.frames.addFrames(1, 0, 1, 2, 2, 1, 0);
        this.frames.addFrames(2, 0, 1, 2, 2, 1, 0);
        this.frames.addFrames(3, 0, 1, 2, 2, 1, 0);
        this.frames.addFrames(4, 0, 1, 2, 2, 1, 0);
        this.frames.addFrames(5, 0, 1, 2, 2, 1, 0);

        this.frames.createIdleKeyFrame(0, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0.0D, 0.0D, 0.0D), "head", CompanionAnimation.MovementType.HEAD)));
        this.frames.createIdleKeyFrame(1, Collections.singletonList(new CompanionAnimation(MathUtils.angle(-2.5D, 0.0D, 0.0D), "head", CompanionAnimation.MovementType.HEAD)));
        this.frames.createIdleKeyFrame(2, Collections.singletonList(new CompanionAnimation(MathUtils.angle(-5.0D, 0.0D, 0.0D), "head", CompanionAnimation.MovementType.HEAD)));
        this.frames.createIdleKeyFrame(3, Collections.singletonList(new CompanionAnimation(MathUtils.angle(2.5D, 0.0D, 0.0D), "head", CompanionAnimation.MovementType.HEAD)));
        this.frames.createIdleKeyFrame(4, Collections.singletonList(new CompanionAnimation(MathUtils.angle(5.0D, 0.0D, 0.0D), "head", CompanionAnimation.MovementType.HEAD)));

        this.frames.addIdleFrames(0, 0, 1, 2, 1, 0, 3, 4, 3);
    }
}
