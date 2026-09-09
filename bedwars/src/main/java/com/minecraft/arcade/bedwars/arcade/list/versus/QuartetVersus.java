package com.minecraft.arcade.bedwars.arcade.list.versus;

import com.minecraft.arcade.bedwars.arcade.Arcade;
import com.minecraft.core.arcade.category.ArcadeCategory;

public class QuartetVersus extends Arcade {

    public QuartetVersus(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.BEDWARS_VERSUS_QUARTET);
    }
}
