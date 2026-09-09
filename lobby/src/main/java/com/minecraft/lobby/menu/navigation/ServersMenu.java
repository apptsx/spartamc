package com.minecraft.lobby.menu.navigation;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.menu.server.arcade.custom.CustomArcadeMenu;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import com.minecraft.lobby.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServersMenu extends Menu {

    private final User user;
    private final NavigationType type;

    public ServersMenu(Player player, NavigationType type) {
        super(player, type.equals(NavigationType.SERVER) ? "Modos de jogo" : "Selecionar Sala", 3);

        this.user = (User) User.of(getPlayer().getUniqueId());
        this.type = type;

        setShowTitlePage(false);
    }

    @Override
    public void handle() {
        clear();

        switch (type) {
            case SERVER: {
                // Bedwars - slot 10
                buildComponent(10, ServerType.BEDWARS);
                
                // Duels - slot 11
                buildComponent(11, ServerType.DUELS);
                
                // PvP - slot 12
                buildComponent(12, ServerType.PVP);
                
                // The Bridge - slot 13
                buildComponent(13, ServerType.THE_BRIDGE);
                
                // Hunger Games - slot 14
                buildComponent(14, ServerType.HUNGERGAMES);
                
                // Sala personalizada - slot 26 (último slot)
                addItem(26, Item.of(Material.NAME_TAG, "§aJogue com amigos!",
                                "§7Crie salas customizadas",
                                "§7para jogar com seus amigos.",
                                "",
                                "§7Requer o rank " + RankType.MAX.getBoldColorName().toUpperCase(),
                                "",
                                "§eClique para criar!")
                        .click(event -> {
                            if (!user.getAccount().hasRank(RankType.MAX)) {
                                sound(MenuSound.ERROR);
                                return;
                            }
                            
                            sound(MenuSound.PAGINATED);
                            new CustomArcadeMenu(getPlayer(), this).handle();
                        }));
                break;
            }

            case ROOM: {
                addItem(10, Item.of(Material.WOOL, 14, "§cLobby #1",
                                "§7Jogadores: §a" + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers(),
                                "",
                                "§cSala atual.")
                        .click(event -> {
                            close();
                            sound(MenuSound.ERROR);
                        }));
                break;
            }
        }

        display();
    }

    protected void buildComponent(int slot, ServerType server, String... lore) {
        Item item = Item.of(Material.getMaterial(server.getIconId()), "§a" + server.getName())
                .flags(ItemFlag.values());

        ServerType serverLobby = server.getServerLobby();

        int online = Core.getServerData().getOnlinePlayers(server) + (serverLobby != null ? Core.getServerData().getOnlinePlayers(serverLobby) : 0);

        List<String> itemLore = new ArrayList<>(Arrays.asList(
                "§8" + server.getStyle().getName(),
                ""));

        itemLore.addAll(Arrays.asList(lore));

        itemLore.addAll(Arrays.asList(
                "",
                "§b" + Constant.ARROW_ALT_SYMBOL + " Clique para jogar!",
                "§e" + Util.formatNumber(online) + " jogando."
        ));

        // Finalizando configuração de Item
        item.lore(itemLore)
                .updater(view -> {
                    if (!view.getTitle().equalsIgnoreCase(getTitle())) return;

                    // Atualizando última e penúltima linha
                    boolean blink = (System.currentTimeMillis() / 1000) % 2 == 0;

                    int onlinePlayers = Core.getServerData().getOnlinePlayers(server) + (serverLobby != null ? Core.getServerData().getOnlinePlayers(serverLobby) : 0);

                    itemLore.set(itemLore.size() - 2, "§a" + (blink ? Constant.ARROW_ALT_SYMBOL : "") + " Clique para jogar!");
                    itemLore.set(itemLore.size() - 1, "§7" + Util.formatNumber(onlinePlayers) + " jogando.");

                    item.lore(itemLore);

                    view.setItem(slot, item);

                    getPlayer().updateInventory();
                });

        item.click(event -> {
            close();
            sound(MenuSound.DONE);

            user.getAccount().redirect(serverLobby != null ? serverLobby : server);
        });

        addItem(slot, item);
    }

    public enum NavigationType {
        SERVER, ROOM
    }
}