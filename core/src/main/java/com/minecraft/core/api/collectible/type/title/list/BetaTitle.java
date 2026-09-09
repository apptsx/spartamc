package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.api.collectible.type.title.TitleCategory;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class BetaTitle extends TitleCollectible {

    public BetaTitle() {
        super("Desde o Início", TitleCategory.GERAL, CollectibleRarity.COMUM, Arrays.asList(RankType.BETA), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Você está aqui desde o começo!"
        ));
    }

    @Override
    public String getTitle() {
        return "§1§l ✪ DESDE O INICIO ✪";
    }
}