package com.minecraft.core.bukkit.manager.list;

import com.minecraft.core.Core;
import com.minecraft.core.bukkit.service.task.TaskBase;
import com.minecraft.core.util.list.loader.ClassLoader;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TaskManager {

    @Getter
    private static final List<TaskBase> tasks = new ArrayList<>();

    public static void handle(Plugin plugin, String directory) {
        Instant startedAt = Instant.now();

        int loaded = 0;

        Core.getLogger().info("Registrando tasks...");

        for (Class<?> taskClass : ClassLoader.getClassesForPackage(plugin, directory)) {
            if (TaskBase.class.isAssignableFrom(taskClass)) {
                try {
                    TaskBase task = (TaskBase) taskClass
                            .getConstructor(Plugin.class)
                            .newInstance(plugin);

                    task.init();
                    tasks.add(task);

                    loaded++;
                } catch (Exception e) {
                    Core.getLogger().log(Level.WARNING, "Não foi possível registrar a task " + taskClass.getSimpleName(), e);
                }
            }
        }

        if (loaded > 0)
            Core.getLogger().info("Registro de tasks concluído com sucesso. (Total de tasks registradas: " + tasks.size()
                    + " em " + Duration.between(startedAt, Instant.now()).toMillis() + "ms)");
    }

    public static void unloadTasks() {
        tasks.clear();
        Bukkit.getScheduler().cancelAllTasks();
    }
}
