package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.api.collectible.type.title.TitleCategory;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SpartaLeagueBedwars extends TitleCollectible {

    public SpartaLeagueBedwars() {
        super(com.minecraft.core.Constant.SERVER_NAME + " League Bedwars", TitleCategory.BEDWARS, CollectibleRarity.EPIC, Arrays.asList(RankType.ADMIN), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Exclusivo para §6§lCHAMPION"
        ));
    }

    @Override
    public String getTitle() {
        return "§e§l✪ §6Vencedor " + com.minecraft.core.Constant.SERVER_NAME + " League §b(BW) §e§l✪";
    }

    @Override
    public String getDynamicTitle(Player player) {
        return getTitle();
    }
}