package com.minecraft.arcade.bedwars.menu.custom;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.menu.custom.map.RoomMapSelectMenu;
import com.minecraft.arcade.bedwars.menu.custom.preference.RoomPreferenceMenu;
import com.minecraft.core.Constant;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.custom.RoomCustom;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomRoomMenu extends Menu {

    private final Arena arena;

    public CustomRoomMenu(Player player, Arena arena) {
        super(player, "Sua sala", 5);

        this.arena = arena;
    }

    @Override
    public void handle() {
        clear();

        ArcadeCategory arcade = arena.getArcade().getCategory();

        addItem(11, Item.of(Material.getMaterial(arcade.getIconId()),
                        "§a" + arcade.getServer().getName() + " " + arcade.getName())
                .amount(arena.getSlot().ordinal())
                .flags(ItemFlag.values()));

        addItem(13, handleMapList(arena));

        addItem(15, Item.of(Material.DIODE, "§aConfigurações",
                        "§7Customize as configurações",
                        "§7das salas.",
                        "",
                        "§eClique para editar!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new RoomPreferenceMenu(getPlayer(), arena, this).handle();
                }));

        // Botoes
        handleButtons();

        display();
    }

    protected void handleButtons() {
        addItem(28, Item.of(Material.SKULL_ITEM, 3, "§aIniciar sala",
                        "§eClique para iniciar a sala.")
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzI0NTAzNjc4MmM2YTdlMjVmZjg0YzUwODdhNjU5NjZkNjUwYzAzM2JhMzdlMDA5NjVlOGI5OTAyMWIzZWNhZSJ9fX0=")
                .click(event -> {
                    close();

                    if (arena.getTotalPlayers() < 2) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cA sala precisa ter pelo menos 2 jogadores para iniciar.");

                        return;
                    }

                    if (!arena.isPhase(RoomPhase.WAITING)) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não pode iniciar esta partida.");

                        return;
                    }

                    sound(MenuSound.DONE);

                    arena.setPhase(RoomPhase.PLAYING);
                }));

        addItem(30, Item.of(Material.SKULL_ITEM, 3, "§b" + (arena.getContext().isCounterEnabled() ? "Pausar" : "Ativar") + " temporizador",
                        "§eClique para " + (arena.getContext().isCounterEnabled() ? "pausar" : "ativar") + " o temporizador.")
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmQ5ZmVlNDA1MzIzMmFmZmU0YzdiZWMxMTc3MmFlM2JkYTQ0Y2JjZmFiZGJjMzFlM2QwYzA1MGY1MzNlZTA2YiJ9fX0=")
                .click(event -> {
                    close();
                    sound(MenuSound.DONE);

                    arena.getContext().setCounterEnabled(!arena.getContext().isCounterEnabled());

                    arena.send(arena.getContext().isCounterEnabled()
                            ? "§aO temporizador de tempo foi ativado."
                            : "§cO temporizador de tempo foi pausado.");
                }));

        addItem(32, Item.of(Material.SKULL_ITEM, 3, "§eReiniciar sala",
                        "§eClique para reiniciar a sala.")
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2RhMjlmZTIzYjY5Zjc2YmM0OTQ3NDEwMjIyNmIwNjk5Y2EyYTVhZDExZjdhNDg1NDJiMTNhZDljYWJhZjg5ZCJ9fX0=")
                .click(event -> {
                    close();
                    sound(MenuSound.DONE);

                    arena.reload(getPlayer());
                }));

        addItem(34, Item.of(Material.SKULL_ITEM, 3, "§cFechar sala",
                        "§eClique para fechar a sala.")
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzNhMDNjMDZmZmUyMzU2Y2UwMGFlZjViNzA4ODc4ZDJmZTQzNjVhOTdiYzRkYWUxZTE1NDJjMjdiMmViMzBkZCJ9fX0=")
                .click(event -> {
                    close();
                    sound(MenuSound.DONE);

                    arena.unload();
                }));
    }

    protected Item handleMapList(Arena arena) {
        RoomCustom custom = arena.getCustom();

        Item item = Item.of(Material.EMPTY_MAP, "§eMapas");

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Selecione os mapas",
                "§7disponíveis nas partidas.",
                ""
        ));

        List<Map> mapList = custom.getMaps();

        if (!mapList.isEmpty()) {

            if (mapList.size() == 1)
                lore.add("§7Selecionado: §a" + mapList.get(0).getName());
            else {
                lore.add("§7Selecionados:");

                if (mapList.size() > 5) {
                    List<Map> topFiveMaps = mapList.subList(0, 5);

                    topFiveMaps.forEach(map -> lore.add("§8" + Constant.ARROW_ALT_SYMBOL + " §6" + map.getName()));

                    int remainingMaps = mapList.size() - 5;

                    lore.add(" §b+" + remainingMaps + "§7 mapas...");
                } else
                    mapList.forEach(map -> lore.add("§8" + Constant.ARROW_ALT_SYMBOL + " §6" + map.getName()));
            }

            lore.add("");
        }

        lore.add("§aClique para editar!");

        return item.lore(lore)
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new RoomMapSelectMenu(getPlayer(), arena, this).handle();
                });
    }
}
