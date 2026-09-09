package com.minecraft.core.api.collectible.type.artifact.listener;

import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.core.api.collectible.type.artifact.list.GrappleArtifact;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ArtifactListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void use(PlayerInteractEvent event) {
        Action action = event.getAction();
        
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK && 
            action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack hand = player.getItemInHand();

        if (hand == null || hand.getType() == Material.AIR) return;

        CollectibleOperator operator = CollectibleOperator.of(player, CollectibleCategory.ARTIFACT);

        if (operator != null) {
            ArtifactCollectible artifact = (ArtifactCollectible) operator.getCollectible();

            if (artifact.getIcon() != null && artifact.getIcon().getType() == hand.getType()) {
                event.setCancelled(true);
                
                if (artifact instanceof GrappleArtifact) {
                    ((GrappleArtifact) artifact).onPlayerInteract(event);
                } else {
                    if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                        artifact.handle(player);
                    }
                }
            }
        }
    }
}
