package com.minecraft.lobby.menu.patch;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PatchLogsConfirmMenu extends Menu {

    private final UUID patchLogId;
    private final String date;

    public PatchLogsConfirmMenu(Player player, UUID patchLogId, String date, Menu last) {
        super(player, "Confirmar Remoção", last, 1);
        this.patchLogId = patchLogId;
        this.date = date;
    }

    @Override
    public void handle() {
        clear();

        addItem(2, Item.of(Material.BOOK, "§cPatch: " + date)
                .lore(
                        "§7Esta patch log será",
                        "§7removida permanentemente.",
                        "",
                        "§eTem certeza?"
                ));

        addItem(4, Item.of(Material.BOOK, "§e§lConfirmar Remoção")
                .lore(
                        "§7Clique em §a§lVERDE§7 para",
                        "§7confirmar a remoção.",
                        "",
                        "§aClique para confirmar!"
                )
                .click(event -> {
                    sound(MenuSound.DONE);
                    PatchLogStorage.getInstance().removePatchLog(patchLogId);
                    getPlayer().sendMessage("§aPatch log removida com sucesso!");
                    if (getLast() != null) {
                        getLast().handle();
                    } else {
                        new PatchLogsRemoveMenu(getPlayer()).handle();
                    }
                }));

        addItem(6, Item.of(Material.BOOK, "§e§lCancelar Remoção")
                .lore(
                        "§7Clique em §c§lVERMELHO§7 para",
                        "§7cancelar a remoção.",
                        "",
                        "§cClique para cancelar!"
                )
                .click(event -> {
                    sound(MenuSound.DONE);
                    getPlayer().sendMessage("§cRemoção cancelada.");
                    if (getLast() != null) {
                        getLast().handle();
                    }
                }));

        addItem(8, Item.of(Material.EMERALD_BLOCK, "§a§lCONFIRMAR")
                .lore(
                        "§7Clique para confirmar",
                        "§7a remoção da patch log.",
                        "",
                        "§a§lCONFIRMAR"
                )
                .click(event -> {
                    sound(MenuSound.DONE);
                    PatchLogStorage.getInstance().removePatchLog(patchLogId);
                    getPlayer().sendMessage("§aPatch log removida com sucesso!");
                    if (getLast() != null) {
                        getLast().handle();
                    } else {
                        new PatchLogsRemoveMenu(getPlayer()).handle();
                    }
                }));

        display();
    }
}