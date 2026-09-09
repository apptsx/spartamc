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
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class LightningStormArtifact extends ArtifactCollectible {

    public LightningStormArtifact() {
        super("Tempestade", CollectibleRarity.MYTHICAL, new ArrayList<>(), System.currentTimeMillis());

        setIcon(Item.of(Material.BLAZE_ROD));
        setLore(Arrays.asList(
                "§7Invoca uma tempestade de raios",
                "§7ao seu redor!"
        ));
    }

    @Override
    public void handle(Player host) {
        if (hasCooldown(host)) return;

        setCooldown(host, java.util.concurrent.TimeUnit.SECONDS.toMillis(15));

        host.getWorld().playSound(host.getLocation(), Sound.ZOMBIE_WOOD, 1.0f, 0.5f);
        host.sendMessage("§5⚡ Invocando tempestade...");

        Location center = host.getLocation();
        World world = center.getWorld();
        Random random = new Random();

        try {
            ParticleEffect.SMOKE_LARGE.display(0.8f, 0.8f, 0.8f, 0.05f, 20, center);
            ParticleEffect.TOWN_AURA.display(0.5f, 1.0f, 0.5f, 0.1f, 15, center);
        } catch (Exception e) {
            world.playEffect(center, Effect.SMOKE, 10);
        }

        for (int i = 0; i < 8; i++) {
            final int strikeNumber = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!host.isOnline()) {
                        this.cancel();
                        return;
                    }

                    double offsetX = (random.nextDouble() - 0.5) * 10;
                    double offsetZ = (random.nextDouble() - 0.5) * 10;
                    Location strikeLoc = center.clone().add(offsetX, 0, offsetZ);

                    LightningStrike lightning = world.strikeLightning(strikeLoc);

                    try {
                        ParticleEffect.EXPLOSION_NORMAL.display(0, 0, 0, 0, 8, strikeLoc);
                        ParticleEffect.EXPLOSION_LARGE.display(0, 0, 0, 0, 3, strikeLoc);
                        ParticleEffect.SMOKE_LARGE.display(0.5f, 0.5f, 0.5f, 0.05f, 15, strikeLoc);
                        ParticleEffect.CRIT.display(0.3f, 0.3f, 0.3f, 0.1f, 10, strikeLoc);
                        ParticleEffect.SPELL_WITCH.display(0.2f, 0.5f, 0.2f, 0.05f, 8, strikeLoc);
                    } catch (Exception e) {
                        world.playEffect(strikeLoc, Effect.EXPLOSION_LARGE, 0);
                        world.playEffect(strikeLoc, Effect.SMOKE, 5);
                    }

                    if (strikeNumber == 7) {
                        world.playSound(center, Sound.ZOMBIE_WOOD, 1.0f, 0.5f);
                        host.sendMessage("§a⚡ Tempestade invocada!");
                    }
                }
            }.runTaskLater(Core.getJavaPlugin(), i * 5L);
        }

        for (Entity entity : host.getNearbyEntities(8, 8, 8)) {
            if (entity instanceof Player && !entity.equals(host)) {
                ((Player) entity).sendMessage("§5⚡ Uma tempestade foi invocada por " + host.getName() + "!");
            }
        }
    }
}
