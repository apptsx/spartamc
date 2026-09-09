package com.minecraft.core.bukkit.listener;

import com.minecraft.core.api.collectible.nms.module.balloon.BalloonEntity;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import net.minecraft.server.v1_8_R3.EntityBat;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BalloonListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getClickedBlock() == null) return;

        CollectibleOperator.of(player, com.minecraft.core.api.collectible.CollectibleCategory.BALLOON);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        org.bukkit.entity.Entity entity = event.getRightClicked();

        if (entity == null) return;

        if (entity instanceof org.bukkit.entity.Bat) {
            event.setCancelled(true);
            return;
        }

        if (entity instanceof org.bukkit.entity.ArmorStand) {
            event.setCancelled(true);
            return;
        }

        Object handle = null;
        try {
            handle = entity.getClass().getMethod("getHandle").invoke(entity);
        } catch (Exception e) {
            return;
        }

        if (handle instanceof BalloonEntity) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        org.bukkit.entity.Entity entity = event.getEntity();

        if (entity == null) return;

        if (entity instanceof org.bukkit.entity.Bat) {
            event.setCancelled(true);
            return;
        }

        if (entity instanceof org.bukkit.entity.ArmorStand) {
            event.setCancelled(true);
            return;
        }

        Object handle = null;
        try {
            handle = entity.getClass().getMethod("getHandle").invoke(entity);
        } catch (Exception e) {
            return;
        }

        if (handle instanceof BalloonEntity) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        CollectibleOperator.of(player, com.minecraft.core.api.collectible.CollectibleCategory.BALLOON);
    }
}