package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.api.collectible.type.title.TitleCategory;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.medal.Medal;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class MedalTitle extends TitleCollectible {

    public MedalTitle() {
        super("Medalha", TitleCategory.GERAL, CollectibleRarity.COMUM, Arrays.asList(RankType.VIP), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Mostra sua Medalha atual",
                "§7como título!"
        ));
    }

    @Override
    public String getTitle() {
        return "§7Medalha";
    }

    @Override
    public String getDynamicTitle(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) {
            return getTitle();
        }
        Medal medal = account.getMedal();
        if (medal == null || medal == Medal.NONE) {
            return "§7Sem Medalha";
        }
        return medal.getColoredSymbol();
    }
}