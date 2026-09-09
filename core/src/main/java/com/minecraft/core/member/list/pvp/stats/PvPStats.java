package com.minecraft.core.member.list.pvp.stats;

import com.minecraft.core.member.list.pvp.stats.list.ArenaStats;
import com.minecraft.core.member.list.pvp.stats.list.LavaStats;
import com.minecraft.core.member.list.pvp.stats.list.MLGStats;
import lombok.Getter;

@Getter
public class PvPStats {

    private final ArenaStats arena = new ArenaStats(), fps = new ArenaStats();

    private final LavaStats lava = new LavaStats();
    private final MLGStats mlg = new MLGStats();
}
