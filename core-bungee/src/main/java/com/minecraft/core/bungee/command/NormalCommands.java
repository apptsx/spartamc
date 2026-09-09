package com.minecraft.core.bungee.command;

import com.minecraft.core.Constant;
import com.minecraft.core.account.Account;
import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.util.Util;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;

public class NormalCommands implements CommandInheritor {

    @Command(name = "ping", aliases = {"ms"})
    public void ping(BungeeCommandContext context) {
        ProxiedPlayer player = context.getPlayer();
        String[] args = context.getArgs();

        if (args.length == 0) {
            if (player.getPing() >= 150) {
                player.sendMessage(TextComponent.fromLegacyText("§eSeu ping: §c" + Util.formatNumber(player.getPing()) + "ms"));
            } else {
                player.sendMessage(TextComponent.fromLegacyText("§eSeu ping: §a" + Util.formatNumber(player.getPing()) + "ms"));
            }
            return;
        }

        ProxiedPlayer target = context.getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(TextComponent.fromLegacyText(TARGET_NOT_FOUND));
            return;
        }

        Account targetAccount = context.getAccount(target.getUniqueId());

        if (target.getPing() >= 150) {
            player.sendMessage(TextComponent.fromLegacyText("§eO ping de " + targetAccount.getTag().getByPrefix(targetAccount.getTagPrefix()) + targetAccount.getNickname() + "§e é: §c" + Util.formatNumber(target.getPing()) + "ms"));
        } else {
            player.sendMessage(TextComponent.fromLegacyText("§eO ping de " + targetAccount.getTag().getByPrefix(targetAccount.getTagPrefix()) + targetAccount.getNickname() + "§e é: §a" + Util.formatNumber(target.getPing()) + "ms"));
        }
    }

    @Command(name = "discord", aliases = {"dc"})
    public void discord(BungeeCommandContext context) {
        TextComponent message = new TextComponent("§eClique ");
        TextComponent clickButton = new TextComponent("§e§lAQUI");

        clickButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§6Clique!")));
        clickButton.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://" + Constant.SERVER_DISCORD));

        message.addExtra(clickButton);
        message.addExtra(" §epara acessar o nosso Discord!");

        context.getSender().send(message);
    }

    @Command(name = "tiktok", aliases = {"ttk"})
    public void tiktok(BungeeCommandContext context) {
        TextComponent message = new TextComponent("§dClique ");
        TextComponent clickButton = new TextComponent("§5§lAQUI");

        clickButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§6Clique!")));
        clickButton.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://" + Constant.SERVER_TIKTOK));

        message.addExtra(clickButton);
        message.addExtra(" §dpara acessar o nosso Tik tok!");

        context.getSender().send(message);
    }

    @Command(name = "tell", aliases = {"msg", "message", "w", "whisper"})
    public void message(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length <= 1) {
            account.send("§cUso: /" + context.getLabel() + " [jogador] [mensagem].");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null || !target.isOnline()) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        if (account.hasBlock(target) || target.hasBlock(account)) {
            account.send("§cVocê não pode enviar mensagem para jogadores bloqueados.");
            return;
        }

        if (!target.getToggle().isAllowMessages()) {
            account.send("§cO jogador " + target.getNickname() + " não está recebendo mensagens.");
            return;
        }

        String message = context.getMessage(1, args);

        account.setLastMessage(target.getId());
        target.setLastMessage(account.getId());
        account.send("§eVocê §7⥤ " + target.getTag().getByPrefix(target.getTagPrefix()) + target.getNickname() + "§e: §f " + message);
        target.send(account.getTag().getByPrefix(account.getTagPrefix()) + account.getNickname() + " §7⥤ §eVocê: §f" + message);
    }

    @Command(name = "reply", aliases = {"responder", "r"})
    public void reply(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (!account.hasLastMessage()) {
            account.send("§cVocê não tem mensagens pendentes.");
            return;
        }

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getLabel() + " (mensagem).");
            return;
        }

        Account target = context.getAccount(account.getContext().getLastMessage());

        account.setLastMessage(Constant.DEFAULT_ID);

        if (target == null || !target.isOnline()) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.hasBlock(target) || target.hasBlock(account)) {
            account.send("§cVocê não pode enviar mensagem para jogadores bloqueados.");
            return;
        }

        String message = context.getMessage(0, args);
        account.send("§eVocê §7⥤ " + target.getTag().getByPrefix(target.getTagPrefix()) + target.getNickname() + "§e: §f " + message);
        target.send(account.getTag().getByPrefix(account.getTagPrefix()) + account.getNickname() + " §7⥤ §eVocê: §f" + message);
    }
}
