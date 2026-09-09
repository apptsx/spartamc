package com.minecraft.core.controller.list;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.controller.Controller;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.UUID;

public class AccountController extends Controller<UUID, Account> {

    @Override
    public void save(Account account) {
        getCache().put(account.getId(), account);
    }

    public Account of(String name) {
        return of(account -> account.getNickname().equalsIgnoreCase(name));
    }

    public void send(String... message) {
        list().forEach(account -> account.send(message));
    }

    public void send(BaseComponent... message) {
        list().forEach(account -> account.send(message));
    }

    public void send(RankType rank, String... message) {
        filter(account -> account.hasRank(rank)).forEach(account -> account.send(message));
    }

    public void log(CommandSender sender, String message) {
        filter(account -> !account.equals(sender) && account.getToggle().isAllowLogs() && account.getRankType().ordinal() >= RankType.MODPLUS.ordinal()
                && (sender.isPlayer() ? of(sender.getId()) != null && account.hasRank(of(sender.getId()).getRankType()) : account.hasRank(RankType.ADMIN)))
                .forEach(account -> account.send("§7§o[" + message + "]"));
    }
}
