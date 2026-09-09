package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCategory;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class StarTitle extends TitleCollectible {

    public StarTitle() {
        super("Velocidade", TitleCategory.GERAL, CollectibleRarity.EPIC, Arrays.asList(RankType.BOOSTER), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Eu sou a velocidade..."
        ));
    }

    @Override
    public String getTitle() {
        return "§d✮ Eu sou a §d§lvelocidade... §d✮";
    }
}
