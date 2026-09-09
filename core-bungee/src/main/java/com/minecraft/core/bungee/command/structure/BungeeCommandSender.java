package com.minecraft.core.bungee.command.structure;

import com.minecraft.core.Constant;
import com.minecraft.core.command.CommandSender;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

@RequiredArgsConstructor
public class BungeeCommandSender implements CommandSender {

    private final net.md_5.bungee.api.CommandSender sender;

    @Override
    public UUID getId() {
        return isPlayer() ? ((ProxiedPlayer) sender).getUniqueId() : Constant.DEFAULT_ID;
    }

    @Override
    public String getName() {
        return isPlayer() ? sender.getName() : "CONSOLE";
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof ProxiedPlayer;
    }

    @Override
    public void send(String... message) {
        for (String text : message)
            sender.sendMessage(new TextComponent(text));
    }

    @Override
    public void send(BaseComponent... message) {
        sender.sendMessage(message);
    }
}
