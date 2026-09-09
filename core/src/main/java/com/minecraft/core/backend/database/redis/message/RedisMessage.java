package com.minecraft.core.backend.database.redis.message;

import com.minecraft.core.Core;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RedisMessage {

    private final String channel;

    public void send() {
        Core.getRedis().publish(channel, Core.GSON.toJson(this));
    }
}
