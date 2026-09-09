package com.minecraft.lobby.menu.shop.coupon;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CouponAnvilListener implements Listener {

    static final Map<UUID, CouponAnvilMenu> activeMenus = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        CouponAnvilMenu menu = activeMenus.get(player.getUniqueId());
        if (menu != null) {
            menu.handleClick(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        CouponAnvilMenu menu = activeMenus.get(player.getUniqueId());
        if (menu != null) {
            menu.handleDrag(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        CouponAnvilMenu menu = activeMenus.remove(player.getUniqueId());
        if (menu != null) {
            menu.handleClose(event);
        }
    }
}
