package com.minecraft.lobby.menu.arcade;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.api.party.Party;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.feature.ArcadeFeature;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.backend.database.redis.message.types.route.arcade.ArenaSearchMessage;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.menu.server.arcade.editor.ArcadeEditingMenu;
import com.minecraft.core.bukkit.menu.server.arcade.map.ArcadeMapMenu;
import com.minecraft.core.server.Server;
import com.minecraft.lobby.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArcadeSlotMenu extends Menu {

    private final User user;
    private final ArcadeCategory arcade;

    public ArcadeSlotMenu(Player player, Menu last, ArcadeCategory arcade) {
        super(player, "Jogar " + arcade.getName(), last, 4);

        this.user = (User) User.of(player.getUniqueId());
        this.arcade = arcade;
    }

    @Override
    public void handle() {
        clear();

        Account account = user.getAccount();

        List<Slot> slots = arcade.getSlots();

        int i = 10;
        for (Slot slot : slots) {
            Material icon = Material.getMaterial(arcade.getIconId());

            if (icon == null) icon = Material.WEB;

            addItem(i, Item.of(icon, "§a" + arcade.getName() + " " + slot.getId(), "§eClique para jogar!")
                    .amount(slot.ordinal())
                    .click(event -> {
                        close();
                        sound(MenuSound.DONE);

                        Server server = Core.getServerData().of(arcade.getServer());

                        if (server == null || server.isDead()) {
                            getPlayer().sendMessage(String.format(Constant.SERVER_NOT_FOUND_MESSAGE, arcade.getServer().getName()));
                            return;
                        }

                        ArcadeRouteContext route = ArcadeRouteContext.builder()
                                .arcade(arcade)
                                .slot(slot)
                                .join(Join.PLAYER)
                                .build();

                        Party party = account.getParty();

                        if (party != null) {
                            if (party.isAuthor(account.getId()))
                                route.setLink(party.getMembersId());
                            else {
                                account.send("§cApenas o dono da party pode procurar salas.");
                                return;
                            }
                        }

                        new ArenaSearchMessage(getPlayer().getUniqueId(), route).send();
                    }));

            i++;
        }

        addCloseButton();

        if (!arcade.getLore().isEmpty()) {
            List<String> lore = new ArrayList<>(Arrays.asList("§8" + arcade.getStyle().getName(), ""));

            lore.addAll(arcade.getLore());

            addItem(getTotalSlots() - 6, Item.of(Material.BOOK, "§aComo jogar?", lore));
        }

        addItem(16, Item.of(Material.MAP, "§aSelecionar mapa",
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
                    new ArcadeMapMenu(getPlayer(), arcade, Type.CASUAL, this).handle();
                }));

        if (arcade.hasFeature(ArcadeFeature.EDITABLE_MENU))
            addItem(getTotalSlots() - 4, Item.of(Material.ANVIL, "§aEditar inventário",
                            "§7Altere o modelo que o",
                            "§7seu inventário é organizado.",
                            "",
                            "§eClique para ver!")
                    .click(event -> {
                        sound(MenuSound.PAGINATED);
                        new ArcadeEditingMenu(getPlayer(), arcade, this).handle();
                    }));


        if (isReturnable())
            addBackButton();

        display();
    }
}
