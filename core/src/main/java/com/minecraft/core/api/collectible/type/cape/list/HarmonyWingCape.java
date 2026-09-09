package com.minecraft.core.api.collectible.type.cape.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.cape.CapeCollectible;

import java.util.ArrayList;
import java.util.Collections;

public class HarmonyWingCape extends CapeCollectible {

    public HarmonyWingCape() {
        super("Harmony Wing", CollectibleRarity.EPIC, new ArrayList<>(), "harmony_wing", 0.10f, 1726800002000L);
        setLore(Collections.singletonList("§7Asas da harmonia renderizadas em partículas."));
    }
}