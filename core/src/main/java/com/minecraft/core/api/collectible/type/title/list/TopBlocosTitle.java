package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCategory;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class TopBlocosTitle extends TitleCollectible {

    public TopBlocosTitle() {
        super("Top Blocos", TitleCategory.GERAL, CollectibleRarity.EPIC, Arrays.asList(RankType.VIP), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Mostra quantos blocos",
                "§7coloridos você colocou!"
        ));
    }

    @Override
    public String getTitle() {
        return "§e<nick> §b0 Blocos coloridos!";
    }

    @Override
    public String getDynamicTitle(Player player) {
        long count = com.minecraft.core.backend.data.list.api.WoolsPlacedData.getInstance().getWools(player.getUniqueId());
        return "§e" + player.getName() + " §bcolocou: §b" + count + " Blocos coloridos!";
    }
}
