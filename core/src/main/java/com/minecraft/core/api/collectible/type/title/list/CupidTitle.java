package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.api.collectible.type.title.TitleCategory;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CupidTitle extends TitleCollectible {

    public CupidTitle() {
        super("Cupido", TitleCategory.GERAL, CollectibleRarity.EPIC, Arrays.asList(RankType.ADMIN), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Mostra que você arrasou corações!"
        ));
    }

    @Override
    public String getTitle() {
        return "§4§l♥ §cVocê§e arrasou corações! §4§l♥";
    }

    @Override
    public String getDynamicTitle(Player player) {
        return "§4§l♥ §c" + player.getName() + "§e arrasou corações! §4§l♥";
    }
}