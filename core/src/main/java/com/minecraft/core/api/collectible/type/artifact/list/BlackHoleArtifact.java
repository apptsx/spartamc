package com.minecraft.core.api.collectible.type.artifact.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.core.api.collectible.util.particle.ParticleEffect;
import com.minecraft.core.api.item.Item;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BlackHoleArtifact extends ArtifactCollectible {

    public BlackHoleArtifact() {
        super("Buraco Negro", CollectibleRarity.EPIC, new ArrayList<>(), 1726526700284L);

        setIcon(Item.of(Material.OBSIDIAN));
        setLore(Arrays.asList("§7Faça surgir um", "§7buraco negro no mapa."));
    }


    @Override
    public void handle(Player host) {
        if (hasCooldown(host)) return;

        int strands = 8;
        int particles = 30;
        float radius = 6;
        float curve = 12;
        double rotation = Math.PI / 4;

        Location location = host.getLocation();
        host.getWorld().playSound(location, Sound.PORTAL_TRAVEL, 1.5f, 0.5f);

        try {
            ParticleEffect.SMOKE_LARGE.display(0.8f, 1.5f, 0.8f, 0.15f, 30, location);
            ParticleEffect.SPELL_WITCH.display(0.6f, 0.6f, 0.6f, 0.1f, 25, location);
            ParticleEffect.PORTAL.display(0.5f, 1.0f, 0.5f, 0.1f, 20, location);
            ParticleEffect.SLIME.display(0.4f, 0.4f, 0.4f, 0.05f, 15, location);
            ParticleEffect.TOWN_AURA.display(0.3f, 0.5f, 0.3f, 0.05f, 12, location);
        } catch (Exception e) {
            host.getWorld().playEffect(location, Effect.SMOKE, 10);
        }

        List<Player> nearByEntities = location.getWorld().getNearbyEntities(location, 6, 4, 6)
                .stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toList());

        for (int i = 1; i <= strands; i++) {
            for (int j = 1; j <= particles; j++) {
                float ratio = (float) j / particles;
                double angle = curve * ratio * 2 * Math.PI / strands + (2 * Math.PI * i / strands) + rotation;
                double x = Math.cos(angle) * ratio * radius;
                double z = Math.sin(angle) * ratio * radius;

                location.add(x, 0, z);

                try {
                    ParticleEffect.SMOKE_LARGE.display((float) Math.random() * 2.0F, 1.5F, (float) Math.random() * 2.0F, 0.2F, 10, location);
                    ParticleEffect.SPELL_WITCH.display(0.3f, 0.3f, 0.3f, 0.1f, 5, location);
                    ParticleEffect.PORTAL.display(0.2f, 0.2f, 0.2f, 0.1f, 5, location);
                } catch (Exception ex) {
                    location.getWorld().playEffect(location, Effect.SMOKE, 0);
                }

                location.subtract(x, 0, z);
            }
        }

        nearByEntities.forEach(player -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 0));
            player.playSound(player.getLocation(), Sound.PORTAL_TRAVEL, 1.5f, 2);
        });

        setCooldown(host, TimeUnit.SECONDS.toMillis(15));
    }
}

