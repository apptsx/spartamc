package com.minecraft.core.account.context.objects.clan;

import com.minecraft.core.Constant;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ClanMetadata {

    private UUID id = Constant.DEFAULT_ID;
    private long lastUpdate = System.currentTimeMillis();
}
