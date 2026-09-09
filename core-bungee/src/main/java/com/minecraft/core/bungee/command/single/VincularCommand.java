package com.minecraft.core.bungee.command.single;

import com.minecraft.core.Constant;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.Rank;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.bungee.service.discord.DiscordService;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;

public class VincularCommand implements CommandInheritor {

    @Command(name = "vincular")
    public void vincular(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (account == null) {
            context.getSender().send("§cErro ao obter dados da conta.");
            return;
        }

        if (DiscordService.getInstance().isLinked(account.getName())) {
            account.send("§cSua conta já está vinculada a um Discord!");
            account.send("§cUse /desvincular para desvincular primeiro.");
            return;
        }

        String code = DiscordService.getInstance().generateLinkCode(account.getName());

        account.send("");
        account.send("§eSeu código para se vincular é: §b" + code);
        account.send("");
        account.send("§7Use este código no bot do Discord e depois");
        account.send("§7use §b/vincular " + code + " §7aqui no jogo!");
        account.send("");
    }

    @Command(name = "vincular.discord", aliases = {"vincular.dc", "link"}, onlyPlayer = false)
    public void vincularDiscord(BungeeCommandContext context) {
        String[] args = context.getArgs();

        if (args.length == 0) {
            context.getSender().send("§cUso: /vincular <código>");
            return;
        }

        String code = args[0];
        Object result = DiscordService.getInstance().useLinkCode(code, "discord_id_here", "discord_user");

        if (result == null) {
            context.getSender().send("§cCódigo inválido ou expirado!");
            return;
        }

        if (result instanceof String) {
            context.getSender().send((String) result);
            return;
        }

        String playerName = (String) result;
        context.getSender().send("§aSua conta foi vinculada com sucesso!");
        context.getSender().send("§aBoas-vindas @" + playerName + "!");

        checkAndAddChefeRank(context, playerName);
    }

    private void checkAndAddChefeRank(BungeeCommandContext context, String playerName) {
        Account account = context.getAccount();
        if (account == null) return;

        if (account.getName().equalsIgnoreCase(Constant.CHEFE_OWNER_NAME)) {
            account.setRank(Rank.builder().type(RankType.CHEFE).build());
            context.getSender().send("§6§lVocê recebeu o rank CHEFE!");
        }
    }

    @Command(name = "desvincular")
    public void desvincular(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (account == null) {
            context.getSender().send("§cErro ao obter dados da conta.");
            return;
        }

        if (!DiscordService.getInstance().isLinked(account.getName())) {
            account.send("§cSua conta não está vinculada a nenhum Discord!");
            return;
        }

        boolean success = DiscordService.getInstance().unlinkAccount(account.getName());

        if (success) {
            account.send("§aSua conta foi desvinculada com sucesso!");
            account.send("§7Agora você pode vincular novamente usando /vincular");
        } else {
            account.send("§cErro ao desvincular. Tente novamente.");
        }
    }
}