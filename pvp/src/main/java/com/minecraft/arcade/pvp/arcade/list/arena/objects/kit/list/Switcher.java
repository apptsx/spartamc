package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.PvP;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Collections;

public class Switcher extends Kit implements Listener {

    private final String METADATA_NAME = "switcher_metadata";

    public Switcher() {
        super("Switcher", Item.of(Material.SNOW_BALL), KitStyle.STRATEGY,
                Collections.singletonList("§7Troque de lugar com seus inimigos."));

        setSpecialItems(Item.of(Material.SNOW_BALL, "§aSwitcher"));
        setCooldown(10);

        setRanks(RankType.VIP);
        setPrice(15000);
    }

    @EventHandler
    public void use(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        ItemStack item = player.getItemInHand();

        if (item == null || item.getType().equals(Material.AIR)) return;

        if (event.getAction().name().contains("RIGHT")) {
            if (isUsingKit(player) && withSpecial(item)) {
                event.setCancelled(true);

                player.updateInventory();

                if (hasCooldown(player)) return;
                else applyCooldown(player);

                Snowball snowball = player.launchProjectile(Snowball.class);

                snowball.setMetadata(METADATA_NAME, new FixedMetadataValue(PvP.getInstance(), player));
            }
        }
    }

    @EventHandler
    public void switcher(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager().hasMetadata(METADATA_NAME)) {
            Player player = (Player) event.getDamager().getMetadata(METADATA_NAME).get(0).value();

            if (player == null) return;

            Player entity = (Player) event.getEntity();

            if (isUsingKit(player) && isAllow(entity)) {
                if (isNeo(entity)) {
                    player.sendMessage("§cO jogador " + entity.getName() + " está usando o Neo.");
                    return;
                }

                Location loc = entity.getLocation().clone();

                entity.teleport(player.getLocation().clone());

                player.teleport(loc);
            }
        }
    }
}
