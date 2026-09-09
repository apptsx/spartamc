package com.minecraft.core.bukkit.api.hologram.touch;

import org.bukkit.entity.Player;

public interface TouchHandler {
    void handle(Player player, Touch touch);
}
