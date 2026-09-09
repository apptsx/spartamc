package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCategory;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class BedwarsKills extends TitleCollectible {

    public BedwarsKills() {
        super("Kills Bedwars", TitleCategory.BEDWARS, CollectibleRarity.COMUM, Arrays.asList(com.minecraft.core.account.context.objects.rank.type.RankType.MEMBER), System.currentTimeMillis());

        setIcon(Item.of(Material.NAME_TAG));
        setLore(Arrays.asList(
                "§7Mostra seu total de kills no BedWars."
        ));
    }

    @Override
    public String getTitle() {
        return "§eTotal de kills: §b0 §8(Bedwars)";
    }

    @Override
    public String getDynamicTitle(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return getTitle();

        BedMember member = (BedMember) account.loadMember(ServerType.BEDWARS);
        if (member == null) return getTitle();

        return "§eTotal de kills: §b" + member.getTotalKills() + " §8(Bedwars)";
    }
}
