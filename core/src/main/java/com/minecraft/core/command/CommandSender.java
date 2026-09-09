package com.minecraft.core.command;

import net.md_5.bungee.api.chat.BaseComponent;

import java.util.List;
import java.util.UUID;

public interface CommandSender {

    UUID getId();

    String getName();

    boolean isPlayer();

    void send(String... message);

    void send(BaseComponent... message);

    default void send(List<String> message) {
        message.forEach(this::send);
    }
}
