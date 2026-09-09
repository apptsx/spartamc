package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class QuickDropper extends Kit implements Listener {

    public QuickDropper() {
        super("QuickDropper", Item.of(Material.MUSHROOM_SOUP), KitStyle.STRATEGY,
                Collections.singletonList("§7Drope sopas automaticamente."));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void drop(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isUsingKit(player)) {
            ItemStack item = player.getItemInHand();

            if (item == null || item.getType() == Material.AIR) return;

            if (item.getType().equals(Material.BOWL)) {
                player.getInventory().remove(item);
                player.getWorld().dropItem(player.getLocation(), item);
            }
        }
    }
}
