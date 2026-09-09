package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.arcade.pvp.PvP;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list.gladiator.Gladiator;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list.gladiator.objects.GladiatorRing;
import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Kunai extends Kit implements Listener {

    private final Map<UUID, Arrow> arrowMap;

    public Kunai() {
        super("Kunai", Item.of(Material.ARROW), KitStyle.STRATEGY,
                Arrays.asList(
                        "§7Jogue uma kunai,",
                        "§7e agache-se para ir",
                        "§7até ela."
                ));

        setRanks(RankType.VIP);
        setPrice(30000);

        setSpecialItems(Item.of(Material.ARROW, "§aKunai"));
        setCooldown(10);

        setRestrictedKits(Stomper.class, Gladiator.class, Phantom.class, Ninja.class);

        this.arrowMap = new ConcurrentHashMap<>();
    }

    @EventHandler
    public void onKunai(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isUsingKit(player) && withSpecial(player.getItemInHand()) && event.getAction().name().contains("RIGHT")) {

            if (GladiatorRing.inRing(player)) {
                player.sendMessage("§cVocê não pode usar a Kunai em uma arena.");
                return;
            }

            if (hasCooldown(player)) return;

            Arrow arrow = player.launchProjectile(Arrow.class);

            arrow.setCustomName("§aKunai");
            arrow.setMetadata("untouchable", new FixedMetadataValue(PvP.getInstance(), true));

            arrowMap.put(player.getUniqueId(), arrow);

            applyCooldown(player);
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        org.bukkit.entity.Item item = event.getItem();

        if (isAllow(player) && item.hasMetadata("untouchable"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (isUsingKit(player)) {
            Arrow arrow = arrowMap.get(player.getUniqueId());

            if (arrow != null) {

                Location arrowLocation = arrow.getLocation(), playerLocation = player.getLocation();

                if (playerLocation.distance(arrowLocation) > 30) {
                    arrow.remove();
                    arrowMap.remove(player.getUniqueId());

                    player.sendMessage("§cVocê está longe demais da sua Kunai.");
                    return;
                }

                player.sendMessage("§aVocê se teletransportou até a sua Kunai.");

                player.teleport(new Location(arrowLocation.getWorld(), arrowLocation.getX(), arrowLocation.getY(), arrowLocation.getZ(),
                        playerLocation.getYaw(), playerLocation.getPitch()));

                player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1.0f, 1.0f);

                arrow.remove();
                arrowMap.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();

            if (arrow.hasMetadata("untouchable") && arrow.getShooter() instanceof Player) {
                Player shooter = (Player) arrow.getShooter();

                if (event.getHitEntity() instanceof Player) {
                    Player hit = (Player) event.getHitEntity();

                    arrow.remove();

                    if (isAllow(hit)) {
                        shooter.teleport(hit);
                        shooter.playSound(shooter.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    }

                    arrowMap.remove(shooter.getUniqueId());
                } else {
                    Core.getPlatform().runSync(() -> {
                        if (arrow.isValid()) {
                            arrow.remove();
                            arrowMap.remove(shooter.getUniqueId());
                        }
                    }, 20 * 5);
                }
            }
        }
    }
}
