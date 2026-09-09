package com.minecraft.arcade.bedwars.menu.game.play;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.menu.server.arcade.map.ArcadeMapMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class PlayAgainMenu extends Menu {

    private final User user;

    public PlayAgainMenu(Player player) {
        super(player, "Jogar novamente?", 3);

        this.user = (User) User.of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        Arena arena = user.getArena();

        addItem(11, Item.of(Material.EYE_OF_ENDER, "§aJogar novamente",
                        "§eClique para jogar novamente.")
                .click(event -> {
                    close();
                    sound(MenuSound.DONE);

                    getPlayer().performCommand("playagain");
                }));

        addItem(15, handleMapSelector(user.getAccount(), arena.getArcade().getCategory(), arena.getType()));

        addCloseButton();

        display();
    }

    protected Item handleMapSelector(Account account, ArcadeCategory arcade, Type type) {
        return Item.of(Material.MAP, "§aSelecionar mapa",
                        "§7Decida o mapa que você",
                        "§7deseja jogar.",
                        "",
                        "§7Exclusivo para §aVIPs",
                        "",
                        "§eClique para ver!")
                .flags(ItemFlag.values())
                .click(event -> {

                    if (!account.isVIP()) {
                        sound(MenuSound.ERROR);
                        account.send("§cVocê não pode selecionar mapas.");
                        return;
                    }

                    sound(MenuSound.PAGINATED);
                    new ArcadeMapMenu(getPlayer(), arcade, type, this).handle();
                });
    }
}
