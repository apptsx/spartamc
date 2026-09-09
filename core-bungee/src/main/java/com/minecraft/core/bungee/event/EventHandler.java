package com.minecraft.core.bungee.event;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Event;

public class EventHandler extends Event {

    public void call() {
        ProxyServer.getInstance().getPluginManager().callEvent(this);
    }
}
