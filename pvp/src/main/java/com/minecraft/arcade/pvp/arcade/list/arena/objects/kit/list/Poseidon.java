package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;

public class Poseidon extends Kit implements Listener {

    public Poseidon() {
        super("Poseidon", Item.of(Material.WATER_BUCKET), KitStyle.COMBAT,
                Collections.singletonList(
                        "§7Fique mais forte ao entrar na água."
                ));

        setRanks(RankType.VIP);
        setPrice(10000);
    }

    @EventHandler
    public void handle(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Material self = event.getTo().getBlock().getRelative(BlockFace.SELF).getType();

        if (isUsingKit(player)) {
            if (self.name().contains("WATER"))
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0));
            else if (player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE))
                player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
        }
    }
}
