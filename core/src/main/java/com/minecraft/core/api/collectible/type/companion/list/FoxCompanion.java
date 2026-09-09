package com.minecraft.core.api.collectible.type.companion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionAnimation;
import com.minecraft.core.api.collectible.util.math.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class FoxCompanion extends CompanionCollectible {

    public FoxCompanion() {
        super("Raposa", CollectibleRarity.RARE, new ArrayList<>(), 3, 1726515756782L,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTJhOTExM2IzMDFmZDRiZDhlYTlhMjQ0OTY4ZWFkM2M1ZGM5MDQ3MjE1ODg1MDc5MDI4YjdlOTg4NzBiNTBjZiJ9fX0=");

        this.frames.createKeyFrame(0, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(-0.85, 0.65, -0.45), "foot", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(-0.85, 1, -0.75), "left_foot", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(-0.85, 1, -1.15), "down_foot", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(-0.85, 1, -1.45), "down_left_foot", CompanionAnimation.MovementType.ARM_RIGHT)
        ));

        this.frames.createKeyFrame(1, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(-0.85, 0.65, -0.75), "foot", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(-0.85, 1, -0.45), "left_foot", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(-0.85, 0.65, -1.45), "down_foot", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(-0.85, 1, -1.15), "down_left_foot", CompanionAnimation.MovementType.ARM_RIGHT)
        ));

        this.frames.createKeyFrame(2, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(-0.85, 0.65, -0.6), "foot", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(-0.85, 1, -0.6), "left_foot", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(-0.85, 0.65, -1.3), "down_foot", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(-0.85, 1, -1.3), "down_left_foot", CompanionAnimation.MovementType.ARM_RIGHT)
        ));
    }
}
