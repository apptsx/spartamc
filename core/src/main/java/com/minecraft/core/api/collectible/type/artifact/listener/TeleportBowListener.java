package com.minecraft.core.api.collectible.type.artifact.listener;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.type.artifact.list.TeleportBowArtifact;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class TeleportBowListener implements Listener {

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Arrow) {
            Arrow arrow = (Arrow) entity;

            if (arrow.getShooter() instanceof Player) {
                Player player = (Player) arrow.getShooter();

                if (player.getItemInHand() != null && player.getItemInHand().getType().name().contains("BOW")) {
                    arrow.setMetadata("Spark_teleport_arrow", new FixedMetadataValue(Core.getJavaPlugin(), player.getUniqueId().toString()));

                    TeleportBowArtifact.onArrowShoot(player, arrow);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Arrow) {
            Arrow arrow = (Arrow) entity;

            if (arrow.hasMetadata("Spark_teleport_arrow")) {
                String playerId = arrow.getMetadata("Spark_teleport_arrow").get(0).asString();
                Player player = Core.getPlatform().getPlayer(java.util.UUID.fromString(playerId), Player.class);

                if (player != null && player.isOnline()) {
                    TeleportBowArtifact.onArrowHit(player, arrow.getLocation());
                }

                arrow.remove();
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        TeleportBowArtifact.onPlayerQuit(event.getPlayer());
    }
}
