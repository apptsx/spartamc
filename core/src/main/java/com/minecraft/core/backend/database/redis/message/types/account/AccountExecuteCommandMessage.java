package com.minecraft.core.backend.database.redis.message.types.account;

import com.minecraft.core.Constant;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class AccountExecuteCommandMessage extends RedisMessage {

    private final UUID sender;
    private final String command;

    public AccountExecuteCommandMessage(UUID sender, String command) {
        super(Constant.REDIS_ACCOUNT_EXECUTE_COMMAND_CHANNEL);

        this.sender = sender;
        this.command = command;
    }
}
