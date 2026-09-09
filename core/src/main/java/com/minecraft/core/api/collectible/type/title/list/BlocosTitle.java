package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.api.collectible.type.title.TitleCategory;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.backend.data.list.api.WoolsPlacedData;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

public class BlocosTitle extends TitleCollectible {

    public BlocosTitle() {
        super("Blocos coloridos", TitleCategory.GERAL, CollectibleRarity.RARE, Arrays.asList(RankType.VIP), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Mostra quantos blocos",
                "§7coloridos você colocou!"
        ));
    }

    @Override
    public String getTitle() {
        return "§eVocê colocou: §b0 §eblocos coloridos!";
    }

    @Override
    public String getDynamicTitle(Player player) {
        UUID uuid = player.getUniqueId();
        long count = WoolsPlacedData.getInstance().getWools(uuid);
        return "§b" + player.getName() + " §ecolocou: §b" + count + " §eblocos coloridos!";
    }
}