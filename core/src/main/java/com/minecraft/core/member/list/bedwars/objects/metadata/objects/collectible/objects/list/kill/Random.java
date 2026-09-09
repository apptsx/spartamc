package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.kill;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.BedCollectible;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import org.bukkit.Material;

public class Random extends BedCollectible {

    public Random() {
        super(BedCollectibleType.FINAL_KILL, null, "Aleatório", Item.of(Material.NAME_TAG), System.currentTimeMillis());

        setLore("§7Selecione um colecionável", "§7aleatório que você possui.");
    }
}
