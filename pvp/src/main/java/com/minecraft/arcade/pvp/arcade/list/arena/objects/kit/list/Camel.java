package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;

public class Camel extends Kit implements Listener {

    public Camel() {
        super("Camel", Item.of(Material.SAND), KitStyle.COMBAT,
                Collections.singletonList("§7Seja veloz nas areias."));

        setRanks(RankType.VIP);
        setPrice(10000);
    }

    @EventHandler
    public void onCamel(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Block down = event.getTo().getBlock().getRelative(BlockFace.DOWN);

        if (isUsingKit(player)) {
            if (down.getType() == Material.SAND)
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 6, 0));
        }
    }
}
