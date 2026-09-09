package com.minecraft.core.api.collectible.type.cape.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.cape.CapeCollectible;

import java.util.ArrayList;
import java.util.Collections;

public class RainbowWingCape extends CapeCollectible {

    public RainbowWingCape() {
        super("Rainbow Wing", CollectibleRarity.EPIC, new ArrayList<>(), "rainbow_wing", 0.10f, 1726800005000L);
        setLore(Collections.singletonList("§7Asas arco-íris formando sua capa em partículas."));
    }
}