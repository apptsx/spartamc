package com.minecraft.core.backend.database.redis.message.types.punish;

import com.minecraft.core.Constant;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class PunishOpenMessage extends RedisMessage {

    private final UUID staffId;
    private final String targetName;
    private final PunishmentCategory preSelectedCategory;

    public PunishOpenMessage(UUID staffId, String targetName) {
        this(staffId, targetName, null);
    }

    public PunishOpenMessage(UUID staffId, String targetName, PunishmentCategory preSelectedCategory) {
        super(Constant.REDIS_PUNISH_OPEN_CHANNEL);
        this.staffId = staffId;
        this.targetName = targetName;
        this.preSelectedCategory = preSelectedCategory;
    }
}
