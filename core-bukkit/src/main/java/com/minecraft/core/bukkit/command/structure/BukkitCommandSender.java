package com.minecraft.core.bukkit.command.structure;

import com.minecraft.core.Constant;
import com.minecraft.core.command.CommandSender;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public class BukkitCommandSender implements CommandSender {

    private final org.bukkit.command.CommandSender sender;

    @Override
    public UUID getId() {
        return isPlayer() ? ((Player) sender).getUniqueId() : Constant.DEFAULT_ID;
    }

    @Override
    public String getName() {
        return isPlayer() ? sender.getName() : "CONSOLE";
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override
    public void send(String... message) {
        sender.sendMessage(message);
    }

    @Override
    public void send(BaseComponent... message) {
        if (!isPlayer()) {
            sender.sendMessage("§cSomente jogadores podem receber mensagens de componentes.");
            return;
        }

        Player player = (Player) sender;

        player.sendMessage(message);
    }
}
