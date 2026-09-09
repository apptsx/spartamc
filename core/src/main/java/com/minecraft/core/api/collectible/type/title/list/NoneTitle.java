package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.api.collectible.type.title.TitleCategory;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collections;

public class NoneTitle extends TitleCollectible {

    public NoneTitle() {
        super("Nenhum", TitleCategory.GERAL, CollectibleRarity.COMUM, Collections.emptyList(), System.currentTimeMillis());

        setIcon(Item.of(Material.BARRIER));
        setLore(Arrays.asList(
                "§7Nenhum título ativo.",
                "§7Clique para remover seu título."
        ));
    }

    @Override
    public boolean hasAccess(RankType rank) {
        return true;
    }

    @Override
    public String getTitle() {
        return " ";
    }
}