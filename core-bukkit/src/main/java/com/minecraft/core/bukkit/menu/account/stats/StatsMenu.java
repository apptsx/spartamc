package com.minecraft.core.bukkit.menu.account.stats;

import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.stats.BedWarsStatsMenu;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class StatsMenu extends Menu {

    private final Account target;

    public StatsMenu(Player player, Account target, Menu last) {
        super(player, "Estatísticas", last, 5);

        this.target = target;
    }

    @Override
    public void handle() {
        clear();

        int slot = 10;
        for (ServerType server : ServerType.values()) {
            if (!server.isShown()) continue;
            
            // Não mostrar HGMIX, CHAMPION e EVENT
            if (server.equals(ServerType.HGMIX) || server.equals(ServerType.CHAMPION) || server.equals(ServerType.EVENT) || server.equals(ServerType.PARTY) || server.equals(ServerType.EGGWARS) || server.equals(ServerType.SKYWARS)) continue;

            addItem(slot, Item.of(Material.getMaterial(server.getIconId()), "§a" + server.getName(), "§eClique para ver!")
                    .flags(ItemFlag.values())
                    .click(event -> {
                        sound(MenuSound.PAGINATED);

                        if (server.equals(ServerType.BEDWARS))
                            new BedWarsStatsMenu(getPlayer(), this).handle();
                        else
                            new StatsInfoMenu(getPlayer(), target, server, this).handle();
                    }));
            slot++;
        }

        if (isReturnable())
            addBackButton();
        else
            addCloseButton();

        display();
    }
}