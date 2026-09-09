package com.minecraft.core.account.context.objects.friend.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
public class FriendRequest {

    private final UUID sender;

    private final long startedAt = System.currentTimeMillis(), expiresAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3);

    public boolean hasExpired() {
        return expiresAt < System.currentTimeMillis();
    }
}
