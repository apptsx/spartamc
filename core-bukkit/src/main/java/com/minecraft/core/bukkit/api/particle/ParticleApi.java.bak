package com.minecraft.core.bukkit.api.particle;

import com.minecraft.core.Core;
import com.minecraft.core.arcade.room.map.area.Cuboid;
import org.bukkit.Location;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class ParticleApi {

    public static void spawnHappyParticles(Location baseLocation, int radius, int amount, int particleAmount, double height) {
        for (int i = 0; i < amount; i++) {
            Location randomLocation = getRandomLocationInRadius(baseLocation, radius)
                    .clone().add(0, height, 0);

            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.VILLAGER_HAPPY, randomLocation);

            particle.setAmount(particleAmount);
            particle.display();
        }
    }

    public static void spawnHappyParticles(Location center, int amount, int particleAmount, double height) {
        center = center.clone().add(0, height, 0);

        Location finalCenter = center;
        Core.getPlatform().runSync(() -> {
            Location[] relatives = Cuboid.getRelativeLocations(finalCenter);

            Location[] frontRelatives = Cuboid.getRelativeLocations(relatives[0]);

            for (int i = 0; i < amount; i++) {
                for (Location relative : relatives) {
                    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.VILLAGER_HAPPY, getRandomLocationInRadius(relative, 0));

                    particle.setAmount(particleAmount);
                    particle.display();
                }
            }

            for (int i = 0; i < amount; i++) {
                for (Location frontRelative : frontRelatives) {
                    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.VILLAGER_HAPPY, getRandomLocationInRadius(frontRelative, 0));

                    particle.setAmount(particleAmount);
                    particle.display();
                }
            }
        });
    }

    public static Location getRandomLocationInRadius(Location center, int radius) {
        double angle = Math.random() * 2 * Math.PI;
        double distance = Math.random() * radius;
        double xOffset = Math.cos(angle) * distance;
        double zOffset = Math.sin(angle) * distance;

        return center.clone().add(xOffset, 0, zOffset);
    }
}
