package com.minecraft.arcade.bedwars.command;

import com.minecraft.arcade.bedwars.arcade.Arcade;
import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.backend.database.redis.message.types.route.arcade.ArenaSearchMessage;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class PlayCommands implements CommandInheritor {

    @Command(name = "start", rank = RankType.MODPLUS)
    public void start(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        Arena arena = user.getArena();

        if (arena.isPhase(RoomPhase.PLAYING)) {
            player.sendMessage("§cA partida já iniciou!");
            return;
        }
        
        // Validar mínimo de 2 jogadores
        int totalPlayers = arena.getTotalPlayers();
        if (totalPlayers < 2) {
            player.sendMessage("§cSão necessários pelo menos 2 jogadores para iniciar a partida!");
            player.sendMessage("§eJogadores atuais: §f" + totalPlayers);
            return;
        }

        arena.setPhase(RoomPhase.PLAYING);
    }

    @Command(name = "playagain")
    public void playAgain(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        Arcade arcade = user.getArcade();

        Arena arena = user.getArena();

        user.reset();

        new ArenaSearchMessage(player.getUniqueId(), ArcadeRouteContext.builder()
                .arcade(arcade.getCategory())
                .slot(arena.getSlot())
                .type(arena.getType())
                .join(user.getJoin())
                .build()).send();
    }
}
