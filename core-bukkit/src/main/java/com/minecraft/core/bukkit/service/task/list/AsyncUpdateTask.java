package com.minecraft.core.bukkit.service.task.list;

import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.AsyncUpdateEvent;
import com.minecraft.core.bukkit.service.task.TaskBase;
import org.bukkit.plugin.Plugin;

public class AsyncUpdateTask extends TaskBase {

    private long ticks;

    public AsyncUpdateTask(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void init() {
        getPlugin().getServer().getScheduler().runTaskTimerAsynchronously(getPlugin(), this, 0L, 1L);
    }

    @Override
    public void run() {
        ticks++;

        UpdateType type = ticks % 20 == 0 ? UpdateType.SECOND
                : ticks % 1200 == 0 ? UpdateType.MINUTE
                : ticks % 72000 == 0 ? UpdateType.HOUR : UpdateType.TICK;

        new AsyncUpdateEvent(type, ticks).call();
    }
}