package com.minecraft.arcade.bedwars.structure.generator.objects.type.ore.list;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.structure.generator.objects.type.ore.OreGenerator;
import com.minecraft.core.member.list.bedwars.objects.enums.BedOre;

public class EmeraldGenerator extends OreGenerator {

    public EmeraldGenerator(int id, Arena arena) {
        super(id, arena, BedOre.EMERALD, 60);

        setMaxStackSize(2);
    }

    @Override
    public void spawn() {
        drop(getArena(), getLocation());
    }
}
