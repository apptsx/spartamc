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
import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.bungee.service.discord.DiscordService;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.util.list.TimeUtil;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BanCommands implements CommandInheritor {

    @Completer(name = "ban")
    public List<String> banCompleter(BungeeCommandContext context) {
        return getPlayerNames(context);
    }

    @Command(name = "ban", rank = RankType.HELPER, onlyPlayer = false)
    public void ban(BungeeCommandContext context) {
        CommandSender sender = context.getSender();
        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.send("§cUso: /ban [jogador]");
            sender.send("§cOu: /ban [jogador] [tempo] [motivo]");
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

        // GUI mode: /ban <player>
        if (args.length == 1) {
            if (target.getActivePunishment(PunishmentCategory.BAN) != null) {
                sender.send("§cO jogador " + target.getName() + " já está banido.");
                return;
            }
            if (!sender.isPlayer()) {
                sender.send("§cO modo GUI está disponível apenas para jogadores.");
                return;
            }
            new PunishOpenMessage(sender.getId(), target.getName(), PunishmentCategory.BAN).send();
            sender.send("§aMenu de banimento aberto para " + target.getName() + "!");
            log(sender, sender.getName() + " abriu o menu de banimento para " + target.getName());
            return;
        }

        if (args.length < 3) {
            sender.send("§cUso: /ban [jogador] [tempo] [motivo]");
            sender.send("§7Exemplo: /ban celest 7d xitado");
            sender.send("§7Tempos: 1m (minutos), 1h (horas), 1d (dias), -1 (permanente)");
            return;
        }

        if (target.getActivePunishment(PunishmentCategory.BAN) != null) {
            sender.send("§cO jogador " + target.getName() + " já está banido.");
            return;
        }

        String timeStr = args[1];
        long expiresAt = -1L;

        if (!timeStr.equals("-1")) {
            if (!TimeUtil.isValidTime(timeStr)) {
                sender.send("§cTempo inválido! Use: 1m, 1h, 1d ou -1 para permanente.");
                return;
            }
            expiresAt = TimeUtil.getTime(timeStr);
        }

        String reason = context.getMessage(2, args);

        Punishment punishment = Punishment.builder()
                .author(sender.getId())
                .player(target.getId())
                .server(target.getServerType())
                .arcade(target.getRoute().isValidArcade() ? target.getArcadeRoute().getArcade() : ArcadeCategory.NONE)
                .ip(target.getIpAddress())
                .reason(PunishmentReason.CHEATING)
                .category(PunishmentCategory.BAN)
                .cause(reason)
                .expiresAt(expiresAt)
                .build();

        Core.getPunishmentData().save(punishment);

        // Enviar embed para o Discord
        int banCount = Core.getPunishmentData().count(target.getId(), PunishmentCategory.BAN);
        DiscordService.getInstance().sendPunishmentEmbed(
                target.getName(), "BAN", reason, banCount, sender.getName(), expiresAt
        );

        String timeDisplay = expiresAt == -1 ? "permanentemente" : "por " + timeStr;
        
        sender.send("§aVocê baniu §f" + target.getName() + " §a" + timeDisplay + " §7(" + reason + ")");

        if (target.proxiedPlayer() != null) {
            target.proxiedPlayer().disconnect(TextComponent.fromLegacyText(String.format(Constant.BAN_TEMPLATE_MESSAGE,
                    expiresAt == -1 ? "permanentemente" : "temporariamente",
                    PunishmentReason.CHEATING.getName(),
                    punishment.getId())));
        }

        log(sender, sender.getName() + " baniu " + target.getName() + " " + timeDisplay + " (" + reason + ")");
    }

    @Completer(name = "mute")
    public List<String> muteCompleter(BungeeCommandContext context) {
        return getPlayerNames(context);
    }

    @Command(name = "mute", rank = RankType.HELPER, onlyPlayer = false)
    public void mute(BungeeCommandContext context) {
        CommandSender sender = context.getSender();
        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.send("§cUso: /mute [jogador]");
            sender.send("§cOu: /mute [jogador] [tempo] [motivo]");
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

        // GUI mode: /mute <player>
        if (args.length == 1) {
            if (target.getActivePunishment(PunishmentCategory.MUTE) != null) {
                sender.send("§cO jogador " + target.getName() + " já está mutado.");
                return;
            }
            if (!sender.isPlayer()) {
                sender.send("§cO modo GUI está disponível apenas para jogadores.");
                return;
            }
            new PunishOpenMessage(sender.getId(), target.getName(), PunishmentCategory.MUTE).send();
            sender.send("§aMenu de mute aberto para " + target.getName() + "!");
            log(sender, sender.getName() + " abriu o menu de mute para " + target.getName());
            return;
        }

        if (args.length < 3) {
            sender.send("§cUso: /mute [jogador] [tempo] [motivo]");
            sender.send("§7Exemplo: /mute celest 1h spam");
            sender.send("§7Tempos: 1m (minutos), 1h (horas), 1d (dias), -1 (permanente)");
            return;
        }

        if (target.getActivePunishment(PunishmentCategory.MUTE) != null) {
            sender.send("§cO jogador " + target.getName() + " já está mutado.");
            return;
        }

        String timeStr = args[1];
        long expiresAt = -1L;

        if (!timeStr.equals("-1")) {
            if (!TimeUtil.isValidTime(timeStr)) {
                sender.send("§cTempo inválido! Use: 1m, 1h, 1d ou -1 para permanente.");
                return;
            }
            expiresAt = TimeUtil.getTime(timeStr);
        }

        String reason = context.getMessage(2, args);

        Punishment punishment = Punishment.builder()
                .author(sender.getId())
                .player(target.getId())
                .server(target.getServerType())
                .arcade(target.getRoute().isValidArcade() ? target.getArcadeRoute().getArcade() : ArcadeCategory.NONE)
                .ip(target.getIpAddress())
                .reason(PunishmentReason.COMMUNITY)
                .category(PunishmentCategory.MUTE)
                .cause(reason)
                .expiresAt(expiresAt)
                .build();

        Core.getPunishmentData().save(punishment);

        // Enviar embed para o Discord
        int muteCount = Core.getPunishmentData().count(target.getId(), PunishmentCategory.MUTE);
        DiscordService.getInstance().sendPunishmentEmbed(
                target.getName(), "MUTE", reason, muteCount, sender.getName(), expiresAt
        );

        String timeDisplay = expiresAt == -1 ? "permanentemente" : "por " + timeStr;
        
        sender.send("§eVocê mutou §f" + target.getName() + " §e" + timeDisplay + " §7(" + reason + ")");
        target.send("§cVocê foi mutado " + (expiresAt == -1 ? "permanentemente" : "por " + timeStr) + ".\n§7Motivo: " + reason);

        log(sender, sender.getName() + " mutou " + target.getName() + " " + timeDisplay + " (" + reason + ")");
    }

    @Completer(name = "kick")
    public List<String> kickCompleter(BungeeCommandContext context) {
        return getPlayerNames(context);
    }

    @Command(name = "kick", rank = RankType.TRIAL, onlyPlayer = false)
    public void kick(BungeeCommandContext context) {
        CommandSender sender = context.getSender();
        String[] args = context.getArgs();

        if (args.length < 2) {
            sender.send("§cUso: /kick [jogador] [motivo]");
            sender.send("§7Exemplo: /kick celest sair sem motivo");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            sender.send(TARGET_NOT_FOUND);
            return;
        }

        if (sender.getId().equals(target.getId())) {
            sender.send("§cVocê não pode se expulsar.");
            return;
        }

        if (sender.isPlayer()) {
            Account account = context.getAccount();
            if (!account.hasRank(target.getRankType()) || account.hasOnlyRank(target.getRankType())) {
                sender.send("§cVocê não pode expulsar alguém superior ou igual a você.");
                return;
            }
        }

        String reason = context.getMessage(1, args);

        sender.send("§aVocê expulsou §f" + target.getName() + " §7(" + reason + ")");

        if (target.proxiedPlayer() != null) {
            target.proxiedPlayer().disconnect(TextComponent.fromLegacyText("§cVocê foi expulso do servidor.\n§7Motivo: " + reason));
        }

        log(sender, sender.getName() + " expulsou " + target.getName() + " (" + reason + ")");
    }

    @Command(name = "unban", rank = RankType.MOD, onlyPlayer = false)
    public void unban(BungeeCommandContext context) {
        CommandSender sender = context.getSender();
        String[] args = context.getArgs();

        if (args.length < 2) {
            sender.send("§cUso: /unban [ID da punição] [motivo]");
            return;
        }

        String punishmentId = args[0];
        Punishment punishment = Core.getPunishmentData().of(punishmentId);

        if (punishment == null) {
            sender.send("§cPunição com ID \"" + punishmentId + "\" não foi encontrada.");
            return;
        }

        if (!punishment.isValid()) {
            sender.send("§cEsta punição já foi revogada.");
            return;
        }

        if (punishment.getCategory() != PunishmentCategory.BAN) {
            sender.send("§cEsta punição não é um ban. Use /unmute.");
            return;
        }

        String reason = context.getMessage(1, args);

        punishment.revoke(sender.getId(), reason);

        Account target = Core.getAccountData().of(punishment.getPlayer());

        sender.send("§aVocê desbaniu §f" + (target != null ? target.getName() : punishment.getPlayerName()));
        log(sender, sender.getName() + " desbaniu " + punishment.getPlayerName() + " por " + reason);
    }

    @Command(name = "unmute", rank = RankType.MOD, onlyPlayer = false)
    public void unmute(BungeeCommandContext context) {
        CommandSender sender = context.getSender();
        String[] args = context.getArgs();

        if (args.length < 2) {
            sender.send("§cUso: /unmute [ID da punição] [motivo]");
            return;
        }

        String punishmentId = args[0];
        Punishment punishment = Core.getPunishmentData().of(punishmentId);

        if (punishment == null) {
            sender.send("§cPunição com ID \"" + punishmentId + "\" não foi encontrada.");
            return;
        }

        if (!punishment.isValid()) {
            sender.send("§cEsta punição já foi revogada.");
            return;
        }

        if (punishment.getCategory() != PunishmentCategory.MUTE) {
            sender.send("§cEsta punição não é um mute. Use /unban.");
            return;
        }

        String reason = context.getMessage(1, args);

        punishment.revoke(sender.getId(), reason);

        Account target = Core.getAccountData().of(punishment.getPlayer());

        sender.send("§aVocê desmutou §f" + (target != null ? target.getName() : punishment.getPlayerName()));
        log(sender, sender.getName() + " desmutou " + punishment.getPlayerName() + " por " + reason);
    }
}
