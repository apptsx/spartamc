package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.SellerCollectible;
import org.bukkit.Material;

public class Mirror extends SellerCollectible {

    public Mirror() {
        super(CollectibleRarity.MYTHICAL, "Espelho", Item.of(Material.THIN_GLASS), System.currentTimeMillis());

        setLore("§7As skins serão", "§7uma cópia sua!");
        setRanks(RankType.VIP);
    }
}
