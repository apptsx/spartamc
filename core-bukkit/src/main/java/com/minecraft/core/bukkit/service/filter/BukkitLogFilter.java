package com.minecraft.core.bukkit.service.filter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public class BukkitLogFilter extends AbstractFilter {

    public void registerFilter() {
        Logger logger = (Logger) LogManager.getRootLogger();
        logger.addFilter(this);
    }

    @Override
    public Result filter(LogEvent event) {
        return event == null ? Result.NEUTRAL : isLoggable(event.getMessage().getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return isLoggable(msg.getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return isLoggable(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return msg == null ? Result.NEUTRAL : isLoggable(msg.toString());
    }

    private Result isLoggable(String msg) {
        if (msg != null) {
            if (msg.contains("PACKET")) {
                return Result.NEUTRAL;
            }

            if (msg.contains("issued server command")) {
                String[] split = msg.split("issued server command: /");

                if (split.length == 1) return Result.DENY;

                String name = split[0], command = split[1];

                System.out.println(name + "executou: /" + command);
                return Result.DENY;
            }

            if (msg.contains("twice")) {
                return Result.DENY;
            } else if (msg.contains("handleDisconnection")) {
                return Result.DENY;
            } else if (msg.contains("com.mojang.authlib.GameProfile@")) {
                return Result.DENY;
            } else if (msg.contains("lost connection: Disconnected")) {
                return Result.DENY;
            } else if (msg.contains("left the game.")) {
                return Result.DENY;
            } else if (msg.contains("logged in with entity id")) {
                return Result.DENY;
            } else if (msg.contains("lost connection: Timed out")) {
                return Result.DENY;
            } else if (msg.contains("UUID of player")) {
                return Result.DENY;
            } else if (msg.contains("Internal Exception")) {
                return Result.DENY;
            } else if (msg.contains("lost connection")) {
                return Result.DENY;
            } else if (msg.contains("has disconnected")) {
                return Result.DENY;
            } else if (msg.contains("reason")) {
                return Result.DENY;
            } else if (msg.contains("disconnected with")) {
                return Result.DENY;
            }
        }

        return Result.NEUTRAL;
    }
}