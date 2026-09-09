package com.minecraft.core.api.collectible.type.companion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionAnimation;
import com.minecraft.core.api.collectible.util.math.MathUtils;

import java.util.ArrayList;
import java.util.Collections;

public class DiglettCompanion extends CompanionCollectible {

    public DiglettCompanion() {
        super("Diglett", CollectibleRarity.RARE, new ArrayList<>(), 5, 1726514967370L,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTRkZTlmNzI3OTdkMTY5NTk4YzEwZGJmMzE5MmJhN2Q5OWZlZjg3YmRjZmM5OWViZTk3NjYyNDI0NWU5OTgifX19");

        this.frames.createKeyFrame(0, Collections.singletonList(new CompanionAnimation(MathUtils.angle(0, 0, 0), "buik1", CompanionAnimation.MovementType.HEAD)));
    }
}
