package com.minecraft.core;

import net.md_5.bungee.api.chat.BaseComponent;

import java.util.UUID;

public interface Platform {

    /* Schedule */
    void runAsync(Runnable runnable);

    void runSync(Runnable runnable);

    void runSync(Runnable runnable, long delay);

    /* Player Finder */
    <T> T getPlayer(String name, Class<T> tClass);

    <T> T getPlayer(UUID id, Class<T> tClass);

    boolean isOnlinePlayer(UUID id);

    /* Message Sender */
    void sendBroadcast(boolean withPrefix, String... message);

    default void sendBroadcast(String message) {
        sendBroadcast(true, message);
    }

    void sendMessage(UUID playerId, String... message);

    void sendMessage(UUID playerId, BaseComponent... message);
}
