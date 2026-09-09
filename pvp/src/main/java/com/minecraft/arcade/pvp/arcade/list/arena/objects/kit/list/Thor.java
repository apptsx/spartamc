package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.PvP;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.Collections;
import java.util.Set;

public class Thor extends Kit implements Listener {

    public Thor() {
        super("Thor", Item.of(Material.WOOD_AXE), KitStyle.COMBAT,
                Collections.singletonList("§7Solte rajadas de trovão."));

        setSpecialItems(Item.of(Material.WOOD_AXE, "§aThor").flags(ItemFlag.HIDE_UNBREAKABLE).unbreakable());
        setCooldown(15);

        setRanks(RankType.VIP);
        setPrice(20000);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        ItemStack item = player.getItemInHand();

        if (item == null || item.getType() == Material.AIR) return;

        if (event.getAction().name().contains("RIGHT") && isUsingKit(player) && withSpecial(item)) {
            if (hasCooldown(player)) return;

            event.setCancelled(true);

            Location location = player.getTargetBlock((Set<Material>) null, 25).getLocation();

            player.getWorld().strikeLightning(location);
            player.getWorld().setMetadata("thor", new FixedMetadataValue(PvP.getInstance(), System.currentTimeMillis() + 4000L));

            if (location.getBlock().getY() >= 110) {
                Location newLocation = location.clone();

                if (newLocation.getBlock().getType() == Material.NETHERRACK) {
                    newLocation.getWorld().createExplosion(newLocation, 2.5F);
                }
            }

            item.setDurability((short) 0);

            applyCooldown(player);

            player.updateInventory();
        }
    }

    @EventHandler
    public void damage(PlayerDamageEvent event) {
        Player player = event.getPlayer();

        if (event.getCause().equals(EntityDamageEvent.DamageCause.LIGHTNING)) {
            MetadataValue value = player.getMetadata("thor").stream().findFirst().orElse(null);

            if (value == null) {
                event.setDamage(3.0D);

                player.setFireTicks(150);
            } else if (value.asLong() > System.currentTimeMillis()) {
                event.setCancelled(true);

                value.invalidate();
            }
        }
    }
}
