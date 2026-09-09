package com.minecraft.core.bukkit.service.packet;

import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.service.packet.type.CommandListPacket;
import com.minecraft.core.bukkit.service.packet.type.HologramActionPacket;
import com.minecraft.core.bukkit.service.packet.type.NpcActionPacket;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class PacketHook {

    private final Plugin plugin;

    public void handle() {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        
        if (manager == null) {
            BukkitCore.getInstance().getLogger().warning("ProtocolLib não está cargado. PacketHook ignorado.");
            return;
        }

        manager.addPacketListener(new NpcActionPacket(plugin, BukkitCore.getManager().getNpc()));
        manager.addPacketListener(new HologramActionPacket(plugin, BukkitCore.getManager().getHologram()));

        manager.addPacketListener(new CommandListPacket(plugin));
    }
}
