package com.minecraft.arcade.bedwars.menu.custom.preference;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.room.custom.RoomCustom;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RoomPreferenceMenu extends Menu {

    private final Arena arena;

    public RoomPreferenceMenu(Player player, Arena arena, Menu last) {
        super(player, "Configurações", last, 3);

        this.arena = arena;

        setAllowClickItemWithQuantity(true);
    }

    @Override
    public void handle() {
        clear();

        RoomCustom custom = arena.getCustom();

        Slot slot = arena.getSlot();

        int minPlayers = slot.getMaxPlayers() / 2;

        int teamSize = custom.getTeamSize();

        addItem(10, Item.of(Material.SKULL_ITEM, 3, "§aTamanho do time")
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWE2YTIyZDA3NWVjNTQzYmNjOWEyYzZlMWE1ZWI5YWIwMDc0ZjNmZDhiYWJiYTU0NWUyZThhNjAzYTRjOGNhOCJ9fX0=")
                .amount(teamSize)
                .lore("§7Defina a quantidade de",
                        "§7jogadores por time.",
                        "",
                        "§7Quantidade: §a" + teamSize,
                        "",
                        "§eClique esquerdo: §fAumentar",
                        "§eClique direito: §fDiminuir")
                .click(event -> {
                    if (event.isLeftClick()) {
                        custom.setTeamSize(custom.getTeamSize() + 1);

                        sound(MenuSound.DONE);
                        handle();
                    } else if (event.isRightClick()) {

                        if (teamSize <= minPlayers) {
                            sound(MenuSound.ERROR);
                            return;
                        }

                        custom.setTeamSize(custom.getTeamSize() - 1);

                        sound(MenuSound.DONE);
                        handle();
                    }
                }));

        if (isReturnable())
            addBackButton();

        display();
    }
}
