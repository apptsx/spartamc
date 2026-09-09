package com.minecraft.core.bukkit.listener.account;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.route.RouteContext;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.backend.data.list.user.AccountData;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.core.bukkit.manager.list.PermissionManager;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.controller.list.AccountController;
import com.minecraft.core.util.Util;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Instant;
import java.util.UUID;
import java.util.logging.Level;

public class DataListener implements Listener {

    private final AccountData data;
    private final AccountController controller;

    public DataListener() {
        this.data = Core.getAccountData();
        this.controller = Core.getAccountController();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void load(AsyncPlayerPreLoginEvent event) {
        UUID id = event.getUniqueId();
        String name = event.getName();

        Instant startTime = Instant.now();

        try {
            Account account = data.of(id, true);

            if (account == null) {
                account = data.save(new Account(id, name));
                if (account == null) {
                    controller.remove(id);
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE);
                    return;
                }
            }

            controller.save(account);

            Core.getLogger().info("[Account] Conta de " + name + " iniciada. (Tempo: " + Util.formatInstant(startTime) + ")");

            event.allow();
        } catch (Exception e) {
            controller.remove(id);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE);
            Core.getLogger().log(Level.SEVERE, "Erro ao carregar a conta de " + name + "!", e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void check(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        Account account = controller.of(player.getUniqueId());

        if (account != null) {
            account.setDefaultProfileSkin();

            event.allow();
        } else
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void join(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        Account account = controller.of(player.getUniqueId());

        if (account == null) {
            player.kickPlayer(Constant.ACCOUNT_LOAD_FAILED_MESSAGE);
            return;
        }

        if (!account.hasTag(account.getTag()))
            account.setTag(account.getDefaultTag());

        if (account.getSkin().isValid())
            ProtocolHandler.changePlayerSkin(player, account.getSkin(), false);

        if (account.isUsingFake())
            ProtocolHandler.changePlayerName(player, account.getNickname());

        if (!Core.getServerType().isArcade())
            account.setRoute(RouteContext.builder()
                    .senderId(account.getId())
                    .serverType(Core.getServerType())
                    .serverId(Core.getServerId())
                    .serverPort(Core.getServerPort())
                    .arcade(ArcadeRouteContext.builder().build())
                    .updatedAt(System.currentTimeMillis())
                    .build());

        player.setPlayerListHeaderFooter(new TextComponent(Constant.SERVER_TAB_HEADER), new TextComponent(Constant.SERVER_TAB_FOOTER));

        PermissionManager.loadPermissions(player);

        CollectibleOperator.enable(player);

        Core.getLogger().info(player.getName() + " entrou no servidor.");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void quit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        handlePlayerExit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void kick(PlayerKickEvent event) {
        event.setLeaveMessage(null);
        handlePlayerExit(event.getPlayer());
    }

    private void handlePlayerExit(Player player) {
        UserModel.remove(player.getUniqueId());

        PermissionManager.unloadPermissions(player);

        BukkitCore.getManager().getSidebar().remove(player);
        BukkitCore.getManager().getCooldown().resetCooldown(player);

        TagManager.removeTag(player);
        CollectibleOperator.unload(player);

        controller.remove(player.getUniqueId());

        Core.getLogger().info(player.getName() + " saiu do servidor.");
    }
}