package com.minecraft.auth.command;

import com.minecraft.auth.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.auth.state.AuthState;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;

public class RegisterCommand implements CommandInheritor {

    @Command(name = "register", aliases = {"registrar"})
    public void register(BukkitCommandContext context) {
        User user = (User) User.of(context.getSender().getId());

        Account account = user.getAccount();
        if (account.isAuthState(AuthState.OK)) {
            account.send("§cUtilize §e/login (senha)§c para se autenticar.");
            return;
        }

        if (!user.isLocked()) {
            account.send("§cVocê já se autenticou no servidor.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length <= 1) {
            account.send("§cUtilize /" + context.getLabel() + " (senha) (confirmar-senha).");
            return;
        }

        String password = args[0], confirmPassword = args[1];

        if (password.length() < 4) {
            account.send("§cA sua senha não pode ter menos de 4 caractéres.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            account.send("§cAs senhas informadas não coincidem, tente novamente!");
            return;
        }

        user.setLocked(false);
        account.setPassword(password);
        account.send("§aRegistro realizado com sucesso! Redirecionando...");
        Core.getPlatform().runSync(account::redirectToHub, 10L);
    }
}
