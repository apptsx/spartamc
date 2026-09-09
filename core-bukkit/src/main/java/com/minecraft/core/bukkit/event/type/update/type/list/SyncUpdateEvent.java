package com.minecraft.core.bukkit.event.type.update.type.list;

import com.minecraft.core.bukkit.event.type.update.UpdateEvent;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;

public class SyncUpdateEvent extends UpdateEvent {
    public SyncUpdateEvent(UpdateType type, long ticks) {
        super(type, ticks);
    }
}
