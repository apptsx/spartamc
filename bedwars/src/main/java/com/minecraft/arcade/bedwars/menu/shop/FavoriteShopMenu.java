package com.minecraft.arcade.bedwars.menu.shop;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.enums.BedShop;
import com.minecraft.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import com.minecraft.core.member.list.bedwars.objects.menu.item.BedItem;
import com.minecraft.core.member.list.bedwars.objects.menu.type.BedMenuType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FavoriteShopMenu extends Menu {

    private final Arena arena;

    private final BedMember member;
    private final BedWarsItem item;

    public FavoriteShopMenu(Player player, BedWarsItem item, Menu last) {
        super(player, "Selecionando espaço...", last, 6, 21);

        User user = (User) User.of(player.getUniqueId());

        this.arena = user.getArena();
        this.member = user.getMember();

        this.item = item;
    }

    @Override
    public void handle() {
        clear();

        List<BedItem> list = member.getMenu(BedMenuType.FAVORITE).getItemList();

        /* Adicionando item selecionado */
        List<String> lore = new ArrayList<>();

        lore.add("");
        lore.addAll(item.getLore());
        lore.addAll(Arrays.asList("", "§eAdicionando..."));

        addItem(4, Item.fromStack(item.getStack())
                .name("§c" + item.getName())
                .lore(lore));

        /* Adicionando Espaços Vázios */
        int slot = 19, last = slot;
        for (int i = 0; i < getMaxItems(); i++) {
            int finalSlot = slot;
            BedItem item = list.stream().filter(root -> root.getSlot() == finalSlot).findFirst().orElse(null);

            if (item != null) {
                BedWarsItem root = BedWarsItem.of(item.getName());

                if (root != null && !root.isAvailableForMode(arena.getType())) {
                    addItem(slot, buildIncompatibleItem());
                } else
                    addItem(slot, buildEmptySpace());
            } else
                addItem(slot, buildEmptySpace());

            slot++;
            if (slot == (last + 7)) {
                slot += 2;
                last = slot;
            }
        }

        // Adicionando os itens favoritos do jogador
        for (BedItem root : list) {
            BedWarsItem item = BedWarsItem.of(root.getName());

            if (item == null) continue;

            if (!item.isAvailableForMode(arena.getType()))
                addItem(root.getSlot(), buildIncompatibleItem());
            else
                addItem(root.getSlot(), Item.fromStack(item.getStack())
                        .name("§a" + item.getName())
                        .lore("§7Você não pode mexer."));
        }

        if (isReturnable())
            addBackButton(45);

        display();
    }

    protected Item buildEmptySpace() {
        return Item.of(Material.STAINED_GLASS_PANE, 14, "§cEspaço vázio!",
                        "§eClique para selecionar!")
                .click(event -> {
                    member.setFavoriteItem(event.getSlot(), item);

                    sound(MenuSound.DONE);

                    new ShopMenu(getPlayer(), BedShop.FAVORITE).handle();
                });
    }

    protected Item buildIncompatibleItem() {
        return Item.of(Material.STAINED_GLASS_PANE, 14, "§cItem incompatível!",
                "§7Este espaço está ocupado",
                "§7por um item incompatível.");
    }
}
