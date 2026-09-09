package com.minecraft.core.api.collectible.type.companion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionAnimation;
import com.minecraft.core.api.collectible.util.math.MathUtils;

import java.util.ArrayList;
import java.util.Collections;

public class BeeCompanion extends CompanionCollectible {

    public BeeCompanion() {
        super("Abelha", CollectibleRarity.EPIC, new ArrayList<>(), 5, 1726514967370L,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTI3YWI4YmZmMWU1MjcxNDhiYTc0ZjNjZDYyYjc0MWRkNjU1ZGM3ZDg3NTk5OGE2NzNlZWEwNjJhMTlmMDRmMiJ9fX0=");

        this.frames.createKeyFrame(0, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0, 0, 0), "head", CompanionAnimation.MovementType.HEAD)));
        this.frames.createKeyFrame(1, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0, 0, 15), "head", CompanionAnimation.MovementType.HEAD)));
        this.frames.createKeyFrame(2, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0, 0, -15), "head", CompanionAnimation.MovementType.HEAD)));

        this.frames.addFrames(0, 0, 1, 0, 2, 0);

        this.frames.createIdleKeyFrame(0, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0, 0, 0), "head", CompanionAnimation.MovementType.HEAD)));
        this.frames.createIdleKeyFrame(1, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0, 0, 15), "head", CompanionAnimation.MovementType.HEAD)));
        this.frames.createIdleKeyFrame(2, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0, 0, -15), "head", CompanionAnimation.MovementType.HEAD)));

        this.frames.addIdleFrames(0, 0, 1, 0, 2, 0);
    }
}
