package com.minecraft.core.api.collectible.type.companion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionAnimation;
import com.minecraft.core.api.collectible.util.math.MathUtils;

import java.util.ArrayList;
import java.util.Collections;

public class R2D2Companion extends CompanionCollectible {

    public R2D2Companion() {
        super("R2D2", CollectibleRarity.MYTHICAL, new ArrayList<>(), 3, 1726514967370L,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2NlYmM5Nzc5OGMyZTM2MDU1MWNhYjNkZDVkYjZkNTM0OTdmZTYzMDQwOTQxYzlhYzQ5MWE1OWNiZjM4M2E3YSJ9fX0=");

        this.frames.createKeyFrame(0, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0, 0, 0), "head", CompanionAnimation.MovementType.HEAD)));
        this.frames.createKeyFrame(1, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0, 180, 0), "head", CompanionAnimation.MovementType.HEAD)));
        this.frames.createKeyFrame(2, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0, 360, 0), "head", CompanionAnimation.MovementType.HEAD)));

        this.frames.addFrames(0, 0, 1, 2);
    }
}
