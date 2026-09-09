package com.minecraft.core.bukkit.command;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.server.type.ServerType;

public class PlayCommands implements CommandInheritor {

    @Command(name = "lobby", aliases = {"hub", "l"})
    public void hub(BukkitCommandContext context) {
        if (Core.getServerType() != null && Core.getServerType().equals(ServerType.HUB)) {
            return;
        }

        Account account = context.getAccount();

        if (account.inServer(ServerType.HUB)) {
            account.send("§cVocê já está conectado no Lobby Principal!");
            return;
        }

        ServerType server = account.getServerType();

        try {
            Class<?> teleportMessageClass = Class.forName("com.minecraft.core.backend.database.redis.message.types.route.LobbyTeleportMessage");
            java.lang.reflect.Constructor<?> constructor = teleportMessageClass.getConstructor(java.util.UUID.class, ServerType.class);
            Object message = constructor.newInstance(account.getId(), server);
            java.lang.reflect.Method sendMethod = teleportMessageClass.getMethod("send");
            sendMethod.invoke(message);
        } catch (Exception e) {
            Core.getLogger().fine("LobbyTeleportMessage não disponível (servidor não é lobby): " + e.getMessage());
        }

        account.redirect(ServerType.HUB);
    }
}
