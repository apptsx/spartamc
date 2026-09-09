package com.minecraft.core.bukkit.listener.handler;

import com.minecraft.core.Core;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.loader.ClassLoader;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ListenerHandler {

    private final BukkitCore bukkit;

    public void handle(String path) {
        Instant now = Instant.now();

        int loaded = 0;

        Core.getLogger().info("Registrando eventos...");

        for (Class<?> listenerClass : ClassLoader.getClassesForPackage(bukkit, path)) {
            if (listenerClass.isAnonymousClass() || listenerClass.getName().contains("$")) {
                continue;
            }
            if (Listener.class.isAssignableFrom(listenerClass)) {
                if (listenerClass.isAnnotationPresent(IgnoreEvent.class)) continue;

                try {
                    Listener listener = (Listener) listenerClass.newInstance();

                    bukkit.getServer().getPluginManager().registerEvents(listener, bukkit);

                    for (Method method : listenerClass.getMethods()) {
                        if (method.isAnnotationPresent(EventHandler.class))
                            loaded++;
                    }

                } catch (Exception e) {
                    Core.getLogger().log(Level.SEVERE, "Não foi possível registrar o evento " + listenerClass.getSimpleName(), e);
                }
            }
        }

        if (loaded > 0)
            Core.getLogger().info("Registro de eventos concluído. (Total de eventos registrados: " + loaded + " em " + Util.formatInstant(now) + ")");
    }
}
