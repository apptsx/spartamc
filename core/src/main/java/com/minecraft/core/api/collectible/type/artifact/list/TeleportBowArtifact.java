package com.minecraft.core.api.collectible.type.artifact.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.core.api.collectible.util.particle.ParticleEffect;
import com.minecraft.core.api.item.Item;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TeleportBowArtifact extends ArtifactCollectible {

    private static final Map<UUID, TeleportData> activeTeleports = new HashMap<>();

    private static class TeleportData {
        Location targetLocation;
        Player player;
        BukkitTask teleportTask;

        TeleportData(Player player, Location target) {
            this.player = player;
            this.targetLocation = target;
        }

        void cleanup() {
            if (teleportTask != null) {
                teleportTask.cancel();
            }
        }
    }

    public TeleportBowArtifact() {
        super("Arco do Voo", CollectibleRarity.MYTHICAL, new ArrayList<>(), System.currentTimeMillis());

        setIcon(Item.of(Material.BOW));
        setLore(Arrays.asList(
                "§7Arco místico que teleporta",
                "§7você gradualmente até onde",
                "§7a flecha caiu!"
        ));
    }

    @Override
    public void handle(Player host) {
        if (hasCooldown(host)) return;

        if (activeTeleports.containsKey(host.getUniqueId())) {
            TeleportData data = activeTeleports.get(host.getUniqueId());
            data.cleanup();
            activeTeleports.remove(host.getUniqueId());
            host.sendMessage("§cTeleporte cancelado!");
            return;
        }

        host.sendMessage("§b🏹 Atire uma flecha para se teleportar!");
        host.playSound(host.getLocation(), Sound.WOOD_CLICK, 1.0f, 1.0f);

        setCooldown(host, java.util.concurrent.TimeUnit.SECONDS.toMillis(15));
    }

    public static void onArrowShoot(Player player, Arrow arrow) {
        arrow.setMetadata("teleport_arrow", new FixedMetadataValue(Core.getJavaPlugin(), player.getUniqueId().toString()));
    }

    public static void onArrowHit(Player player, Location hitLocation) {
        UUID uuid = player.getUniqueId();

        if (activeTeleports.containsKey(uuid)) {
            activeTeleports.get(uuid).cleanup();
        }

        player.sendMessage("§b✨ Iniciando teleporte...");
        player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);

        TeleportData data = new TeleportData(player, hitLocation);
        activeTeleports.put(uuid, data);

        Location startLoc = player.getLocation();
        double totalDistance = startLoc.distance(hitLocation);
        int teleportSteps = (int) Math.max(5, totalDistance * 2);
        int delayBetweenTeleports = (int) Math.max(3, 10 - (totalDistance * 0.3));

        final int[] currentStep = {0};

        data.teleportTask = Bukkit.getScheduler().runTaskTimer(Core.getJavaPlugin(), () -> {
            if (!player.isOnline() || player.isDead()) {
                data.cleanup();
                activeTeleports.remove(uuid);
                return;
            }

            currentStep[0]++;

            double progress = (double) currentStep[0] / teleportSteps;

            Location targetStep = new Location(
                    hitLocation.getWorld(),
                    startLoc.getX() + (hitLocation.getX() - startLoc.getX()) * progress,
                    startLoc.getY() + (hitLocation.getY() - startLoc.getY()) * progress,
                    startLoc.getZ() + (hitLocation.getZ() - startLoc.getZ()) * progress
            );

            Block targetBlock = targetStep.getBlock();
            Block belowBlock = targetStep.clone().add(0, -1, 0).getBlock();

            if (!targetBlock.getType().isSolid() && belowBlock.getType().isSolid()) {
                targetStep.add(0, 1, 0);
            } else if (targetBlock.getType().isSolid()) {
                targetStep.add(0, 2, 0);
            }

            player.teleport(targetStep);

            try {
                ParticleEffect.PORTAL.display(0.5f, 1.0f, 0.5f, 0.2f, 15, targetStep);
                ParticleEffect.ENCHANTMENT_TABLE.display(0.3f, 0.5f, 0.3f, 0.1f, 10, targetStep);
                ParticleEffect.SPELL_WITCH.display(0.3f, 0.3f, 0.3f, 0.1f, 8, targetStep);
            } catch (Exception e) {
                targetStep.getWorld().playEffect(targetStep, org.bukkit.Effect.PORTAL, 0);
            }

            targetStep.getWorld().playSound(targetStep, Sound.ENDERMAN_TELEPORT, 0.5f, 1.5f);

            if (currentStep[0] >= teleportSteps || player.getLocation().distance(hitLocation) < 0.5) {
                player.teleport(hitLocation);
                data.cleanup();
                activeTeleports.remove(uuid);

                player.sendMessage("§a✨ Teleporte concluído!");
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);

                try {
                    ParticleEffect.EXPLOSION_NORMAL.display(0, 0, 0, 0, 3, hitLocation);
                    ParticleEffect.ENCHANTMENT_TABLE.display(0.5f, 1.0f, 0.5f, 0.2f, 20, hitLocation);
                } catch (Exception e) {
                    hitLocation.getWorld().playEffect(hitLocation, org.bukkit.Effect.EXPLOSION_HUGE, 0);
                }
            }
        }, delayBetweenTeleports, delayBetweenTeleports);
    }

    public static void onPlayerQuit(Player player) {
        UUID uuid = player.getUniqueId();

        if (activeTeleports.containsKey(uuid)) {
            activeTeleports.get(uuid).cleanup();
            activeTeleports.remove(uuid);
        }
    }
}
