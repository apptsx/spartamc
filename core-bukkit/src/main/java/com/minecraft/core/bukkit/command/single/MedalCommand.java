package com.minecraft.core.bukkit.command.single;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.medal.Medal;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.bukkit.manager.list.MedalManager;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;

import java.util.ArrayList;
import java.util.List;

public class MedalCommand implements CommandInheritor {

    @Completer(name = "medal", subCommands = {"medalha"})
    public List<String> medalCompleter(BukkitCommandContext context) {
        Account account = context.getAccount();

        List<String> list = new ArrayList<>(),
                medals = MedalManager.getAvailableMedals(account).stream().map(Medal::getName).toList();

        String[] args = context.getArgs();

        if (args.length > 0 && !args[0].isEmpty()) {
            String medalName = args[0].toLowerCase();

            for (String medal : medals) {
                if (medal.toLowerCase().startsWith(medalName))
                    list.add(medal);
            }
        } else
            list.addAll(medals);

        return list;
    }

    @Command(name = "medal", aliases = {"medalha"})
    public void medal(BukkitCommandContext context) {
        Account account = context.getAccount();
        String[] args = context.getArgs();

        if (args.length == 0) {
            MedalManager.sendMedals(account);
            return;
        }

        Medal medal = Medal.of(args[0]);

        if (medal == null || !account.hasMedal(medal)) {
            account.send("§cMedalha não encontrada.");
            return;
        }

        if (account.isUsingMedal(medal)) {
            account.send("§cVocê já selecionou a medalha " + medal.getColoredName() + "§c.");
            return;
        }

        account.setMedal(medal);
        account.send("§aA medalha " + medal.getColoredName() + "§a foi selecionada.");
    }
}
