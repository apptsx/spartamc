package com.minecraft.core.bukkit.event.type.player;

import com.minecraft.core.bukkit.event.EventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageEvent;

@Getter
@Setter
@AllArgsConstructor
public class PlayerDamageEvent extends EventHandler implements Cancellable {

    private final Player player;
    private final EntityDamageEvent.DamageCause cause;

    private double damage, currentHealth;

    private final double finalDamage;

    private boolean cancelled;

    public boolean isDead() {
        return currentHealth <= 0.05;
    }
}
