package com.minecraft.core.backend.database.redis.message.types.account;

import com.minecraft.core.Constant;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class AccountNicknameMenuMessage extends RedisMessage {

    private final UUID playerId;

    public AccountNicknameMenuMessage(UUID playerId) {
        super(Constant.REDIS_ACCOUNT_NICKNAME_MENU_CHANNEL);

        this.playerId = playerId;
    }
}

