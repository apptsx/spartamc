package com.minecraft.core.bungee.listener;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.api.punishment.Punishment;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import com.minecraft.core.bungee.service.discord.DiscordService;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;
import java.util.List;

public class ChatListener implements Listener {

    @EventHandler
    public void chat(ChatEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        String message = event.getMessage();

        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account == null) return;

        if (!event.isCommand()) {
            Punishment mute = account.getActivePunishment(PunishmentCategory.MUTE);
            if (mute != null) {
                String timeMessage = mute.isTemporary() 
                    ? " §7(Expira em: §f" + com.minecraft.core.util.list.TimeUtil.formatTime(mute.getExpiresAt(), com.minecraft.core.util.list.TimeUtil.TimeFormat.SHORT) + "§7)"
                    : "";
                account.send("§cVocê está mutado! Motivo: §f" + mute.getCause() + timeMessage);
                event.setCancelled(true);
                return;
            }

            if (account.getToggle().isStaffChat() && account.isStaffer()) {
                Tag tag = account.getDefaultTag();

                Core.getAccountController().send(RankType.HELPER, "§9[STAFF] " + tag.getPrefix() + account.getName() + ": §f"
                        + Util.color(message));

                event.setCancelled(true);
                return;
            }

            String serverName = player.getServer() != null ? player.getServer().getInfo().getName() : "LOBBY";
            if (serverName != null) {
                DiscordService.getInstance().sendLobbyChatMessage(
                    serverName,
                    account.getName(),
                    message,
                    account.getRankType().getColor().toString()
                );
            }

        } else {
            if (!account.isPremium() && account.inServer(ServerType.AUTH) && !isAuthCommand(message))
                event.setCancelled(true);
        }
    }

    protected boolean isAuthCommand(String command) {
        List<String> list = Arrays.asList("/login", "/logar", "/register", "/registrar");

        return list.stream().anyMatch(command::startsWith);
    }
}
