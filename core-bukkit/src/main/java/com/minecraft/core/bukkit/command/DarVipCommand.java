package com.minecraft.core.bukkit.command;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.assignment.Assignment;
import com.minecraft.core.account.context.objects.rank.Rank;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.util.list.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DarVipCommand implements CommandInheritor {

    @Completer(name = "darvip")
    public List<String> darVipCompleter(BukkitCommandContext context) {
        String[] args = context.getArgs();
        if (args.length == 1) {
            return getPlayerNames(context);
        }
        if (args.length == 2) {
            return Stream.of(RankType.values())
                    .map(RankType::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("30d");
            suggestions.add("7d");
            suggestions.add("15d");
            suggestions.add("never");
            return suggestions.stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Command(name = "darvip", rank = RankType.ADMIN, runAsync = true)
    public void darVip(BukkitCommandContext context) {
        String[] args = context.getArgs();

        if (args.length < 2) {
            context.getSender().send("§cUso: /darvip [jogador] [rank] [tempo]");
            context.getSender().send("§cRanks: " + Stream.of(RankType.values())
                    .filter(r -> r.getRole().name().equals("VIP") || r.getRole().name().equals("SPECIAL"))
                    .map(r -> r.getName().toLowerCase().replace(" ", ""))
                    .collect(Collectors.joining(", ")));
            context.getSender().send("§cTempo: 30d, 7d, never (padrão: 30d)");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            context.getSender().send(TARGET_NOT_FOUND);
            return;
        }

        String rankInput = args[1];
        RankType rankType = RankType.of(rankInput);

        if (rankType == null) {
            rankType = RankType.of(rankInput.toUpperCase().replace("+", "_PLUS").replace(" ", "_"));
        }

        if (rankType == null) {
            context.getSender().send("§cRank '" + args[1] + "' não encontrado!");
            context.getSender().send("§cRanks: " + Stream.of(RankType.values())
                    .filter(r -> r.getRole().name().equals("VIP") || r.getRole().name().equals("SPECIAL"))
                    .map(r -> r.getName().toLowerCase().replace(" ", ""))
                    .collect(Collectors.joining(", ")));
            return;
        }

        long expiresAt;
        if (args.length >= 3) {
            if (args[2].equalsIgnoreCase("-1") || args[2].equalsIgnoreCase("never") || args[2].equalsIgnoreCase("n")) {
                expiresAt = -1L;
            } else {
                expiresAt = TimeUtil.getTime(args[2]);
                if (expiresAt <= System.currentTimeMillis()) {
                    context.getSender().send("§cTempo inválido! Use formatos como: 30d, 7d, 15d, never");
                    return;
                }
            }
        } else {
            expiresAt = TimeUtil.getTime("30d");
        }

        Rank rank = Rank.builder()
                .type(rankType)
                .assignment(Assignment.STAFF)
                .author(context.getSender().getId())
                .expiresAt(expiresAt)
                .build();

        target.setRank(rank);

        Tag defaultTag = Tag.of(rankType);
        if (defaultTag != null && target.hasTag(defaultTag) && target.getTag() != defaultTag) {
            target.setTag(defaultTag);
        }

        String timeFormatted = expiresAt == -1L ? "§6permanente" : "§6" + TimeUtil.formatTime(expiresAt);
        context.getSender().send("§aRank " + rankType.getColoredName() + "§a dado para §f" + target.getNickname() + "§a por " + timeFormatted + "§a!");

        if (target.player() != null) {
            target.send("§aVocê recebeu o rank " + rankType.getColoredName() + "§a por " + timeFormatted + "§a!");
        }

        Core.getAccountData().update(target, "data");
    }
}
