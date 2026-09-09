package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list.grappler.Grappler;
import com.minecraft.arcade.pvp.user.factory.list.ArenaUser;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;

public class Stomper extends Kit implements Listener {

    public Stomper() {
        super("Stomper", Item.of(Material.IRON_BOOTS), KitStyle.COMBAT,
                Collections.singletonList("§7Esmague seus oponentes."));

        setRanks(RankType.VIP);
        setPrice(45000);

        setRestrictedKits(AntiStomper.class, Kangaroo.class, Grappler.class, Phantom.class, Ninja.class);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStomper(EntityDamageEvent event) {
        if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL) && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (!isUsingKit(player)) return;

            ArenaUser user = (ArenaUser) ArenaUser.of(player.getUniqueId());

            if (user.isLauncher()) return;

            double damage = event.getDamage();

            if (damage <= 2) return;

            event.setDamage(Math.min(damage, 4));


            for (Entity entity : player.getNearbyEntities(5, 3, 5)) {
                if (entity instanceof Player) {
                    Player target = (Player) entity;

                    if (player.getUniqueId().equals(target.getUniqueId())) continue;

                    if (!isAllow(target)) continue;

                    if (target.getNoDamageTicks() > 20)
                        return;

                    if (target.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE))
                        continue;

                    ArenaUser stomped = (ArenaUser) ArenaUser.of(target.getUniqueId());

                    if (stomped == null || stomped.isUsingKit(AntiStomper.class)) continue;

                    double damageToReceive = damage;

                    if (target.isSneaking())
                        damageToReceive = 4;

                    if ((target.getHealth() - damageToReceive) <= 0) {
                        user.getArcade().handleDeath(stomped, user);
                    } else {
                        target.damage(damageToReceive, player);
                    }
                }
            }
        }
    }
}
