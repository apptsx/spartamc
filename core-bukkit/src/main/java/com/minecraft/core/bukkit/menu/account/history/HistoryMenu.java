package com.minecraft.core.bukkit.menu.account.history;

import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.api.punishment.Punishment;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.util.list.DateUtil;
import com.minecraft.core.util.list.TimeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryMenu extends Menu {

    private final Account target;
    private final PunishmentCategory category;

    public HistoryMenu(Player player, Account target, PunishmentCategory category) {
        super(player, "Histórico de " + target.getNickname(), 5, 21);

        this.target = target;
        this.category = category;
    }

    @Override
    public void handle() {
        clear();

        List<Punishment> list = target.getPunishments(category);

        if (list.isEmpty())
            addErrorButton("§cNão há punições...");
        else {

            buildPageItems(list, 10, (punishment, slot) -> {

                List<String> lore = new ArrayList<>(Arrays.asList(
                        "",
                        "§fAutor: §7" + punishment.getAuthorName(),
                        "§fMotivo: §7" + punishment.getCause(),
                        " §7§o" + punishment.getReason().getName(),
                        "",
                        "§fSituação: " + (punishment.isValid() ? "§aAtivo" : "§cRevogado")
                ));

                if (punishment.isValid())
                    lore.add("§fExpira em: §7" + (punishment.isTemporary() ? TimeUtil.formatTime(punishment.getExpiresAt(), TimeUtil.TimeFormat.SHORT)
                            : "Nunca"));

                lore.addAll(Arrays.asList(
                        "",
                        "§fServidor: §7" + punishment.getServer().getName()
                ));

                if (punishment.getArcade() != null && !punishment.getArcade().equals(ArcadeCategory.NONE))
                    lore.add("§fModo de jogo: §7" + punishment.getArcade().getName());

                lore.addAll(Arrays.asList(
                        "",
                        "§fData: §7" + DateUtil.getDate(punishment.getCreatedAt())));

                addItem(slot, Item.of(Material.PAPER, "§a" + punishment.getId(), lore));
            });
        }

        display();
    }
}
