package com.minecraft.core.bukkit.service.packet.type;

import com.minecraft.core.Core;
import com.minecraft.core.bukkit.api.npc.Npc;
import com.minecraft.core.bukkit.api.npc.action.Action;
import com.minecraft.core.bukkit.api.npc.action.NpcAction;
import com.minecraft.core.bukkit.api.npc.type.client.NpcClient;
import com.minecraft.core.bukkit.manager.list.NpcManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class NpcActionPacket extends PacketAdapter {

    private final NpcManager manager;

    public NpcActionPacket(Plugin plugin, NpcManager manager) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY);

        this.manager = manager;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        try {
            Player player = event.getPlayer();
            PacketContainer packet = event.getPacket();

            int entityId = packet.getIntegers().read(0);
            EntityUseAction action = packet.getEntityUseActions().read(0);

            if (action == EntityUseAction.ATTACK || action == EntityUseAction.INTERACT) {
                for (Npc npc : manager.getNPCs()) {
                    if (!npc.isSpawned() || npc.getEntityId() != entityId) continue;

                    if (npc instanceof NpcClient && !((NpcClient) npc).getReceiver().equals(player)) continue;

                    Location loc = player.getLocation();

                    if (loc.distance(npc.getLocation()) < 5) {
                        NpcAction npcAction = npc.getAction();

                        if (npcAction == null) return;

                        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 2f, 1.5f);

                        Core.getPlatform().runSync(() -> npcAction.handleAction(player,
                                action.equals(EntityUseAction.ATTACK) ? Action.LEFT : Action.RIGHT));
                    }
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }

}