package com.minecraft.core.api.collectible.type.balloon.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.balloon.BalloonCollectible;

import java.util.ArrayList;

public class MCBalloon extends BalloonCollectible {

    public MCBalloon() {
        super("MC Donalds", CollectibleRarity.RARE, new ArrayList<>(), 1726495658912L);

        addFrames("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjI5M2I4ODM3NzVjNTMzNjI2OWQxNTZjMjA0NGY0NGQyNjlkNTY0ZjI2ODYzOGE1ZTZmNTZlMjBhYjE0NWMifX19=");
    }
}
