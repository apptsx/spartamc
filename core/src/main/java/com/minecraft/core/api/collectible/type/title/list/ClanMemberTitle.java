package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.api.collectible.type.title.TitleCategory;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ClanMemberTitle extends TitleCollectible {

    public ClanMemberTitle() {
        super("Membro de clan", TitleCategory.GERAL, CollectibleRarity.RARE, Arrays.asList(RankType.VIP), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Mostra o clan que você participa!"
        ));
    }

    @Override
    public String getTitle() {
        return "§eMembro de: §cNenhum";
    }

    @Override
    public String getDynamicTitle(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null || !account.hasClan()) {
            return "§eMembro de: §cNenhum";
        }

        String clanTag = account.getClanTag();
        return "§eMembro de: " + clanTag;
    }
}