package com.minecraft.core.bungee;

import com.minecraft.core.Core;
import com.minecraft.core.bungee.server.BungeeServer;
import com.minecraft.core.Constant;
import com.minecraft.core.backend.database.redis.listener.RedisListener;
import com.minecraft.core.bungee.command.structure.BungeeCommandHandler;
import com.minecraft.core.bungee.listener.handler.ListenerHandler;
import com.minecraft.core.bungee.service.filter.BungeeLogFilter;
import com.minecraft.core.bungee.service.redis.BungeeRedis;
import com.minecraft.core.bungee.service.task.UpdateTask;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BungeeCore extends Plugin {

    @Getter
    private static BungeeCore instance;
    
    private RedisListener redisListener;

    @Override
    public void onLoad() {
        getLogger().info("Iniciando Bungee...");

        instance = this;

        File pluginFolder = getDataFolder() != null ? getDataFolder().getAbsoluteFile() : new File("plugins/core-bungee");
        File spartaConfigDir;

        try {
            File pluginsDir = pluginFolder.getParentFile();
            File serverDir = pluginsDir != null ? pluginsDir.getParentFile() : null;
            File serversRoot = serverDir != null ? serverDir.getParentFile() : null;

            if (serversRoot != null) {
                spartaConfigDir = new File(serversRoot, com.minecraft.core.Constant.CONFIG_DIR_NAME);
            } else if (serverDir != null) {
                spartaConfigDir = new File(serverDir, com.minecraft.core.Constant.CONFIG_DIR_NAME);
            } else {
                spartaConfigDir = new File(pluginFolder, com.minecraft.core.Constant.CONFIG_DIR_NAME);
            }
        } catch (Exception e) {
            spartaConfigDir = new File(pluginFolder, com.minecraft.core.Constant.CONFIG_DIR_NAME);
        }

        spartaConfigDir.mkdirs();

        File configFile = new File(spartaConfigDir, "config.json");
        if (!configFile.exists()) {
            try (InputStream in = getResourceAsStream("config.json")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                    getLogger().info("config.json criado em: " + configFile.getAbsolutePath());
                }
            } catch (IOException e) {
                getLogger().warning("Erro ao criar config.json: " + e.getMessage());
            }
        }

        System.setProperty("sparta.config.path", configFile.getAbsolutePath());
        getLogger().info("SpartaConfig: " + configFile.getAbsolutePath());

        new BungeeLogFilter().registerFilter();

        Core.load(new BungeePlatform(this), new BungeeServer(), getLogger());

        Core.getPlatform().runAsync(() -> {
            try {
                Thread.sleep(500);
                
                if (Core.getRedis().isAvailable()) {
                    Core.getPartyData().clear();
                    Core.getAccountData().clearCache();
                } else {
                    getLogger().warning("Redis não está disponível, pulando limpeza de cache.");
                }
            } catch (Exception e) {
                getLogger().warning("Erro ao limpar cache: " + e.getMessage());
            }
        });
    }

    @Override
    public void onEnable() {
        Core.getPlatform().runAsync(() -> {
            try {
                Core.getServerData().start(2024);
            } catch (Exception e) {
                getLogger().severe("Erro ao iniciar ServerData: " + e.getMessage());
                e.printStackTrace();
            }
        });

        new BungeeCommandHandler(this).handle(Constant.SOURCE_DIR + ".core.bungee.command");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".core.bungee");

        redisListener = new RedisListener(new BungeeRedis(), Constant.REDIS_CHANNELS);
        Core.getPlatform().runAsync(() -> redisListener.load());

        getProxy().getScheduler().schedule(this, new UpdateTask(), 0, 1, TimeUnit.SECONDS);

        getLogger().info("Iniciando Discord Bot...");
        Core.getPlatform().runAsync(() -> {
            try {
                com.minecraft.core.bungee.service.discord.DiscordService.getInstance().start();
            } catch (Exception e) {
                getLogger().severe("Erro ao iniciar Discord: " + e.getMessage());
            }
        });

        getLogger().info("Bungee iniciado com sucesso.");

        getProxy().getScheduler().schedule(this, () -> {
            Core.getPlatform().runAsync(() -> {
                try {
                    for (com.minecraft.core.account.Account account : Core.getAccountController().list()) {
                        boolean isOwner = account.getName().equalsIgnoreCase(com.minecraft.core.Constant.CHEFE_OWNER_NAME);

                        // Remove CHEFE de quem não é o dono
                        if (account.hasRank(com.minecraft.core.account.context.objects.rank.type.RankType.CHEFE)
                                && !isOwner) {
                            account.removeRank(com.minecraft.core.account.context.objects.rank.type.RankType.CHEFE);
                            Core.getLogger().info("Removido rank CHEFE da conta: " + account.getName());
                        }

                        // Adiciona CHEFE ao dono se não tiver
                        if (isOwner
                                && !account.hasRank(com.minecraft.core.account.context.objects.rank.type.RankType.CHEFE)) {
                            account.setRank(com.minecraft.core.account.context.objects.rank.Rank.builder()
                                    .type(com.minecraft.core.account.context.objects.rank.type.RankType.CHEFE)
                                    .build());
                            Core.getLogger().info("Adicionado rank CHEFE ao dono: " + account.getName());
                        }
                    }
                    Core.getLogger().info("Verificação de ranks CHEFE concluída.");
                } catch (Exception e) {
                    getLogger().severe("Erro ao verificar ranks CHEFE: " + e.getMessage());
                }
            });
        }, 30, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        Core.getSkinCacheData().clear();
        
        if (redisListener != null) {
            redisListener.unload();
        }
        
        try {
            Core.getServerData().stop();
            Core.getLogger().info("[BungeeCore] Servidor removido do Redis com sucesso.");
        } catch (Exception e) {
            getLogger().severe("[BungeeCore] Erro ao remover servidor do Redis: " + e.getMessage());
        }

        Core.unload();
    }
}
