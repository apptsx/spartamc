package com.minecraft.lobby.menu.patch;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PatchLogsMenu extends Menu {

    public PatchLogsMenu(Player player) {
        super(player, "Patch Logs", 6);
    }

    @Override
    public void handle() {
        clear();

        List<PatchLogStorage.PatchLog> patchLogs = PatchLogStorage.getInstance().getPatchLogs();

        if (patchLogs.isEmpty()) {
            addItem(22, Item.of(Material.BOOK, "§cNenhuma patch log encontrada")
                    .lore("§7Não há patch logs ainda."));
        } else {
            int slot = 10;
            for (PatchLogStorage.PatchLog patchLog : patchLogs) {
                if (slot == 17) slot = 19;
                if (slot == 26) slot = 28;
                if (slot == 35) slot = 37;
                if (slot > 53) break;

                addItem(slot, Item.of(Material.BOOK, "§6§lPatch: §e" + patchLog.getDate())
                        .lore(
                                "§7Clique para ler esta",
                                "§7patch log.",
                                "",
                                "§eClique para abrir!"
                        )
                        .click(event -> {
                            sound(MenuSound.DONE);
                            openPatchLog(getPlayer(), patchLog.getId());
                        }));
                slot++;
            }
        }

        addItem(49, Item.of(Material.ARROW, "§cVoltar")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    if (getLast() != null) {
                        getLast().handle();
                    }
                }));

        display();
    }

    private void openPatchLog(Player player, UUID patchLogId) {
        PatchLogStorage.PatchLog patchLog = PatchLogStorage.getInstance().getPatchLog(patchLogId);
        if (patchLog == null) {
            player.sendMessage("§cPatch log não encontrada!");
            return;
        }

        org.bukkit.inventory.ItemStack book = new org.bukkit.inventory.ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("Patch: " + patchLog.getDate());
        meta.setAuthor(com.minecraft.core.Constant.SERVER_NAME);
        meta.setPages(patchLog.getContent());
        book.setItemMeta(meta);

        player.getInventory().setItemInHand(book);
        player.sendMessage("§e§lPATCH LOGS");
        player.sendMessage("§7Patch: §e" + patchLog.getDate());
        player.sendMessage("§7Clique no livro em sua mão para ler!");
    }
}