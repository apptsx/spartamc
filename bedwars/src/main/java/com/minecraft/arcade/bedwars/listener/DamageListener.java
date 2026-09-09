package com.minecraft.arcade.bedwars.listener;

import com.minecraft.arcade.bedwars.arcade.Arcade;
import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.arcade.feature.ArcadeFeature;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageEvent;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import com.minecraft.core.member.list.bedwars.objects.ability.enums.AbilityType;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class DamageListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDamage(PlayerDamageEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || user.getArena() == null) {
            // Usuário sem arena (bypass ou carregando)
            event.setCancelled(true);
            return;
        }

        Arcade arcade = user.getArena().getArcade();

        if (!arcade.hasFeature(ArcadeFeature.DAMAGE)) {
            event.setCancelled(true);
            return;
        }

        // Verificar habilidade NO_FALL para cancelar dano de queda
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (user.getContext().hasAbility(AbilityType.NO_FALL)) {
                var noFallAbility = user.getContext().getAbility(AbilityType.NO_FALL);
                if (noFallAbility != null && noFallAbility.isActive()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        event.setCancelled(user.isProtected());

        /* Evento não cancelado */
        if (!event.isCancelled() && event.isDead()) {
            event.setCancelled(true);

            User killer = user.inCombat() ? (User) User.of(user.getCombat().getTarget().getUniqueId()) : null;

            arcade.handleDeath(user.getArena(), user, killer, Arcade.DeathCause.PLAYER);
        }
    }

    @EventHandler
    public void onVoid(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || user.getArena() == null || user.getTeam() == null) return;

        Arena arena = user.getArena();
        Arcade arcade = arena.getArcade();

        Location to = event.getTo(),
                spawn = user.getTeam().getBase();

        if (to.getY() <= spawn.getY() - 80) {
            if (!user.isProtected()) {
                player.teleport(player.getLocation().clone().add(0, spawn.getY() + 35, 0));

                User killer = user.inCombat() ? (User) User.of(user.getCombat().getTarget().getUniqueId()) : null;

                arcade.handleDeath(arena, user, killer, Arcade.DeathCause.VOID);
            } else
                player.teleport(spawn);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamageTarget(PlayerDamageTargetEvent event) {
        Player target = event.getTarget(), damager = event.getPlayer();

        User user = (User) User.of(target.getUniqueId()), killer = (User) User.of(damager.getUniqueId());

        if (user == null || killer == null || user.getArena() == null || killer.getArena() == null) {
            event.setCancelled(true);
            return;
        }

        Arcade arcade = user.getArena().getArcade();

        if (damager.equals(target)) {
            event.setCancelled(true);
            return;
        }

        if (user.isProtected() || killer.isProtected()) {
            event.setCancelled(true);
            return;
        }

        if (!arcade.hasFeature(ArcadeFeature.DAMAGE) || !arcade.equals(killer.getArena().getArcade())) {
            event.setCancelled(true);
            return;
        }
        
        // Verificar se ambos os jogadores têm time antes de comparar
        if (killer.getTeam() != null && user.getTeam() != null && killer.getTeam().equals(user.getTeam())) {
            event.setCancelled(true);
            return;
        }

        if (!event.isCancelled()) {
            if (event.isDead()) {
                event.setCancelled(true);

                arcade.handleDeath(user.getArena(), user, killer, Arcade.DeathCause.PLAYER);
            } else
                user.setCombat(damager);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onDeadByMonster(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Creature) {
            Player player = (Player) event.getEntity();

            Creature creature = (Creature) event.getDamager();

            User user = (User) User.of(player.getUniqueId());

            if (user == null || user.getArena() == null || user.isProtected() || !user.inState(ArcadeState.ALIVE)) {
                event.setCancelled(true);
                return;
            }

            Arcade arcade = user.getArcade();

            boolean dead = (player.getHealth() - event.getFinalDamage()) <= 0.5;

            if (dead) {
                event.setCancelled(true);

                if (creature.getTarget() != null && creature.getTarget().equals(player))
                    creature.setTarget(null);

                arcade.handleDeath(user.getArena(), user, null, Arcade.DeathCause.PLAYER);
            }
        }
    }
}
