package com.minecraft.core.account.context.objects.permission;

import com.minecraft.core.account.context.objects.assignment.Assignment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Permission {

    private final String key;

    private final Assignment assignment;
    private final UUID author;

    private final long assignedAt = System.currentTimeMillis(), expiresAt;

    public boolean isPermanent() {
        return expiresAt <= -1L;
    }

    public boolean hasExpired() {
        return !isPermanent() && expiresAt <= System.currentTimeMillis();
    }
}
