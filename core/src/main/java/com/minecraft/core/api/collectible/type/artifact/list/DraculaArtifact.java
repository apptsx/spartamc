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
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class DraculaArtifact extends ArtifactCollectible {

    private final Map<Bat, ArmorStand> batMap;

    public DraculaArtifact() {
        super("Dracula", CollectibleRarity.EPIC, new ArrayList<>(), 1726601469158L);

        this.batMap = new HashMap<>();

        setIcon(Item.of(Material.FERMENTED_SPIDER_EYE));
        setLore(Arrays.asList("§7Vire um morcego,", "§7e desapareça!"));
    }


    @Override
    public void handle(Player host) {
        if (hasCooldown(host)) return;

        Location loc = host.getLocation();
        
        try {
            ParticleEffect.SMOKE_LARGE.display(0.5f, 1.0f, 0.5f, 0.1f, 25, loc);
            ParticleEffect.SPELL_WITCH.display(0.5f, 0.5f, 0.5f, 0.1f, 20, loc);
            ParticleEffect.SLIME.display(0.4f, 0.4f, 0.4f, 0.05f, 15, loc);
            ParticleEffect.PORTAL.display(0.3f, 0.5f, 0.3f, 0.1f, 12, loc);
            ParticleEffect.SPELL_INSTANT.display(0.2f, 0.2f, 0.2f, 0.05f, 10, loc);
        } catch (Exception e) {
            host.getWorld().playEffect(loc, Effect.SMOKE, 10);
        }
        
        host.getWorld().playSound(loc, Sound.ZOMBIE_WOOD, 1.0f, 0.5f);
        host.sendMessage("§5🦇 Você se transformou em Dracula!");

        for (int i = 0; i < 12; ++i) {
            Bat bat = host.getWorld().spawn(host.getLocation().add(0.0D, 1.0D, 0.0D), Bat.class);
            ArmorStand ghost = bat.getWorld().spawn(bat.getLocation(), ArmorStand.class);

            ghost.setSmall(true);
            ghost.setGravity(false);
            ghost.setVisible(false);
            bat.setPassenger(ghost);

            batMap.put(bat, ghost);
        }

        host.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 280, 1));
        host.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 280, 1));

        Bukkit.getScheduler().scheduleSyncDelayedTask(Core.getJavaPlugin(), () -> {
            batMap.keySet().forEach(Entity::remove);
            batMap.values().forEach(ArmorStand::remove);
            batMap.clear();
            if (host.isOnline()) {
                host.sendMessage("§5🦇 Você voltou ao normal!");
                host.getWorld().playSound(host.getLocation(), Sound.ZOMBIE_WOOD, 1.0f, 0.5f);
            }
        }, 260L);

        setCooldown(host, TimeUnit.MINUTES.toMillis(1));
    }
}
