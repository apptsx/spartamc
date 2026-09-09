package com.minecraft.lobby.menu.navigation.bedwars;

import com.minecraft.core.Core;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class BedVersusMenu extends Menu {

    public BedVersusMenu(Player player) {
        super(player, ServerType.BEDWARS.getName() + " 1v1/2v2/3v3/4v4", 3);
    }

    @Override
    public void handle() {
        clear();

        List<ArcadeCategory> list = ArcadeCategory.list(arcade -> arcade.getServer().equals(ServerType.BEDWARS)
                && arcade.name().contains("VERSUS"));

        if (list.isEmpty())
            addErrorButton("§cNão há modos de versus...");
        else {
            int slot = 10;
            for (ArcadeCategory arcade : list) {

                int finalSlot = slot;
                addItem(slot, Item.of(Material.getMaterial(arcade.getIconId()), "§a" + arcade.getName(),
                                "§7" + Util.formatNumber(Core.getArcadeData().getOnlinePlayers(arcade)) + " jogando agora!")
                        .amount(arcade.getSlots().get(0).ordinal())
                        .updater(view -> {
                            if (!view.getTitle().equalsIgnoreCase(getTitle())) return;

                            Item item = getContents().get(finalSlot);

                            if (item != null) {
                                List<String> lore = item.getMeta().getLore();

                                lore.set(0, "§7" + Util.formatNumber(Core.getArcadeData().getOnlinePlayers(arcade)) + " jogando agora!");

                                item.lore(lore);

                                view.setItem(finalSlot, item);
                                getPlayer().updateInventory();
                            }
                        })
                        .click(event -> {
                            close();
                            sound(MenuSound.PAGINATED);

                            new BedNavigationMenu(getPlayer(), arcade, this).handle();
                        }));

                slot += 2;
            }
        }

        display();
    }
}
