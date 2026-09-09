package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.api.collectible.type.title.TitleCategory;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class GrammyTitle extends TitleCollectible {

    public GrammyTitle() {
        super("Grammy Awards", TitleCategory.GERAL, CollectibleRarity.EPIC, Arrays.asList(RankType.BETA), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Prêmio Grammy!"
        ));
    }

    @Override
    public String getTitle() {
        return "§6§l♬ §e§lGRAMMY AWARDS §6§l♬";
    }
}