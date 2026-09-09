package com.minecraft.core.bukkit.api.particle;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Effect;
import org.bukkit.entity.Player;

public class ParticleApi {

    public static void spawnHappyParticles(Location center, int rangeX, int rangeY, int rangeZ, double speed) {
        if (center == null || center.getWorld() == null) return;
        
        World world = center.getWorld();
        
        for (double x = center.getX() - rangeX; x <= center.getX() + rangeX; x += rangeX * 0.5) {
            for (double y = center.getY() - rangeY; y <= center.getY() + rangeY; y += rangeY * 0.5) {
                for (double z = center.getZ() - rangeZ; z <= center.getZ() + rangeZ; z += rangeZ * 0.5) {
                    Location loc = new Location(world, x, y, z);
                    world.playEffect(loc, Effect.HAPPY_VILLAGER, 1);
                }
            }
        }
    }
}
