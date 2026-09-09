package com.minecraft.core.api.collectible.type.companion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionAnimation;
import com.minecraft.core.api.collectible.util.math.MathUtils;

import java.util.ArrayList;
import java.util.Collections;

public class DuckCompanion extends CompanionCollectible {

    public DuckCompanion() {
        super("Pato", CollectibleRarity.EPIC, new ArrayList<>(), 5, 1726514967370L,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjEyMDJiNzhjYWJkMTE2ZjEyZjdjYjc2NzFiNThmZTQ0NTgzMTBhMTdiZmIzMGEwOTMzMTg2M2ViMTg1ZmI4MCJ9fX0=");

        this.frames.createKeyFrame(0, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0, 0, 0), "head", CompanionAnimation.MovementType.HEAD)));

        this.frames.addFrames(0, 0);

        this.frames.createIdleKeyFrame(0, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0, 0, 0), "head", CompanionAnimation.MovementType.HEAD)));
        this.frames.createIdleKeyFrame(1, Collections.singletonList(new CompanionAnimation(MathUtils.angle(-2.5, 0, 0), "head", CompanionAnimation.MovementType.HEAD)));
        this.frames.createIdleKeyFrame(2, Collections.singletonList(new CompanionAnimation(MathUtils.angle(-5, 0, 0), "head", CompanionAnimation.MovementType.HEAD)));
        this.frames.createIdleKeyFrame(3, Collections.singletonList(new CompanionAnimation(MathUtils.angle(2.5, 0, 0), "head", CompanionAnimation.MovementType.HEAD)));
        this.frames.createIdleKeyFrame(4, Collections.singletonList(new CompanionAnimation(MathUtils.angle(5, 0, 0), "head", CompanionAnimation.MovementType.HEAD)));

        this.frames.addIdleFrames(0, 0, 1, 2, 1, 0, 3, 4, 3);
    }
}
