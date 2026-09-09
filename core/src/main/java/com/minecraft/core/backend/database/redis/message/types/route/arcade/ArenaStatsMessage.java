package com.minecraft.core.backend.database.redis.message.types.route.arcade;

import com.minecraft.core.Constant;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ArenaStatsMessage extends RedisMessage {

    private final UUID sender;
    private final RequestStatus status;

    private String identifier;

    public ArenaStatsMessage(UUID sender, RequestStatus status) {
        super(Constant.REDIS_ARENA_STATS_CHANNEL);

        this.sender = sender;
        this.status = status;
    }

    @Getter
    @AllArgsConstructor
    public enum RequestStatus {

        SEARCHING("§aProcurando sala..."),
        FOUND("§aSala encontrada!"),
        NOT_FOUND("§cOps! Nenhuma sala foi encontrada."),
        SENDING("§aEnviando para %s...");

        private final String name;
    }
}
