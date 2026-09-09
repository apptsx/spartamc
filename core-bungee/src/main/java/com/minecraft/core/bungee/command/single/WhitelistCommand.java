package com.minecraft.core.bungee.command.single;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.backend.data.list.server.ServerData;
import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.payload.whitelist.ServerWhitelist;

import java.util.List;

public class WhitelistCommand implements CommandInheritor {

    private final ServerData serverData = Core.getServerData();

    @Command(name = "whitelist", rank = RankType.MODPLUS, onlyPlayer = false)
    public void whitelist(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        List<String> subCommands = context.getSubCommands();

        String[] args = context.getArgs();

        if (args.length == 0 || !subCommands.contains(args[0].toLowerCase())) {
            String label = "/" + context.getLabel();

            sender.send("§cUso do " + label + ":",
                    "§c* " + label + " list §7- Lista todos os servidores",
                    "§c* " + label + " on <servidor> §7- Ativa a whitelist",
                    "§c* " + label + " off <servidor> §7- Desativa a whitelist",
                    "§c* " + label + " add <servidor> <jogador> §7- Adiciona jogador",
                    "§c* " + label + " remove <servidor> <jogador> §7- Remove jogador",
                    "§c* " + label + " info <servidor> §7- Mostra informações",
                    "§c* " + label + " status <servidor> §7- Alterna status");
        }
    }

    @Command(name = "whitelist.list", rank = RankType.MODPLUS, onlyPlayer = false)
    public void whitelistList(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        List<Server> servers = serverData.getServerList();

        if (servers.isEmpty()) {
            sender.send("§cNenhum servidor encontrado.");
            return;
        }

        sender.send("§eServidores disponíveis §7(" + servers.size() + ")§e:");
        
        for (Server server : servers) {
            ServerWhitelist whitelist = server.getWhitelist();
            String status = whitelist.isEnabled() ? "§a✓ Ativo" : "§c✗ Inativo";
            
            sender.send("§7- §f" + server.getNameAndId() + " §8| " + status + 
                       " §8| §7Jogadores: §f" + whitelist.getPlayers().size());
        }
    }

    @Command(name = "whitelist.info", rank = RankType.MODPLUS, onlyPlayer = false)
    public void whitelistInfo(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.send("§cUso: /" + context.getCommandLabel() + " <servidor>.");
            return;
        }

        Server server = serverData.findByNameAndId(args[0]);

        if (server == null) {
            sender.send("§cO servidor solicitado não foi encontrado.");
            return;
        }

        ServerWhitelist whitelist = server.getWhitelist();

        sender.send("§eInformações sobre a whitelist:",
                "§7Servidor: §f" + server.getName(),
                "§7Estado: §f" + (whitelist.isEnabled() ? "§aAtivo" : "§cInativo"),
                "§7Jogadores na whitelist: §f" + whitelist.getPlayers().size());
        
        if (!whitelist.getPlayers().isEmpty()) {
            sender.send("§7Jogadores: §f" + String.join("§7, §f", whitelist.getPlayers()));
        }
    }

    @Command(name = "whitelist.status", rank = RankType.MODPLUS, onlyPlayer = false)
    public void whitelistStatus(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.send("§cUso: /" + context.getCommandLabel() + " <servidor>.");
            return;
        }

        Server server = serverData.findByNameAndId(args[0]);

        if (server == null) {
            sender.send("§cO servidor solicitado não foi encontrado.");
            return;
        }

        ServerWhitelist whitelist = server.getWhitelist();

        whitelist.setEnabled(!whitelist.isEnabled());

        sender.send(whitelist.isEnabled()
                ? "§aA whitelist do servidor §e\"" + server.getNameAndId() + "\"§a foi ativada."
                : "§cA whitelist do servidor §e\"" + server.getNameAndId() + "\"§c foi desativada.");

        server.refresh();
        serverData.saveWhitelist(server);
    }
    
    @Command(name = "whitelist.on", rank = RankType.MODPLUS, onlyPlayer = false)
    public void whitelistOn(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.send("§cUso: /" + context.getCommandLabel() + " <servidor>.");
            return;
        }

        Server server = serverData.findByNameAndId(args[0]);

        if (server == null) {
            sender.send("§cO servidor solicitado não foi encontrado.");
            return;
        }

        ServerWhitelist whitelist = server.getWhitelist();

        if (whitelist.isEnabled()) {
            sender.send("§cA whitelist do servidor §e\"" + server.getNameAndId() + "\"§c já está ativada.");
            return;
        }

        whitelist.setEnabled(true);
        sender.send("§aA whitelist do servidor §e\"" + server.getNameAndId() + "\"§a foi ativada.");
        server.refresh();
        serverData.saveWhitelist(server);
    }
    
    @Command(name = "whitelist.off", rank = RankType.MODPLUS, onlyPlayer = false)
    public void whitelistOff(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.send("§cUso: /" + context.getCommandLabel() + " <servidor>.");
            return;
        }

        Server server = serverData.findByNameAndId(args[0]);

        if (server == null) {
            sender.send("§cO servidor solicitado não foi encontrado.");
            return;
        }

        ServerWhitelist whitelist = server.getWhitelist();

        if (!whitelist.isEnabled()) {
            sender.send("§cA whitelist do servidor §e\"" + server.getNameAndId() + "\"§c já está desativada.");
            return;
        }

        whitelist.setEnabled(false);
        sender.send("§cA whitelist do servidor §e\"" + server.getNameAndId() + "\"§c foi desativada.");
        server.refresh();
        serverData.saveWhitelist(server);
    }

    @Command(name = "whitelist.add", rank = RankType.MODPLUS, onlyPlayer = false)
    public void whitelistAdd(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length <= 1) {
            sender.send("§cUso: /" + context.getCommandLabel() + " <servidor> <jogador>.");
            return;
        }

        Server server = serverData.findByNameAndId(args[0]);

        if (server == null) {
            sender.send("§cO servidor solicitado não foi encontrado.");
            return;
        }

        ServerWhitelist whitelist = server.getWhitelist();

        String playerName = args[1].toLowerCase();

        if (whitelist.hasPlayer(playerName)) {
            sender.send("§cO jogador §e" + playerName + "§c já está na whitelist do servidor §e\"" + server.getNameAndId() + "\"§c.");
            return;
        }

        whitelist.getPlayers().add(playerName);
        server.refresh();
        serverData.saveWhitelist(server);

        sender.send("§aO jogador §e" + playerName + "§a foi adicionado na whitelist do servidor §e\"" + server.getNameAndId() + "\"§a.");
    }

    @Command(name = "whitelist.remove", rank = RankType.MODPLUS, onlyPlayer = false)
    public void whitelistRemove(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length <= 1) {
            sender.send("§cUso: /" + context.getCommandLabel() + " <servidor> <jogador>.");
            return;
        }

        Server server = serverData.findByNameAndId(args[0]);

        if (server == null) {
            sender.send("§cO servidor solicitado não foi encontrado.");
            return;
        }

        ServerWhitelist whitelist = server.getWhitelist();

        String playerName = args[1].toLowerCase();

        if (!whitelist.hasPlayer(playerName)) {
            sender.send("§cO jogador §e" + playerName + "§c não está na whitelist do servidor §e\"" + server.getNameAndId() + "\"§c.");
            return;
        }

        whitelist.getPlayers().remove(playerName);
        server.refresh();
        serverData.saveWhitelist(server);

        sender.send("§eO jogador §e" + playerName + "§e foi removido da whitelist do servidor §e\"" + server.getNameAndId() + "\"§e.");
    }
}
