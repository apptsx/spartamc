package com.minecraft.core.bukkit.menu.account.medal;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.medal.Medal;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.manager.list.MedalManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MedalMenu extends Menu {

    private final Account account;

    public MedalMenu(Player player, Menu last) {
        super(player, "Medalhas", last, 6, 28);

        this.account = Core.getAccountController().of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        List<Medal> medals = MedalManager.getAvailableMedals(account);

        if (medals.isEmpty() || medals.stream().allMatch(medal -> medal.equals(Medal.NONE))) {
            addItem(22, Item.of(Material.WEB, "§cVocê não possui medalhas",
                    "§7Adquira medalhas em: §e" + Constant.SERVER_STORE));
        } else {
            buildPageItems(medals, 10, (medal, slot) -> {
                boolean isUsing = account.isUsingMedal(medal);

                List<String> lore = new ArrayList<>(Arrays.asList(
                        "§7" + medal.getColoredName(),
                        ""
                ));

                if (isUsing) {
                    lore.add("§a✓ Medalha selecionada");
                } else {
                    lore.add("§eClique para selecionar!");
                }

                addItem(slot, Item.of(Material.DOUBLE_PLANT, medal.getColoredSymbol() + " " + medal.getColoredName(), lore)
                        .click(event -> {
                            if (isUsing) {
                                sound(MenuSound.ERROR);
                                account.send("§cVocê já selecionou a medalha " + medal.getColoredName() + "§c.");
                                return;
                            }

                            account.setMedal(medal);
                            sound(MenuSound.SUCCESS);
                            account.send("§eA medalha " + medal.getColoredName() + "§e foi selecionada.");

                            handle();
                        }));
            });
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}