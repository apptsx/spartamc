package com.minecraft.arcade.bedwars.command;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class ChatCommand implements CommandInheritor {

    @Command(name = "globalchat", aliases = {"g"})
    public void globalChat(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        Arena arena = user.getArena();

        if (!arena.isPhase(RoomPhase.PLAYING) || arena.isSlot(Slot.SOLO)) {
            player.sendMessage("§cEste comando está indisponível no momento.");
            return;
        }

        if (!user.inState(ArcadeState.ALIVE)) {
            player.sendMessage("§cVocê não pode usar o chat global.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage("§cUso: /" + context.getLabel() + " (mensagem).");
            return;
        }

        String message = context.getMessage(0, args);

        arena.chat(Arena.ChatType.GLOBAL, user, message);
    }
}
