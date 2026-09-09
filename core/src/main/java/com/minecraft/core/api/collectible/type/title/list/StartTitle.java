package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCategory;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class StartTitle extends TitleCollectible {

    public StartTitle() {
        super("Um Novo Começo", TitleCategory.GERAL, CollectibleRarity.EPIC, Arrays.asList(RankType.BETA), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Um novo começo!"
        ));
    }

    @Override
    public String getTitle() {
        return "§b§l❃ §3Um novo começo! §b§l❃";
    }
}
