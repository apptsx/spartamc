package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;

public class Viper extends Kit implements Listener {

    public Viper() {
        super("Viper", Item.of(Material.SPIDER_EYE), KitStyle.COMBAT,
                Collections.singletonList("§7Envenene os seus inimigos."));

        setRanks(RankType.VIP);
        setPrice(15000);
    }

    @EventHandler(ignoreCancelled = true)
    public void onViper(PlayerDamageTargetEvent event) {
        Player player = event.getTarget(), damager = event.getPlayer();

        if (isUsingKit(damager) && isAllow(player)) {
            if (Core.RANDOM.nextInt(5) == 2) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 0));

                player.getLocation().getWorld().playEffect(player.getLocation().clone().add(0.0, 0.4, 0.0), Effect.STEP_SOUND, 159, 13);
            }
        }
    }
}
