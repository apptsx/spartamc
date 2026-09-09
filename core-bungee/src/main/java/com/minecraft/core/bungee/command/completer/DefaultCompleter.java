package com.minecraft.core.bungee.command.completer;

import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Completer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultCompleter implements CommandInheritor {

    @Completer(name = "ping", subCommands = {"ms", "tell", "msg", "account", "acc"})
    public List<String> memberName(BungeeCommandContext context) {
        return getNameList(context);
    }

    @Completer(name = "pf", subCommands = {"playerfinder"}, rank = RankType.MOD)
    public List<String> modName(BungeeCommandContext context) {
        return getNameList(context);
    }

    protected List<String> getNameList(BungeeCommandContext context) {
        List<String> list = new ArrayList<>(),
                names = Core.getAccountController().list().stream()
                        .map(Account::getNickname)
                        .collect(Collectors.toList());

        String[] args = context.getArgs();

        if (args.length > 0 && !args[0].isEmpty()) {
            String playerName = args[0].toLowerCase();

            names.stream().filter(name -> name.toLowerCase().startsWith(playerName)).forEach(list::add);
        } else
            list.addAll(names);

        return list;
    }
}
