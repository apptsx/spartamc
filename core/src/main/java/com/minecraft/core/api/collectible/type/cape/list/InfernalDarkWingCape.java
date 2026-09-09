package com.minecraft.core.api.collectible.type.cape.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.cape.CapeCollectible;

import java.util.ArrayList;
import java.util.Collections;

public class InfernalDarkWingCape extends CapeCollectible {

    public InfernalDarkWingCape() {
        super("Infernal Dark Wing", CollectibleRarity.EPIC, new ArrayList<>(), "infernal_dark_wing", 0.10f, 1726800004000L);
        setLore(Collections.singletonList("§7Asas sombrias infernais em partículas."));
    }
}