package com.minecraft.core.api.collectible.type.artifact.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.core.api.collectible.util.particle.ParticleEffect;
import com.minecraft.core.api.item.Item;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SnowBlastArtifact extends ArtifactCollectible {

    private static final double RADIUS = 6.0;
    private static final double PUSH_FORCE = 3.0;

    public SnowBlastArtifact() {
        super("Nevoeiro Congelante", CollectibleRarity.RARE, new ArrayList<>(), System.currentTimeMillis());

        setIcon(Item.of(Material.SNOW_BALL));
        setLore(Arrays.asList(
                "§7Cria uma explosão de neve",
                "§7que empurra jogadores próximos!"
        ));
    }

    @Override
    public void handle(Player host) {
        if (hasCooldown(host)) return;

        setCooldown(host, java.util.concurrent.TimeUnit.SECONDS.toMillis(15));

        Location center = host.getLocation();
        World world = center.getWorld();

        world.playSound(center, Sound.STEP_SNOW, 1.0f, 1.0f);
        world.playSound(center, Sound.STEP_SNOW, 0.8f, 1.5f);
        host.sendMessage("§b❄ Nevoeiro Congelante ativado!");

        try {
            ParticleEffect.SNOWBALL.display(2.5f, 2.5f, 2.5f, 0.6f, 60, center);
            ParticleEffect.SNOW_SHOVEL.display(2f, 2f, 2f, 0.15f, 40, center);
            ParticleEffect.CLOUD.display(1.5f, 1.5f, 1.5f, 0.1f, 25, center);
            ParticleEffect.TOWN_AURA.display(1f, 1f, 1f, 0.05f, 20, center);
        } catch (Exception e) {
            world.playEffect(center, Effect.SNOWBALL_BREAK, 0);
            world.playEffect(center, Effect.SMOKE, 5);
        }

        List<Entity> entities = world.getEntities();
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !(entity instanceof Player) || (entity instanceof Player && !entity.equals(host))) {
                if (entity.getLocation().distance(center) <= RADIUS) {
                    Vector pushDirection = entity.getLocation().toVector().subtract(center.toVector()).normalize();
                    pushDirection.setY(0.5);
                    entity.setVelocity(pushDirection.multiply(PUSH_FORCE));

                    if (entity instanceof LivingEntity) {
                        try {
                            ParticleEffect.SNOWBALL.display(0.3f, 0.3f, 0.3f, 0.1f, 8, entity.getLocation());
                            ParticleEffect.CLOUD.display(0.2f, 0.2f, 0.2f, 0.05f, 5, entity.getLocation());
                        } catch (Exception e) {
                            world.playEffect(entity.getLocation(), Effect.SNOWBALL_BREAK, 0);
                        }
                    }
                }
            }
        }

        world.playEffect(center, Effect.SNOWBALL_BREAK, 1);

        for (int i = 0; i < 20; i++) {
            final int step = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    double progress = step / 20.0;
                    double currentRadius = RADIUS * progress;

                    try {
                        ParticleEffect.SNOW_SHOVEL.display((float) currentRadius, 1.2f, (float) currentRadius, 0.05f, 8, center);
                        ParticleEffect.CLOUD.display((float) (currentRadius * 0.5), 0.5f, (float) (currentRadius * 0.5), 0.02f, 5, center);
                    } catch (Exception e) {
                        world.playEffect(center, Effect.SMOKE, step);
                    }
                    world.playEffect(center, Effect.STEP_SOUND, Material.SNOW_BLOCK);

                    if (step >= 20) {
                        try {
                            ParticleEffect.CLOUD.display(2.5f, 1.5f, 2.5f, 0.15f, 40, center);
                            ParticleEffect.SNOWBALL.display(1.5f, 1.5f, 1.5f, 0.2f, 25, center);
                        } catch (Exception e) {
                            world.playEffect(center, Effect.SMOKE, 0);
                        }
                        this.cancel();
                    }
                }
            }.runTaskLater(Core.getJavaPlugin(), step);
        }
    }
}
