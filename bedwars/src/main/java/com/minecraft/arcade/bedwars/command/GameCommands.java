package com.minecraft.arcade.bedwars.command;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.menu.custom.CustomRoomMenu;
import com.minecraft.arcade.bedwars.menu.game.TeamTrackerMenu;
import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.arcade.bedwars.user.context.objects.tracker.UserTracker;
import com.minecraft.core.arcade.room.custom.RoomCustom;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class GameCommands implements CommandInheritor {

    @Command(name = "bussola", aliases = {"compass"})
    public void compass(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE) || !user.getArena().isPhase(RoomPhase.PLAYING)) {
            player.sendMessage("§cEste comando está indisponível no momento.");
            return;
        }

        UserTracker tracker = user.getContext().getTracker();

        if (tracker.isValid()) {
            Team team = tracker.getTeam();

            player.sendMessage("§cVocê está rastreando o " + team.getColor() + "Time " + team.getName() + "§c.");
            return;
        }

        new TeamTrackerMenu(player, user.getArena()).handle();
    }

    @Command(name = "menu")
    public void menu(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        Arena arena = user.getArena();

        if (!arena.isCustom()) {
            player.sendMessage("§cVocê não está em uma sala customizada.");
            return;
        }

        RoomCustom custom = arena.getCustom();

        if (!custom.isAuthor(player.getUniqueId())) {
            player.sendMessage("§cVocê não é o autor da sala customizada.");
            return;
        }

        new CustomRoomMenu(player, arena).handle();
    }
}
