package com.minecraft.core.backend.database.redis.message.types.route;

import com.minecraft.core.Constant;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import com.minecraft.core.server.type.ServerType;
import lombok.Getter;

import java.util.UUID;

@Getter
public class LobbyTeleportMessage extends RedisMessage {

    private final UUID playerId;
    private final ServerType minigameServerType;

    public LobbyTeleportMessage(UUID playerId, ServerType minigameServerType) {
        super(Constant.REDIS_LOBBY_TELEPORT_CHANNEL);
        this.playerId = playerId;
        this.minigameServerType = minigameServerType;
    }
}

