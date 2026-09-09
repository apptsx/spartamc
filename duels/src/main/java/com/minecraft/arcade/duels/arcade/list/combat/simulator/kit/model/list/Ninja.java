package com.minecraft.arcade.duels.arcade.list.combat.simulator.kit.model.list;

import com.minecraft.core.api.item.Item;
import com.minecraft.arcade.duels.arcade.list.combat.simulator.kit.model.Kit;
import com.minecraft.arcade.duels.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class Ninja extends Kit {

    public Ninja() {
        super("Ninja", Style.MOVEMENT, Item.of(Material.NETHER_STAR), 5, Arrays.asList(
                "§7Agache-se e teleporte-se",
                "§7até o seu oponente."
        ), new ArrayList<>());
    }

    @EventHandler
    public void onShift(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (isUsing(player)) {
            if (hasCooldown(player)) return;

            User user = (User) User.of(player.getUniqueId());

            if (user != null && user.inCombat()) {
                Player target = user.getCombat().getTarget();

                if (target == null) return;

                if (isInvincible(target)) {
                    player.sendMessage("§cVocê não pode usar o Ninja em jogadores com o kit Neo.");
                    return;
                }

                player.teleport(target);

                applyCooldown(player);
            }
        }
    }
}
