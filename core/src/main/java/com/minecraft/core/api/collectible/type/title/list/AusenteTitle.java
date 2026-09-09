package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.api.collectible.type.title.TitleCategory;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class AusenteTitle extends TitleCollectible {

    public AusenteTitle() {
        super("Ausente", TitleCategory.GERAL, CollectibleRarity.COMUM, Arrays.asList(RankType.VIP), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Mostra que você está ausente",
                "§7Não perturbe!"
        ));
    }

    @Override
    public String getTitle() {
        return "§cAUSENTE";
    }

    @Override
    public String getDynamicTitle(Player player) {
        long time = System.currentTimeMillis();
        boolean evenSecond = (time / 1000) % 2 == 0;
        return evenSecond ? "§cZzZ" : "§czZz";
    }
}
