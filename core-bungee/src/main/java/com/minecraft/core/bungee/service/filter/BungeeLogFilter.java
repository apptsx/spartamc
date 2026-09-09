package com.minecraft.core.bungee.service.filter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import java.util.Arrays;
import java.util.List;

public final class BungeeLogFilter extends AbstractFilter {

    private final List<String> messages = Arrays.asList("Plugin listener", "Event", "InitialHandler", "ServerConnector", "UpstreamBridge", "DownstreamBridge");

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

    private Result isLoggable(String message) {
        if (message == null || messages.contains(message)) return Result.DENY;

        if (message.contains("executed command")) {
            String[] split = message.split("executed command: /");

            String name = split[0], command = split[1];

            System.out.println(name + "executou: /" + command);
            return Result.DENY;
        }

        return Result.NEUTRAL;
    }
}