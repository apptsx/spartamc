package com.minecraft.arcade.bedwars.arcade.list.versus;

import com.minecraft.arcade.bedwars.arcade.Arcade;
import com.minecraft.core.arcade.category.ArcadeCategory;

public class DuoVersus extends Arcade {

    public DuoVersus(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.BEDWARS_VERSUS_DUO);
    }
}
