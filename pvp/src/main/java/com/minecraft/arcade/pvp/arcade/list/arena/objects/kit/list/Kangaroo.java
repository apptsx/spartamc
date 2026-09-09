package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageEvent;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Kangaroo extends Kit implements Listener {

    private final List<UUID> jumpList;

    public Kangaroo() {
        super("Kangaroo", Item.of(Material.FIREWORK), KitStyle.MOVEMENT, Collections.singletonList("§7Pule como um Canguru."));

        this.jumpList = new ArrayList<>();

        setSpecialItems(Item.of(Material.FIREWORK, "§aKangaroo"));
        setRestrictedKits(Anchor.class);

        setCooldown(8);

        setRanks(RankType.VIP);
        setPrice(30000);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isUsingKit(player) && event.getAction() != Action.PHYSICAL && withSpecial(event.getItem())) {
            event.setCancelled(true);

            if (jumpList.contains(player.getUniqueId()))
                return;

            if (hasCooldown(player))
                return;

            Vector vector = player.getEyeLocation().getDirection().multiply(player.isSneaking() ? 2.3F : 0.7f).setY(player.isSneaking() ? 0.5 : 1F);

            player.setFallDistance(-1.0F);
            player.setVelocity(vector);

            jumpList.add(player.getUniqueId());
        }
    }

    @EventHandler
    public void onJumpRemove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (isUsingKit(player) && jumpList.contains(player.getUniqueId()) && player.isOnGround())
            jumpList.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCooldown(PlayerDamageTargetEvent event) {
        if (isUsingKit(event.getPlayer()))
            applyCooldown(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(PlayerDamageEvent event) {
        if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            Player player = event.getPlayer();

            if (isUsingKit(player)) {
                if (jumpList.contains(player.getUniqueId())) {
                    jumpList.remove(player.getUniqueId());

                    event.setCancelled(true);
                } else {
                    double damage = 1.5D,
                            remainingLife = player.getHealth() - damage;

                    if (remainingLife <= 0.5)
                        event.setCancelled(true);
                    else
                        event.setDamage(1.5D);
                }
            }
        }
    }

}
