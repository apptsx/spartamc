package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.api.collectible.type.title.TitleCategory;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class StaffPunishsTopTitle extends TitleCollectible {

    public StaffPunishsTopTitle() {
        super("Top 1 Punições", TitleCategory.STAFF, CollectibleRarity.EPIC, Arrays.asList(RankType.ADMIN), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Mostra que você é o TOP 1 em punições!"
        ));
    }

    @Override
    public String getTitle() {
        return "§1§l✹ §9§lTOP §1§l1 §9§lPUNIÇÕES §1§l✹";
    }
}