package com.minecraft.core.bungee.command.structure;

import com.minecraft.core.command.CommandContext;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;
import java.util.stream.Collectors;

public class BungeeCommandContext extends CommandContext {

    public BungeeCommandContext(CommandSender sender, String[] args, String label, int subCommands) {
        super(new BungeeCommandSender(sender), args, label, subCommands);
    }

    @Override
    public List<String> getServerPlayerList() {
        return ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList());
    }

    public List<String> getSubCommands() {
        return new ArrayList<>(BungeeCommandHandler.getCommands().keySet()).stream()
                .filter(command -> command.startsWith(getLabel()) && command.split("\\.").length >= 2)
                .map(command -> command.split("\\.")[1])
                .sorted(Comparator.comparing(String::toLowerCase))
                .collect(Collectors.toList());
    }

    public ProxiedPlayer getPlayer() {
        return getPlayer(getSender().getId());
    }

    public ProxiedPlayer getPlayer(UUID id) {
        return ProxyServer.getInstance().getPlayer(id);
    }

    public ProxiedPlayer getPlayer(String name) {
        return ProxyServer.getInstance().getPlayer(name);
    }
}
