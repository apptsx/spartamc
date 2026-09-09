package com.minecraft.lobby.menu.shop;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class MedalhasShopMenu extends Menu {

    public MedalhasShopMenu(Player player, Menu last) {
        super(player, "Loja: Medalhas", last, 6);
    }

    @Override
    public void handle() {
        clear();

        // Fileira 1 (igual a principal)
        addItem(1, Item.of(Material.EMERALD, "§aRanks")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new ShopMenu(getPlayer()).handle();
                }));

        addItem(2, Item.of(Material.INK_SACK, 5, "§5Max§6+")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new MaxPlusShopMenu(getPlayer(), this).handle();
                }));

        addItem(3, Item.of(Material.DOUBLE_PLANT, 0, "§aMedalhas"));

        addItem(7, Item.of(Material.BOOK, "§aComo pagar?")
                .flags(ItemFlag.values())
                .lore("§7Ao escolher um produto", "§7voce recebera um mapa", "§7com o QR CODE do §aPix.", "", "§7Basta escanear e aguardar!"));

        addItem(8, Item.of(Material.PAPER, "§aMeus pedidos")
                .flags(ItemFlag.values())
                .lore("§eClique para ver!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new MeusPedidosMenu(getPlayer(), this).handle();
                }));

        // Fileira 2: Vidro verde no slot 12 (sob Medalhas)
        for (int i = 9; i <= 17; i++) {
            if (i == 12) {
                addItem(i, Item.of(Material.STAINED_GLASS_PANE, 5, " "));
            } else {
                addItem(i, Item.of(Material.STAINED_GLASS_PANE, 7, " "));
            }
        }

        // Fileira 4: Slot 31 - Sem estoque
        addItem(31, Item.of(Material.STAINED_GLASS_PANE, 14, "§cSem estoque"));

        display();
    }
}
