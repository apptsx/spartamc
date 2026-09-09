package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collections;

public class Fireman extends Kit implements Listener {

    public Fireman() {
        super("Fireman", Item.of(Material.WATER_BUCKET), KitStyle.STRATEGY, Collections.singletonList("§7Seja imune ao fogo!"));

        setRanks(RankType.VIP);
        setPrice(10000);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireman(PlayerDamageEvent event) {
        Player player = event.getPlayer();

        if (isUsingKit(player) && (event.getCause().name().contains("FIRE") || event.getCause().equals(EntityDamageEvent.DamageCause.LAVA)))
            event.setCancelled(true);
    }
}
