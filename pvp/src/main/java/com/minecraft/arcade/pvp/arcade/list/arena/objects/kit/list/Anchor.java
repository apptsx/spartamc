package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.PvP;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collections;

public class Anchor extends Kit implements Listener {

    public Anchor() {
        super("Anchor", Item.of(Material.ANVIL), KitStyle.COMBAT,
                Collections.singletonList(
                        "§7Não dê, nem receba knockback."
                ));

        setRestrictedKits(Kangaroo.class);

        setRanks(RankType.VIP);
        setPrice(15000);
    }

    @EventHandler
    public void onAnchor(PlayerDamageTargetEvent event) {
        Player target = event.getTarget(), damager = event.getPlayer();

        if ((isUsingKit(target) || isUsingKit(damager)) && !isNeo(target))
            handleVelocity(target);
    }

    protected void handleVelocity(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setVelocity(new Vector(0.0, -1, 0.0));
                player.playSound(player.getLocation(), Sound.ANVIL_USE, 1.0f, 1.0f);

                cancel();
            }
        }.runTaskLater(PvP.getInstance(), 1L);
    }
}
