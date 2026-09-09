package com.minecraft.core.backend.database.redis.message.types.punish;

import com.minecraft.core.Constant;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.api.punishment.objects.enums.PunishmentReason;
import com.minecraft.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class PunishExecuteMessage extends RedisMessage {

    private final UUID staffId;
    private final String targetName;
    private final PunishmentCategory category;
    private final PunishmentReason reason;
    private final long expiresAt;
    private final String motive;

    public PunishExecuteMessage(UUID staffId, String targetName, PunishmentCategory category, PunishmentReason reason, long expiresAt, String motive) {
        super(Constant.REDIS_PUNISH_EXECUTE_CHANNEL);
        this.staffId = staffId;
        this.targetName = targetName;
        this.category = category;
        this.reason = reason;
        this.expiresAt = expiresAt;
        this.motive = motive;
    }
}
