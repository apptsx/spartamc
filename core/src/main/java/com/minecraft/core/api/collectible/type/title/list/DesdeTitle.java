package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.api.collectible.type.title.TitleCategory;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.util.list.DateUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class DesdeTitle extends TitleCollectible {

    public DesdeTitle() {
        super("Desde", TitleCategory.GERAL, CollectibleRarity.COMUM, Arrays.asList(RankType.MEMBER), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Mostra há quanto tempo você",
                "§7está no servidor!"
        ));
    }

    @Override
    public String getTitle() {
        return "§eDesde: §bData";
    }

    @Override
    public String getDynamicTitle(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) {
            return getTitle();
        }
        String date = DateUtil.getDate(account.getContext().getCreatedAt());
        return "§eDesde: §b" + date;
    }
}