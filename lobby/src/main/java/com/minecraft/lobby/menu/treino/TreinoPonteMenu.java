package com.minecraft.lobby.menu.treino;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.lobby.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class TreinoPonteMenu extends Menu {

    private final User user;

    public TreinoPonteMenu(Player player) {
        super(player, "Treino Ponte", 3);

        this.user = (User) User.of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        addItem(12, Item.of(Material.NETHER_STAR, "§6Multiplayer")
                .flags(ItemFlag.values())
                .lore(
                        "§7Treine pontes com",
                        "§7outros jogadores!",
                        "",
                        "§eClique para jogar!"
                )
                .click(event -> {
                    sound(MenuSound.DONE);
                    user.getAccount().redirect(ServerType.PONTES);
                }));

        addItem(14, Item.of(Material.NETHER_STAR, "§6Solo")
                .flags(ItemFlag.values())
                .lore(
                        "§7Treine pontes",
                        "§7sozinho!",
                        "",
                        "§eClique para jogar!"
                )
                .click(event -> {
                    sound(MenuSound.DONE);
                    user.getAccount().redirect(ServerType.PONTES);
                }));

        setTitle("§7Treino de Pontes");
        display();
    }
}