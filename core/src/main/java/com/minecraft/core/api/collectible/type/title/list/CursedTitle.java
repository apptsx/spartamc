package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.api.collectible.type.title.TitleCategory;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class CursedTitle extends TitleCollectible {

    public CursedTitle() {
        super("Cursed", TitleCategory.STAFF, CollectibleRarity.EPIC, Arrays.asList(RankType.CHEFE), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§bTitulo especial"
        ));
    }

    @Override
    public String getTitle() {
        return "§4§l友 §c§lCURSED §4§l友";
    }

    @Override
    public String getDynamicTitle(org.bukkit.entity.Player player) {
        long frame = System.currentTimeMillis() / 300;
        
        String c1 = "§4", c2 = "§c", c3 = "§4";
        
        int phase = (int) (frame % 6);
        switch (phase) {
            case 0:
                c1 = "§4"; c2 = "§c"; c3 = "§4";
                break;
            case 1:
                c1 = "§c"; c2 = "§f"; c3 = "§c";
                break;
            case 2:
                c1 = "§f"; c2 = "§c"; c3 = "§f";
                break;
            case 3:
                c1 = "§c"; c2 = "§4"; c3 = "§c";
                break;
            case 4:
                c1 = "§4"; c2 = "§c"; c3 = "§4";
                break;
            case 5:
                c1 = "§c"; c2 = "§f"; c3 = "§c";
                break;
        }

        return c1 + "§l友 " + c2 + "§lCURSED " + c3 + "§l友";
    }
}