package com.minecraft.core.bungee.command;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.punishment.Punishment;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.api.punishment.objects.enums.PunishmentReason;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.backend.database.redis.message.types.punish.PunishOpenMessage;
import com.minecraft.core.bungee.BungeeCore;
import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.util.list.TimeUtil;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;

public class PunishmentCommands implements CommandInheritor {

    @Command(name = "punir", aliases = {"punish", "p"}, rank = RankType.HELPER, onlyPlayer = false)
    public void punish(BungeeCommandContext context) {
        CommandSender sender = context.getSender();
        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.send("§cUso: /" + context.getLabel() + " [jogador]");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            sender.send(TARGET_NOT_FOUND);
            return;
        }

        if (sender.getId().equals(target.getId())) {
            sender.send("§cVocê não pode punir a si mesmo.");
            return;
        }

        if (sender.isPlayer()) {
            Account account = context.getAccount();
            if (!account.hasRank(target.getRankType()) || account.hasOnlyRank(target.getRankType())) {
                sender.send("§cVocê não pode punir alguém superior ou igual a você.");
                return;
            }
        }

        if (target.getActivePunishment(PunishmentCategory.BAN) != null || target.getActivePunishment(PunishmentCategory.MUTE) != null) {
            sender.send("§cO jogador " + target.getName() + " já possui uma punição em aberto.");
            return;
        }

        if (!sender.isPlayer()) {
            sender.send("§cO modo GUI está disponível apenas para jogadores. Use /ban ou /mute para punir pelo console.");
            return;
        }

        new PunishOpenMessage(sender.getId(), target.getName()).send();
        sender.send("§aMenu de punição aberto!");
        log(sender, sender.getName() + " abriu o menu de punição para " + target.getName());
    }

    @Command(name = "despunir", aliases = {"unpunish", "unban", "unmute"}, rank = RankType.MOD, onlyPlayer = false)
    public void unpunish(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length < 2) {
            sender.send("§cUso: /" + context.getLabel() + " [ID da punição] [motivo].");
            sender.send("§7Você pode encontrar o ID da punição na mensagem de banimento ou usando /conta [jogador].");
            return;
        }

        String punishmentId = args[0];

        Punishment punishment = Core.getPunishmentData().of(punishmentId);

        if (punishment == null) {
            sender.send("§cPunição com ID \"" + punishmentId + "\" não foi encontrada.");
            return;
        }

        if (!punishment.isValid()) {
            sender.send("§cEsta punição já foi revogada anteriormente.");
            return;
        }

        if (punishment.getRevocation().isValid()) {
            sender.send("§cEsta punição já foi revogada.");
            return;
        }

        String reason = context.getMessage(1, args);

        punishment.revoke(sender.getId(), reason);

        Account target = Core.getAccountData().of(punishment.getPlayer());

        if (target != null) {
            if (punishment.getCategory().equals(PunishmentCategory.BAN)) {
                sender.send("§aVocê desbaniu o jogador " + target.getName() + ".");
            } else if (punishment.getCategory().equals(PunishmentCategory.MUTE)) {
                sender.send("§aVocê desmutou o jogador " + target.getName() + ".");
            } else {
                sender.send("§aVocê revogou a punição do jogador " + target.getName() + ".");
            }
        } else {
            sender.send("§aVocê revogou a punição ID \"" + punishmentId + "\".");
        }

        log(sender, sender.getName() + " revogou a punição " + punishmentId + " de " + punishment.getPlayerName() + " por " + reason);
        
        // REMOVIDA A CHAMADA AO BOT
    }
}