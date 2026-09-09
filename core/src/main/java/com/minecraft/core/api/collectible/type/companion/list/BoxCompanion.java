package com.minecraft.core.api.collectible.type.companion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionAnimation;
import com.minecraft.core.api.collectible.util.math.MathUtils;

import java.util.ArrayList;
import java.util.Collections;

public class BoxCompanion extends CompanionCollectible {

    public BoxCompanion() {
        super("Caixa de Som", CollectibleRarity.EPIC, new ArrayList<>(), 5, 1726514967370L,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTFmMjZjMjFjYmUxZjNlNTg4MmVjNTk1MDFlNTBjNTUyNzU3ZjgxODI5YzY1YzNmYzQzMjEwNTVhZTU2ZDIxYyJ9fX0=");

        this.frames.createKeyFrame(0, Collections.singletonList(
                new CompanionAnimation(MathUtils.angle(0, 0, 0), "top", CompanionAnimation.MovementType.HEAD)));
    }
}
