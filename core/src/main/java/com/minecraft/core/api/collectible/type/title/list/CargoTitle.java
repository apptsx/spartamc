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

public class CargoTitle extends TitleCollectible {

    public CargoTitle() {
        super("Cargo", TitleCategory.GERAL, CollectibleRarity.RARE, Arrays.asList(RankType.VIP), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Mostra seu maior rank!"
        ));
    }

    @Override
    public String getTitle() {
        return "§eMaior rank: §7Nenhum";
    }

    @Override
    public String getDynamicTitle(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) {
            return getTitle();
        }
        RankType rank = account.getRankType();
        return "§eMaior rank: " + rank.getColoredName();
    }
}