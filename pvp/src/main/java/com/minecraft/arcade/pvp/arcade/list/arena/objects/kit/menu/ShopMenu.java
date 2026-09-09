package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.menu;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.controller.KitController;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.user.factory.list.ArenaUser;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.pvp.PvPMember;
import com.minecraft.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopMenu extends Menu {

    private final ArenaUser user;

    public ShopMenu(Player player) {
        super(player, "Loja de Kits", 6, 21);

        this.user = (ArenaUser) ArenaUser.of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        PvPMember member = user.getMember();

        List<Kit> list = KitController.getKitsUserDontHave(getPlayer());

        if (list.isEmpty())
            addErrorButton("§cVocê já possui todos os kits!");
        else {

            buildPageItems(list, 10, (kit, slot) -> {

                boolean canBuy = member.getCoins() >= kit.getPrice();

                List<String> lore = new ArrayList<>(kit.getDescription());

                lore.addAll(Arrays.asList(
                        "",
                        "§7Preço: §6" + Util.formatNumber(kit.getPrice()) + " coins",
                        canBuy ? "§aClique para adquirir!" : "§cFaltam coins."
                ));

                addItem(slot, Item.fromStack(kit.getIcon())
                        .flags(ItemFlag.values())
                        .name("§a" + kit.getName())
                        .lore(lore)
                        .click(event -> {

                            if (!canBuy) {
                                sound(MenuSound.ERROR);
                                getPlayer().sendMessage("§cVocê não tem coins suficientes para comprar este kit.");
                                return;
                            }

                            close();
                            sound(MenuSound.DONE);

                            kit.setKit(getPlayer());
                            getPlayer().sendMessage("§aVocê adquiriu o kit " + kit.getName() + ".");
                        }));
            });
        }

        addItem(getTotalSlots() - 5, Item.of(Material.EMERALD, "§7Coins: §6" + Util.formatNumber(member.getCoins())));

        display();
    }
}
