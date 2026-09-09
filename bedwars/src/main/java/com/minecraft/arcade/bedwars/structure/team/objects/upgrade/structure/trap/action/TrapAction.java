package com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.trap.action;

import com.minecraft.arcade.bedwars.structure.team.Team;
import org.bukkit.entity.Player;

public interface TrapAction {
    void execute(Player executor, Team team);
}
