package com.minecraft.core.api.collectible.type.artifact.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.core.api.collectible.util.particle.EnumSound;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CookieFortuneArtifact extends ArtifactCollectible {

    public CookieFortuneArtifact() {
        super("Fortuna de Cookies", CollectibleRarity.COMUM, new ArrayList<>(), 1726599947258L);

        setIcon(Item.of(Material.COOKIE));
        setLore(Collections.singletonList("§7Cookies, Cookies, Cookies!"));
    }

    @Override
    public void handle(Player host) {
        if (hasCooldown(host)) return;

        new BukkitRunnable() {
            private final List<org.bukkit.entity.Item> items = new ArrayList<>();

            private int time = 20;

            @Override
            public void run() {
                if (time == -30 || !host.isOnline()) {
                    items.forEach(org.bukkit.entity.Item::remove);
                    items.clear();

                    cancel();
                    return;
                }

                if (time > 0) {
                    EnumSound.CHICKEN_EGG_POP.play(host.getWorld(), host.getLocation(), 1.0F, 1.0F);

                    org.bukkit.entity.Item item = host.getWorld().dropItem(host.getLocation().clone().add(0.5, 1.7, 0.5), new Item(Material.COOKIE));

                    item.setPickupDelay(999999999);
                    items.add(item);
                }

                --time;
            }
        }.runTaskTimer(Core.getJavaPlugin(), 0, 2);

        setCooldown(host, TimeUnit.SECONDS.toMillis(30));
    }
}
