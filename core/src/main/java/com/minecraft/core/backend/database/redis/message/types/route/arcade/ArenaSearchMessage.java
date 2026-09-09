package com.minecraft.core.backend.database.redis.message.types.route.arcade;

import com.minecraft.core.Constant;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ArenaSearchMessage extends RedisMessage {

    private final UUID sender;
    private final ArcadeRouteContext route;

    public ArenaSearchMessage(UUID sender, ArcadeRouteContext route) {
        super(Constant.REDIS_ARENA_SEARCH_CHANNEL);

        this.sender = sender;
        this.route = route;
    }
}
