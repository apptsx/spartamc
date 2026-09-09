package com.minecraft.core.backend.database.redis.message.types.account;

import com.minecraft.core.Constant;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class AccountPartyListMessage extends RedisMessage {

    private final UUID sender;

    public AccountPartyListMessage(UUID sender) {
        super(Constant.REDIS_ARENA_PARTY_LIST_CHANNEL);

        this.sender = sender;
    }
}
