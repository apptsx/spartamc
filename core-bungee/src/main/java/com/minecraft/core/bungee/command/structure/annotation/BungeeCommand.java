package com.minecraft.core.bungee.command.structure.annotation;

import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bungee.command.structure.BungeeCommandHandler;
import com.minecraft.core.command.annotation.Command;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static com.minecraft.core.command.CommandInheritor.WITHOUT_PERMISSION;

public class BungeeCommand extends net.md_5.bungee.api.plugin.Command {

    public BungeeCommand(String label) {
        super(label);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        handleCommand(sender, getName(), args);
    }

    protected void registerCommand(Map.Entry<Method, Object> entry, CommandSender sender, String[] args, String label, int subCommand) {
        try {
            entry.getKey().invoke(entry.getValue(), new BungeeCommandContext(sender, args, label, subCommand));
        } catch (Exception e) {
            Core.getLogger().log(Level.WARNING, "Não foi possível registrar o comando " + label, e);
        }
    }

    protected void handleCommand(CommandSender sender, String label, String[] args) {
        Map<String, Map.Entry<Method, Object>> commands = BungeeCommandHandler.getCommands();

        for (int i = args.length; i >= 0; i--) {
            StringBuilder builder = new StringBuilder();

            builder.append(label.toLowerCase());

            for (int x = 0; x < i; x++) {
                builder.append(".").append(args[x].toLowerCase());
            }

            String commandLabel = builder.toString();

            if (commands.containsKey(commandLabel)) {

                Map.Entry<Method, Object> entry = commands.get(commandLabel);

                Command command = entry.getKey().getAnnotation(Command.class);

                boolean isPlayer = sender instanceof ProxiedPlayer;

                if (!isPlayer) {
                    if (command.onlyPlayer()) {
                        sender.sendMessage(TextComponent.fromLegacyText("§cSomente jogadores podem usar este comando."));
                        return;
                    }

                } else {
                    if (!command.rank().equals(RankType.MEMBER)) {
                        Account account = Core.getAccountController().of(((ProxiedPlayer) sender).getUniqueId());

                        if (account == null || !account.hasRank(command.rank())) {
                            sender.sendMessage(TextComponent.fromLegacyText(WITHOUT_PERMISSION));
                            return;
                        }
                    }
                }

                /* Register Bungee Command */
                String replacedLabel = label.replace(".", " ");

                int subCommand = commandLabel.split("\\.").length - 1;

                if (command.runAsync())
                    CompletableFuture.runAsync(() -> registerCommand(entry, sender, args, replacedLabel, subCommand));
                else
                    registerCommand(entry, sender, args, replacedLabel, subCommand);

            }
        }
    }
}