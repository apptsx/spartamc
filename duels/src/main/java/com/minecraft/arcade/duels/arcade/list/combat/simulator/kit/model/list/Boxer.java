package com.minecraft.arcade.duels.arcade.list.combat.simulator.kit.model.list;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import com.minecraft.arcade.duels.arcade.list.combat.simulator.kit.model.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.Arrays;

public class Boxer extends Kit {

    public Boxer() {
        super("Boxer", Style.COMBAT, Item.of(Material.QUARTZ), -1,
                Arrays.asList("§7Reduza o dano e fique mais", "§7forte com as mãos."), new ArrayList<>());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBoxer(PlayerDamageTargetEvent event) {
        Player player = event.getPlayer(), target = event.getTarget();

        if (isUsing(target) && event.getDamage() > 1.0D)
            event.setDamage(event.getDamage() - 0.25D);

        if (isUsing(player) && player.getItemInHand().getType() == Material.AIR) {
            event.setDamage(event.getDamage() + 2.0D);
            return;
        }

        if (isUsing(player) && player.getItemInHand().getType() != Material.AIR)
            event.setDamage(event.getDamage() + 0.25D);
    }
}
