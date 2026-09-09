package com.minecraft.core.bukkit.command.structure;

import com.minecraft.core.command.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BukkitCommandContext extends CommandContext {

    public BukkitCommandContext(CommandSender sender, String[] args, String label, int subCommands) {
        super(new BukkitCommandSender(sender), args, label, subCommands);
    }

    @Override
    public List<String> getServerPlayerList() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    public Player getPlayer() {
        return getPlayer(getSender().getId());
    }

    public Player getPlayer(UUID id) {
        return Bukkit.getPlayer(id);
    }

    public Player getPlayer(String name) {
        return Bukkit.getPlayer(name);
    }
}
