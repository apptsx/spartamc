package com.minecraft.core.bukkit.service.task.list;

import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import com.minecraft.core.bukkit.service.task.TaskBase;
import org.bukkit.plugin.Plugin;

public class SyncUpdateTask extends TaskBase {

    private long ticks;

    public SyncUpdateTask(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void init() {
        getPlugin().getServer().getScheduler().runTaskTimer(getPlugin(), this, 0L, 1L);
    }

    @Override
    public void run() {
        ticks++;

        UpdateType type = ticks % 20 == 0 ? UpdateType.SECOND
                : ticks % 1200 == 0 ? UpdateType.MINUTE
                : ticks % 72000 == 0 ? UpdateType.HOUR : UpdateType.TICK;

        new SyncUpdateEvent(type, ticks).call();
    }
}