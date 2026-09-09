package com.minecraft.core.api.collectible.type.particle.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.particle.ParticleCollectible;
import com.minecraft.core.api.collectible.util.particle.ParticleEffect;
import com.minecraft.core.api.item.Item;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SmokeParticle extends ParticleCollectible {

    public SmokeParticle() {
        super("Fumaça", CollectibleRarity.RARE, Arrays.asList(RankType.MEMBER), System.currentTimeMillis());
        
        setIcon(Item.of(Material.COAL));
        setLore(Arrays.asList(
                "§7Partículas de fumaça",
                "§7ao seu redor."
        ));
    }

    @Override
    public void display(Player player, com.minecraft.core.api.collectible.type.particle.ParticleType type) {
        Location loc = player.getLocation();

        switch (type) {
            case SIMPLES:
                player.getWorld().playEffect(loc, org.bukkit.Effect.SMOKE, 2);
                break;
            case ESPIRAL:
            case ESPIRAL_DUPLO:
            case CIRCULAR:
                player.getWorld().playEffect(loc, org.bukkit.Effect.SMOKE, 5);
                break;
        }
    }

    @Override
    public long getInterval() {
        return 12L;
    }
}