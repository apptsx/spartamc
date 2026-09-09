package com.minecraft.core.bukkit.api.permission.regex;

import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.permission.injector.CraftBukkitInterface;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class RegexPermissions implements Listener {

    public static RegexPermissions instance;

    private final Plugin plugin;
    private final PermissionList permsList;
    private final PEXPermissionSubscriptionMap subscriptionHandler;

    public RegexPermissions() {
        this.plugin = BukkitCore.getInstance();

        instance = this;

        this.subscriptionHandler = PEXPermissionSubscriptionMap.inject(plugin, plugin.getServer().getPluginManager());
        this.permsList = PermissionList.inject(plugin.getServer().getPluginManager());

        this.injectAllPermissibles();
    }

    public void onDisable() {
        this.subscriptionHandler.uninject();
        this.uninjectAllPermissibles();
    }

    public PermissionList getPermissionList() {
        return this.permsList;
    }

    public void injectPermissible(final Player player) {
        try {
            final PermissiblePEX permissible = new PermissiblePEX(player, this.plugin);
            final PermissibleInjector injector = new PermissibleInjector.ClassPresencePermissibleInjector(
                    CraftBukkitInterface.getCBClassName("entity.CraftHumanEntity"), "perm", true);
            boolean success = false;
            if (injector.isApplicable(player)) {
                final Permissible oldPerm = injector.inject(player, permissible);
                if (oldPerm != null) {
                    permissible.setPreviousPermissible(oldPerm);
                    success = true;
                }
            }
            if (!success) {
                this.plugin.getLogger().warning("Unable to inject PEX's permissible for " + player.getName());
            }
            permissible.recalculatePermissions();
        } catch (Throwable e) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to inject permissible for " + player.getName(), e);
        }
    }

    private void injectAllPermissibles() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.injectPermissible(player);
        }
    }

    private void uninjectPermissible(final Player player) {
        try {
            boolean success = false;
            final PermissibleInjector injector = new PermissibleInjector.ClassPresencePermissibleInjector(
                    CraftBukkitInterface.getCBClassName("entity.CraftHumanEntity"), "perm", true);
            if (injector.isApplicable(player)) {
                final Permissible pexPerm = injector.getPermissible(player);
                if (pexPerm instanceof PermissiblePEX) {
                    if (injector.inject(player, ((PermissiblePEX) pexPerm).getPreviousPermissible()) != null) {
                        success = true;
                    }
                } else {
                    success = true;
                }
            }
            if (!success) {
                this.plugin.getLogger()
                        .warning("No Permissible injector found for your server implementation (while uninjecting for " + player.getName() + "!");
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void uninjectAllPermissibles() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.uninjectPermissible(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        injectPermissible(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        uninjectPermissible(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(final PlayerKickEvent event) {
        uninjectPermissible(event.getPlayer());
    }

}
