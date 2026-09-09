package com.minecraft.arcade.bedwars.menu.upgrade.trap;

import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.trap.Trap;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.trap.enums.TrapCategory;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.trap.enums.TrapType;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.api.protocol.ProtocolHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrapMenu extends Menu {

    private final User user;
    private final Team team;

    public TrapMenu(Player player, Menu last, Team team) {
        super(player, "Armadilhas", last, 6, 12);

        this.user = (User) User.of(player.getUniqueId());

        this.team = team;
    }

    @Override
    public void handle() {
        clear();

        List<TrapType> typeList = Stream.of(TrapType.values())
                .sorted(Comparator.comparing(TrapType::getName))
                .collect(Collectors.toList());

        if (typeList.isEmpty())
            addErrorButton("§cNão há armadilhas...");
        else {
            for (int i = 27; i < 36; i++)
                addItem(i, Item.of(Material.STAINED_GLASS_PANE, 15).name("§7§m-"));

            buildPageItems(typeList, 10, (type, slot) -> {
                Item icon = type.getIcon().clone()
                        .flags(ItemFlag.values());

                List<String> description = new ArrayList<>(type.getDescription());

                boolean has = team.getTraps().stream().anyMatch(trap -> trap.getType().equals(type)),
                        canPurchase = type.canPurchase(getPlayer());

                description.addAll(Arrays.asList(
                        "",
                        "§7Custo: §b" + type.getCost() + " diamante" + (type.getCost() > 1 ? "s" : ""),
                        "",
                        has ? "§cArmadilha adquirida." : canPurchase
                                ? "§eClique para comprar!"
                                : "§cVocê não tem diamantes suficientes."
                ));

                icon.name(((has || !canPurchase) ? "§c" : "§a") + type.getName());
                icon.lore(description);

                icon.click(event -> {
                    int totalTraps = team.getTraps().size();

                    if (totalTraps >= 3) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê já atingiu o limite de armadilhas.");

                        return;
                    }

                    if (has) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê já adquiriu a armadilha " + type.getName() + ".");

                        return;
                    }

                    if (!canPurchase) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem diamantes suficientes. " +
                                "Faltam " + user.getRemainingOre(type.getOre(), type.getCost()) + " diamantes.");

                        return;
                    }

                    sound(MenuSound.DONE);

                    user.removeOre(type.getOre(), type.getCost());

                    team.getTraps().add(new Trap(type, TrapCategory.of(totalTraps)));

                    team.getPlayers().forEach(player -> ProtocolHandler.sendBar(player, team.getColor() + getPlayer().getName()
                            + "§e adquiriu §6Armadilha " + type.getName()));

                    handle();
                });

                addItem(slot, icon);
            });

            int trapIndex = 0;
            for (int i = 38; i <= 42; i += 2) {
                Item item;

                int id = trapIndex + 1;

                if (trapIndex >= team.getTraps().size()) {
                    // Se a lista não tem mais elementos, criar um item vazio
                    String numberName = id == 1 ? "primeiro" : id == 2 ? "segundo" : "terceiro";

                    item = new Item(Material.STAINED_GLASS, id, 15)
                            .name("§c" + id + "ª armadilha: Vazia!")
                            .lore("§7O " + numberName + " invasor da sua ilha",
                                    "§7irá ativar essa armadilha.",
                                    "",
                                    "§7Ao adquirir uma armadilha, ela será",
                                    "§7enfileirada aqui.");
                } else {
                    Trap trap = new ArrayList<>(team.getTraps()).get(trapIndex);

                    item = trap.getIcon().clone()
                            .flags(ItemFlag.values())
                            .name("§e" + trap.getName())
                            .lore(trap.getDescription());
                }

                addItem(i, item);
                trapIndex++;
            }
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
