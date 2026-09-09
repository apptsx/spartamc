package com.minecraft.core.api.collectible.type.trail;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class TrailListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        
        String title = event.getInventory().getTitle();
        
        if (title != null && title.equals("§6§lEscolha a cor do rastro")) {
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() == org.bukkit.Material.WOOL) {
                TrailCollectible.handleColorSelection(player, clicked);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        
        if (newItem == null || newItem.getType() == org.bukkit.Material.AIR) {
            TrailCollectible.checkAndRemoveTrailArrow(player);
        } else if (newItem.getType() != org.bukkit.Material.ARROW) {
            TrailCollectible.checkAndRemoveTrailArrow(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack dropped = event.getItemDrop().getItemStack();
        
        if (dropped != null && dropped.getType() == org.bukkit.Material.ARROW) {
            TrailCollectible.checkAndRemoveTrailArrow(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        TrailCollectible.checkAndRemoveTrailArrow(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        TrailCollectible.removeTrailArrow(event.getPlayer());
        TrailCollectible.cleanupAll();
    }
}
