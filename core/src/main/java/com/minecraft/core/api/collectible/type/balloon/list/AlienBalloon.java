package com.minecraft.core.api.collectible.type.balloon.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.balloon.BalloonCollectible;

import java.util.ArrayList;

public class AlienBalloon extends BalloonCollectible {

    public AlienBalloon() {
        super("Alienígena", CollectibleRarity.EPIC, new ArrayList<>(), 1726495658912L);

        addFrames("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTM4YjdmZjExMzYxZmMzMTAwNzAwNTUwOGI4M2ZjMzBhZTJkMTc4MjJmNTEwYzJmZGM1NjAxNzcxYzUxNzYyMiJ9fX0=");
    }
}
