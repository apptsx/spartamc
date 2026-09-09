package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.menu;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.controller.KitController;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitCategory;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.arcade.pvp.user.factory.list.ArenaUser;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;

public class KitMenu extends Menu {

    private final ArenaUser user;

    private final KitCategory category;

    public KitMenu(Player player, KitCategory category) {
        super(player, "Selecionar Kit " + (category.ordinal() + 1), 6, 21);

        this.user = (ArenaUser) ArenaUser.of(player.getUniqueId());

        this.category = category;
    }

    @Override
    public void handle() {
        clear();

        List<Kit> list = new ArrayList<>(KitController.getList());

        if (list.isEmpty())
            addErrorButton("§cNão há kits...");
        else {
            list.sort((one, two) -> {
                boolean hasAccessOne = KitController.isReleased(getPlayer(), one, category);
                boolean hasAccessTwo = KitController.isReleased(getPlayer(), two, category);

                // Verifica se o kit é desbloqueado ou bloqueado
                if (hasAccessOne && !hasAccessTwo) {
                    return -1; // `one` deve vir antes porque o jogador tem acesso
                } else if (!hasAccessOne && hasAccessTwo) {
                    return 1; // `two` deve vir antes porque o jogador tem acesso
                } else {
                    // Se ambos forem iguais em termos de acesso, aplica a ordenação por nome
                    if (one.isEmpty())
                        return -1;
                    else if (two.isEmpty())
                        return 1;
                    else
                        return one.getName().compareTo(two.getName());
                }
            });

            Kit selected = user.getKit(category);

            addItem(getTotalSlots() - 5, Item.fromStack(selected.getIcon())
                    .name("§eKit atual - §6" + selected.getName()));

            buildPageItems(list, getTotalSlots() - 7, getTotalSlots() - 3, 10, (kit, slot) -> {
                final boolean using = user.isUsingKit(kit, category),
                        blocked = using || !KitController.isReleased(getPlayer(), kit, category),
                        released = KitController.isReleased(getPlayer(), kit, category);

                Item model = (released
                        ? Item.fromStack(kit.getIcon()).flags(ItemFlag.values())
                        : Item.of(Material.STAINED_GLASS_PANE, 14));

                model.name((!blocked ? "§a" : "§c") + kit.getName());

                List<String> lore = new ArrayList<>();

                KitStyle style = kit.getStyle();

                if (!style.equals(KitStyle.NONE))
                    lore.add("§8" + style.getName());

                lore.add("");
                lore.addAll(kit.getDescription());

                lore.add("");
                lore.add(!released ? "§cVocê não tem acesso."
                        : !using ? "§eClique para selecionar!" : "§cJá selecionado!");

                model.lore(lore);
                model.click(event -> {
                    if (blocked) {
                        sound(MenuSound.ERROR);
                        return;
                    }

                    close();

                    KitController.exchangeKit(user, kit, category);
                });

                addItem(slot, model);
            });
        }

        display();
    }
}
