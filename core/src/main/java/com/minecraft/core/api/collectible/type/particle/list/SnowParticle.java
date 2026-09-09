package com.minecraft.core.api.collectible.type.particle.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.particle.ParticleCollectible;
import com.minecraft.core.api.collectible.util.particle.ParticleEffect;
import com.minecraft.core.api.item.Item;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SnowParticle extends ParticleCollectible {

    public SnowParticle() {
        super("Neve", CollectibleRarity.EPIC, Arrays.asList(RankType.VIP), System.currentTimeMillis());
        
        setIcon(Item.of(Material.SNOW_BALL));
        setLore(Arrays.asList(
                "§7Partículas de neve ao redor!"
        ));
    }

    @Override
    public void display(Player player, com.minecraft.core.api.collectible.type.particle.ParticleType type) {
        Location loc = player.getLocation();

        switch (type) {
            case SIMPLES:
                player.getWorld().playEffect(loc, Effect.SNOWBALL_BREAK, 2);
                break;
            case ESPIRAL:
            case ESPIRAL_DUPLO:
            case CIRCULAR:
                player.getWorld().playEffect(loc, Effect.SNOWBALL_BREAK, 5);
                break;
        }
    }

    @Override
    public long getInterval() {
        return 8L;
    }
}