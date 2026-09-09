package com.minecraft.arcade.duels.arcade.list.combat.simulator.kit.model.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.item.Item;
import com.minecraft.arcade.duels.arcade.list.combat.simulator.kit.model.Kit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;

public class Monk extends Kit {

    public Monk() {
        super("Monk", Style.STRATEGY, Item.of(Material.BLAZE_ROD), 12,
                Arrays.asList("§7Bagunçe o inventário", "§7do oponente."), Collections.singletonList(Item.of(Material.BLAZE_ROD, "§bMonk")));
    }

    @EventHandler
    public void onMonk(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer(), clicked = (Player) event.getRightClicked();

            if (hasCooldown(player)) return;

            if (isUsing(player) && isKitItem(player.getItemInHand())) {
                int randomSlot = Core.RANDOM.nextInt(36);

                ItemStack current = (clicked.getItemInHand() != null ? clicked.getItemInHand().clone() : null),
                        random = (clicked.getInventory().getItem(randomSlot) != null ? clicked.getInventory().getItem(randomSlot).clone() : null);

                clicked.getInventory().setItem(randomSlot, current);

                if (random == null) {
                    clicked.setItemInHand(null);
                } else {
                    clicked.getInventory().setItemInHand(random);
                }

                applyCooldown(player);

                player.sendMessage("§aVocê bagunçou o inventário de " + clicked.getName() + ".");

                clicked.sendMessage("§eO seu inventário foi bagunçado por " + player.getName() + ".");
                clicked.playSound(clicked.getLocation(), Sound.PISTON_RETRACT, 1.5f, 2.0f);
            }
        }
    }
}
