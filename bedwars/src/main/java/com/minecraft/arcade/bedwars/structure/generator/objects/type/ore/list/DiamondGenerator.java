package com.minecraft.arcade.bedwars.structure.generator.objects.type.ore.list;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.structure.generator.objects.type.ore.OreGenerator;
import com.minecraft.core.member.list.bedwars.objects.enums.BedOre;

public class DiamondGenerator extends OreGenerator {

    public DiamondGenerator(int id, Arena arena) {
        super(id, arena, BedOre.DIAMOND, 30);

        setMaxStackSize(4);
    }

    @Override
    public void spawn() {
        drop(getArena(), getLocation());
    }
}
