package com.minecraft.lobby.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.lobby.menu.patch.PatchLogsRemoveMenu;
import org.bukkit.entity.Player;

public class RemovePatchLogCommand implements CommandInheritor {

    @Command(name = "removepatchlog", aliases = {"removepatch"}, rank = RankType.CHEFE)
    public void removepatchlog(BukkitCommandContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            player.sendMessage("§cApenas jogadores podem usar este comando.");
            return;
        }

        Account account = context.getAccount();
        if (!account.hasRank(RankType.CHEFE)) {
            player.sendMessage("§cApenas Court pode usar este comando.");
            return;
        }

        new PatchLogsRemoveMenu(player).handle();
    }
}