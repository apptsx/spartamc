package com.minecraft.core.bukkit.service.redis.listener;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.Rank;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.api.skin.Skin;
import com.minecraft.core.api.skin.library.SkinLibrary;
import com.minecraft.core.arcade.ArcadeHolder;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.room.event.ArenaSearchedEvent;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.backend.database.redis.message.types.account.*;
import com.minecraft.core.backend.database.redis.message.types.punish.PunishOpenMessage;
import com.minecraft.core.backend.database.redis.message.types.route.LobbyTeleportMessage;
import com.minecraft.core.backend.database.redis.message.types.route.RoomInfoMessage;
import com.minecraft.core.backend.database.redis.message.types.route.RoomRequestMessage;
import com.minecraft.core.backend.database.redis.message.types.route.arcade.ArenaPartyWarpMessage;
import com.minecraft.core.backend.database.redis.message.types.route.arcade.ArenaSearchMessage;
import com.minecraft.core.backend.database.redis.message.types.route.arcade.ArenaStatsMessage;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.option.ServerOptions;
import com.minecraft.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.core.bukkit.api.vanish.Vanish;
import com.minecraft.core.bukkit.event.type.account.AccountClanChangeEvent;
import com.minecraft.core.bukkit.event.type.account.AccountRankUpdateEvent;
import com.minecraft.core.bukkit.event.type.account.AccountRefreshEvent;
import com.minecraft.core.bukkit.event.type.player.PlayerArenaWarpEvent;
import com.minecraft.core.bukkit.event.type.server.ServerRedisMessageEvent;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.bukkit.menu.account.friend.FriendMenu;
import com.minecraft.core.bukkit.menu.account.friend.FriendRequestMenu;
import com.minecraft.core.bukkit.menu.account.history.HistoryMenu;
import com.minecraft.core.bukkit.menu.server.party.PartyMenu;
import com.minecraft.core.bukkit.menu.server.punish.PunishMenu;
import com.minecraft.core.server.Server;
import com.minecraft.core.util.list.ReflectionUtil;
import com.google.gson.JsonObject;
import com.minecraft.core.bukkit.menu.account.nickname.NicknameMenu;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class RedisListener implements Listener {

    @EventHandler
    public void onServerRedisMessage(ServerRedisMessageEvent event) {
        String channel = event.getChannel();

        JsonObject json = event.getJson();

        if (json == null) {
            Core.getLogger().severe("Não foi possível recolher os dados do canal " + channel + ".");
            return;
        }

        switch (channel.toLowerCase()) {
            case Constant.REDIS_ACCOUNT_UPDATE_CHANNEL: {
                AccountUpdateMessage message = Core.GSON.fromJson(json, AccountUpdateMessage.class);

                if (message == null) return;

                Account account = message.getAccount();

                if (account == null) return;

                try {
                    Field field = ReflectionUtil.getField(Account.class, message.getField());

                    Object old = field.get(account), value = Core.GSON.fromJson(message.getValue(), field.getGenericType());

                    field.set(account, value);

                    new AccountRefreshEvent(account, field, old, value).call();
                } catch (Exception e) {
                    account.send("§cNão foi possível atualizar as suas informações!");

                    Core.getLogger().log(Level.WARNING, "Não foi possível atualizar as informações de " + account.getName(), e);
                }
                break;
            }

            case Constant.REDIS_ARENA_SEARCH_CHANNEL: {
                ArenaSearchMessage message = Core.GSON.fromJson(json, ArenaSearchMessage.class);

                if (message == null) return;

                ArcadeRouteContext route = message.getRoute();

                if (!route.isValid()) {
                    Core.getLogger().warning("[AS] Não foi possível buscar a arena para " + message.getSender() + ". Rota inválida!");
                    return;
                }

                ArcadeCategory category = route.getArcade();

                Account account = Core.getAccountData().of(message.getSender());

                if (account == null) {
                    Core.getLogger().warning("[AS] Não foi possível buscar a arena para " + message.getSender() + ". Jogador não encontrado!");
                    return;
                }

                List<Server> serverList = Core.getServerData().getServerList(server -> server.getType().equals(category.getServer()));

                Server server = serverList.stream().findFirst().orElse(null);

                if (server == null) {
                    account.sendGlobal(String.format(Constant.SERVER_NOT_FOUND_MESSAGE, category.getServer().getName()));
                    return;
                }

                if (!server.getType().equals(Core.getServerType())) return;

                ArcadeHolder arcade = BukkitCore.getManager().getArcade().read(route.getArcade());

                if (arcade == null) {
                    Core.getLogger().warning("[AS] Não foi possível buscar a arena para " + account.getNickname() + ". Jogo não encontrado!");

                    new ArenaStatsMessage(message.getSender(), ArenaStatsMessage.RequestStatus.NOT_FOUND).send();
                    return;
                }

                Room arena = arcade.findBestArena(account.getId(), route);

                new ArenaStatsMessage(message.getSender(), ArenaStatsMessage.RequestStatus.SEARCHING).send();

                if (arena == null) {
                    Core.getLogger().warning("[AS] Não foi possível buscar a arena para " + account.getNickname() + ". Arena não encontrada!");

                    new ArenaStatsMessage(message.getSender(), ArenaStatsMessage.RequestStatus.NOT_FOUND).send();
                    return;
                }

                ArenaStatsMessage stats = new ArenaStatsMessage(message.getSender(), ArenaStatsMessage.RequestStatus.SENDING);

                stats.setIdentifier(arena.getIdentifier());
                stats.send();

                new ArenaSearchedEvent(account, arena, route).callEvent();

                break;
            }

            case Constant.REDIS_ARENA_PARTY_LIST_CHANNEL: {
                AccountPartyListMessage message = Core.GSON.fromJson(json, AccountPartyListMessage.class);

                if (message == null) return;

                Player player = Bukkit.getPlayer(message.getSender());

                if (player != null)
                    new PartyMenu(player).handle();
                break;
            }

            case Constant.REDIS_ARENA_PARTY_WARP_CHANNEL: {
                ArenaPartyWarpMessage message = Core.GSON.fromJson(json, ArenaPartyWarpMessage.class);

                if (message == null) return;

                Account receiver = message.getReceiver();

                ArcadeRouteContext route = message.getRoute();

                if (!receiver.inServer(route.getArcade().getServer())) {
                    Core.getLogger().warning("Servidor não é igual.");
                    return;
                }

                ArcadeHolder arcade = BukkitCore.getManager().getArcade().read(route.getArcade());

                if (arcade == null) {
                    Core.getLogger().warning("Não foi possível encontrar o jogo solicitado.");
                    return;
                }

                Room arena = arcade.findBestArena(receiver.getId(), route);

                if (arena == null) {
                    Core.getLogger().info("Não foi possível encontrar a arena desejada.");
                    return;
                }

                new PlayerArenaWarpEvent(receiver.player(), arena).call();
                break;
            }

            case Constant.REDIS_ACCOUNT_VANISH_CHANNEL: {
                AccountVanishMessage message = Core.GSON.fromJson(json, AccountVanishMessage.class);

                if (message == null) return;

                Player player = Bukkit.getPlayer(message.getId());

                if (player != null)
                    Core.getPlatform().runSync(() -> Vanish.handle(player));

                break;
            }

            case Constant.REDIS_ACCOUNT_RANK_UPDATE_CHANNEL: {
                UUID id = UUID.fromString(json.get("id").getAsString());

                Rank rank = Core.GSON.fromJson(json.get("rank").getAsString(), Rank.class);

                Account account = Core.getAccountData().of(id);

                if (account == null) return;

                new AccountRankUpdateEvent(account, rank).call();
                break;
            }

            case Constant.REDIS_ACCOUNT_FRIEND_MENU_CHANNEL: {
                AccountFriendMessage message = Core.GSON.fromJson(json, AccountFriendMessage.class);

                if (message == null) return;

                Player sender = Bukkit.getPlayer(message.getSender());

                if (sender == null) return;

                AccountFriendMessage.FriendMenuType type = message.getType();

                switch (type) {
                    case LIST: {
                        new FriendMenu(sender, null).handle();
                        break;
                    }
                    case REQUEST: {
                        new FriendRequestMenu(sender, null).handle();
                        break;
                    }
                }

                break;
            }

            case Constant.REDIS_ACCOUNT_CLAN_CHANGE_CHANNEL: {
                AccountClanChangeMessage message = Core.GSON.fromJson(json, AccountClanChangeMessage.class);

                if (message == null) return;

                Account account = message.getAccount();

                if (account == null || !account.isOnline()) return;

                new AccountClanChangeEvent(account).call();
                break;
            }

            case Constant.REDIS_ACCOUNT_PUNISHMENTS_CHANNEL: {
                AccountPunishmentsMessage message = Core.GSON.fromJson(json, AccountPunishmentsMessage.class);

                if (message == null) return;

                Player viewer = Bukkit.getPlayer(message.getViewer());

                if (viewer == null) return;

                Account target = Core.getAccountData().of(message.getTarget());

                if (target == null) {
                    viewer.sendMessage("§cNão foi possível encontrar os dados do alvo.");
                    return;
                }

                new HistoryMenu(viewer, target, message.getCategory()).handle();
                break;
            }

            case Constant.REDIS_ACCOUNT_NICKNAME_MENU_CHANNEL: {
                AccountNicknameMenuMessage message = Core.GSON.fromJson(json, AccountNicknameMenuMessage.class);

                if (message == null) return;

                Player player = Bukkit.getPlayer(message.getPlayerId());

                if (player == null) return;

                Core.getPlatform().runSync(() -> {
                    new NicknameMenu(player, null).handle();
                });

                break;
            }

            case Constant.REDIS_PUNISH_OPEN_CHANNEL: {
                PunishOpenMessage message = Core.GSON.fromJson(json, PunishOpenMessage.class);

                if (message == null) return;

                Player viewer = Bukkit.getPlayer(message.getStaffId());

                if (viewer == null) return;

                Core.getPlatform().runSync(() -> new PunishMenu(viewer, message.getStaffId(), message.getTargetName(), message.getPreSelectedCategory()).handle());
                break;
            }

            case Constant.REDIS_ACCOUNT_NICK_CHANGE_CHANNEL: {
                AccountNickChangeMessage message = Core.GSON.fromJson(json, AccountNickChangeMessage.class);

                if (message == null) return;

                Account account = message.getAccount();

                Player player = account.player();
                if (player == null) return;

                String nickname = message.getNickname();

                boolean itsNotReset = !account.getName().equalsIgnoreCase(nickname);

                Skin skin = itsNotReset ? SkinLibrary.random() : account.getDefaultProfileSkin();

                account.setSkin(skin);

                /* Enviando Protocolos */
                if (ServerOptions.CHANGE_SKIN_AND_TAG_IN_FAKE)
                    ProtocolHandler.changePlayerSkin(player, skin, false);

                if (ServerOptions.CHANGE_SKIN_AND_TAG_IN_FAKE)
                    account.setTag(itsNotReset ? Tag.MEMBER : account.getDefaultTag());

                ProtocolHandler.changePlayerName(player, nickname);

                Core.getPlatform().runSync(() -> TagManager.updateTag(account), 10);

                account.send(itsNotReset ? "§aAlteração concluída! O seu nome foi alterado." : "§eVocê voltou para o seu nome original.");
                account.sound(Sound.NOTE_PLING);

                break;
            }

            case Constant.REDIS_ACCOUNT_SKIN_RESET_CHANNEL: {
                AccountSkinResetMessage message = Core.GSON.fromJson(json, AccountSkinResetMessage.class);

                if (message == null) return;

                Account account = message.getAccount();

                if (account == null) return;

                account.setDefaultProfileSkin();

                break;
            }

            // Gerar nova sala no Arcade
            case Constant.REDIS_ROOM_REQUEST_CHANNEL: {
                RoomRequestMessage message = Core.GSON.fromJson(json, RoomRequestMessage.class);

                if (message == null) return;

                ArcadeCategory arcade = message.getArcade();

                // Servidor de jogos não é o mesmo que foi solicitado
                if (!arcade.getServer().equals(Core.getServerType())) return;

                ArcadeHolder game = BukkitCore.getManager().getArcade().read(arcade);

                if (game == null) {
                    new RoomInfoMessage(message.getRequester(), RoomInfoMessage.InfoType.GAME_NOT_FOUND, null, null, null, null, 0).send();
                    return;
                }

                Slot slot = message.getSlot();

                Map map = game.getMap(message.getMapId());

                if (map == null) {
                    new RoomInfoMessage(message.getRequester(), RoomInfoMessage.InfoType.UNKNOWN_ERROR, null, null, null, null, 0).send();
                    return;
                }

                Type mode = message.getType();

                Room room = game.setupRoom(map, slot, mode);

                if (room == null) {
                    new RoomInfoMessage(message.getRequester(), RoomInfoMessage.InfoType.UNKNOWN_ERROR, null, null, null, null, 0).send();
                    return;
                }

                new RoomInfoMessage(message.getRequester(), RoomInfoMessage.InfoType.DONE, room.getIdentifier(), arcade, slot, mode, map.getId()).send();

                Core.getPlatform().runSync(() -> {
                    if (room.getWorld() == null) {
                        new RoomInfoMessage(message.getRequester(), RoomInfoMessage.InfoType.UNKNOWN_ERROR, null, null, null, null, 0).send();
                        return;
                    }

                    room.getProperties().putAll(message.getProperties());
                    room.loadConfigurations();
                    new RoomInfoMessage(message.getRequester(), RoomInfoMessage.InfoType.SENDING_TO, room.getIdentifier(), arcade, slot, mode, map.getId()).send();

                }, 20L);

                break;
            }

            case Constant.REDIS_LOBBY_TELEPORT_CHANNEL: {
                LobbyTeleportMessage message = Core.GSON.fromJson(json, LobbyTeleportMessage.class);

                if (message == null) return;

                if (Core.getServerType() == null || !Core.getServerType().equals(ServerType.HUB)) {
                    break;
                }

                UUID playerId = message.getPlayerId();
                ServerType minigameServerType = message.getMinigameServerType();

                try {
                    Class<?> teleportDataClass = Class.forName("com.hish.lobby.util.LobbyTeleportData");
                    java.lang.reflect.Method setMethod = teleportDataClass.getMethod("setPendingTeleport", UUID.class, ServerType.class);
                    setMethod.invoke(null, playerId, minigameServerType);
                } catch (Exception e) {
                    Core.getLogger().warning("Erro ao processar teleporte de lobby: " + e.getMessage());
                }

                break;
            }

            // Executar comandos do jogador
            case Constant.REDIS_ACCOUNT_EXECUTE_COMMAND_CHANNEL: {
                AccountExecuteCommandMessage message = Core.GSON.fromJson(json, AccountExecuteCommandMessage.class);
                
                if (message == null) return;
                
                Player player = Bukkit.getPlayer(message.getSender());
                
                if (player == null) return;
                
                String command = message.getCommand().replace("/", "");
                
                Bukkit.getScheduler().runTask(BukkitCore.getInstance(), () -> {
                    Bukkit.getServer().dispatchCommand(player, command);
                });
                break;
            }
        }
    }
}
