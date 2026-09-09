package com.minecraft.core.bungee.command.single;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.clan.Clan;
import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;

public class ClanChatCommand implements CommandInheritor {

    private static final String CLAN_CHAT_PREFIX = "§6[ClanChat]";

    @Command(name = "clan.chat", aliases = {"cc", "cchat", "clanchat"})
    public void clanChat(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasClan()) {
            account.send("§cVocê não está em nenhum clã!");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getLabel() + " [mensagem]");
            return;
        }

        String message = context.getMessage(0, args);
        Clan clan = account.getClan();

        TextComponent clanMessage = new TextComponent(
                CLAN_CHAT_PREFIX + " " +
                account.getTag().getByPrefix(account.getTagPrefix()) + account.getNickname() +
                " §6» §f" + message
        );

        for (Account member : clan.getOnlineMembers()) {
            member.send(clanMessage);
        }
    }

    @Completer(name = "clan.chat", subCommands = {"cc", "cchat", "clanchat"})
    public List<String> ccCompleter(BungeeCommandContext context) {
        return java.util.Collections.emptyList();
    }
}
