package com.minecraft.core.member.list.duels.metadata.objects.collectible.objects.list.kill;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.DuelCollectible;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.objects.type.DuelCollectibleType;
import org.bukkit.Material;

public class Random extends DuelCollectible {

    public Random() {
        super(DuelCollectibleType.EFFECT_KILL, null, "Aleatório", Item.of(Material.NAME_TAG), System.currentTimeMillis());

        setLore("§7Selecione um colecionável", "§7aleatório que você possui.");
    }
}
