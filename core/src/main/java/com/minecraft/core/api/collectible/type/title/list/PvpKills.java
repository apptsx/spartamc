package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCategory;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.pvp.PvPMember;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class PvpKills extends TitleCollectible {

    public PvpKills() {
        super("Kills PvP", TitleCategory.PVP, CollectibleRarity.COMUM, Arrays.asList(com.minecraft.core.account.context.objects.rank.type.RankType.MEMBER), System.currentTimeMillis());

        setIcon(Item.of(Material.NAME_TAG));
        setLore(Arrays.asList(
                "§7Mostra seu total de kills no PvP."
        ));
    }

    @Override
    public String getTitle() {
        return "§eTotal de kills: §b0 §8(PvP)";
    }

    @Override
    public String getDynamicTitle(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return getTitle();

        PvPMember member = (PvPMember) account.loadMember(ServerType.PVP);
        if (member == null) return getTitle();

        return "§eTotal de kills: §b" + member.getArenaStats().getKills() + " §8(PvP)";
    }
}
