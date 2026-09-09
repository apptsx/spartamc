package com.minecraft.arcade.duels.listener;

import com.minecraft.core.arcade.feature.ArcadeFeature;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageEvent;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import com.minecraft.arcade.duels.arcade.Arcade;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.arcade.list.combat.pearlfight.PearlFight;
import com.minecraft.arcade.duels.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DamageListener implements Listener {

    // Rastreia jogadores que acabaram de teleportar com ender pearl no PearlFight
    private final Set<UUID> enderPearlTeleported = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            Player player = event.getPlayer();
            User user = (User) User.of(player.getUniqueId());
            
            if (user != null && user.getArcade() instanceof PearlFight) {
                UUID playerId = player.getUniqueId();
                enderPearlTeleported.add(playerId);
                
                // Remove o jogador do set após 2 segundos (tempo suficiente para o dano de queda ocorrer)
                JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("Duels");
                if (plugin != null) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            enderPearlTeleported.remove(playerId);
                        }
                    }.runTaskLater(plugin, 40L); // 2 segundos = 40 ticks
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamage(PlayerDamageEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Arena arena = user.getArena();

        Arcade arcade = user.getArcade();

        if (arena.isPhase(RoomPhase.PLAYING)) {
            if (!arcade.hasFeature(ArcadeFeature.DAMAGE)) {
                event.setCancelled(true);
                return;
            }

            if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL) && !arcade.hasFeature(ArcadeFeature.FALL_DAMAGE)) {
                event.setCancelled(true);
                return;
            }

            if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL) && arcade instanceof PearlFight && enderPearlTeleported.contains(player.getUniqueId())) {
                event.setCancelled(true);
                enderPearlTeleported.remove(player.getUniqueId());
                return;
            }
        }

        event.setCancelled(user.isProtected());

        if (!event.isCancelled() && event.isDead()) {
            event.setCancelled(true);

            User killer = user.inCombat() ? (User) User.of(user.getCombat().getTarget().getUniqueId()) : null;

            arcade.handleDeath(user, killer, Arcade.DeathCause.PLAYER);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamageTarget(PlayerDamageTargetEvent event) {
        Player player = event.getPlayer(), target = event.getTarget();

        User user = (User) User.of(target.getUniqueId()), killer = (User) User.of(player.getUniqueId());

        if (user == null || killer == null) {
            event.setCancelled(true);
            return;
        }

        if (player.equals(target)) {
            event.setCancelled(true);
            return;
        }

        Arcade arcade = user.getArcade();

        if (killer.isProtected()) {
            event.setCancelled(true);
            return;
        }

        if (!arcade.hasFeature(ArcadeFeature.DAMAGE) || !arcade.equals(killer.getArcade())) {
            event.setCancelled(true);
            return;
        }

        if (!event.isCancelled()) {

            if (arcade.hasFeature(ArcadeFeature.NOT_TAKE_LIFE)) {
                event.setDamage(0);
                return;
            }

            if (event.isDead()) {
                event.setCancelled(true);

                arcade.handleDeath(user, killer, Arcade.DeathCause.PLAYER);
            } else
                user.setCombat(player);
        }
    }
}
