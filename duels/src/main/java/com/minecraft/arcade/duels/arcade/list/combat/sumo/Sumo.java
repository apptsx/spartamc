package com.minecraft.arcade.duels.arcade.list.combat.sumo;

import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.arcade.duels.arcade.Arcade;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.arcade.objects.style.SidebarStyle;
import com.minecraft.arcade.duels.user.User;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

@IgnoreEvent
public class Sumo extends Arcade {

    public Sumo(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.DUELS_SUMO);

        setStyle(SidebarStyle.LATENCY);
    }

    @Override
    public void handleHotbar(Player player, Arena arena) {
        if (!arena.isPhase(RoomPhase.PLAYING))
            handleDefaultHotbar(player);
        else
            player.getInventory().clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void onVoid(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (isValid(player)) {
            Arena arena = user.getArena();

            Location to = event.getTo(), spawn = arena.getLocation("spawn");

            /* Está caindo */
            if (to.getY() <= (spawn.getY() - 10) && !player.isOnGround() && arena.isPhase(RoomPhase.PLAYING)) {
                if (arena.isSlot(Slot.SOLO)) {

                    arena.getTeams().stream().filter(team -> !team.equals(user.getTeam())).findFirst().ifPresent(arena::setWinner);

                    arena.send(user.getTeam().getColor() + player.getName() + "§e caiu no void.");

                    arena.setPhase(RoomPhase.ENDING);

                    player.teleport(spawn.clone().add(0, 4, 0));
                    user.setState(ArcadeState.DEAD);
                }
            }
        }
    }
}
