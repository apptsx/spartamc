package com.minecraft.lobby.command;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.lobby.menu.patch.PatchLogsMenu;

public class PatchLogsCommand implements CommandInheritor {

    @Command(name = "patchlog", aliases = {"patch", "patchlogs"})
    public void patchlog(BukkitCommandContext context) {
        context.getPlayer().sendMessage("§e§lPATCH LOGS");
        context.getPlayer().sendMessage("§7Use: §e/addpatchlog §7para adicionar");
        context.getPlayer().sendMessage("§7Use: §e/removepatchlog §7para remover");
    }
}