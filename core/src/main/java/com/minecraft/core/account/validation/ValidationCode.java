package com.minecraft.core.account.validation;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class ValidationCode {

    private final UUID playerId;
    private final String playerName;
    private final String code;
    private final long createdAt;
    private final long expiresAt;
    
    private boolean validated;

    public ValidationCode(UUID playerId, String playerName, String code) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.code = code;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = createdAt + TimeUnit.MINUTES.toMillis(10); // Expira em 10 minutos
        this.validated = false;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    public boolean isValid() {
        return validated && !isExpired();
    }
}
