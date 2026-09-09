package com.minecraft.core.api.collectible.type.cape.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.cape.CapeCollectible;

import java.util.ArrayList;
import java.util.Collections;

public class HopeWingCape extends CapeCollectible {

    public HopeWingCape() {
        super("Hope Wing", CollectibleRarity.RARE, new ArrayList<>(), "hope_wing", 0.10f, 1726800003000L);
        setLore(Collections.singletonList("§7Asas da esperança em partículas brilhantes."));
    }
}