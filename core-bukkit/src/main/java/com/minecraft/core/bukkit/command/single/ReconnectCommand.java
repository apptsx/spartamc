package com.minecraft.core.bukkit.command.single;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.reconnect.Reconnect;
import com.minecraft.core.backend.database.redis.message.types.route.arcade.ArenaSearchMessage;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;

public class ReconnectCommand implements CommandInheritor {

    @Command(name = "reconectar", aliases = {"retornar", "reconnect", "rejoin"})
    public void reconnect(BukkitCommandContext context) {
        Account account = context.getAccount();
        Reconnect reconnect = Core.getReconnectData().of(account.getId());

            if (reconnect == null) {
                account.send("§cNão há jogos em andamento.");
                return;
            }

            Core.getLogger().info("[Reconnect] ✅ Reconexão encontrada! Arena: " + reconnect.getRoute().getArenaIdentifier() +
                    ", Time: " + reconnect.getTeamId() + ", Servidor: " + reconnect.getRoute().getServerId());

            new ArenaSearchMessage(account.getId(), reconnect.getRoute()).send();
        }
}
