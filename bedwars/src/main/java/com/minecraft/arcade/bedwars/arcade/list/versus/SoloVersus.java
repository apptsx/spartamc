package com.minecraft.arcade.bedwars.arcade.list.versus;

import com.minecraft.arcade.bedwars.arcade.Arcade;
import com.minecraft.core.arcade.category.ArcadeCategory;

public class SoloVersus extends Arcade {

    public SoloVersus(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.BEDWARS_VERSUS_SOLO);
    }
}
