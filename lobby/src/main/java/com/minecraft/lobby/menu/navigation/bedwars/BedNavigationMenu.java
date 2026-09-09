package com.minecraft.lobby.menu.navigation.bedwars;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.api.party.Party;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.backend.database.redis.message.types.route.arcade.ArenaSearchMessage;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.menu.server.arcade.map.ArcadeMapMenu;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import com.minecraft.core.server.Server;
import com.minecraft.core.util.Util;
import com.minecraft.lobby.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.List;
import java.util.stream.Collectors;

public class BedNavigationMenu extends Menu {

    private final ArcadeCategory arcade;

    public BedNavigationMenu(Player player, ArcadeCategory arcade, Menu last) {
        super(player, arcade.getServer().getName() + " " + arcade.getName(), last, 4);

        this.arcade = arcade;
    }

    @Override
    public void handle() {
        clear();

        Account account = Core.getAccountController().of(getPlayer().getUniqueId());

        User user = (User) User.of(getPlayer().getUniqueId());
        BedMember member = user != null ? user.getMember(BedMember.class) : null;

        List<Type> types;
        if (member != null) {
            Type preferredType = member.isProfile(ProfileType.RANKED) ? Type.COMPETITIVE : Type.CASUAL;
            types = arcade.getTypes().stream()
                    .filter(type -> type.equals(preferredType))
                    .collect(Collectors.toList());
        } else {
            types = arcade.getTypes();
        }

        int modeSlot = 11, mapSlot = 15;
        for (Type type : types) {

            /* Adicionar modo */
            int finalModeSlot = modeSlot;

            boolean versus = arcade.name().contains("VERSUS");

            addItem(modeSlot, Item.of(Material.getMaterial(arcade.getIconId()), "§a" + arcade.getName() + (!versus ? " (" + type.getName() + ")" : ""),
                            "§7" + Util.formatNumber(Core.getArcadeData().getOnlinePlayers(arcade)) + " jogando agora.",
                            "",
                            "§eClique para jogar!")
                    .updater(view -> {
                        if (!view.getTitle().equalsIgnoreCase(getTitle())) return;

                        Item item = getContents().get(finalModeSlot);

                        if (item != null) {
                            List<String> lore = item.getMeta().getLore();

                            lore.set(0, "§7" + Util.formatNumber(Core.getArcadeData().getOnlinePlayers(arcade)) + " jogando agora.");

                            item.lore(lore);

                            view.setItem(finalModeSlot, item);
                            getPlayer().updateInventory();
                        }
                    })
                    .click(event -> {
                        close();
                        sound(MenuSound.DONE);

                        Server server = Core.getServerData().of(arcade.getServer());

                        ArcadeRouteContext route = ArcadeRouteContext.builder()
                                .arcade(arcade)
                                .slot(arcade.getSlots().get(0))
                                .type(type)
                                .join(Join.PLAYER)
                                .build();

                        route.setServerId(server != null ? server.getId() : 0);

                        Party party = account.getParty();

                        if (party != null) {
                            if (party.isAuthor(account.getId()))
                                route.setLink(party.getMembersId());
                            else {
                                account.send("§cApenas o dono da party pode procurar salas.");
                                return;
                            }
                        }

                        new ArenaSearchMessage(account.getId(), route).send();
                    }));

            addItem(versus ? 15 : mapSlot, handleMapSelector(account, arcade, type));
            modeSlot += 3;
            mapSlot += 3;
        }

        addCloseButton();

        if (isReturnable())
            addBackButton();

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
