package com.minecraft.core.bungee.command;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.account.context.objects.toggle.Toggle;
import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.bungee.service.discord.DiscordService;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;
import java.util.stream.Collectors;

public class ModeratorCommands implements CommandInheritor {

    @Command(name = "staffchat", aliases = {"sc"}, rank = RankType.HELPER)
    public void staffChat(BungeeCommandContext context) {
        Account account = context.getAccount();
        String[] args = context.getArgs();

        // Se tem mensagem, enviar para o staffchat
        if (args.length > 0) {
            String message = context.getMessage(0, args);
            
            // Enviar para todos os staff online (prefixo AZUL)
            Core.getAccountController().filter(acc -> acc.isStaffer()).forEach(staff -> {
                if (staff.isOnline()) {
                    Tag tag = account.getDefaultTag();
                    staff.send("§9[STAFF] " + tag.getPrefix() + account.getName() + "§f: " + message);
                }
            });
            
            // Enviar log para webhook do Discord
            DiscordService.getInstance().sendStaffChatMessage(account.getName(), message);
            
            return;
        }

        // Se não tem mensagem, enviar ajuda
        account.send("§cUtilize /sc <mensagem> para enviar ao staffchat.");
    }

    @Command(name = "adminchat", aliases = {"ac"}, rank = RankType.ADMIN)
    public void adminChat(BungeeCommandContext context) {
        Account account = context.getAccount();
        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /ac <mensagem> para enviar ao adminchat.");
            return;
        }

        String message = context.getMessage(0, args);
        
        Core.getAccountController().filter(acc -> acc.getRankType() == RankType.ADMIN || acc.getRankType() == RankType.CHEFE).forEach(admin -> {
            if (admin.isOnline()) {
                Tag tag = account.getDefaultTag();
                admin.send("§c[ADMIN] " + tag.getPrefix() + account.getName() + "§f: " + message);
            }
        });
    }

    @Command(name = "broadcast", aliases = {"bc"}, rank = RankType.ADMIN, onlyPlayer = false)
    public void broadcast(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.send("§cUtilize /" + context.getLabel() + " (mensagem).");
            return;
        }

        String message = context.getMessage(0, args);

        Core.getPlatform().sendBroadcast(message);
    }

    @Command(name = "playerfind", rank = RankType.MOD, runAsync = true, onlyPlayer = false)
    public void playerfinder(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.send("§cUtilize /" + context.getLabel() + " [jogador].");
            return;
        }

        Account account = context.getAccount(args[0]);

        if (account == null) {
            sender.send(TARGET_NOT_FOUND);
            return;
        }

        List<Account> list = Core.getAccountData().getAlts(account.getIpAddress())
                .stream()
                .filter(alt -> !alt.equals(account))
                .toList();

        if (list.isEmpty())
            sender.send("§cEste jogador não possui contas alternativas.");
        else {
            StringBuilder builder = new StringBuilder();

            builder.append("§aContas Alternativas (§f").append(list.size()).append("§a): ");

            int index = 1;
            boolean end = false;
            for (Account alt : list) {
                if (index >= list.size()) end = true;

                builder.append("§f").append(account.getName()).append(end ? "." : ", ");

                index++;
            }

            sender.send(builder.toString());
        }
    }
}
