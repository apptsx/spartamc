package com.minecraft.arcade.duels.command;

import com.minecraft.arcade.duels.arcade.Arcade;
import com.minecraft.core.Constant;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.user.User;
import org.bukkit.entity.Player;

public class GameCommands implements CommandInheritor {

    @Command(name = "playagain")
    public void playAgain(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();

        Arcade arcade = user.getArcade();

        Arena current = user.getArena();

        ArcadeRouteContext route = ArcadeRouteContext.builder()
                .arcade(arcade.getCategory())
                .slot(current.getSlot())
                .join(user.getJoin())
                .maxPlayers(current.getMaxPlayers())
                .build();

        if (account.isPartyOwner())
            route.setLink(account.getParty().getMembersId());

        Arena arena = arcade.findBestArena(player.getUniqueId(), route);

        if (arena == null) {
            player.sendMessage(Constant.NO_ROOM_AVAILABLE_MESSAGE);
            return;
        }

        current.quit(player);

        arena.join(player);

        if (route.isLinked())
            account.getParty().redirect(route);
    }

    @Command(name = "start", rank = RankType.MOD)
    public void start(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        Arena arena = user.getArena();

        if (arena.isPhase(RoomPhase.PLAYING)) {
            player.sendMessage("§cA partida já começou!");
            return;
        }

        arena.setPhase(RoomPhase.PLAYING);
    }
}
