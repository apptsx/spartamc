package com.minecraft.arcade.duels.arcade.list.combat.simulator.kit.model.list;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageEvent;
import com.minecraft.arcade.duels.arcade.list.combat.simulator.kit.model.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.Collections;

public class Fireman extends Kit {

    public Fireman() {
        super("Fireman", Style.STRATEGY, Item.of(Material.LAVA_BUCKET), 0,
                Collections.singletonList("§7Não receba dano de fogo."), new ArrayList<>());
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireman(PlayerDamageEvent event) {
        Player player = event.getPlayer();

        if (isUsing(player) && (event.getCause().equals(EntityDamageEvent.DamageCause.LAVA) || event.getCause().name().startsWith("FIRE")))
            event.setCancelled(true);
    }
}
