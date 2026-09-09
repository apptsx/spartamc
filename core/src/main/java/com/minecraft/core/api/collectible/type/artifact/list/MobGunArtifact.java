package com.minecraft.core.api.collectible.type.artifact.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.core.api.collectible.util.particle.EnumSound;
import com.minecraft.core.api.item.Item;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class MobGunArtifact extends ArtifactCollectible {

    public MobGunArtifact() {
        super("Mob Gun", CollectibleRarity.EPIC, new ArrayList<>(), 1726603463478L);

        setIcon(Item.of(Material.ARROW));
        setLore(Collections.singletonList("§7Atira, atira!"));
    }

    @Override
    public void handle(Player host) {
        if (hasCooldown(host)) return;

        new BukkitRunnable() {
            private final List<Entity> entity = new ArrayList<>();

            private int time = 8;
            private int type = 0;

            @Override
            public void run() {
                if (time == -10) {
                    entity.forEach(Entity::remove);
                    entity.clear();
                    return;
                }
                if (time > 0) {
                    EnumSound.CHICKEN_EGG_POP.play(host, 1.0f, 1.0f);

                    EntityType[] entityTypes = new EntityType[]{EntityType.PIG, EntityType.VILLAGER, EntityType.OCELOT, EntityType.COW,
                            EntityType.SPIDER, EntityType.SQUID, EntityType.CHICKEN, EntityType.SHEEP, EntityType.WOLF, EntityType.WITCH,
                            EntityType.BLAZE, EntityType.HORSE, EntityType.ZOMBIE, EntityType.SKELETON, EntityType.MUSHROOM_COW, EntityType.CAVE_SPIDER,
                            EntityType.PIG_ZOMBIE};

                    EntityType entityType = entityTypes[ThreadLocalRandom.current().nextInt(type + 1)];
                    Entity mob = host.getWorld().spawnEntity(host.getEyeLocation().add(0.0D, -0.5D, 0.0D), entityType);
                    if (entityType == EntityType.VILLAGER) {
                        ((Villager) mob).setProfession(Villager.Profession.BUTCHER);
                    } else if (entityType == EntityType.SHEEP) {
                        ((Sheep) mob).setColor(DyeColor.WHITE);
                    } else if (entityType == EntityType.HORSE) {
                        ((Horse) mob).setColor(Horse.Color.BROWN);
                    }

                    mob.setPassenger(null);
                    mob.setVelocity(new Vector(mob.getLocation()
                            .getDirection().getX() / 2.0D, 0.0D, mob.getLocation().getDirection().getZ() / 2.0D));
                    entity.add(mob);

                    type++;
                }
                --time;
            }
        }.runTaskTimer(Core.getJavaPlugin(), 0, 13);

        setCooldown(host, TimeUnit.MINUTES.toMillis(1));
    }
}
