package com.minecraft.core.bukkit.api.hologram;

import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.row.HologramRow;
import com.minecraft.core.bukkit.api.hologram.row.animated.AnimatedRow;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import com.minecraft.core.bukkit.manager.list.HologramManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.NumberConversions;

public class HologramListener implements Listener {

    private final HologramManager manager;

    public HologramListener() {
        this.manager = BukkitCore.getManager().getHologram();
    }

    protected synchronized void updateHolograms() {
        for (Hologram hologram : manager.getHolograms().values()) {
            if (hologram == null || hologram.getLocation() == null) continue;

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getWorld().equals(hologram.getWorld()))
                    hologram.getViewers().remove(player);

                if (hologram instanceof HologramClient) {
                    HologramClient client = (HologramClient) hologram;

                    if (client.getReceiver().equals(player)) {
                        if (!player.getWorld().equals(hologram.getWorld())) {
                            client.despawnTo(player);
                        } else {
                            // Para HologramClient, sempre mantém visível se o jogador está no mesmo mundo
                            // O canSpawn pode ter problemas com cálculo de distância quando muito perto
                            // Então só verificamos se o jogador está no mundo
                            if (!hologram.getViewers().contains(player)) {
                                client.spawnTo(player);
                            }
                            // Não fazemos despawn baseado em distância para HologramClient
                            // porque são específicos para cada jogador e devem permanecer visíveis
                        }
                    }
                }

                if (hologram instanceof HologramServer) {
                    if (manager.canSpawn(hologram, player.getLocation()))
                        hologram.spawnTo(player);
                    else
                        hologram.despawnTo(player);
                }
            }

            if (hologram.isTemporary()) {
                hologram.updateTemporaryRow();

                if (hologram.hasExpired()) {
                    manager.getHolograms().remove(hologram.getId());

                    if (hologram instanceof HologramClient) {
                        HologramClient client = (HologramClient) hologram;

                        manager.removeClient(client);
                    }

                    if (hologram instanceof HologramServer)
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player == null || !player.isOnline()) continue;

                            hologram.despawnTo(player);
                        }
                }
            }
        }

    }

    protected synchronized void onExit(Player player) {
        for (Hologram hologram : manager.getHolograms().values()) {
            if (!(hologram instanceof HologramClient)) continue;

            HologramClient client = (HologramClient) hologram;

            if (client.getReceiver().equals(player))
                manager.removeClient(client);
        }
    }

    @EventHandler
    public void onUpdate(SyncUpdateEvent event) {
        if (event.isType(UpdateType.TICK)) {
            for (Hologram hologram : manager.getHolograms().values()) {
                if (hologram == null || hologram.getRows().isEmpty()) continue;

                for (HologramRow row : hologram.getRows()) {
                    if (!(row instanceof AnimatedRow)) continue;

                    AnimatedRow animated = (AnimatedRow) row;

                    animated.rotate();
                }
            }
        }

        if (event.isType(UpdateType.SECOND)) {
            updateHolograms();
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        onExit(event.getPlayer());
    }

    @EventHandler
    public void kick(PlayerKickEvent event) {
        onExit(event.getPlayer());
    }
}