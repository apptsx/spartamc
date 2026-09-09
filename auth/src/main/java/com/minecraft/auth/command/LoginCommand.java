package com.minecraft.auth.command;

import com.minecraft.auth.user.User;
import com.minecraft.core.Constant;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.auth.state.AuthState;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;

public class LoginCommand implements CommandInheritor {

    @Command(name = "login", aliases = {"logar"})
    public void login(BukkitCommandContext context) {
        User user = (User) User.of(context.getSender().getId());

        Account account = user.getAccount();

        if (account.isAuthState(AuthState.PENDENT)) {
            account.send("§cUtilize §e/register (senha) (confirmar-senha)§c para se cadastrar.");
            return;
        }

        if (!user.isLocked()) {
            account.send("§cVocê já se autenticou no servidor.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getLabel() + " (senha).");
            return;
        }

        String password = args[0];

        if (!account.samePassword(password)) {
            if (user.getTotalAttempts() <= 1) {
                context.getPlayer().kickPlayer(
                        Constant.SERVER_TITLE
                                + "\n\n§cO limite de tentativas foi excedido."
                                + "\n§cAcesse o servidor, e tente novamente!"
                                + "\n\n§cEm caso de problemas, contate: §e" + Constant.SERVER_DISCORD
                );
                return;
            }

            user.setTotalAttempts(user.getTotalAttempts() - 1);
            account.send("§cVocê digitou a sua senha incorretamente! +" + user.getTotalAttempts() + " tentativa"
                    + (user.getTotalAttempts() > 1 ? "s" : "") + ".");

            return;
        }

        user.setLocked(false);

        // Redirecionar jogador para o Lobby Principal com pequeno delay para garantir sincronização
        account.send("§aLogin realizado com sucesso! Redirecionando...");
        com.minecraft.core.Core.getPlatform().runSync(account::redirectToHub, 10L);
    }
}
