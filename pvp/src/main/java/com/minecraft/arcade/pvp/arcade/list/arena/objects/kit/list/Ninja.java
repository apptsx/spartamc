package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.arcade.pvp.user.factory.list.ArenaUser;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Arrays;

public class Ninja extends Kit implements Listener {

    public Ninja() {
        super("Ninja", Item.of(Material.NETHER_STAR), KitStyle.STRATEGY,
                Arrays.asList("§7Teleporte-se até o", "§7último jogador que você", "§7bateu."));

        setCooldown(5);

        setRanks(RankType.VIP);
        setPrice(30000);

        setRestrictedKits(Stomper.class);
    }

    @EventHandler
    public void onNinja(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (isUsingKit(player)) {
            ArenaUser user = (ArenaUser) ArenaUser.of(player.getUniqueId());

            if (hasCooldown(player)) return;

            if (user.inCombat()) {
                Player target = user.getCombat().getTarget();

                if (isNeo(target)) {
                    player.sendMessage("§cO jogador " + target.getName() + " está usando o Neo.");
                    return;
                }

                if (player.getLocation().distance(target.getLocation()) > 25) {
                    player.sendMessage("§cVocê está muito longe do seu alvo!");
                    return;
                }

                player.teleport(target.getLocation());
                player.setFallDistance(0.0F);

                player.sendMessage("§eVocê foi teleportado até §6" + target.getName() + "§e!");

                applyCooldown(player);
            }
        }
    }
}
