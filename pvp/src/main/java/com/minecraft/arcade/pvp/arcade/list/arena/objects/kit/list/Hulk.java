package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.PvP;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class Hulk extends Kit implements Listener {

    public Hulk() {
        super("Hulk", Item.of(Material.SADDLE), KitStyle.COMBAT,
                Arrays.asList("§7Suba os inimigos", "§7nas costas."));

        setCooldown(15);

        setRanks(RankType.VIP);
        setPrice(10000);
    }

    @EventHandler
    public void onUse(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer(), target = (Player) event.getRightClicked();

            if (isUsingKit(player) && isAllow(target) && player.getItemInHand().getType().equals(Material.AIR)
                    && !player.isInsideVehicle() && !target.isInsideVehicle()) {
                if (hasCooldown(player)) return;

                if (isNeo(target)) {
                    player.sendMessage("§cO jogador " + target.getName() + " está usando o Neo.");
                    return;
                }

                applyCooldown(player);

                player.setPassenger(event.getRightClicked());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamagePlayer(PlayerDamageTargetEvent event) {
        Player player = event.getTarget();
        Player hulk = event.getPlayer();

        if (hulk.getPassenger() != null && hulk.getPassenger() == player && isUsingKit(hulk)
                && hulk.getPassenger() == player) {
            event.setCancelled(true);
            player.setSneaking(true);

            Vector v = hulk.getEyeLocation().getDirection().multiply(1.6F);
            v.setY(0.6D);
            player.setVelocity(v);

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.setSneaking(false);
                }
            }.runTaskLater(PvP.getInstance(), 10L);
        }
    }
}
