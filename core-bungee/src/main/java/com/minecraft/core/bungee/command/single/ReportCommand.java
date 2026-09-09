package com.minecraft.core.bungee.command.single;

import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.report.Report;
import com.minecraft.core.api.report.context.ReportContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReportCommand implements CommandInheritor {

    @Completer(name = "report", subCommands = {"reportar", "rp", "denunciar"})
    public List<String> reportCompleter(BungeeCommandContext context) {
        return getPlayerNames(context);
    }

    @Command(name = "report", aliases = {"reportar", "rp", "denunciar"})
    public void report(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (account.hasCooldown(Constant.REPORT_SEND_COOLDOWN_KEY)) {
            account.send("§cAguarde " + account.getFormattedCooldown(Constant.REPORT_SEND_COOLDOWN_KEY) + " para denunciar novamente.");
            return;
        }

        if (args.length <= 1) {
            account.send("§cUso /" + context.getLabel() + " [jogador] [motivo]");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        String reason = context.getMessage(1, args);

        Report report = Core.getReportData().of(target.getId());

        if (report.hasContext(account.getId())) {
            account.send("§cAguarde para denunciar este jogador novamente.");
            return;
        }

        report.addContext(ReportContext.builder()
                .sender(account.getId())
                .reason(reason)
                .expiresAt(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5))
                .build());

        account.send("§aVocê denunciou §f" + target.getNickname() + " §apor §f" + reason);

        String horario = new SimpleDateFormat("HH:mm").format(new Date());

        TextComponent staffMessage = new TextComponent("§c§lNOVA DENÚNCIA §7(Clique para ir)\n");
        
        TextComponent suspeito = new TextComponent("§cSuspeito: §f" + target.getNickname());
        suspeito.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/go " + target.getNickname()));
        suspeito.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Clique para ir até o jogador")));
        
        TextComponent motivo = new TextComponent("\n§cMotivo: " + reason);
        motivo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/go " + target.getNickname()));
        motivo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Clique para ir até o jogador")));
        
        TextComponent horarioComp = new TextComponent("\n§cHorário: " + horario);
        horarioComp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/go " + target.getNickname()));
        horarioComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Clique para ir até o jogador")));
        
        staffMessage.addExtra(suspeito);
        staffMessage.addExtra(motivo);
        staffMessage.addExtra(horarioComp);
        
        Core.getAccountController().filter(Account::isStaffer).forEach(staffer -> {
            if (staffer.isOnline() && staffer.getToggle().isShowReports()) {
                staffer.send(staffMessage);
            }
        });

        try {
            com.minecraft.core.bungee.service.discord.DiscordService.getInstance().sendReportEmbed(
                    target.getNickname(),
                    reason,
                    account.getNickname(),
                    target.isOnline()
            );
        } catch (Exception e) {
            com.minecraft.core.Core.getLogger().severe("[Discord] Erro ao enviar report: " + e.getMessage());
        }

        if (!account.isStaffer())
            account.setCooldown(Constant.REPORT_SEND_COOLDOWN_KEY, TimeUnit.SECONDS.toMillis(10));
    }
}
