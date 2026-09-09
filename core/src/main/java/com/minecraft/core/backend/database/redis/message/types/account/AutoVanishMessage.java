package com.minecraft.core.backend.database.redis.message.types.account;

import com.minecraft.core.Constant;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AutoVanishMessage extends RedisMessage {

    private final UUID id;

    public AutoVanishMessage(UUID id) {
        super(Constant.REDIS_ACCOUNT_VANISH_CHANNEL);
        this.id = id;
    }
}