package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EnchantingInventory;

import java.util.Collections;

public class Specialist extends Kit implements Listener {

    public Specialist() {
        super("Specialist", Item.of(Material.BOOK), KitStyle.STRATEGY,
                Collections.singletonList("§7Colete XP e upe os seus itens."));

        setSpecialItems(Item.of(Material.BOOK, "§aSpecialist"));

        setRanks(RankType.VIP);
        setPrice(30000);
    }

    @EventHandler
    public void onEnchant(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isUsingKit(player) && withSpecial(player.getItemInHand()) && event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            event.setCancelled(true);

            Block block = new Location(player.getWorld(), 501, 0, 500).getBlock();

            block.setType(Material.ENCHANTMENT_TABLE);

            player.openEnchanting(block.getLocation(), true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSendExp(PlayerDamageTargetEvent event) {
        Player player = event.getTarget(), killer = event.getTarget();

        if (event.getFinalDamage() >= player.getHealth() && isUsingKit(killer))
            killer.setLevel(killer.getLevel() + 1);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void preEnchant(PrepareItemEnchantEvent event) {
        Player enchanter = event.getEnchanter();

        if (isUsingKit(enchanter) && event.getInventory() instanceof EnchantingInventory) {
            EnchantingInventory inventory = (EnchantingInventory) event.getInventory();

            if (withSpecial(event.getItem()))
                event.setCancelled(true);
        }
    }
}
