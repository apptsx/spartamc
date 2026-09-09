package com.minecraft.core.bungee.listener;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bungee.event.list.update.UpdateEvent;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.list.StringUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class BungeeListener implements Listener {

    private static final Set<String> BLOCKED_COMMANDS = new HashSet<>(Arrays.asList(
            "about", "plugins", "pl", "server", "help", "restart", "start", "stop"
    ));

    @EventHandler
    public void onTabComplete(net.md_5.bungee.api.event.TabCompleteEvent event) {
        String label = event.getCursor();
        if (label == null || label.isEmpty()) return;
        
        if (label.startsWith("/")) {
            label = label.substring(1).toLowerCase();
            
            for (String blocked : BLOCKED_COMMANDS) {
                if (label.equals(blocked) || label.startsWith(blocked)) {
                    event.setCancelled(true);
                    event.getSuggestions().clear();
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        CompletableFuture.runAsync(() -> {
            Core.getServerData().updatePlayerCounter(ProxyServer.getInstance().getOnlineCount());
            Core.getAccountController().list().forEach(Account::checkInfos);
        });
    }

    @EventHandler
    public void motd(ProxyPingEvent event) {
        ServerPing ping = event.getResponse();

        String motdNormal = "§l §r                    " + com.minecraft.core.Constant.SERVER_TITLE + " §8[1.8/1.21]\n§l §r             §e§lVENHA JOGAR NA §1§lBETA§e§l!";
        String motdManutencao = "§l §r                    " + com.minecraft.core.Constant.SERVER_TITLE + " §8[1.8/1.21]\n§l §r             §c§lSERVIDOR EM MANUTENÇÃO!";

        Server server = Core.getServerData().local();

        if (server != null && server.getWhitelist().isEnabled())
            motdNormal = motdManutencao;

        ping.setDescriptionComponent(new TextComponent(motdNormal));

        event.setResponse(ping);
    }

    @EventHandler
    public void connect(ServerConnectEvent event) {
        ProxiedPlayer proxied = event.getPlayer();
        
        // Verificar whitelist do proxy ao entrar pela primeira vez
        if (event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            Server localServer = Core.getServerData().local();
            
            if (localServer != null && localServer.getWhitelist().isEnabled()) {
                Account account = Core.getAccountController().of(proxied.getUniqueId());
                
                if (account != null) {
                    String playerName = account.getNickname().toLowerCase();
                    
                    // Verificar se o jogador está na whitelist ou tem permissão de staff
                    if (!localServer.getWhitelist().hasPlayer(playerName) && 
                        !localServer.getWhitelist().hasRank(account.getRank().getType())) {
                        
                        event.setCancelled(true);
                        proxied.disconnect(TextComponent.fromLegacyText(
                            com.minecraft.core.Constant.SERVER_TITLE + "\n\n" +
                            "§eNo momento, estamos em manutenção.\n" +
                            "§eO servidor está disponível somente para jogadores autorizados.\n" +
                            "§7Saiba mais em: §9" + com.minecraft.core.Constant.SERVER_DISCORD + "\n\n"
                        ));
                        return;
                    }
                }
            }
        }
        
        // Verificar whitelist do servidor de destino
        if (event.getTarget() != null) {
            Server targetServer = Core.getServerData().findByProxyServer(event.getTarget());
            
            if (targetServer != null && targetServer.getWhitelist().isEnabled()) {
                Account account = Core.getAccountController().of(proxied.getUniqueId());
                
                if (account != null) {
                    String playerName = account.getNickname().toLowerCase();
                    
                    // Verificar se o jogador está na whitelist ou tem permissão de staff
                    if (!targetServer.getWhitelist().hasPlayer(playerName) && 
                        !targetServer.getWhitelist().hasRank(account.getRank().getType())) {
                        
                        event.setCancelled(true);
                        
                        if (event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
                            proxied.disconnect(TextComponent.fromLegacyText(
                                com.minecraft.core.Constant.SERVER_TITLE + "\n\n" +
                                "§eNo momento, estamos em manutenção. \n" +
                                "§eO servidor está disponível somente para jogadores autorizados.\n" +
                                "§7Saiba mais em: §9" + com.minecraft.core.Constant.SERVER_DISCORD
                            ));
                        } else {
                            proxied.sendMessage(TextComponent.fromLegacyText(
                                "§cO servidor §e" + targetServer.getName() + "§c está em whitelist!"
                            ));
                        }
                        return;
                    }
                }
            }
        }
        
        if (event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            Account account = Core.getAccountController().of(proxied.getUniqueId());

            if (account == null) {
                event.setCancelled(true);

                proxied.disconnect(TextComponent.fromLegacyText(Constant.ACCOUNT_LOAD_FAILED_MESSAGE));
                return;
            }

            ServerType category = proxied.getPendingConnection().isOnlineMode() ? ServerType.HUB : ServerType.AUTH;

            Server server = Core.getServerData().of(category);

            if (server == null || server.getProxyServer() == null) {
                event.setCancelled(true);

                proxied.disconnect(TextComponent.fromLegacyText(String.format(Constant.SERVER_NOT_FOUND_MESSAGE, category.getName())));
                return;
            }

            event.setTarget(server.getProxyServer());
        }
    }
}