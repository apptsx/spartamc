package com.minecraft.core.api.collectible.type.particle.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.particle.ParticleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class HeartParticle extends ParticleCollectible {

    public HeartParticle() {
        super("Corações", CollectibleRarity.RARE, Arrays.asList(RankType.MEMBER), System.currentTimeMillis());
        
        setIcon(Item.of(Material.RED_ROSE));
        setLore(Arrays.asList(
                "§7Corações flutuando",
                "§7ao seu redor."
        ));
    }

    @Override
    public void display(Player player, com.minecraft.core.api.collectible.type.particle.ParticleType type) {
        Location loc = player.getLocation(); // Usar a localização do jogador como base

        switch (type) {
            case SIMPLES:
                renderSimple(player, loc, Effect.HEART, 2);
                break;
            case ESPIRAL:
                renderSpiral(player, loc, Effect.HEART, 2.0, 12);
                break;
            case ESPIRAL_DUPLO:
                renderDoubleSpiral(player, loc, Effect.HEART, 2.0, 12);
                break;
            case CIRCULAR:
                renderCircular(player, loc, Effect.HEART, 2.0, 20);
                break;
        }
    }

    @Override
    public long getInterval() {
        return 10L;
    }
}
