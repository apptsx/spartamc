package com.minecraft.core.bungee.listener;

import com.minecraft.core.api.punishment.Punishment;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.api.reconnect.Reconnect;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.bungee.BungeeCore;
import com.minecraft.core.bungee.event.list.account.AccountRefreshEvent;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.Rank;
import com.minecraft.core.account.context.objects.route.RouteContext;
import com.minecraft.core.api.mojang.MojangApi;
import com.minecraft.core.backend.data.list.user.AccountData;
import com.minecraft.core.backend.database.redis.message.types.account.AccountSkinResetMessage;
import com.minecraft.core.controller.list.AccountController;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.flag.ServerFlag;
import com.minecraft.core.server.payload.whitelist.ServerWhitelist;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DataListener implements Listener {

    private final BungeeCore bungee = BungeeCore.getInstance();

    private final AccountData data = Core.getAccountData();
    private final AccountController controller = Core.getAccountController();

    @EventHandler
    public void onAccountRefresh(AccountRefreshEvent event) {
        Account account = event.getAccount();

        if (Core.getPlatform().isOnlinePlayer(account.getId()))
            controller.save(account);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPreLogin(PreLoginEvent event) {
        PendingConnection connection = event.getConnection();
        String userName = connection.getName();
        
        event.registerIntent(bungee);
        
        CompletableFuture.runAsync(() -> {
            Core.getLogger().info("[PreLogin] Verificando premium para: " + userName);
            MojangApi.ResponseCode responseCode = Core.MOJANG_API.isPremium(userName);
            Core.getLogger().info("[PreLogin] Resultado para " + userName + ": " + responseCode);
            
            if (responseCode.equals(MojangApi.ResponseCode.DONE)) {
                Core.getLogger().info("[PreLogin] " + userName + " é premium, usando online mode");
                connection.setOnlineMode(true);
            } else {
                Core.getLogger().info("[PreLogin] " + userName + " não é premium, usando offline mode");
                connection.setOnlineMode(false);
                connection.setUniqueId(UUID.nameUUIDFromBytes(("OfflinePlayer:" + userName.toUpperCase()).getBytes(StandardCharsets.UTF_8)));
            }
        }).thenRun(() -> event.completeIntent(bungee));
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        PendingConnection connection = event.getConnection();

        UUID id = connection.getUniqueId();

        String userName = connection.getName();
        String ip = connection.getAddress().getAddress().getHostAddress();

        event.registerIntent(bungee);

        CompletableFuture.runAsync(() -> loadAccountData(event, id, userName, ip)).thenRun(() -> event.completeIntent(bungee));
    }

    private void loadAccountData(LoginEvent event, UUID id, String userName, String ip) {
        try {
            Core.getLogger().info("[Login] Carregando conta para " + userName + " (UUID: " + id + ", OnlineMode: " + event.getConnection().isOnlineMode() + ")");
            
            Account account = data.of(id, true);
            if (account == null) {
                Core.getLogger().info("[Login] Conta não existe, criando nova para " + userName + " (UUID: " + id + ")");
                account = createNewAccount(event, id, userName);
            } else {
                Core.getLogger().info("[Login] Conta encontrada para " + userName + " (UUID: " + id + ")");
            }

            if (account == null || !isAccountAllowedToJoin(account, event)) return;

            updateAccountDetails(account, userName, ip, event.getConnection().isOnlineMode());
            controller.save(account);

        } catch (Exception e) {
            controller.remove(id);
            Core.getLogger().log(Level.WARNING, "Erro ao carregar a conta de " + userName, e);
        }
    }

    private Account createNewAccount(LoginEvent event, UUID id, String userName) {
        Core.getLogger().info("[Login] Criando conta no banco para " + userName + " (UUID: " + id + ")");
        Account account = data.save(new Account(id, userName));
        if (account == null) {
            Core.getLogger().warning("Conta não encontrada para " + userName);
            event.setCancelReason(TextComponent.fromLegacyText(Constant.ACCOUNT_LOAD_FAILED_MESSAGE));
            event.setCancelled(true);
        }

        return account;
    }

    private boolean isAccountAllowedToJoin(Account account, LoginEvent event) {
        // Verificar punição apenas por UUID do jogador, não por IP
        // Isso evita que o autor de uma punição seja bloqueado por punições que ele mesmo aplicou
        Punishment punishment = Core.getPunishmentData().of(account.getId(), PunishmentCategory.BAN);

        if (punishment != null) {
            data.removeCache(account);

            event.setCancelReason(TextComponent.fromLegacyText(String.format(Constant.BAN_TEMPLATE_MESSAGE,
                    punishment.isTemporary() ? "temporariamente" : "permanentemente",
                    punishment.getReason().getName(),
                    punishment.getId())));

            event.setCancelled(true);
            return false;
        }

        Server local = Core.getLocalServer();

        if (local != null) {
            ServerWhitelist whitelist = local.getWhitelist();

            if (whitelist.isEnabled() && !whitelist.hasPlayer(account.getName()) && !account.isStaffer()) {
                data.removeCache(account);

                event.setCancelReason(TextComponent.fromLegacyText(com.minecraft.core.Constant.SERVER_TITLE + "\n\n" +
                        "§eNo momento, estamos em manutenção, o servidor está disponível somente para jogadores autorizados.\n" +
                        "§7Saiba mais em: §9" + com.minecraft.core.Constant.SERVER_DISCORD));

                event.setCancelled(true);
                return false;
            }
        }

        return true;
    }

    private void updateAccountDetails(Account account, String userName, String ip, boolean isOnlineMode) {
        if (!account.isPremium() && isOnlineMode)
            account.setPremium(true);

        if (account.getRank().getType() == null)
            account.setRank(Rank.builder().build());

        if (account.getIpAddress() == null || !account.getIpAddress().equalsIgnoreCase(ip))
            account.setIpAddress(ip);

        if (!account.getName().equalsIgnoreCase(userName))
            account.setName(userName);

        data.cancelExpiration(account);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer proxied = event.getPlayer();

        CompletableFuture.runAsync(() -> postLoginAccountSetup(proxied));
    }

    private void postLoginAccountSetup(ProxiedPlayer proxied) {
        Account account = controller.of(proxied.getUniqueId());

        if (account == null) {
            proxied.disconnect(TextComponent.fromLegacyText(Constant.ACCOUNT_LOAD_FAILED_MESSAGE));
            return;
        }

        account.setOnline(true);
        
        // Auto Vanish - verificar se deve ativar
        if (account.getToggle().isAutoVanish() && account.getRankType().ordinal() >= com.minecraft.core.account.context.objects.rank.type.RankType.HELPER.ordinal()) {
            // Enviar comando de vanish via Redis para o servidor do jogador
            new com.minecraft.core.backend.database.redis.message.types.account.AccountExecuteCommandMessage(
                    account.getId(), "v").send();
            
            Core.getLogger().info(account.getName() + " entrou em AUTO-VANISH.");
        } else {
            account.notifyFriends();
        }

        Core.getLogger().info(account.getName() + " entrou no servidor.");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer proxied = event.getPlayer();
        CompletableFuture.runAsync(() -> handlePlayerDisconnect(proxied));
    }

    private void handlePlayerDisconnect(ProxiedPlayer proxied) {
        Account account = controller.of(proxied.getUniqueId());

        if (account == null) return;

        account.setOnline(false);

        if (account.getRoute().isValidArcade()) {
            ArcadeRouteContext route = account.getArcadeRoute();

            if (route.isValid() && route.getArcade().getServer().hasFlag(ServerFlag.RECONNECT)) {
                Core.getReconnectData().save(new Reconnect(proxied.getUniqueId(), route));
            }
        }

        if (account.isUsingFake()) {
            account.setTag(account.getDefaultTag());
            account.resetFake();
            new AccountSkinResetMessage(account).send();
        }

        account.setRoute(RouteContext.bungee());

        account.updateLastLogin();
        account.notifyFriends("§b" + account.getNickname() + "§e desconectou.");

        Core.getAccountData().startExpiration(account);
    }
}
