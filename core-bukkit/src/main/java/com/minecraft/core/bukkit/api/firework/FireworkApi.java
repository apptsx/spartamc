package com.minecraft.core.bukkit.api.firework;

import com.minecraft.core.Core;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Arrays;

public class FireworkApi {

    public static void random(Location location) {
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);

        FireworkMeta fireworkMeta = firework.getFireworkMeta();

        int id = Core.RANDOM.nextInt(4) + 1;

        FireworkEffect effect = FireworkEffect.builder()
                .flicker(Core.RANDOM.nextBoolean())
                .withColor(Color.WHITE)
                .withColor(Color.YELLOW)
                .withFade(Color.AQUA)
                .with(findType(id))
                .trail(Core.RANDOM.nextBoolean())
                .build();

        fireworkMeta.addEffect(effect);
        fireworkMeta.setPower(Core.RANDOM.nextInt(2) + 1);

        firework.setFireworkMeta(fireworkMeta);
    }

    protected static FireworkEffect.Type findType(int id) {
        return Arrays.stream(FireworkEffect.Type.values()).filter(effect -> effect.ordinal() == id).findFirst().orElse(null);
    }
}
