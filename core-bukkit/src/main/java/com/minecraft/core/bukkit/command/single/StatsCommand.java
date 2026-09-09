package com.minecraft.core.bukkit.command.single;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.bukkit.menu.account.stats.StatsMenu;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;

import java.util.List;

public class StatsCommand implements CommandInheritor {

    @Completer(name = "stats")
    public List<String> statsCompleter(BukkitCommandContext context) {
        return getPlayerNames(context);
    }

    @Command(name = "stats")
    public void stats(BukkitCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            new StatsMenu(context.getPlayer(), account, null).handle();
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        if (!target.getToggle().isAllowStatsView()) {
            account.send("§cO jogador " + target.getName() + " não tem as estatísticas públicas.");
            return;
        }

        new StatsMenu(context.getPlayer(), target, null).handle();
    }
}
