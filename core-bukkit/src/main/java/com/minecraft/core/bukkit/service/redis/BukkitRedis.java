package com.minecraft.core.bukkit.service.redis;

import com.minecraft.core.bukkit.event.type.server.ServerRedisMessageEvent;
import redis.clients.jedis.JedisPubSub;

public class BukkitRedis extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        // Chamar evento personalizado para reutilizar em outras instâncias.
        new ServerRedisMessageEvent(channel, message).call();
    }
}
