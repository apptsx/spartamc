package com.minecraft.core.member.list.duels.metadata.objects.collectible.objects.list.kill;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.DuelCollectible;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.objects.type.DuelCollectibleType;
import org.bukkit.Material;

public class None extends DuelCollectible {

    public None() {
        super(DuelCollectibleType.EFFECT_KILL, null, "Nenhuma", Item.of(Material.BARRIER), System.currentTimeMillis());
    }
}
