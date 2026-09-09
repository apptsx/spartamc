package com.minecraft.core.bukkit.event.type.player;

import com.minecraft.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.core.bukkit.event.EventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
public class PlayerCooldownEndEvent extends EventHandler {
    
    private final Player player;
    private final Cooldown cooldown;
}
