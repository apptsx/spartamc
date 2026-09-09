package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collections;

public class Boxer extends Kit implements Listener {

    public Boxer() {
        super("Boxer", Item.of(Material.QUARTZ), KitStyle.COMBAT,
                Collections.singletonList("§7Receba menos dano."));

        setRanks(RankType.VIP);
        setPrice(15000);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamagePlayer(PlayerDamageTargetEvent event) {
        Player player = event.getPlayer();

        if (isUsingKit(event.getTarget()) && event.getDamage() > 1.0D)
            event.setDamage(event.getDamage() - 0.25D);

        if (isUsingKit(player) && player.getItemInHand().getType() == Material.AIR) {
            event.setDamage(event.getDamage() + 2.0D);
            return;
        }

        if (isUsingKit(player) && player.getItemInHand().getType() != Material.AIR)
            event.setDamage(event.getDamage() + 0.25D);
    }
}
