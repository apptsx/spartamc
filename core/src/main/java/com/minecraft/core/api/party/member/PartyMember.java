package com.minecraft.core.api.party.member;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class PartyMember {

    private final UUID id;

    private final long joinedAt = System.currentTimeMillis();

    private long expiresAt = -1L;

    public Account getAccount() {
        return Core.getAccountData().of(id);
    }

    public boolean hasExpired() {
        return expiresAt > -1L && expiresAt <= System.currentTimeMillis();
    }
}
