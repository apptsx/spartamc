package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.api.collectible.type.title.TitleCategory;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class StaffDestaqueTitle extends TitleCollectible {

    public StaffDestaqueTitle() {
        super("Staff Destaque", TitleCategory.STAFF, CollectibleRarity.EPIC, Arrays.asList(RankType.ADMIN), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Equipe de Destaque!"
        ));
    }

    @Override
    public String getTitle() {
        return "§4§l❂ STAFF DESTAQUE ❂";
    }
}