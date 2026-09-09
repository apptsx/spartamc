package com.minecraft.core.api.collectible.type.companion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionAnimation;
import com.minecraft.core.api.collectible.util.math.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class DragonCompanion extends CompanionCollectible {

    public DragonCompanion() {
        super("Dragão", CollectibleRarity.EPIC, new ArrayList<>(), 5, 1726514967370L,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZjNjhlODJhZDMyMmU1OWZhNDUwN2M0YTM4MWIxNmEwMzRmMDI2ZmRkNmQ3OTNiYTZkMGFkNDFlNjUwZGYyMCJ9fX0=");

        // Frame 0: Posição inicial
        this.frames.createKeyFrame(0, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "head", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "left_wing", CompanionAnimation.MovementType.ARM_LEFT),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "right_wing", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "tail", CompanionAnimation.MovementType.LEG_RIGHT)
        ));

        // Frame 1: Asas batendo para cima
        this.frames.createKeyFrame(1, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(-30, 0, 0), "left_wing", CompanionAnimation.MovementType.ARM_LEFT),
                new CompanionAnimation(MathUtils.angle(-30, 0, 0), "right_wing", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(15, 0, 0), "tail", CompanionAnimation.MovementType.LEG_RIGHT)
        ));

        // Frame 2: Asas batendo para baixo
        this.frames.createKeyFrame(2, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(30, 0, 0), "left_wing", CompanionAnimation.MovementType.ARM_LEFT),
                new CompanionAnimation(MathUtils.angle(30, 0, 0), "right_wing", CompanionAnimation.MovementType.ARM_RIGHT),
                new CompanionAnimation(MathUtils.angle(-15, 0, 0), "tail", CompanionAnimation.MovementType.LEG_RIGHT)
        ));

        // Adicionando os frames para animar continuamente
        this.frames.addFrames(0, 1, 2, 1);
    }
}
