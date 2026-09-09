package com.minecraft.core.backend.database.redis.message.types.account;

import com.minecraft.core.Constant;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class AccountACFlagMessage extends RedisMessage {

    private final String playerName, hackType;
    private final int violations, maxVL;

    public AccountACFlagMessage(String playerName, String hackType, int violations, int maxVL) {
        super(Constant.REDIS_ACCOUNT_ANTICHEAT_CHANNEL);

        this.playerName = playerName;
        this.hackType = hackType;
        this.violations = violations;
        this.maxVL = maxVL;
    }
}
