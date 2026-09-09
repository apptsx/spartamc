package com.minecraft.arcade.pvp;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.option.ServerOptions;
import com.minecraft.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.core.server.type.ServerType;

public class PvP extends BukkitCore {

    @Override
    public void onLoad() {
        super.onLoad();

        /* Configurações do Servidor */
        ServerOptions.DAMAGE_ENABLED = true;
        ServerOptions.DROP_ITEM_ENABLED = true;

        ServerOptions.DEFAULT_CHAT = false;

        saveDefaultConfig();

        Core.setServerType(ServerType.PVP);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        /* Iniciando Comandos/Eventos */
        new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".arcade.pvp");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".arcade.pvp");

        getManager().getArcade().handle(this, Constant.SOURCE_DIR + ".arcade.pvp.arcade.list");

        getLogger().info("PvP iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        getLogger().info("PvP encerrado com sucesso.");
    }
}
