package com.minecraft.auth;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.validation.ValidationManager;
import com.minecraft.core.account.validation.ValidationWebServer;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.option.ServerOptions;
import com.minecraft.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Bukkit;

public class Auth extends BukkitCore {

    @Override
    public void onLoad() {
        super.onLoad();

        getLogger().info("Iniciando Auth...");

        Core.setServerType(ServerType.AUTH);

        ServerOptions.DEFAULT_CHAT = false;
        ServerOptions.CHAT_ENABLED = false;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".auth.command");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".auth");

        this.validationServer = new ValidationWebServer(8080);
        this.validationServer.start();

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            ValidationManager.cleanExpired();
        }, 20L * 60 * 5, 20L * 60 * 5);

        getLogger().info("Auth iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        getLogger().info("Auth desligado com sucesso.");
    }
}
