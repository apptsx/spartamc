package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCategory;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class PvpWins extends TitleCollectible {

    public PvpWins() {
        super("Wins PvP", TitleCategory.PVP, CollectibleRarity.COMUM, Arrays.asList(com.minecraft.core.account.context.objects.rank.type.RankType.MEMBER), System.currentTimeMillis());

        setIcon(Item.of(Material.NAME_TAG));
        setLore(Arrays.asList(
                "§7Mostra seu total de vitórias no PvP."
        ));
    }

    @Override
    public String getTitle() {
        return "§eTotal de vitórias: §b0 §8(PvP)";
    }

    @Override
    public String getDynamicTitle(Player player) {
        return getTitle();
    }
}
