package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageEvent;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Collections;

public class Magma extends Kit implements Listener {

    public Magma() {
        super("Magma", Item.of(Material.MAGMA_CREAM), KitStyle.COMBAT,
                Collections.singletonList("§7Incendeie os seus inimigos."));

        setRanks(RankType.VIP);
        setPrice(20000);
    }

    @EventHandler
    public void onMagma(PlayerDamageTargetEvent event) {
        Player player = event.getTarget(), damager = event.getPlayer();

        if (isUsingKit(damager) && isAllow(player) && Core.RANDOM.nextInt(3) == 0) {
            player.setFireTicks(80);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLava(PlayerDamageEvent event) {
        Player player = event.getPlayer();

        if (isUsingKit(player) && (event.getCause().name().contains("FIRE") || event.getCause().equals(EntityDamageEvent.DamageCause.LAVA)))
            event.setCancelled(true);
    }

    @EventHandler
    public void onWater(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Material self = event.getTo().getBlock().getType();

        if (isUsingKit(player) && self.name().contains("WATER")) {
            player.damage(1.0F);
        }
    }
}
