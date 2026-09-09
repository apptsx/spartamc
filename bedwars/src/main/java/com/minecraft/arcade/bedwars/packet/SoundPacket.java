package com.minecraft.arcade.bedwars.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;

public class SoundPacket extends PacketAdapter {

    public SoundPacket(Plugin plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.NAMED_SOUND_EFFECT);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        String $soundKey = event.getPacket().getStrings().read(0);

        if ($soundKey.startsWith("mob.bat"))
            event.setCancelled(true);
    }
}
