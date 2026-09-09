package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.api.collectible.type.title.TitleCategory;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class SpartaCupTitle extends TitleCollectible {

    public SpartaCupTitle() {
        super(com.minecraft.core.Constant.SERVER_NAME + " Cup", TitleCategory.HUNGER_GAMES, CollectibleRarity.EPIC, Arrays.asList(RankType.ADMIN), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Vencedor da " + com.minecraft.core.Constant.SERVER_NAME + " Cup!"
        ));
    }

    @Override
    public String getTitle() {
        return "§6§l❂ §eVencedor §6" + com.minecraft.core.Constant.SERVER_NAME + " §eCup §6§l❂";
    }
}