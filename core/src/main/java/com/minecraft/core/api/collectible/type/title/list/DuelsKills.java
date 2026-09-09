package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCategory;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.member.list.duels.DuelMember;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class DuelsKills extends TitleCollectible {

    public DuelsKills() {
        super("Kills Duels", TitleCategory.DUELS, CollectibleRarity.COMUM, Arrays.asList(com.minecraft.core.account.context.objects.rank.type.RankType.MEMBER), System.currentTimeMillis());

        setIcon(Item.of(Material.NAME_TAG));
        setLore(Arrays.asList(
                "§7Mostra seu total de kills nos Duels."
        ));
    }

    @Override
    public String getTitle() {
        return "§eTotal de kills: §b0 §8(Duels)";
    }

    @Override
    public String getDynamicTitle(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return getTitle();

        DuelMember member = (DuelMember) account.loadMember(ServerType.DUELS);
        if (member == null) return getTitle();

        int total = 0;
        for (ArcadeCategory arcade : ArcadeCategory.of(ServerType.DUELS)) {
            total += member.getStats(arcade).getKills();
        }
        return "§eTotal de kills: §b" + total + " §8(Duels)";
    }
}
