package com.minecraft.core.bukkit.listener;

import com.minecraft.core.bukkit.BukkitCore;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class ViaVersionListener implements Listener {

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        Plugin plugin = event.getPlugin();
        if (plugin.getName().equalsIgnoreCase("ViaVersion")) {
            BukkitCore.getInstance().getLogger().info("[ViaVersion] Integração ativada - Jogadores de versões recentes podem se conectar!");
        }
    }
}
