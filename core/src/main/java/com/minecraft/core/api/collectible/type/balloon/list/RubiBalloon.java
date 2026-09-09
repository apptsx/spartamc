package com.minecraft.core.api.collectible.type.balloon.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.balloon.BalloonCollectible;

import java.util.ArrayList;

public class RubiBalloon extends BalloonCollectible {

    public RubiBalloon() {
        super("Rubi", CollectibleRarity.COMUM, new ArrayList<>(), 1726495658912L);

        addFrames("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2YwNTI5ZDAzM2Y5MzdkNGU0ZDM0ZDg5OTRmYzQ0NjU4MGQ3MDc4YWVjZWIyYzc5N2NlNGIwMWI2MDQ0ZjAzMiJ9fX0=");
    }
}
