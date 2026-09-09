package com.minecraft.core.api.item.interact;

import org.bukkit.event.player.PlayerInteractEvent;

public interface ItemInteract {
    void runInteract(PlayerInteractEvent event);
}
