package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.list;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.BedDestroyCollectible;
import org.bukkit.Material;

public class None extends BedDestroyCollectible {

    public None() {
        super(null, "Nenhuma", Item.of(Material.BARRIER), System.currentTimeMillis());
    }
}
