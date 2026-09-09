package com.minecraft.lobby.menu.patch;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class PatchLogsRemoveMenu extends Menu {

    public PatchLogsRemoveMenu(Player player) {
        super(player, "Remover Patch Log", 3);
    }

    @Override
    public void handle() {
        clear();

        List<PatchLogStorage.PatchLog> patchLogs = PatchLogStorage.getInstance().getPatchLogs();

        if (patchLogs.isEmpty()) {
            addItem(13, Item.of(Material.BOOK, "§cNenhuma patch log")
                    .lore("§7Não há patch logs para remover."));
        } else {
            int slot = 10;
            for (PatchLogStorage.PatchLog patchLog : patchLogs) {
                if (slot > 16 && slot < 18) slot = 18;
                if (slot > 26) break;

                addItem(slot, Item.of(Material.BOOK, "§c§l" + patchLog.getDate())
                        .lore(
                                "§7Clique para selecionar.",
                                "§7Esta patch log será",
                                "§7removida permanentemente.",
                                "",
                                "§eClique para selecionar!"
                        )
                        .click(event -> {
                            sound(MenuSound.DONE);
                            new PatchLogsConfirmMenu(getPlayer(), patchLog.getId(), patchLog.getDate(), this).handle();
                        }));

                slot++;
            }
        }

        addItem(22, Item.of(Material.ARROW, "§cVoltar")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    if (getLast() != null) {
                        getLast().handle();
                    }
                }));

        display();
    }
}