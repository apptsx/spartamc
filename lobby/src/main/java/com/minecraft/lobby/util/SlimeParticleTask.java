package com.minecraft.lobby.util;

import com.minecraft.core.bukkit.BukkitCore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SlimeParticleTask {

    private static BukkitRunnable task;
    private static final Set<Location> slimeCache = new HashSet<>();
    private static final Random random = new Random();

    public static void start() {
        if (task != null) return;

        scanSlimeBlocks();

        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : slimeCache) {
                    World world = loc.getWorld();
                    if (world == null) continue;
                    if (!world.isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) continue;

                    double xOff = 0.1 + random.nextDouble() * 0.8;
                    double zOff = 0.1 + random.nextDouble() * 0.8;
                    double yOff = 1.0 + random.nextDouble() * 0.3;

                    world.playEffect(loc.clone().add(xOff, yOff, zOff), org.bukkit.Effect.HAPPY_VILLAGER, 0);
                }
            }
        };

        task.runTaskTimer(BukkitCore.getInstance(), 20L, 5L);
    }

    public static void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        slimeCache.clear();
    }

    private static void scanSlimeBlocks() {
        slimeCache.clear();
        for (World world : BukkitCore.getInstance().getServer().getWorlds()) {
            for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < 80; y++) {
                            if (chunk.getBlock(x, y, z).getType() == Material.SLIME_BLOCK) {
                                slimeCache.add(chunk.getBlock(x, y, z).getLocation());
                            }
                        }
                    }
                }
            }
        }
    }
}
