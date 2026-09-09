package com.minecraft.core.api.report.context;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ReportContext {

    private final UUID sender;
    private final String reason;

    private final long timestamp = System.currentTimeMillis(), expiresAt;

    public Account getAccount() {
        return Core.getAccountData().of(sender);
    }

    public boolean isValid() {
        return expiresAt > System.currentTimeMillis();
    }
}
