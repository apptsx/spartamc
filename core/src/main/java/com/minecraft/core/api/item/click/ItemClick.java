package com.minecraft.core.api.item.click;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface ItemClick {
    void runClick(InventoryClickEvent event);
}
