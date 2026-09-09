package com.minecraft.core.bukkit;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.validation.ValidationWebServer;
import com.minecraft.core.backend.data.list.api.WoolsPlacedData;
import com.minecraft.core.backend.database.redis.listener.RedisListener;
import com.minecraft.core.bukkit.api.permission.regex.RegexPermissions;
import com.minecraft.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.core.bukkit.manager.Manager;
import com.minecraft.core.bukkit.manager.list.TaskManager;
import com.minecraft.core.bukkit.server.BukkitServer;
import com.minecraft.core.bukkit.service.filter.BukkitLogFilter;
import com.minecraft.core.bukkit.service.packet.PacketHook;
import com.minecraft.core.bukkit.service.redis.BukkitRedis;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class BukkitCore extends JavaPlugin {

    @Getter
    private static BukkitCore instance;

    @Getter
    private static Manager manager;

    @Getter
    private static RegexPermissions permissions;

    private RedisListener redisListener;
    protected ValidationWebServer validationServer;

    private static final String[] COMMANDS_TO_DISABLE = {
            "testforblocks", "weather", "xp", "reload", "rl", "worldborder", "achievement",
            "blockdata", "clone", "defaultgamemode", "entitydata", "execute", "fill", "gamemode",
            "pardon", "pardon-ip", "replaceitem", "setidletimeout", "testforblock", "title",
            "trigger", "viaver", "ps", "holograms", "hd", "holo", "hologram", "restart", "filter",
            "packetlog", "?", "minecraft:gamerule", "minecraft:gm", "minecraft:gr",
            "minecraft:kill", "minecraft:pl", "minecraft:plugin", "minecraft:plugins", "minecraft:save-all",
            "give", "minecraft:give", "minecraft:list", "minecraft:save-off", "minecraft:save-on", "minecraft:setblock", "minecraft:setworldspawn",
            "minecraft:time", "ver", "version", "minecraft:ver", "minecraft:version", "minecraft:playsound", "playsound", "particle", "minecraft:particle",
            "packet", "packet_filter", "pl", "plugin", "plugins", "list", "minecraft:list",
            "protocol", "protocollib:packet", "protocollib:packet_filter", "protocollib:protocol",
            "viaversion:viaversion", "viaversion", "viaversion:vvbukkit", "vvbukkit", "save-on", "save-off", "setblock",
            "setworldspawn", "kill", "minecraft:tp", "swm"
    };

    @Override
    public void onLoad() {
        getLogger().info("Iniciando Bukkit...");

        instance = this;
        Core.setJavaPlugin(this);

        new BukkitLogFilter().registerFilter();
        Core.load(new BukkitPlatform(this), new BukkitServer(), getLogger());
    }

    @Override
    public void onEnable() {
        manager = new Manager();

        Core.getServerData().start(Bukkit.getMaxPlayers());

        redisListener = new RedisListener(new BukkitRedis(), Constant.REDIS_CHANNELS);
        CompletableFuture.runAsync(() -> redisListener.load());
        
        CompletableFuture.runAsync(() -> WoolsPlacedData.getInstance().loadFromRedis());

        Core.getPlatform().runSync(() -> BukkitUtil.removeCommand(this, COMMANDS_TO_DISABLE), 3L);

        new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".core.bukkit.command");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".core.bukkit");

        TaskManager.handle(this, Constant.SOURCE_DIR + ".core.bukkit.service.task.list");

        Core.getJoinMessageController().handle();

        new PacketHook(this).handle();

        this.permissions = new RegexPermissions();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Core.getServerData().updatePlayerCounter(0);
            } catch (Exception e) {

            }
        }));

        getLogger().info("Bukkit iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        if (this.permissions != null)
            this.permissions.onDisable();

        Core.getServerData().updatePlayerCounter(0);

        if (!Core.getServerType().equals(ServerType.HUB))
            Core.getAccountController().list().forEach(Account::redirectToHub);

        if (Core.getServerType().isArcade())
            manager.getArcade().unload();

        TaskManager.unloadTasks();
        
        if (redisListener != null) {
            redisListener.unload();
        }

        if (validationServer != null) {
            validationServer.stop();
        }
        
        try {
            Core.getServerData().stop();
            Core.getLogger().info("[BukkitCore] Servidor removido do Redis com sucesso.");
        } catch (Exception e) {
            getLogger().severe("[BukkitCore] Erro ao remover servidor do Redis: " + e.getMessage());
        }

        Core.unload();
    }
}
