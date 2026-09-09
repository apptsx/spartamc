package com.minecraft.arcade.bedwars.listener;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.arcade.bedwars.user.context.objects.tracker.UserTracker;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.particle.ParticleApi;
import com.minecraft.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;

public class TeamListener implements Listener {

    @EventHandler
    public void onTracking(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (UserModel model : User.getList()) {
                if (!(model instanceof User)) continue;

                User user = (User) model;

                if (!(user.isPlayer() || user.inState(ArcadeState.ALIVE))) continue;

                Player player = user.getPlayer();

                UserTracker tracker = user.getContext().getTracker();

                if (tracker.isValid()) {
                    User tracking = (User) User.of(tracker.getTracking());

                    if (tracking == null || !tracking.isPlayer() || !tracking.inState(ArcadeState.ALIVE)
                            || !user.getArena().equals(tracking.getArena())) {
                        tracker.cancel();

                        BukkitUtil.removeItemByType(player, Material.COMPASS);
                        continue;
                    }

                    Player trackingPlayer = tracking.getPlayer();

                    double distance = player.getLocation().distance(trackingPlayer.getLocation());

                    Team team = tracker.getTeam();

                    player.setCompassTarget(trackingPlayer.getLocation());

                    ProtocolHandler.sendBar(player, String.format("§eRastreando: %s §eDistância: §b%s",
                            team.getColor() + trackingPlayer.getName(),
                            new DecimalFormat("#.#").format(distance) + "m"));
                }
            }
        }
    }

    @EventHandler
    public void onAutoRegeneration(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (Room room : BukkitCore.getManager().getArcade().getArenas()) {
                if (!(room instanceof Arena)) continue;

                Arena arena = (Arena) room;

                for (Team team : arena.getTeamList()) {
                    if (team == null || team.isDead() || !team.getUpgrade().isAutoRegeneration()) continue;

                    Location base = team.getBase();

                    team.getPlayers().forEach(player -> {
                        if (team.inIsland(player) && !player.hasPotionEffect(PotionEffectType.REGENERATION))
                            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 6, 0));
                    });

                    ParticleApi.spawnHappyParticles(base, team.getArea().getFrontAndBack() - 5, 20, 10, 3.5);
                }
            }
        }
    }

    @EventHandler
    public void onExecuteTrap(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE) || user.isImmuneTraps()) return;

        Arena arena = user.getArena();

        for (Team team : arena.getTeamList()) {
            if (team == null || team.getBase() == null || team.getTraps().isEmpty() || user.getTeam().equals(team))
                continue;

            if (team.inIsland(player) && !team.isInvader(player.getUniqueId())) {
                team.setInvader(player);
                break;
            }
        }
    }
}
