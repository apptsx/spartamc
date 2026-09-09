package com.minecraft.core.command;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface CommandInheritor {

    String WITHOUT_PERMISSION = "§cVocê não possui acesso a esse comando.",
            TARGET_NOT_FOUND = "§cO jogador solicitado não foi encontrado.",
            SAME_PLAYER = "§cVocê não pode executar esse comando em si mesmo.";

    default List<String> getPlayerNames(CommandContext context) {
        List<String> list = new ArrayList<>(),
                names = context.getServerPlayerList();

        String[] args = context.getArgs();

        if (args.length > 0 && !args[0].isEmpty()) {
            String playerName = args[0].toLowerCase();

            for (String name : names) {
                if (name.toLowerCase().startsWith(playerName)) {
                    list.add(name);
                }
            }
        } else
            list.addAll(names);

        return list;
    }

    default void log(CommandSender sender, String message) {
        Core.getAccountController().log(sender, message);
    }

    default void broadcast(String message) {
        Core.getAccountController().send(message);
    }

    default void broadcast(BaseComponent message) {
        Core.getAccountController().send(message);
    }

    default void broadcast(Predicate<Account> filter, String message) {
        Core.getAccountController().filter(filter).forEach(account -> account.send(message));
    }

    default void broadcast(Predicate<Account> filter, BaseComponent message) {
        Core.getAccountController().filter(filter).forEach(account -> account.send(message));
    }
}
