package com.minecraft.arcade.bedwars.menu.management;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.backend.database.redis.message.types.route.LobbyTeleportMessage;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerKickMenu extends Menu {

    private final Arena arena;

    public PlayerKickMenu(Player player, Arena arena, Menu last) {
        super(player, "Expulsar Jogadores", last, 6, 21);

        this.arena = arena;
    }

    @Override
    public void handle() {
        clear();

        List<Player> players = arena.getPlayers().stream()
                .filter(p -> !p.getUniqueId().equals(getPlayer().getUniqueId()))
                .collect(Collectors.toList());

        if (players.isEmpty()) {
            addItem(22, Item.of(Material.BARRIER, "§cNenhum jogador disponível",
                            "§7Não há outros jogadores na sala."));
        } else {
            // Garantir que maxItems está definido corretamente
            if (getMaxItems() <= 0) {
                Core.getLogger().warning("[PlayerKickMenu] maxItems não está definido! Definindo padrão...");
                // Não podemos alterar maxItems diretamente, então vamos usar um método alternativo
                // Mas como já está definido no construtor, isso não deveria acontecer
            }
            buildPageItems(players, 10, (targetPlayer, slot) -> {
                Account targetAccount = Core.getAccountController().of(targetPlayer.getUniqueId());
                User targetUser = (User) User.of(targetPlayer.getUniqueId());

                addItem(slot, Item.of(Material.SKULL_ITEM, 3, "§c" + targetAccount.getNickname(),
                                "§7Rank: " + targetAccount.getRank().getColoredName(),
                                "§7Time: " + (targetUser.getTeam() != null ? targetUser.getTeam().getColor() + targetUser.getTeam().getTypeName() : "§7Nenhum"),
                                "",
                                "§cClique para expulsar!")
                        .skullByBase64(targetAccount.getSkin().getValue())
                        .click(event -> {
                            close();
                            sound(MenuSound.DONE);

                            arena.quit(targetPlayer);
                            
                            // Usar a mesma lógica do LobbyCommand para redirecionar o jogador
                            if (targetAccount.inServer(ServerType.HUB)) {
                                // Verificar se já está no mundo lobby
                                World lobbyWorld = Bukkit.getWorld("lobby");
                                if (lobbyWorld != null && targetPlayer.getWorld().equals(lobbyWorld)) {
                                    targetAccount.send("§cVocê já está conectado no Lobby Principal!");
                                } else {
                                    // Teleportar para o mundo lobby
                                    if (lobbyWorld != null) {
                                        targetPlayer.teleport(lobbyWorld.getSpawnLocation());
                                        targetAccount.send("§aTeleportado para o Lobby Principal!");
                                    } else {
                                        targetAccount.send("§cNão foi possível teleportar para o Lobby Principal!");
                                    }
                                }
                            } else {
                                ServerType server = targetAccount.getServerType();
                                ServerType lobbyType = server.getServerLobby();

                                Server hubServer = Core.getServerData().of(ServerType.HUB);
                                if (hubServer == null || hubServer.isDead()) {
                                    targetAccount.send("§cNenhuma rede de Lobby foi encontrada. Tente novamente em alguns instantes.");
                                } else {
                                    if (lobbyType != null) {
                                        new LobbyTeleportMessage(targetAccount.getId(), server).send();
                                    }

                                    targetAccount.redirect(ServerType.HUB);
                                }
                            }

                            arena.send("§c" + targetAccount.getNickname() + " §efoi expulso da sala por " + getPlayer().getName() + ".");
                            getPlayer().sendMessage("§aJogador " + targetAccount.getNickname() + " expulso com sucesso!");
                        }));
            });
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}

