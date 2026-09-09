package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;

public class Snail extends Kit implements Listener {

    public Snail() {
        super("Snail", Item.of(Material.SOUL_SAND), KitStyle.COMBAT,
                Collections.singletonList("§7Deixe os seus inimigos lentos."));

        setRanks(RankType.VIP);
        setPrice(20000);
    }

    @EventHandler
    public void handle(PlayerDamageTargetEvent event) {
        Player player = event.getTarget(), damager = event.getPlayer();

        if (isUsingKit(damager) && isAllow(player) && Core.RANDOM.nextInt(4) == 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3 * 20, 0));
        }
    }
}
