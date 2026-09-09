package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.BedDestroyCollectible;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import org.bukkit.Location;
import org.bukkit.Material;

public class Rocket extends BedDestroyCollectible {

    public Rocket() {
        super(CollectibleRarity.RARE, "Foguetinho", Item.of(Material.FIREWORK), 1729792699944L);

        setLore("§7Solte um foguetão!");
        setRanks(RankType.VIP);
    }

    @Override
    public void execute(Location location) {
        location = location.clone().add(0.5, 0.5, 0.5);

        for (int i = 0; i < 5; i++)
            BukkitUtil.launchFirework(location);
    }
}
