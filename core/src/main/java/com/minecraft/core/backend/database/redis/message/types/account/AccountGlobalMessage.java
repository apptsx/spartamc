package com.minecraft.core.backend.database.redis.message.types.account;

import com.minecraft.core.Constant;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class AccountGlobalMessage extends RedisMessage {

    private final UUID id;
    private final String[] message;

    public AccountGlobalMessage(UUID id, String... message) {
        super(Constant.REDIS_ACCOUNT_GLOBAL_MESSAGE_CHANNEL);

        this.id = id;
        this.message = message;
    }
}
