package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.Collections;

public class Fisherman extends Kit implements Listener {

    public Fisherman() {
        super("Fisherman", Item.of(Material.FISHING_ROD), KitStyle.STRATEGY,
                Collections.singletonList("§7Fisgue os seus inimigos."));

        setSpecialItems(Item.of(Material.FISHING_ROD, "§aFisherman").unbreakable());

        setRanks(RankType.VIP);
        setPrice(20000);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        Entity caught = event.getCaught();

        if (isUsingKit(player) && caught instanceof Player) {
            player.getItemInHand().setDurability((short) 0);

            if (event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
                if (isNeo((Player) caught))
                    return;

                caught.teleport(player.getLocation());
            }
        }
    }
}
