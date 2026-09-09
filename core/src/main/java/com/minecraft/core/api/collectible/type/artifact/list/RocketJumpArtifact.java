package com.minecraft.core.api.collectible.type.artifact.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.core.api.collectible.util.particle.ParticleEffect;
import com.minecraft.core.api.item.Item;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class RocketJumpArtifact extends ArtifactCollectible {

    public RocketJumpArtifact() {
        super("Pulo Foguete", CollectibleRarity.RARE, new ArrayList<>(), System.currentTimeMillis());

        setIcon(Item.of(Material.FIREWORK));
        setLore(Arrays.asList(
                "§7Use para dar um super pulo",
                "§7com efeitos de fogos de artifício!"
        ));
    }

    @Override
    public void handle(Player host) {
        if (hasCooldown(host)) return;
        if (!host.isOnGround()) {
            host.sendMessage("§cVocê precisa estar no chão para usar o Pulo Foguete.");
            return;
        }

        setCooldown(host, java.util.concurrent.TimeUnit.SECONDS.toMillis(15));

        Location loc = host.getLocation();
        World world = loc.getWorld();

        world.playSound(loc, Sound.FIREWORK_LAUNCH, 1.0f, 1.5f);
        world.playSound(loc, Sound.EXPLODE, 0.5f, 1.0f);
        host.sendMessage("§6🚀 Pulo Foguete ativado!");

        org.bukkit.util.Vector velocity = host.getVelocity();
        velocity.setY(3.5);
        host.setVelocity(velocity);

        for (int i = 0; i < 30; i++) {
            double offsetX = (new Random().nextDouble() - 0.5) * 3;
            double offsetY = new Random().nextDouble() * 3;
            double offsetZ = (new Random().nextDouble() - 0.5) * 3;
            try {
                ParticleEffect.FIREWORKS_SPARK.display((float) offsetX, (float) offsetY, (float) offsetZ, 0.2f, 2, loc.clone().add(offsetX, offsetY, offsetZ));
                ParticleEffect.FLAME.display((float) offsetX * 0.5f, (float) offsetY * 0.5f, (float) offsetZ * 0.5f, 0.05f, 1, loc.clone().add(offsetX * 0.5, offsetY * 0.5, offsetZ * 0.5));
            } catch (Exception e) {
                world.playEffect(loc.clone().add(offsetX, offsetY, offsetZ), Effect.MOBSPAWNER_FLAMES, 0);
            }
        }

        try {
            ParticleEffect.LAVA.display(0.5f, 0.5f, 0.5f, 0.1f, 15, loc);
            ParticleEffect.SMOKE_LARGE.display(0.3f, 0.3f, 0.3f, 0.02f, 10, loc);
            ParticleEffect.TOWN_AURA.display(0.4f, 0.4f, 0.4f, 0.05f, 8, loc);
        } catch (Exception e) {
            world.playEffect(loc, Effect.MOBSPAWNER_FLAMES, 0);
        }

        for (int i = 0; i < 5; i++) {
            final int step = i;
            final Location finalLoc = loc.clone();
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (host.isOnline()) {
                        Location trailLoc = host.getLocation();
                        try {
                            ParticleEffect.EXPLOSION_NORMAL.display(0, 0, 0, 0, 3, trailLoc);
                            ParticleEffect.CRIT.display(0.3f, 0.5f, 0.3f, 0.1f, 5, trailLoc);
                        } catch (Exception e) {
                            trailLoc.getWorld().playEffect(trailLoc, Effect.EXPLOSION_LARGE, 0);
                        }
                    }
                }
            }.runTaskLater(Core.getJavaPlugin(), step * 5L);
        }

        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            if (host.isOnline()) {
                world.playSound(host.getLocation(), Sound.FIREWORK_BLAST2, 1.0f, 1.0f);
                try {
                    ParticleEffect.EXPLOSION_NORMAL.display(0, 0, 0, 0, 8, host.getLocation());
                    ParticleEffect.EXPLOSION_LARGE.display(0, 0, 0, 0, 3, host.getLocation());
                } catch (Exception e) {
                    world.playEffect(host.getLocation(), Effect.EXPLOSION_HUGE, 0);
                }
            }
        }, 20L);
    }
}
