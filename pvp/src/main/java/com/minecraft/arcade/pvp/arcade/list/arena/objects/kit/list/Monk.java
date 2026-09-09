package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class Monk extends Kit implements Listener {

    public Monk() {
        super("Monk", Item.of(Material.BLAZE_ROD), KitStyle.STRATEGY,
                Collections.singletonList("§7Bagunce o inventário dos inimigos."));

        setSpecialItems(Item.of(Material.BLAZE_ROD, "§aMonk"));
        setCooldown(10);

        setRanks(RankType.VIP);
        setPrice(20000);
    }

    @EventHandler
    public void onMonk(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer(), clicked = (Player) event.getRightClicked();

            if (isUsingKit(player) && withSpecial(player.getItemInHand())) {
                if (hasCooldown(player)) return;

                if (isNeo(clicked)) {
                    player.sendMessage("§cO jogador " + clicked.getName() + " está usando o Neo.");
                    return;
                }

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
