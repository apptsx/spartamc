package com.minecraft.core.bukkit.event.type.player;

import com.minecraft.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.core.bukkit.event.EventHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@Getter
@Setter
@RequiredArgsConstructor
public class PlayerCooldownStartEvent extends EventHandler implements Cancellable {

    private final Player player;
    private final Cooldown cooldown;

    @Setter
    private boolean cancelled;
}
