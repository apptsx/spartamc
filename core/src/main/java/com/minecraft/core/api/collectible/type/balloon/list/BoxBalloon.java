package com.minecraft.core.api.collectible.type.balloon.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.balloon.BalloonCollectible;

import java.util.ArrayList;

public class BoxBalloon extends BalloonCollectible {

    public BoxBalloon() {
        super("Caixa Registradora", CollectibleRarity.COMUM, new ArrayList<>(), 1726495658912L);

        addFrames("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2I2NDhiOWE0NGUyODBiY2RmMjVmNGE2NmE5N2JkNWMzMzU0MmU1ZTgyNDE1ZTE1YjQ3NWM2Yjk5OWI4ZDYzNSJ9fX0=");
    }
}
