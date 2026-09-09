package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.hat.HatList;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.BedDestroyCollectible;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bat;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class Ghost extends BedDestroyCollectible {

    private final Map<Bat, ArmorStand> ghostMap;

    public Ghost() {
        super(CollectibleRarity.MYTHICAL, "Fantasmas",
                Item.of(Material.SKULL_ITEM, 3).skullByBase64(HatList.FANTASMA.getCosmetic().getValue()),
                1729792699944L);

        setLore("§7Solte fantasmas assustadores!");
        setRanks(RankType.MAX);

        this.ghostMap = new HashMap<>();
    }

    @Override
    public void execute(Location location) {
        World world = location.getWorld();

        for (int i = 0; i < 6; ++i) {
            Bat bat = world.spawn(location.clone().add(0, 0.5, 0), Bat.class);

            ArmorStand ghost = bat.getWorld().spawn(bat.getLocation(), ArmorStand.class);

            ghost.setHelmet(getIcon());

            ghost.setSmall(true);
            ghost.setGravity(false);
            ghost.setVisible(false);

            bat.setPassenger(ghost);
            bat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));

            this.ghostMap.put(bat, ghost);
        }

        Core.getPlatform().runSync(() -> {
            ghostMap.forEach((bat, stand) -> {
                bat.remove();
                stand.remove();
            });

            ghostMap.clear();
        }, 20 * 3);
    }
}
