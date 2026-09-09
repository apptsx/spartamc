package com.minecraft.core.bukkit.listener.account;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.medal.Medal;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.punishment.Punishment;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.bukkit.api.option.ServerOptions;
import com.minecraft.core.bukkit.event.type.server.ServerChatEvent;
import com.minecraft.core.util.Util;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InfoListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void chat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();

        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account == null) return;

        // Verificar se o jogador está mutado
        Punishment mute = account.getActivePunishment(PunishmentCategory.MUTE);
        if (mute != null) {
            String timeMessage = mute.isTemporary() 
                ? " §7(Expira em: §f" + com.minecraft.core.util.list.TimeUtil.formatTime(mute.getExpiresAt(), com.minecraft.core.util.list.TimeUtil.TimeFormat.SHORT) + "§7)"
                : "";
            account.send("§cVocê está mutado! Motivo: §f" + mute.getCause() + timeMessage);
            event.setCancelled(true);
            return;
        }

        if (account.hasCooldown(Constant.CHAT_COOLDOWN_KEY)) {
            account.send("§cAguarde " + account.getFormattedCooldown(Constant.CHAT_COOLDOWN_KEY) + " para conversar novamente.");
            return;
        }

        if (!account.isStaffer() && !ServerOptions.CHAT_ENABLED) {
            account.send("§cO chat do servidor está desativado.");
            return;
        }

        String message = event.getMessage();

        Predicate<Account> predicate = target -> target != null && target.player() != null && target.player().getWorld().equals(player.getWorld())
                && !target.hasBlock(account);

        if (ServerOptions.DEFAULT_CHAT) {
            Tag tag = account.getTag();

            Core.getAccountController()
                    .filter(predicate)
                    .forEach(target -> {
                        StringBuilder builder = new StringBuilder();
                        
                        // Adicionar medalha se não for NONE (antes do prefixo da tag)
                        if (account.getMedal() != null && !account.getMedal().equals(Medal.NONE)) {
                            builder.append(account.getMedal().getColoredSymbol()).append(" ");
                        }
                        
                        builder.append(tag.getByPrefix(target.getTagPrefix()))
                               .append(account.getNickname())
                               .append(": ")
                               .append(account.hasRank(RankType.VIP) ? "§f" + Util.color(message) : "§7" + message);

                        target.send(TextComponent.fromLegacyText(builder.toString()));
                    });
        } else {
            // Chamando chat customizado

            List<Player> recipients = Core.getAccountController()
                    .filter(predicate)
                    .stream().map(Account::player)
                    .collect(Collectors.toList());

            new ServerChatEvent(player, message, recipients).call();
        }

        if (!account.hasRank(RankType.VIP))
            account.setCooldown(Constant.CHAT_COOLDOWN_KEY, TimeUnit.SECONDS.toMillis(3));

        Core.getLogger().info("[Chat] " + player.getName() + ": " + message);
    }
}
