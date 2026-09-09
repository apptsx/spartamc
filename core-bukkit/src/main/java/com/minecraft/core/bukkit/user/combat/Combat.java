package com.minecraft.core.bukkit.user.combat;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

@Getter
public class Combat {

    private Player target;

    private long updatedAt = System.currentTimeMillis(), expiresAt = System.currentTimeMillis();

    public void update(Player target) {
        this.target = target;

        this.updatedAt = System.currentTimeMillis();
        this.expiresAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(20);
    }

    public boolean isValid() {
        return target != null && expiresAt > System.currentTimeMillis();
    }
}
