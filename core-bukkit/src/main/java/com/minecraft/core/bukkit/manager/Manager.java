package com.minecraft.core.bukkit.manager;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.minecraft.core.bukkit.manager.list.*;
import lombok.Getter;

@Getter
public class Manager {

    private final ProtocolManager protocol = ProtocolLibrary.getProtocolManager();

    private final SidebarManager sidebar = new SidebarManager();
    private final MenuManager menu = new MenuManager();

    private final ArcadeManager arcade = new ArcadeManager();
    private final ArcadeConfigManager arcadeConfig = new ArcadeConfigManager();

    private final NpcManager npc = new NpcManager();
    private final HologramManager hologram = new HologramManager();

    private final CooldownManager cooldown = new CooldownManager();
}
