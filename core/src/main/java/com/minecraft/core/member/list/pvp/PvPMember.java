package com.minecraft.core.member.list.pvp;

import com.minecraft.core.Core;
import com.minecraft.core.member.Member;
import com.minecraft.core.member.list.pvp.stats.PvPStats;
import com.minecraft.core.member.list.pvp.stats.list.ArenaStats;
import com.minecraft.core.member.list.pvp.stats.list.LavaStats;
import com.minecraft.core.member.list.pvp.stats.list.MLGStats;

import java.util.UUID;

public class PvPMember extends Member {

    private PvPStats stats;

    public PvPMember(UUID id, String name) {
        super(id, name);

        this.stats = new PvPStats();
    }

    @Override
    public void save(String... fields) {
        for (String field : fields)
            Core.getPvpData().update(this, field);
    }

    /* Métodos de Estatísticas */
    public PvPStats getPvPStats() {
        return this.stats;
    }

    public void updateStats(PvPStats stats) {
        this.stats = stats;
        save("stats");
    }

    public ArenaStats getArenaStats() {
        return stats.getArena();
    }

    public ArenaStats getFpsStats() {
        return stats.getFps();
    }

    public LavaStats getLavaStats() {
        return stats.getLava();
    }

    public MLGStats getMlgStats() {
        return stats.getMlg();
    }
}
