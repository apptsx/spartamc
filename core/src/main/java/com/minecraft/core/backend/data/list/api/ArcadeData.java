package com.minecraft.core.backend.data.list.api;

import com.minecraft.core.Core;
import com.minecraft.core.arcade.ArcadeHolder;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.payload.ArcadePayload;
import com.minecraft.core.arcade.room.payload.RoomPayload;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import com.minecraft.core.server.type.ServerType;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ArcadeData {

    private final RedisDatabase redis;

    private final String ARCADE_KEY = "arcade:";

    public void save(ArcadeHolder arcade) {
        List<RoomPayload> properties = new ArrayList<>();

        arcade.getRooms().forEach(room -> properties.add(new RoomPayload(room)));

        redis.save(ARCADE_KEY + arcade.getCategory().getServer().name().toLowerCase() + "-" + Core.getServerId() + ":" + arcade.getCategory().name().toLowerCase(),
                new ArcadePayload(Core.getServerId(), arcade.getCategory(), arcade.getMaps(), properties));
    }

    public synchronized List<ArcadePayload> getPayloads() {
        return new ArrayList<>(redis.loadAll(ARCADE_KEY, ArcadePayload.class));
    }

    public ArcadePayload read(ArcadeCategory category) {
        // Primeiro tenta ler do servidor atual
        ArcadePayload payload = read(category, Core.getServerId());
        
        // Se não encontrar, busca de qualquer servidor disponível
        if (payload == null) {
            payload = readFromAnyServer(category);
        }
        
        return payload;
    }

    public ArcadePayload read(ArcadeCategory category, int serverId) {
        return redis.load(ARCADE_KEY + category.getServer().name().toLowerCase() + "-" + serverId + ":" + category.name().toLowerCase(), ArcadePayload.class);
    }
    
    /**
     * Busca dados de arcade de qualquer servidor disponível
     */
    private ArcadePayload readFromAnyServer(ArcadeCategory category) {
        List<ArcadePayload> payloads = getPayloads();
        
        for (ArcadePayload payload : payloads) {
            if (payload.getCategory().equals(category)) {
                return payload;
            }
        }
        
        return null;
    }

    public int getOnlinePlayers(ServerType server) {
        if (!server.isArcade()) return 0;

        List<ArcadeCategory> categories = ArcadeCategory.of(server);

        int sum = 0;

        for (ArcadeCategory category : categories)
            sum += getOnlinePlayers(category);

        return sum;
    }

    public int getOnlinePlayers(ArcadeCategory category) {
        int onlinePlayers = 0;

        for (ArcadePayload payload : getPayloads()) {
            if (payload.getCategory().equals(category))
                onlinePlayers += payload.getOnlinePlayers();
        }

        return onlinePlayers;
    }

    public void delete(ArcadeCategory category) {
        redis.delete(ARCADE_KEY + category.getServer().name().toLowerCase() + "-" + Core.getServerId() + ":" + category.name().toLowerCase());
    }

    public void delete(ServerType server) {
        getPayloads().stream()
                .filter(payload -> payload.getCategory() != null && payload.getCategory().getServer().equals(server))
                .forEach(payload -> redis.delete(ARCADE_KEY + server.name().toLowerCase() + "-" + Core.getServerId() + ":" + payload.getCategory().name().toLowerCase()));
    }

    public void update(ArcadePayload property) {
        redis.update(ARCADE_KEY + property.getCategory().getServer().name().toLowerCase() + "-" + Core.getServerId() + ":" + property.getCategory().name().toLowerCase(), property);
    }
}