package com.minecraft.lobby;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.option.ServerOptions;
import com.minecraft.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.lobby.architect.loader.ArchitectLoader;
import com.minecraft.lobby.listener.WoolBlockListener;
import com.minecraft.lobby.menu.shop.pix.PixValidationService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class Lobby extends BukkitCore {

    private static ArchitectLoader loader;
    private static Lobby instance;

    @Getter
    private PixValidationService pixValidationService;

    public static Lobby getInstance() {
        return instance;
    }

    public static ArchitectLoader getLoader() {
        return loader;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        loader = new ArchitectLoader(this);

        getLogger().info("Iniciando Lobby...");

        Core.setServerType(loader.getArchitect().getServer());

        ServerOptions.SPAWN_CREATURES = true;
        ServerOptions.DEFAULT_CHAT = false;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;

        if (loader.getArchitect() == null) {
            getLogger().severe("Não foi possível carregar as configurações...");

            Bukkit.shutdown();
        } else
            loader.getArchitect().load();

        new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".lobby.command");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".lobby");

        // Registrar abridor do menu de títulos
        com.minecraft.core.bukkit.menu.server.collectible.CollectibleMenu.setTitlesMenuOpener(
                new com.minecraft.lobby.menu.cosmetics.TitlesMenuOpener());

        // Registrar sistema de parkour
        new com.minecraft.lobby.parkour.ParkourListener();
        getCommand("parkour").setExecutor(new com.minecraft.lobby.parkour.ParkourCommand());
        // Inicializar hologramas do parkour (aparecem antes de começar)
        com.minecraft.lobby.parkour.ParkourManager.initializeHolograms();

        // Inicializar sistema PIX
        pixValidationService = new PixValidationService(this);
        pixValidationService.start();

        // Iniciar partículas dos blocos de slime
        com.minecraft.lobby.util.SlimeParticleTask.start();

        getLogger().info("Lobby iniciado com sucesso, feito por celest");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        com.minecraft.lobby.util.SlimeParticleTask.stop();

        if (pixValidationService != null) {
            pixValidationService.stop();
        }

        loader.getArchitect().unload();

        WoolBlockListener.removeAllBlocks();

        // Limpar parkour e hologramas
        com.minecraft.lobby.parkour.ParkourManager.cleanup();

        getLogger().info("Lobby encerrado com sucesso.");
    }
}
