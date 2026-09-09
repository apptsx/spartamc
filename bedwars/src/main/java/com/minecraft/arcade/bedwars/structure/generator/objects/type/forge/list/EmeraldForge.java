package com.minecraft.arcade.bedwars.structure.generator.objects.type.forge.list;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.structure.generator.objects.type.forge.ForgeGenerator;
import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.core.member.list.bedwars.objects.enums.BedOre;
import org.bukkit.Location;

import java.util.concurrent.TimeUnit;

public class EmeraldForge extends ForgeGenerator {

    public EmeraldForge(Arena arena, Team team) {
        super(BedOre.EMERALD, arena, team, TimeUnit.SECONDS.toMillis(15));

        setMaxStackSize(4);
    }

    @Override
    public void spawn() {
        Location center = getLocation();

        if (center != null)
            drop(getArena(), center);
    }

}
