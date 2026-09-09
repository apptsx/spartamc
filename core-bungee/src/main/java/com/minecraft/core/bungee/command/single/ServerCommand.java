package com.minecraft.core.bungee.command.single;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.route.RouteContext;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.type.ServerType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerCommand implements CommandInheritor {

    @Completer(name = "server")
    public List<String> serverCompleter(BungeeCommandContext context) {
        List<String> completions = new ArrayList<>();
        
        String[] args = context.getArgs();
        String input = args.length > 0 ? args[0].toLowerCase() : "";
        
        // Listar todos os servidores registrados no BungeeCord
        for (String serverName : ProxyServer.getInstance().getServers().keySet()) {
            if (input.isEmpty() || serverName.toLowerCase().startsWith(input)) {
                completions.add(serverName);
            }
        }
        
        return completions.stream().sorted().collect(Collectors.toList());
    }

    @Command(name = "server", aliases = {"servidor", "connect", "conectar"}, rank = RankType.MODPLUS)
    public void server(BungeeCommandContext context) {
        Account account = context.getAccount();
        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getLabel() + " <servidor>");
            account.send("§7Servidores disponíveis:");
            
            List<String> servers = new ArrayList<>(ProxyServer.getInstance().getServers().keySet());
            servers.sort(String::compareToIgnoreCase);
            
            if (servers.isEmpty()) {
                account.send("§7Nenhum servidor disponível no momento.");
                return;
            }
            
            for (String serverName : servers) {
                ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(serverName);
                if (serverInfo != null) {
                    int players = serverInfo.getPlayers().size();
                    account.send("§7- §e" + serverName + " §7(" + players + " jogador" + (players != 1 ? "es" : "") + ")");
                }
            }
            return;
        }

        String serverName = String.join(" ", args);
        
        // Buscar servidor no BungeeCord
        ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(serverName);
        
        if (serverInfo == null) {
            account.send("§cServidor não encontrado: §e" + serverName);
            account.send("§7Use /" + context.getLabel() + " para ver os servidores disponíveis.");
            return;
        }
        
        // Verificar se já está no servidor
        if (account.proxiedPlayer().getServer() != null && 
            account.proxiedPlayer().getServer().getInfo().getName().equalsIgnoreCase(serverName)) {
            account.send("§cVocê já está conectado no servidor §e" + serverName + "§c.");
            return;
        }
        
        // Tentar identificar o ServerType pelo nome do servidor
        ServerType serverType = null;
        
        // Tentar mapear o nome do servidor para ServerType
        // Ex: "bedwars-1" -> BEDWARS, "hub-bedwars" -> HUB_BEDWARS
        String serverNameLower = serverName.toLowerCase();
        
        if (serverNameLower.startsWith("hub-")) {
            String hubType = serverNameLower.substring(4); // Remove "hub-"
            serverType = ServerType.of("hub-" + hubType);
        } else if (serverNameLower.startsWith("bedwars") || serverNameLower.contains("bedwars")) {
            serverType = ServerType.BEDWARS;
        } else if (serverNameLower.startsWith("duels") || serverNameLower.contains("duels")) {
            if (serverNameLower.contains("hub")) {
                serverType = ServerType.HUB_DUELS;
            } else {
                serverType = ServerType.DUELS;
            }
        } else if (serverNameLower.startsWith("pvp") || serverNameLower.contains("pvp")) {
            if (serverNameLower.contains("hub")) {
                serverType = ServerType.HUB_PVP;
            } else {
                serverType = ServerType.PVP;
            }
        } else if (serverNameLower.equals("hub") || serverNameLower.equals("lobby")) {
            serverType = ServerType.HUB;
        } else if (serverNameLower.equals("auth")) {
            serverType = ServerType.AUTH;
        }
        
        // Se identificou um ServerType, criar rota e conectar diretamente ao servidor específico
        if (serverType != null) {
            Server server = Core.getServerData().of(serverType);
            
            if (server == null || server.isDead()) {
                account.send("§cO servidor §e" + serverType.getName() + " §cestá offline ou não está disponível no momento.");
                return;
            }
            
            // Criar rota antes de conectar
            ArcadeRouteContext arcadeRoute = null;
            
            if (serverType.isArcade()) {
                // Pegar a primeira categoria arcade disponível do servidor
                List<ArcadeCategory> categories = ArcadeCategory.of(serverType);
                ArcadeCategory category = categories.isEmpty() ? null : categories.get(0);
                
                if (category != null) {
                    arcadeRoute = ArcadeRouteContext.builder()
                            .arcade(category)
                            .build();
                }
            }
            
            RouteContext route = RouteContext.builder()
                    .senderId(account.getId())
                    .serverType(serverType)
                    .serverId(server.getId())
                    .serverPort(server.getPort())
                    .arcade(arcadeRoute != null ? arcadeRoute : ArcadeRouteContext.builder().build())
                    .updatedAt(System.currentTimeMillis())
                    .build();
            
            Core.getRouteData().save(route);
            
            account.send("§aConectando ao servidor §e" + serverName + "§a...");
            // Conectar diretamente ao servidor específico do BungeeCord
            account.proxiedPlayer().connect(serverInfo);
        } else {
            // Se não conseguiu identificar o tipo, tentar conectar diretamente
            // Mas criar uma rota básica para evitar erros
            RouteContext route = RouteContext.builder()
                    .senderId(account.getId())
                    .serverType(ServerType.HUB) // Fallback para HUB
                    .serverId(0)
                    .serverPort(serverInfo.getAddress().getPort())
                    .arcade(ArcadeRouteContext.builder().build())
                    .updatedAt(System.currentTimeMillis())
                    .build();
            
            Core.getRouteData().save(route);
            
            account.send("§aConectando ao servidor §e" + serverName + "§a...");
            account.send("§eAviso: Tipo de servidor não identificado, conectando diretamente.");
            account.proxiedPlayer().connect(serverInfo);
        }
    }
}

