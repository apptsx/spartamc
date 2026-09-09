package com.minecraft.core.api.collectible.type.artifact.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.core.api.collectible.util.firework.FireworkUtil;
import com.minecraft.core.api.collectible.util.particle.ParticleEffect;
import com.minecraft.core.api.item.Item;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class FireworkArtifact extends ArtifactCollectible {

    public FireworkArtifact() {
        super("Foguete", CollectibleRarity.EPIC, new ArrayList<>(), 1726529495972L);

        setIcon(Item.of(Material.FIREWORK));
        setLore(Arrays.asList("§7Vá até o céu", "§7com o seu foguete."));
    }

    @Override
    public void handle(Player host) {
        if (!host.isOnGround()) {
            host.sendMessage("§cVocê precisa estar no chão para usar o Foguete.");
            return;
        }

        if (hasCooldown(host)) return;

        Location loc = host.getLocation();
        
        try {
            ParticleEffect.EXPLOSION_NORMAL.display(0, 0, 0, 0, 10, loc);
            ParticleEffect.FIREWORKS_SPARK.display(0.5f, 1.0f, 0.5f, 0.2f, 20, loc);
            ParticleEffect.FLAME.display(0.3f, 0.5f, 0.3f, 0.1f, 15, loc);
            ParticleEffect.LAVA.display(0.4f, 0.4f, 0.4f, 0.1f, 10, loc);
            ParticleEffect.SMOKE_LARGE.display(0.3f, 0.3f, 0.3f, 0.05f, 8, loc);
        } catch (Exception e) {
            host.getWorld().playEffect(loc, Effect.EXPLOSION_LARGE, 0);
        }

        host.getWorld().playSound(loc, Sound.FIREWORK_LAUNCH, 1.5f, 1.0f);

        Firework firework = host.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        meta.setPower(2);
        meta.addEffect(FireworkUtil.getRandomEffect());

        firework.setFireworkMeta(meta);
        firework.setPassenger(host);

        setCooldown(host, TimeUnit.SECONDS.toMillis(15));
    }
}
