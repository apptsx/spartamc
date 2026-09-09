package com.minecraft.core.bungee.service.redis;

import com.minecraft.core.api.party.Party;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.api.punishment.Punishment;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.api.punishment.objects.enums.PunishmentReason;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.backend.database.redis.message.types.account.AccountACFlagMessage;
import com.minecraft.core.backend.database.redis.message.types.account.AccountExecuteCommandMessage;
import com.minecraft.core.backend.database.redis.message.types.account.AccountGlobalMessage;
import com.minecraft.core.backend.database.redis.message.types.punish.PunishExecuteMessage;
import com.minecraft.core.backend.database.redis.message.types.route.RoomInfoMessage;
import com.minecraft.core.backend.database.redis.message.types.route.arcade.ArenaSearchMessage;
import com.minecraft.core.backend.database.redis.message.types.route.arcade.ArenaStatsMessage;
import com.minecraft.core.bungee.BungeeCore;
import com.minecraft.core.bungee.event.list.account.AccountRefreshEvent;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.route.RouteContext;
import com.minecraft.core.backend.database.redis.message.types.account.AccountUpdateMessage;
import com.minecraft.core.backend.database.redis.message.types.route.RouteSearchMessage;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.payload.ServerPayload;
import com.minecraft.core.server.payload.whitelist.ServerWhitelist;
import com.minecraft.core.util.list.ReflectionUtil;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Level;

public class BungeeRedis extends JedisPubSub {

    @Override
    public void onMessage(String channel, String data) {
        JsonObject json = Core.PARSER.parse(data).getAsJsonObject();

        if (json == null) {
            Core.getLogger().severe("Não foi possível recolher os dados do canal " + channel + ".");
            return;
        }

        switch (channel.toLowerCase()) {
            case Constant.REDIS_ACCOUNT_UPDATE_CHANNEL: {
                AccountUpdateMessage message = Core.GSON.fromJson(json, AccountUpdateMessage.class);

                if (message == null) return;

                Account account = message.getAccount();

                if (account == null) {
                    Core.getLogger().log(Level.WARNING, "[AccountUpdate] Conta não encontrada no controller. UUID: " + message.getAccountId());
                    return;
                }

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

            // Recebendo informações de solicitação das salas
            case Constant.REDIS_ROOM_INFO_CHANNEL: {
                RoomInfoMessage message = Core.GSON.fromJson(json, RoomInfoMessage.class);

                if (message == null) return;

                ProxiedPlayer player = BungeeCore.getInstance().getProxy().getPlayer(message.getRequester());

                if (player == null) return;

                Account account = Core.getAccountController().of(player.getUniqueId());

                if (account == null) return;

                RoomInfoMessage.InfoType info = message.getInfo();

                if (info.equals(RoomInfoMessage.InfoType.SENDING_TO) && message.isValid()) {
                    account.send("§a§lSala criada! §aEnviando-te para " + message.getCode() + "...");
                    
                    ArcadeRouteContext route = ArcadeRouteContext.builder()
                            .arcade(message.getArcade())
                            .slot(message.getSlot())
                            .type(message.getMode())
                            .mapId(message.getMapId())
                            .join(Join.PLAYER)
                            .build();

                    Server server = Core.getServerData().of(message.getArcade().getServer());

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

                    account.getContext().getCooldown().put("room_created_" + message.getCode(), System.currentTimeMillis() + 5000);
                    new ArenaSearchMessage(account.getId(), route).send();
                } else if (!info.equals(RoomInfoMessage.InfoType.DONE)) {
                    account.send(info.getMessage());
                }

                break;
            }

            case Constant.REDIS_ACCOUNT_GLOBAL_MESSAGE_CHANNEL: {
                AccountGlobalMessage message = Core.GSON.fromJson(json, AccountGlobalMessage.class);

                if (message == null) return;

                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(message.getId());

                if (player != null)
                    for (String text : message.getMessage())
                        player.sendMessage(new TextComponent(text));

                break;
            }

            case Constant.REDIS_ROUTE_SEARCH_CHANNEL: {
                RouteSearchMessage message = Core.GSON.fromJson(json, RouteSearchMessage.class);

                if (message == null) {
                    Core.getLogger().info("[RouteSearch] Mensagem nula!");
                    return;
                }

                UUID accountId = message.getAccountId();
                RouteContext route = message.getRoute();

                if (accountId == null) {
                    Core.getLogger().log(Level.WARNING, "[RouteSearch] UUID da conta é nulo.");
                    return;
                }

                // Tentar buscar o jogador diretamente no proxy
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(accountId);

                if (player == null) {
                    Core.getLogger().log(Level.WARNING, "[RouteSearch] Jogador não encontrado no proxy. UUID: " + accountId);
                    return;
                }

                // Buscar a conta no controller
                Account account = Core.getAccountController().of(accountId);

                if (account == null) {
                    Core.getLogger().log(Level.WARNING, "[RouteSearch] Conta não encontrada no controller. UUID: " + accountId + " - Conectando mesmo assim...");
                }

                Server server = Core.getServerData().of(route.getServerType());

                if (server == null) {
                    String errorMsg = String.format(Constant.SERVER_NOT_FOUND_MESSAGE, route.getServerType().getName());
                    player.sendMessage(TextComponent.fromLegacyText(errorMsg));

                    Core.getLogger().log(Level.WARNING, "[RouteSearch] Servidor não encontrado no Redis: " + route.getServerType().getName() + 
                            " (ID: " + route.getServerType().getId() + ")");
                    Core.getLogger().log(Level.INFO, "[RouteSearch] Servidores disponíveis no Redis: " + 
                            Core.getServerData().getServerList().stream().map(s -> s.getType().getName() + ":" + s.getType().getId()).collect(java.util.stream.Collectors.joining(", ")));
                    return;
                }

                if (server.getProxyServer() == null) {
                    String errorMsg = String.format(Constant.SERVER_NOT_FOUND_MESSAGE, route.getServerType().getName());
                    player.sendMessage(TextComponent.fromLegacyText(errorMsg));

                    String expectedName = server.getType().getId().toLowerCase() + (server.getType().hasFlag(com.minecraft.core.server.flag.ServerFlag.MULTI_SERVER) ? "-" + server.getId() : "");
                    Core.getLogger().log(Level.WARNING, "[RouteSearch] Servidor encontrado no Redis mas não no BungeeCord: " + route.getServerType().getName() + 
                            " (Esperado no BungeeCord: '" + expectedName + "')");
                    Core.getLogger().log(Level.INFO, "[RouteSearch] Servidores disponíveis no BungeeCord: " + 
                            ProxyServer.getInstance().getServers().keySet().stream().collect(java.util.stream.Collectors.joining(", ")));
                    return;
                }

                ServerPayload payload = server.getPayload();

                ServerWhitelist whitelist = payload.getWhitelist();

                // Verificar whitelist apenas se account estiver disponível
                if (whitelist.isEnabled() && account != null) {
                    if (!(account.isStaffer() || whitelist.hasRank(account.getRank().getType()) || whitelist.hasPlayer(player.getName()))) {
                        player.sendMessage(TextComponent.fromLegacyText("§cA sala está aberta somente para jogadores autorizados."));
                        return;
                    }
                }

                player.connect(server.getProxyServer());

                String playerName = account != null ? account.getNickname() : player.getName();
                Core.getLogger().info("[SR] " + playerName + " acessou o servidor " + server.getName() + ".");
                break;
            }

            case Constant.REDIS_ARENA_STATS_CHANNEL: {
                ArenaStatsMessage message = Core.GSON.fromJson(json, ArenaStatsMessage.class);

                if (message == null) return;

                Account account = Core.getAccountController().of(message.getSender());

                if (account == null) return;

                ArenaStatsMessage.RequestStatus status = message.getStatus();

                if (status.equals(ArenaStatsMessage.RequestStatus.SENDING) && message.getIdentifier() != null) {
                    boolean recentlyCreated = account.getContext().getCooldown().entrySet().stream()
                            .anyMatch(entry -> entry.getKey().startsWith("room_created_") && entry.getValue() > System.currentTimeMillis());
                    
                    if (!recentlyCreated) {
                        account.send("§aEnviando-te para " + message.getIdentifier() + "...");
                    }
                } else if (!status.equals(ArenaStatsMessage.RequestStatus.SEARCHING)) {
                    account.send(status.getName());
                }

                break;
            }

            // Executar comandos
            case Constant.REDIS_ACCOUNT_EXECUTE_COMMAND_CHANNEL: {
                AccountExecuteCommandMessage message = Core.GSON.fromJson(json, AccountExecuteCommandMessage.class);

                if (message == null) return;

                ProxiedPlayer sender = ProxyServer.getInstance().getPlayer(message.getSender());

                if (sender == null) return;

                String command = message.getCommand().replace("/", "");

                ProxyServer.getInstance().getPluginManager().dispatchCommand(sender, command);
                break;
            }

            // Executar punição via GUI
            case Constant.REDIS_PUNISH_EXECUTE_CHANNEL: {
                PunishExecuteMessage message = Core.GSON.fromJson(json, PunishExecuteMessage.class);

                if (message == null) return;

                Account target = Core.getAccountController().of(message.getTargetName());

                if (target == null) {
                    Core.getLogger().warning("Punir: jogador " + message.getTargetName() + " não encontrado.");
                    return;
                }

                Account staff = Core.getAccountController().of(message.getStaffId());

                if (target.getActivePunishment(message.getCategory()) != null) {
                    if (staff != null && staff.isOnline())
                        staff.send("§cO jogador " + target.getName() + " já possui uma punição em aberto.");
                    return;
                }

                Punishment punishment = Punishment.builder()
                        .author(message.getStaffId())
                        .player(target.getId())
                        .server(target.getServerType())
                        .arcade(target.getRoute().isValidArcade() ? target.getArcadeRoute().getArcade() : ArcadeCategory.NONE)
                        .ip(target.getIpAddress())
                        .reason(message.getReason())
                        .category(message.getCategory())
                        .cause(message.getMotive())
                        .expiresAt(message.getExpiresAt())
                        .build();

                Core.getPunishmentData().save(punishment);

                if (message.getCategory().equals(PunishmentCategory.BAN) && target.proxiedPlayer() != null)
                    target.proxiedPlayer().disconnect(TextComponent.fromLegacyText(String.format(Constant.BAN_TEMPLATE_MESSAGE,
                            punishment.isTemporary() ? "temporariamente" : "permanentemente",
                            message.getReason().getName(),
                            punishment.getId())));
                else if (message.getCategory().equals(PunishmentCategory.MUTE))
                    target.send("§cVocê foi " + (punishment.isTemporary() ? "temporariamente" : "permanentemente") + " mutado no servidor.");

                if (staff != null && staff.isOnline())
                    staff.send("§cVocê puniu o jogador " + target.getName() + " por " + message.getReason().getName() + ".");
                break;
            }

            // Alerta de anti-cheat cross-server
            case Constant.REDIS_ACCOUNT_ANTICHEAT_CHANNEL: {
                AccountACFlagMessage message = Core.GSON.fromJson(json, AccountACFlagMessage.class);
                if (message == null) return;

                TextComponent playerName = new TextComponent("§c§o" + message.getPlayerName() + " ");
                playerName.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/go " + message.getPlayerName()));
                playerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§eClique para ir até o jogador")));

                TextComponent alert = new TextComponent("§ffalhou §c" + message.getHackType() + " §7(VL: " + message.getViolations() + "/" + message.getMaxVL() + ")");

                TextComponent full = new TextComponent("§c[ANTICHEAT] ");
                full.addExtra(playerName);
                full.addExtra(alert);

                Core.getAccountController().filter(acc -> acc.isStaffer() && acc.isOnline() && acc.getToggle().isAllowACFlags()).forEach(staff -> {
                    if (staff.isOnline()) {
                        staff.send(full);
                    }
                });
                break;
            }
        }
    }
}
