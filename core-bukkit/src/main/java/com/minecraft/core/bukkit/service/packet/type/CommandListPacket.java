package com.minecraft.core.bukkit.service.packet.type;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.core.command.annotation.Command;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class CommandListPacket extends PacketAdapter {

    private List<String> defaultCommands;
    private List<String> staffCommands;

    public CommandListPacket(Plugin plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.TAB_COMPLETE);

        Core.getPlatform().runSync(() -> {
            this.defaultCommands = new ArrayList<>(getCommandList());
            this.defaultCommands.addAll(Arrays.asList(
                    "/account", "/acc", "/reply", "/r", "/responder", "/msg", "/message",
                    "/fake", "/nick", "/discord", "/report", "/clan", "/evento",
                    "/party", "/ping", "/ms"));

            this.defaultCommands.sort(Comparator.comparing(String::toString));

            this.staffCommands = new ArrayList<>(defaultCommands);
            this.staffCommands.addAll(Arrays.asList(
                    "/ban", "/mute", "/kick", "/sc", "/bc", "/playerfinder", "/unban", "/unmute", "/sclist"));

            this.staffCommands.sort(Comparator.comparing(String::toString));
        }, 20 * 3);
    }

    private List<String> getCommandList() {
        List<String> commandList = new ArrayList<>(BukkitCommandHandler.getCommandMap().keySet());

        // Adicionando "/" no começo de cada elemento da lista e removendo os que contêm um ponto
        Iterator<String> iterator = commandList.iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();

            if (name.contains(".")) {
                iterator.remove();
            } else {
                commandList.set(commandList.indexOf(name), "/" + name);
            }
        }

        return commandList;
    }

    private List<String> filterCommandsByRank(Account account, List<String> list) {
        if (list.isEmpty()) return Collections.emptyList();

        Iterator<String> iterator = list.iterator();

        while (iterator.hasNext()) {
            String name = iterator.next().substring(1);

            Map.Entry<Method, Object> entry = BukkitCommandHandler.getCommandMap().get(name);

            if (entry == null) continue;

            Command command = entry.getKey().getAnnotation(Command.class);

            if (command == null) continue;

            if (!account.hasRank(command.rank()))
                iterator.remove();
        }

        return list;
    }

    private List<String> getPlayerNames(Player player) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(target -> !player.equals(target) && player.getWorld().equals(target.getWorld()))
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();

        Account account = Core.getAccountController().of(player.getUniqueId());

        String message = event.getPacket().getStrings().read(0);

        List<String> shownCommands = filterCommandsByRank(account, account.isStaffer() ? staffCommands : defaultCommands),
                playerNames = getPlayerNames(player);

        shownCommands.sort(Comparator.comparing(String::toString));
        playerNames.sort(Comparator.comparing(String::toString));

        // Verifica se o jogador digitou apenas '/'
        if (!message.isEmpty() && !message.startsWith("/")) {
            event.setCancelled(true);

            List<String> names = new ArrayList<>();

            for (String playerName : playerNames) {
                if (playerName.toLowerCase().startsWith(message))
                    names.add(playerName);
            }

            sendTabCompletePacket(player, names);
            return;
        }

        if (message.equals("/")) {
            event.setCancelled(true);

            sendTabCompletePacket(player, shownCommands);
        } else if (!message.contains(" ")) {
            event.setCancelled(true);

            List<String> commandList = new ArrayList<>();

            for (String command : shownCommands) {
                if (command.toLowerCase().startsWith(message.toLowerCase()))
                    commandList.add(command);
            }

            sendTabCompletePacket(player, commandList);
        }
    }

    private void sendTabCompletePacket(Player player, List<String> shown) {
        try {
            PacketContainer packet = BukkitCore.getManager().getProtocol().createPacket(PacketType.Play.Server.TAB_COMPLETE);

            packet.getStringArrays().write(0, shown.toArray(new String[0]));

            BukkitCore.getManager().getProtocol().sendServerPacket(player, packet);
        } catch (Exception e) {
            player.sendMessage("§cOcorreu um erro ao tentar te enviar um packet! ;(");

            e.printStackTrace();
        }
    }
}