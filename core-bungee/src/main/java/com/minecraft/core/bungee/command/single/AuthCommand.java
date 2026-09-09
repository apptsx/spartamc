package com.minecraft.core.bungee.command.single;

import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.account.Account;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;

public class AuthCommand implements CommandInheritor {

    @Command(name = "changepassword", aliases = {"trocarsenha"})
    public void changePassword(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (account.isPremium()) {
            account.send("§cEste comando é somente para jogadores piratas.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length <= 1) {
            account.send("§cUtilize /" + context.getLabel() + " (senha-atual) (nova-senha).");
            return;
        }

        String current = args[0];

        if (!account.samePassword(current)) {
            account.send("§cA senha inserida está incorreta!");
            return;
        }

        account.setPassword(args[1]);

        account.send("§aVocê alterou a sua senha com sucesso.");
    }
}
