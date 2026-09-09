package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.BedCollectible;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import org.bukkit.Location;

public class BedDestroyCollectible extends BedCollectible {

    public BedDestroyCollectible(CollectibleRarity rarity, String name, Item icon, long createdAt) {
        super(BedCollectibleType.BED_DESTRUCTION, rarity, name, icon, createdAt);
    }

    public void execute(Location location) {

    }

    public boolean isNotEqual(Class<? extends BedDestroyCollectible> sellerClass) {
        return !sellerClass.isAssignableFrom(getClass());
    }

    public boolean isValid() {
        return !getClass().getSimpleName().equalsIgnoreCase("Nenhuma");
    }
}
