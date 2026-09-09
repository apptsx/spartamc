package com.minecraft.core.bungee;

import com.minecraft.core.Constant;
import com.minecraft.core.Platform;
import com.minecraft.core.util.Util;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

@RequiredArgsConstructor
public class BungeePlatform implements Platform {

    private final BungeeCore bungee;

    @Override
    public void runAsync(Runnable runnable) {
        bungee.getProxy().getScheduler().runAsync(bungee, runnable);
    }

    @Override
    public void runSync(Runnable runnable) {

    }

    @Override
    public void runSync(Runnable runnable, long delay) {

    }

    @Override
    public <T> T getPlayer(String name, Class<T> tClass) {
        ProxiedPlayer player = bungee.getProxy().getPlayer(name);
        return player != null ? tClass.cast(player) : null;
    }

    @Override
    public <T> T getPlayer(UUID id, Class<T> tClass) {
        ProxiedPlayer player = bungee.getProxy().getPlayer(id);
        return player != null ? tClass.cast(player) : null;
    }

    @Override
    public boolean isOnlinePlayer(UUID id) {
        return bungee.getProxy().getPlayer(id) != null;
    }

    @Override
    public void sendBroadcast(boolean withPrefix, String... message) {
        for (String text : message) {
            bungee.getProxy().getPlayers().forEach(player -> player.sendMessage(TextComponent.fromLegacyText(
                    (withPrefix ? Constant.SERVER_PREFIX : "") + Util.color(text))));
        }
    }

    @Override
    public void sendMessage(UUID playerId, String... message) {
        ProxiedPlayer player = getPlayer(playerId, ProxiedPlayer.class);

        if (player != null) {
            for (String text : message)
                player.sendMessage(TextComponent.fromLegacyText(text));
        }
    }

    @Override
    public void sendMessage(UUID playerId, BaseComponent... message) {
        ProxiedPlayer player = getPlayer(playerId, ProxiedPlayer.class);

        if (player != null)
            player.sendMessage(message);
    }
}
