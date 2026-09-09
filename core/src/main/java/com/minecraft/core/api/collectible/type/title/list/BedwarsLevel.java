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

public class BedwarsLevel extends TitleCollectible {

    public BedwarsLevel() {
        super("Nível Bedwars", TitleCategory.BEDWARS, CollectibleRarity.COMUM, Arrays.asList(com.minecraft.core.account.context.objects.rank.type.RankType.MEMBER), System.currentTimeMillis());

        setIcon(Item.of(Material.NAME_TAG));
        setLore(Arrays.asList(
                "§7Mostra seu nível atual no BedWars",
                "§7na forma de título."
        ));
    }

    @Override
    public String getTitle() {
        return "§eNível Bedwars: §7✩0";
    }

    @Override
    public String getDynamicTitle(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return getTitle();

        BedMember member = (BedMember) account.loadMember(ServerType.BEDWARS);
        if (member == null) return getTitle();

        int level = member.getLevel();
        String raw = member.getLevelId(level);
        String display = raw.replace("[", "").replace("]", "");
        return "§eNível Bedwars: " + display;
    }
}
