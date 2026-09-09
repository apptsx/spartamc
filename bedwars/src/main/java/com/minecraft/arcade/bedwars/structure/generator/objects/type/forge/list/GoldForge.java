package com.minecraft.arcade.bedwars.structure.generator.objects.type.forge.list;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.structure.generator.objects.type.forge.ForgeGenerator;
import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.core.member.list.bedwars.objects.enums.BedOre;
import org.bukkit.Location;

import java.util.concurrent.TimeUnit;

public class GoldForge extends ForgeGenerator {

    public GoldForge(Arena arena, Team team) {
        super(BedOre.GOLD, arena, team, TimeUnit.SECONDS.toMillis(5));

        setMaxStackSize(16);
    }

    @Override
    public void spawn() {
        Location center = getLocation();

        if (center != null)
            drop(getArena(), center);
    }
}
