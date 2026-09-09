package com.minecraft.core.bukkit.event.type.player;

import com.minecraft.core.bukkit.event.EventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@Getter
@Setter
@AllArgsConstructor
public class PlayerDamageTargetEvent extends EventHandler implements Cancellable {

    private final Player player, target;

    private final DamageCause cause;
    private final double finalDamage;

    private double damage, currentHealth;
    private boolean cancelled;

    public enum DamageCause {
        PROJECTILE, PLAYER
    }

    public boolean isDead() {
        return currentHealth <= 0.05;
    }
}
