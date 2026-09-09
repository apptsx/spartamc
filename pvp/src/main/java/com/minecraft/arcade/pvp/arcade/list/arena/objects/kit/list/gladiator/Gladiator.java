package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list.gladiator;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list.gladiator.objects.GladiatorRing;
import com.minecraft.arcade.pvp.user.factory.list.ArenaUser;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class Gladiator extends Kit implements Listener {

    public Gladiator() {
        super("Gladiator", Item.of(Material.IRON_FENCE), KitStyle.COMBAT,
                Arrays.asList("§7Desafie outros jogadores", "§7em uma arena nos céus."));

        setCooldown(5);

        setRanks(RankType.VIP);
        setPrice(30000);

        setSpecialItems(Item.of(Material.IRON_FENCE, "§aGladiator"));
    }

    protected void handleQuit(Player player) {
        if (isAllow(player) && GladiatorRing.inRing(player)) {
            GladiatorRing ring = GladiatorRing.of(player);

            Player target = ring.getAnotherPlayer(player);

            if (target != null && isAllow(target)) {
                ArenaUser user = (ArenaUser) ArenaUser.of(player.getUniqueId()),
                        targetUser = (ArenaUser) ArenaUser.of(target.getUniqueId());

                if (user != null && targetUser != null)
                    targetUser.getArcade().handleDeath(user, targetUser);
            }
        }
    }

    @EventHandler
    public void handleRings(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (GladiatorRing ring : GladiatorRing.getRings()) {
                if (ring == null || !ring.isSpawned()) continue;

                if (ring.isValid()) {
                    ring.setTime(ring.getTime() + 1);

                    final int witherTime = 60 * 2;

                    if (ring.getTime() == (witherTime - 5)) {
                        ring.getPlayers().forEach(player -> player.sendMessage("§4§lRINGUE §cOs efeitos do Wither aparecerão em 5 segundos!"));
                    }

                    if (ring.getTime() == witherTime) {
                        ring.getPlayers().forEach(player -> {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 3));

                            player.sendMessage("§4§lRINGUE §cChegou a hora de ver quem é o mais forte!");
                            player.playSound(player.getLocation(), Sound.WITHER_HURT, 1.0f, 1.0f);
                        });
                    }
                } else {
                    /* Cancelar ringue */
                    GladiatorRing.remove(ring);
                }
            }
        }
    }

    @EventHandler
    public void onClickRing(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.hasBlock() && GladiatorRing.inRing(player)) {
            Block clicked = event.getClickedBlock();

            if (clicked.getType().equals(Material.GLASS))
                player.sendBlockChange(clicked.getLocation(), Material.BEDROCK, (byte) 0);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuitRing(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        handleQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onKickRing(PlayerKickEvent event) {
        event.setLeaveMessage(null);

        handleQuit(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeathRing(PlayerDamageTargetEvent event) {
        Player player = event.getPlayer(), target = event.getTarget();

        if (event.getFinalDamage() >= target.getHealth() && GladiatorRing.isSameRing(player, target)) {
            GladiatorRing ring = GladiatorRing.of(player, target);

            GladiatorRing.remove(ring);
        }
    }

    @EventHandler
    public void onArena(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer(), clicked = (Player) event.getRightClicked();

            if (isUsingKit(player) && withSpecial(player.getItemInHand()) && isAllow(clicked)) {

                if (hasCooldown(player)) return;

                if (isNeo(clicked)) {
                    player.sendMessage("§cO jogador " + clicked.getName() + " é imune ao Gladiator.");
                    return;
                }

                if (GladiatorRing.inRing(player)) {
                    player.sendMessage("§cVocê já está batalhando no Gladiator.");
                    return;
                }

                GladiatorRing ring = new GladiatorRing(player, clicked, player.getLocation(), player.getLocation());

                ring.spawn();
                ring.teleport();

                GladiatorRing.getRings().add(ring);
            }
        }
    }
}
