package com.minecraft.core.backend.database.redis.message.types.account;

import com.minecraft.core.Constant;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class AccountPunishmentsMessage extends RedisMessage {

    private final UUID viewer, target;

    private final PunishmentCategory category;

    public AccountPunishmentsMessage(UUID viewer, UUID target, PunishmentCategory category) {
        super(Constant.REDIS_ACCOUNT_PUNISHMENTS_CHANNEL);

        this.viewer = viewer;
        this.target = target;

        this.category = category;
    }
}
