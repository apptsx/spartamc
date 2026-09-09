package com.minecraft.lobby.architect.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.room.map.location.SignedLocation;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.lobby.architect.Architect;
import com.minecraft.lobby.architect.list.bedwars.BedWarsArchitect;
import com.minecraft.lobby.architect.list.eggwars.EggWarsArchitect;
import com.minecraft.lobby.architect.list.duels.DuelsArchitect;
import com.minecraft.lobby.architect.list.main.MainArchitect;
import com.minecraft.lobby.architect.list.pvp.PvPArchitect;
import com.minecraft.lobby.architect.list.skywars.SkyWarsArchitect;
import com.minecraft.lobby.architect.list.thebridge.TheBridgeArchitect;
import com.minecraft.lobby.architect.list.hungergames.HungerGamesArchitect;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecraft.core.util.list.bukkit.MapUtil.getSignedLocation;

@Getter
public class ArchitectLoader {

    private Architect architect;

    public Architect getArchitect() {
        return architect;
    }

    private static final Map<ServerType, Class<? extends Architect>> architectMap = new HashMap<>();

    static {
        architectMap.put(ServerType.HUB, MainArchitect.class);
        architectMap.put(ServerType.HUB_DUELS, DuelsArchitect.class);
        architectMap.put(ServerType.HUB_BEDWARS, BedWarsArchitect.class);
        architectMap.put(ServerType.HUB_PVP, PvPArchitect.class);
        architectMap.put(ServerType.HUB_HUNGERGAMES, HungerGamesArchitect.class);
    }

    public ArchitectLoader(JavaPlugin plugin) {
        Core.getLogger().info("Iniciando configuração...");

        List<SignedLocation> locationList = new ArrayList<>();

        try {
            // Primeiro tenta carregar do config específico do servidor (ex: bedwars/config.json)
            String serverName = plugin.getConfig().getString("server", "HUB").replace("HUB_", "").toLowerCase();
            File serverConfigFile = new File(plugin.getDataFolder(), serverName + "/config.json");
            
            File configFile = serverConfigFile.exists() ? serverConfigFile : new File(plugin.getDataFolder(), "config.json");

            if (!configFile.exists()) {
                Core.getLogger().warning("O arquivo config.json não foi encontrado. Gerando um padrão...");

                plugin.getDataFolder().mkdirs();

                InputStream configStream = plugin.getResource("config.json");

                if (configStream != null) {
                    Files.copy(configStream, configFile.toPath());

                    Core.getLogger().info("O arquivo config.json foi gerado na pasta do plug-in.");
                } else {
                    Core.getLogger().severe("Não foi possível encontrar o arquivo config.json.");

                    Bukkit.shutdown();
                    return;
                }
            }

            JsonObject json = Core.PARSER.parse(new FileReader(configFile)).getAsJsonObject();

            ServerType server = ServerType.of(json.get("server").getAsString());

            if (server == null) {
                server = ServerType.HUB;

                Core.getLogger().warning("Nenhum servidor encontrado, definindo padrão...");
            }

            JsonArray locations = json.getAsJsonArray("locations");

            for (JsonElement location : locations) {
                SignedLocation signed = getSignedLocation(location);

                locationList.add(signed);
            }

            Class<? extends Architect> architectClass = architectMap.get(server);

            if (architectClass != null) {
                // Instancia o Architect com base no ServerType
                Architect architect = architectClass.newInstance();

                architect.getLocations().addAll(locationList);

                this.architect = architect;
            } else {
                throw new IllegalArgumentException("Nenhum Architect registrado para o tipo de servidor: " + server);
            }

            Core.getLogger().info("Configuração estabelecida com sucesso para: " + server.name());
        } catch (Exception e) {
            Core.getLogger().severe("Erro ao carregar o arquivo config.json: " + e.getMessage());

            e.printStackTrace();
        }
    }
}