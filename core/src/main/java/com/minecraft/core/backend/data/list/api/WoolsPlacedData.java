package com.minecraft.core.backend.data.list.api;

import com.minecraft.core.Core;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
public class WoolsPlacedData {

    private static WoolsPlacedData instance;
    private final Map<UUID, Long> woolsPlaced = new ConcurrentHashMap<>();

    private WoolsPlacedData() {}

    public static WoolsPlacedData getInstance() {
        if (instance == null) {
            instance = new WoolsPlacedData();
        }
        return instance;
    }

    public void incrementWools(UUID playerId) {
        woolsPlaced.merge(playerId, 1L, Long::sum);
        saveToRedis(playerId);
    }
    
    public void addWools(UUID playerId, long amount) {
        if (amount > 0) {
            woolsPlaced.merge(playerId, amount, Long::sum);
            saveToRedis(playerId);
        }
    }

    public long getWools(UUID playerId) {
        return woolsPlaced.getOrDefault(playerId, 0L);
    }

    public List<Map.Entry<UUID, Long>> getTopWools(int limit) {
        return woolsPlaced.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private void saveToRedis(UUID playerId) {
        try {
            RedisDatabase redis = Core.getRedis();
            if (redis != null && redis.isAvailable()) {
                String key = "wools_placed:" + playerId.toString();
                redis.getPool().getResource().set(key, String.valueOf(woolsPlaced.get(playerId)));
            }
        } catch (Exception e) {
            Core.getLogger().warning("Erro ao salvar wools placed: " + e.getMessage());
        }
    }

    public void loadFromRedis() {
        try {
            RedisDatabase redis = Core.getRedis();
            if (redis == null || !redis.isAvailable()) return;
            
            Set<String> keys = redis.getPool().getResource().keys("wools_placed:*");
            if (keys == null || keys.isEmpty()) return;
            
            for (String key : keys) {
                String uuidStr = key.replace("wools_placed:", "");
                UUID uuid = UUID.fromString(uuidStr);
                String value = redis.getPool().getResource().get(key);
                if (value != null) {
                    woolsPlaced.put(uuid, Long.parseLong(value));
                }
            }
        } catch (Exception e) {
            Core.getLogger().warning("Erro ao carregar wools placed: " + e.getMessage());
        }
    }
}