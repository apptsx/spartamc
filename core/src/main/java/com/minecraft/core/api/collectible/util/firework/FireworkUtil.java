package com.minecraft.core.api.collectible.util.firework;

import com.minecraft.core.api.collectible.nms.v1_8_R3.NMS;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FireworkUtil {

    private static final List<FireworkEffect> EFFECTS = new ArrayList<>();
    private static final List<FireworkEffect> BLOWUP_EFFECTS = new ArrayList<>();

    static {
        EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.RED).withFade(Color.BLUE).build());
        EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.BLUE).withFade(Color.GREEN).build());
        EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.GREEN).withFade(Color.RED).build());
        EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(Color.RED).withFade(Color.GREEN).build());
        EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(Color.BLUE).withFade(Color.RED).build());
        EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(Color.GREEN).withFade(Color.BLUE).build());
        EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(Color.RED).withFade(Color.BLUE).build());
        EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(Color.BLUE).withFade(Color.GREEN).build());
        EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(Color.GREEN).withFade(Color.RED).build());
        EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(Color.BLUE).withFade(Color.RED).build());
        BLOWUP_EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(Color.RED).withFade(Color.GREEN).build());
        BLOWUP_EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(Color.BLUE).withFade(Color.RED).build());
        BLOWUP_EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(Color.GREEN).withFade(Color.BLUE).build());
        BLOWUP_EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.CREEPER).withColor(Color.RED).withFade(Color.BLUE).build());
        BLOWUP_EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.CREEPER).withColor(Color.BLUE).withFade(Color.GREEN).build());
        BLOWUP_EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.CREEPER).withColor(Color.GREEN).withFade(Color.RED).build());
        BLOWUP_EFFECTS.add(FireworkEffect.builder().with(FireworkEffect.Type.CREEPER).withColor(Color.BLUE).withFade(Color.RED).build());
    }

    public static void playFirework(Location location, FireworkEffect effect) {
        NMS.getInstance().spawnFirework(location, effect);
    }

    public static FireworkEffect getRandomEffect() {
        return EFFECTS.get(ThreadLocalRandom.current().nextInt(EFFECTS.size()));
    }

    public static FireworkEffect getBlowupRandomEffect() {
        return BLOWUP_EFFECTS.get(ThreadLocalRandom.current().nextInt(BLOWUP_EFFECTS.size()));
    }
}