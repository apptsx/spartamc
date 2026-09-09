package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCategory;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.member.list.hungergames.HungerGamesMember;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class HgWinstreak extends TitleCollectible {

    public HgWinstreak() {
        super("Winstreak HungerGames", TitleCategory.HUNGER_GAMES, CollectibleRarity.COMUM, Arrays.asList(com.minecraft.core.account.context.objects.rank.type.RankType.MEMBER), System.currentTimeMillis());

        setIcon(Item.of(Material.NAME_TAG));
        setLore(Arrays.asList(
                "§7Mostra sua winstreak atual no Hunger Games."
        ));
    }

    @Override
    public String getTitle() {
        return "§eWinstreak: §b0 §8(HG)";
    }

    @Override
    public String getDynamicTitle(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return getTitle();

        HungerGamesMember member = (HungerGamesMember) account.loadMember(ServerType.HUNGERGAMES);
        if (member == null) return getTitle();

        return "§eWinstreak: §b" + member.getStats(ArcadeCategory.HUNGERGAMES).getWinStreak() + " §8(HG)";
    }
}
