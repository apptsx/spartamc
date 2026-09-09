package com.minecraft.core.api.collectible.type.companion.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.util.EulerAngle;

@Getter
@RequiredArgsConstructor
public class CompanionAnimation {

    private final EulerAngle angle;

    private final String name;
    private final MovementType type;

    public enum MovementType {
        ARM_RIGHT, ARM_LEFT, LEG_RIGHT, LEG_LEFT, HEAD
    }
}