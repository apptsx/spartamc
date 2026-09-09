package com.minecraft.core.account.context.objects.cosmetic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CosmeticEntry {

    private final String identifier;
    private final long purchasedAt = System.currentTimeMillis();
}
