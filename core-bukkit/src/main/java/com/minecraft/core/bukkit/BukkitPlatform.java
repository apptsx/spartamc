package com.minecraft.core.bukkit;

import com.minecraft.core.Constant;
import com.minecraft.core.Platform;
import com.minecraft.core.util.Util;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public class BukkitPlatform implements Platform {

    private final BukkitCore bukkit;

    @Override
    public void runAsync(Runnable runnable) {
        bukkit.getServer().getScheduler().runTaskAsynchronously(bukkit, runnable);
    }

    @Override
    public void runSync(Runnable runnable) {
        bukkit.getServer().getScheduler().runTask(bukkit, runnable);
    }

    @Override
    public void runSync(Runnable runnable, long delay) {
        bukkit.getServer().getScheduler().runTaskLater(bukkit, runnable, delay);
    }

    @Override
    public <T> T getPlayer(String name, Class<T> tClass) {
        Player player = Bukkit.getPlayer(name);
        return player != null ? tClass.cast(player) : null;
    }

    @Override
    public <T> T getPlayer(UUID id, Class<T> tClass) {
        Player player = Bukkit.getPlayer(id);
        return player != null ? tClass.cast(player) : null;
    }

    @Override
    public boolean isOnlinePlayer(UUID id) {
        return Bukkit.getPlayer(id) != null;
    }

    @Override
    public void sendBroadcast(boolean withPrefix, String... message) {
        for (String text : message)
            bukkit.getServer().getOnlinePlayers().forEach(player -> player.sendMessage((withPrefix ? Constant.SERVER_PREFIX : "") + Util.color(text)));
    }

    @Override
    public void sendMessage(UUID playerId, String... message) {
        Player player = getPlayer(playerId, Player.class);

        if (player != null)
            player.sendMessage(message);
    }

    @Override
    public void sendMessage(UUID playerId, BaseComponent... message) {
        Player player = getPlayer(playerId, Player.class);

        if (player != null)
            player.sendMessage(message);
    }
}
