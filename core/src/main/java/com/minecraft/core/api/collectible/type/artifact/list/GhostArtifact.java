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
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class GhostArtifact extends ArtifactCollectible {

    private final Map<UUID, List<Location>> playerGhosts;

    public GhostArtifact() {
        super("Fantasmas", CollectibleRarity.EPIC, new ArrayList<>(), System.currentTimeMillis());

        this.playerGhosts = new HashMap<>();

        setIcon(Item.of(Material.GHAST_TEAR));
        setLore(Arrays.asList(
                "§7Invoque fantasmas",
                "§7assustadores!"
        ));
    }

    @Override
    public void handle(Player host) {
        if (hasCooldown(host)) return;

        setCooldown(host, TimeUnit.SECONDS.toMillis(15));
        
        host.getWorld().playSound(host.getLocation(), Sound.GHAST_SCREAM, 1.0f, 1.5f);
        host.sendMessage("§f👻 Fantasmas invocados!");

        Location loc = host.getLocation().add(0, 1, 0);
        List<Location> ghostLocations = new ArrayList<>();
        
        Random random = new Random();
        for (int i = 0; i < 30; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 4;
            double offsetY = random.nextDouble() * 3;
            double offsetZ = (random.nextDouble() - 0.5) * 4;
            Location ghostLoc = loc.clone().add(offsetX, offsetY, offsetZ);
            
            try {
                ParticleEffect.SMOKE_NORMAL.display(0.3f, 0.3f, 0.3f, 0.05f, 3, ghostLoc);
                ParticleEffect.SPELL_WITCH.display(0.2f, 0.2f, 0.2f, 0.05f, 2, ghostLoc);
                ParticleEffect.SLIME.display(0.2f, 0.2f, 0.2f, 0.05f, 2, ghostLoc);
                ParticleEffect.PORTAL.display(0.2f, 0.2f, 0.2f, 0.1f, 3, ghostLoc);
            } catch (Exception e) {
                host.getWorld().playEffect(ghostLoc, Effect.SMOKE, 0);
            }
            
            ghostLocations.add(ghostLoc);
        }

        try {
            ParticleEffect.SMOKE_LARGE.display(0.5f, 1.0f, 0.5f, 0.1f, 20, loc);
            ParticleEffect.SPELL_WITCH.display(0.5f, 0.5f, 0.5f, 0.1f, 15, loc);
            ParticleEffect.SLIME.display(0.4f, 0.4f, 0.4f, 0.05f, 10, loc);
            ParticleEffect.ENCHANTMENT_TABLE.display(0.3f, 0.5f, 0.3f, 0.05f, 8, loc);
        } catch (Exception e) {
            host.getWorld().playEffect(loc, Effect.SMOKE, 5);
        }

        playerGhosts.put(host.getUniqueId(), ghostLocations);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Core.getJavaPlugin(), () -> {
            playerGhosts.remove(host.getUniqueId());
        }, 300L);
    }
}
