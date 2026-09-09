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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class EnderShiftArtifact extends ArtifactCollectible {

    private static final int MAX_DISTANCE = 15;

    public EnderShiftArtifact() {
        super("Ender Shift", CollectibleRarity.EPIC, new ArrayList<>(), System.currentTimeMillis());

        setIcon(Item.of(Material.ENDER_PORTAL_FRAME));
        setLore(Arrays.asList(
                "§7Teleporte-se para frente",
                "§7com efeitos de Ender Pearl!"
        ));
    }

    @Override
    public void handle(Player host) {
        if (hasCooldown(host)) return;

        Location startLoc = host.getLocation();
        Vector directionVector = startLoc.getDirection().normalize();

        Location targetLoc = findSafeLocation(host, directionVector);

        if (targetLoc == null) {
            host.sendMessage("§cNão há espaço seguro para teleportar!");
            return;
        }

        setCooldown(host, java.util.concurrent.TimeUnit.SECONDS.toMillis(15));

        host.sendMessage("§d✦ Ender Shift ativado!");
        host.getWorld().playSound(startLoc, Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);

        spawnTeleportEffect(startLoc);
        host.teleport(targetLoc);
        spawnTeleportEffect(targetLoc);

        host.setFallDistance(0);

        for (int i = 0; i < 5; i++) {
            final int step = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!host.isOnline()) {
                        this.cancel();
                        return;
                    }

                    Location particleLoc = startLoc.clone().add(
                            (targetLoc.getX() - startLoc.getX()) * (step / 5.0),
                            (targetLoc.getY() - startLoc.getY()) * (step / 5.0),
                            (targetLoc.getZ() - startLoc.getZ()) * (step / 5.0)
                    );

                    try {
                        ParticleEffect.PORTAL.display(0.3f, 0.3f, 0.3f, 0.15f, 8, particleLoc);
                        ParticleEffect.SMOKE_NORMAL.display(0.3f, 0.3f, 0.3f, 0.08f, 5, particleLoc);
                        ParticleEffect.SPELL_WITCH.display(0.2f, 0.3f, 0.2f, 0.05f, 3, particleLoc);
                        ParticleEffect.ENCHANTMENT_TABLE.display(0.2f, 0.3f, 0.2f, 0.05f, 3, particleLoc);
                    } catch (Exception e) {
                        particleLoc.getWorld().playEffect(particleLoc, Effect.PORTAL, 0);
                        particleLoc.getWorld().playEffect(particleLoc, Effect.SMOKE, 0);
                    }
                }
            }.runTaskLater(Core.getJavaPlugin(), (long) (step * 2));
        }

        host.getWorld().playSound(targetLoc, Sound.ENDERMAN_TELEPORT, 1.0f, 0.8f);
    }

    private Location findSafeLocation(Player player, Vector direction) {
        Location current = player.getLocation().clone();
        Block feet = current.getBlock();
        Block below = feet.getRelative(BlockFace.DOWN);

        for (int i = 1; i <= MAX_DISTANCE; i++) {
            Location target = player.getLocation().clone().add(direction.clone().multiply(i));

            feet = target.getBlock();
            below = feet.getRelative(BlockFace.DOWN);

            if (isWalkable(feet) && isWalkable(below) && !feet.getType().isSolid() && !below.getType().isSolid()) {
                return target.clone().add(0, 1, 0);
            }
        }

        return null;
    }

    private boolean isWalkable(Block block) {
        Material type = block.getType();
        return type == Material.AIR;
    }

    private void spawnTeleportEffect(Location loc) {
        World world = loc.getWorld();

        try {
            ParticleEffect.PORTAL.display(0.6f, 1.2f, 0.6f, 0.15f, 40, loc);
            ParticleEffect.SMOKE_NORMAL.display(0.4f, 1.0f, 0.4f, 0.08f, 25, loc);
            ParticleEffect.SPELL_WITCH.display(0.5f, 0.5f, 0.5f, 0.1f, 15, loc);
            ParticleEffect.ENCHANTMENT_TABLE.display(0.3f, 0.6f, 0.3f, 0.08f, 12, loc);
            ParticleEffect.FLAME.display(0.2f, 0.3f, 0.2f, 0.05f, 10, loc);
        } catch (Exception e) {
            world.playEffect(loc, Effect.PORTAL, 0);
            world.playEffect(loc, Effect.SMOKE, 0);
        }

        world.playEffect(loc, Effect.ENDER_SIGNAL, 1);
    }
}
