package com.minecraft.core.bukkit.api.npc;

import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.npc.action.Action;
import com.minecraft.core.bukkit.api.npc.action.NpcAction;
import com.minecraft.core.bukkit.api.npc.type.client.NpcClient;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import com.minecraft.core.bukkit.manager.list.NpcManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;

public class NpcListener implements Listener {

    private final NpcManager manager;

    public NpcListener() {
        manager = BukkitCore.getManager().getNpc();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUpdate(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player != null)
                    updatePlayer(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        updatePlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinedPlayer(PlayerJoinEvent event) {
        updatePlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuitedPlayer(PlayerQuitEvent event) {
        manager.removeClients(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractedPlayer(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof LivingEntity) {
            LivingEntity clicked = (LivingEntity) event.getRightClicked();

            Npc npc = manager.getNPC(clicked.getEntityId());

            if (npc != null) {
                event.setCancelled(true);

                NpcAction action = npc.getAction();

                if (action != null)
                    action.handleAction(event.getPlayer(), Action.RIGHT);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMoveToNpc(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Location playerLocation = player.getLocation();

        for (Npc npc : manager.getNPCs()) {
            if (!npc.isSpawned() || !player.getWorld().equals(npc.getWorld()) || npc.isContact()) continue;

            Location npcLocation = npc.getLocation();

            /// mexi nisso n kkk oxi
            // Validar se os mundos são os mesmos antes de calcular distância
            if (npcLocation == null || npcLocation.getWorld() == null || 
                !npcLocation.getWorld().equals(playerLocation.getWorld())) {
                continue;
            }

            if (playerLocation.distance(npcLocation) < 1) {
                Vector push = new Vector(playerLocation.getX() - npcLocation.getX(), playerLocation.getY() - npcLocation.getY(),
                        playerLocation.getZ() - npcLocation.getZ()).normalize();

                player.setVelocity(push.multiply(0.4));

                player.playSound(player.getLocation(), Sound.PISTON_EXTEND, 1, 1);
            }
        }
    }

    private void updatePlayer(Player viewer) {
        for (Npc npc : manager.getNPCs()) {
            if (!npc.isSpawned()) continue;

            if (!viewer.getWorld().equals(npc.getWorld()) || (npc instanceof NpcClient && !((NpcClient) npc).getReceiver().equals(viewer)))
                npc.getViewers().remove(viewer);
            else if (!manager.canSpawn(viewer.getLocation(), npc.getLocation())) {
                npc.despawnTo(viewer);
            } else
                npc.spawnTo(viewer);
        }
    }
}