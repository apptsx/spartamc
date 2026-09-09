package com.minecraft.arcade.duels.arcade.list.combat.simulator.kit.menu;

import com.minecraft.core.Constant;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.util.list.loader.ClassLoader;
import com.minecraft.arcade.duels.Duels;
import com.minecraft.arcade.duels.arcade.list.combat.simulator.kit.model.Kit;
import com.minecraft.arcade.duels.user.factory.list.SimulatorUser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KitMenu extends Menu {

    private final SimulatorUser user;

    private final List<Kit> kitList = ClassLoader.getClassesForPackage(Duels.getInstance(), Constant.SOURCE_DIR + ".arcade.duels.arcade.list.combat.simulator.kit.model.list")
            .stream().filter(kitClass -> kitClass != null && Kit.class.isAssignableFrom(kitClass))
            .map(kitClass -> {
                try {
                    return (Kit) kitClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList());

    public KitMenu(Player player) {
        super(player, "Selecionar Kit", 4);

        this.user = (SimulatorUser) SimulatorUser.of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        if (kitList.isEmpty())
            addErrorButton("§cNão há kits...");
        else {
            int slot = 10;
            for (Kit kit : kitList) {
                List<String> lore = new ArrayList<>(Arrays.asList("§8" + kit.getStyle().getName(), ""));

                lore.addAll(kit.getLore());
                lore.addAll(Arrays.asList(
                        "",
                        !user.isUsingKit(kit) ? "§eClique para selecionar!" : "§cJá selecionado."
                ));

                addItem(slot, Item.fromStack(kit.getIcon())
                        .name("§a" + kit.getName())
                        .lore(lore)
                        .flags(ItemFlag.values())
                        .click(event -> {
                            if (user.isUsingKit(kit)) {
                                sound(MenuSound.ERROR);
                                return;
                            }

                            user.setKit(kit);
                            user.getSidebar().updateRow("kit", "Kit: §a" + kit.getName());

                            sound(MenuSound.PAGINATED);
                            handle();
                        }));

                slot++;
            }
        }

        addCloseButton();

        display();
    }
}
