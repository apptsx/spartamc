package com.minecraft.core.bukkit.api.npc.action;

import org.bukkit.entity.Player;

public interface NpcAction {
    void handleAction(Player player, Action action);
}
