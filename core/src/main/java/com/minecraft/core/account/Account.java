package com.minecraft.core.account;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.context.AccountContext;
import com.minecraft.core.account.context.objects.auth.AuthMetadata;
import com.minecraft.core.account.context.objects.auth.state.AuthState;
import com.minecraft.core.account.context.objects.block.Block;
import com.minecraft.core.account.context.objects.clan.ClanMetadata;
import com.minecraft.core.account.context.objects.cosmetic.CosmeticEntry;
import com.minecraft.core.account.context.objects.fake.Fake;
import com.minecraft.core.account.context.objects.friend.Friend;
import com.minecraft.core.account.context.objects.friend.FriendMetadata;
import com.minecraft.core.account.context.objects.friend.request.FriendRequest;
import com.minecraft.core.account.context.objects.joinmessage.JoinMessageMetadata;
import com.minecraft.core.account.context.objects.medal.Medal;
import com.minecraft.core.account.context.objects.permission.Permission;
import com.minecraft.core.account.context.objects.punishment.PunishmentHistory;
import com.minecraft.core.account.context.objects.rank.Rank;
import com.minecraft.core.account.context.objects.rank.info.RankInfo;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.route.RouteContext;
import com.minecraft.core.account.context.objects.skin.SkinMetadata;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.account.context.objects.tag.prefix.TagPrefix;
import com.minecraft.core.account.context.objects.tag.role.TagRole;
import com.minecraft.core.account.context.objects.toggle.Toggle;
import com.minecraft.core.api.clan.Clan;
import com.minecraft.core.api.clan.color.ClanColor;
import com.minecraft.core.api.clan.member.ClanMember;
import com.minecraft.core.api.clan.role.ClanRole;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.party.Party;
import com.minecraft.core.api.punishment.Punishment;
import com.minecraft.core.api.joinmessage.JoinMessage;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.api.skin.Skin;
import com.minecraft.core.api.skin.objects.SkinType;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.backend.database.redis.message.types.account.AccountGlobalMessage;
import com.minecraft.core.backend.database.redis.message.types.route.RouteSearchMessage;
import com.minecraft.core.member.Member;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.list.TimeUtil;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.github.paperspigot.Title;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Account {

    private final UUID id;
    private String name;

    private AccountContext context;

    public Account(UUID id, String name) {
        this.id = id;
        this.name = name;
        /* Configure Metadata */
        AccountContext context = new AccountContext();

        if (Core.isUntouchable(id)) {
            context.getRankInfo().setRank(Rank.builder().type(RankType.ADMIN).build());
        } else {
            context.getRankInfo().setRank(Rank.builder().type(RankType.MEMBER).build());
        }

        context.setHistory(new PunishmentHistory(Core.isUntouchable(id)));

        this.context = context;
    }

    @Override
    public boolean equals(Object accountObj) {
        if (accountObj == null || getClass() != accountObj.getClass()) return false;

        Account account = (Account) accountObj;

        return account.getId().equals(id) && account.getName().equalsIgnoreCase(name);
    }

    protected void save(String... fields) {
        for (String field : fields)
            Core.getAccountData().update(this, field);
    }

    /* --------------------------
           Métodos utilitários
    ---------------------------- */
    public Player player() {
        return Core.getPlatform().getPlayer(id, Player.class);
    }

    public ProxiedPlayer proxiedPlayer() {
        return Core.getPlatform().getPlayer(id, ProxiedPlayer.class);
    }

    public void sendGlobal(String... message) {
        new AccountGlobalMessage(id, message).send();
    }

    public void send(String... message) {
        Core.getPlatform().sendMessage(id, message);
    }

    public void send(BaseComponent... message) {
        Core.getPlatform().sendMessage(id, message);
    }

    public void send(List<String> message) {
        message.forEach(this::send);
    }

    public void sound(Sound sound) {
        player().playSound(player().getLocation(), sound, 2, 2);
    }

    public void title(String title) {
        title(title, "");
    }

    public void title(String title, String subTitle) {
        player().sendTitle(new Title(title, subTitle, 0, 20, 80));
    }

    public void title(String title, String subTitle, int stay, int fadeOut) {
        player().sendTitle(new Title(title, subTitle, 0, stay, fadeOut));
    }

    /* --------------------------
            Área dos membros
     ---------------------------- */
    public <T extends Member> T getMember(Class<T> memberClass) {
        return Core.getMemberController().of(id, memberClass);
    }

    public Member loadMember(ServerType server) {
        switch (server) {
            case DUELS:
                return Core.getDuelsData().of(id);
            case PVP:
                return Core.getPvpData().of(id);
            case BEDWARS:
                return Core.getBedWarsData().of(id);
            case EGGWARS:
                return Core.getEggWarsData().of(id);
            case SKYWARS:
                return Core.getSkyWarsData().of(id);
            default:
                return null;
        }
    }

    public void setName(String name) {
        this.name = name;

        /* Alterar o nome no Clan */
        if (hasClan()) {
            Clan clan = getClan();

            ClanMember member = clan.getMember(id);

            if (member != null) {
                member.setName(name);
                clan.updateMember(member);
            }
        }

        save("name");
    }

    public String getNickname() {
        return context.getFake().isValid() ? context.getFake().getNick() : name;
    }

    public String getColoredName() {
        return getTag().getColor() + (getTag().ordinal() >= Tag.PARTNER.ordinal() ? "§o" : "") + getNickname();
    }

    public String getFormattedDisplayName() {
        if (getTag() == Tag.MAX_PLUS) {
            char colorChar = getMaxPlusColor();
            int level = getMaxPlusLevel();
            String maxColor = level == 2 ? "§b" : level == 3 ? "§6" : "§5";
            return maxColor + getNickname() + "§" + colorChar + "+";
        }
        return getTag().getColor() + (getTag().isRole(com.minecraft.core.account.context.objects.tag.role.TagRole.SPECIAL) || getTag().isRole(com.minecraft.core.account.context.objects.tag.role.TagRole.STAFF) ? "§o" : "") + getNickname();
    }

    public void checkInfos() {
        refreshRankInfo();
        refreshPermissions();
    }

    /* --------------------------
          Contexto da Conta
   ---------------------------- */
    public void saveContext(AccountContext context) {
        this.context = context;
        save("context");
    }

    /* --------------------------
        Histórico de Punições
    ---------------------------- */
    public PunishmentHistory getHistory() {
        return context.getHistory();
    }

    public Punishment getActivePunishment(PunishmentCategory category) {
        Punishment punishment = getHistory().getPunishments(search -> search.getCategory().equals(category) && search.isValid())
                .stream().findFirst().orElse(null);

        if (punishment != null && punishment.hasExpired()) {
            punishment.revoke(Constant.DEFAULT_ID, "Expirado");

            updatePunishment(punishment);
            return null;
        }

        return punishment;
    }

    public void updateHistory(PunishmentHistory history) {
        context.setHistory(history);
        saveContext(context);
    }

    public void addPunishmentToHistory(Punishment punishment) {
        PunishmentHistory history = getHistory();

        history.addPunishment(punishment);
        updateHistory(history);
    }

    public void updatePunishment(Punishment punishment) {
        PunishmentHistory history = getHistory();

        history.updatePunishment(punishment);
        updateHistory(history);
    }

    public List<Punishment> getPunishments(PunishmentCategory category) {
        return getHistory().getPunishments(category);
    }

    /* --------------------------
                Clan
     ---------------------------- */
    public Clan getClan() {
        return Core.getClanData().of(context.getClan().getId());
    }

    public boolean isClanLeader() {
        return hasClan() && getClan().isLeader(id);
    }

    public boolean hasClan() {
        return getClan() != null;
    }

    public boolean hasClanRole(ClanRole role) {
        return hasClan() && getClan().getMember(id).getRole().ordinal() >= role.ordinal();
    }

    public void setClan(UUID clanId) {
        ClanMetadata clan = context.getClan();

        clan.setId(clanId);
        clan.setLastUpdate(System.currentTimeMillis());

        context.setClan(clan);
        saveContext(context);
    }

    public String getClanName() {
        return hasClan() ? getClan().getName() : "...";
    }

    public String getClanTag() {
        if (!hasClan()) return "...";
        Clan clan = getClan();
        String tag = clan.getTag();
        String colorCode = clan.getTagColor() != null && ClanColor.isValid(clan.getTagColor()) 
                ? clan.getTagColor() 
                : ClanColor.getDefaultColor();
        
        ClanColor clanColor = ClanColor.getByCode(colorCode);
        
        // Verificar se é uma cor com gradiente
        if (clanColor != null && clanColor.hasGradient()) {
            return " " + applyGradient(tag, clanColor.getGradientStart(), clanColor.getGradientEnd());
        }
        
        // Se a tag já contém códigos de cor, usa ela diretamente (cores misturadas)
        if (tag.contains("§") || tag.contains("&")) {
            // Converte & para § e aplica maiúsculo apenas nas letras (não nos códigos de cor)
            tag = formatTagWithColors(tag);
            return " " + colorCode + "[" + tag + "]";
        }
        
        // Se não tem códigos de cor, usa a cor definida normalmente
        return " " + colorCode + "[" + tag.toUpperCase() + "]";
    }
    
    /**
     * Aplica gradiente em uma tag, dividindo no meio
     */
    private String applyGradient(String tag, String startColor, String endColor) {
        tag = tag.toUpperCase();
        int length = tag.length();
        int midPoint = length / 2;
        
        StringBuilder result = new StringBuilder("[");
        
        // Primeira metade com cor inicial
        for (int i = 0; i < midPoint; i++) {
            result.append(startColor).append(tag.charAt(i));
        }
        
        // Segunda metade com cor final
        for (int i = midPoint; i < length; i++) {
            result.append(endColor).append(tag.charAt(i));
        }
        
        result.append("§r]");
        return result.toString();
    }
    
    /**
     * Formata uma tag que contém códigos de cor, convertendo & para § e aplicando maiúsculo apenas nas letras
     */
    private String formatTagWithColors(String tag) {
        StringBuilder result = new StringBuilder();
        boolean inColorCode = false;
        
        for (int i = 0; i < tag.length(); i++) {
            char c = tag.charAt(i);
            
            if (c == '&' || c == '§') {
                // É um código de cor
                result.append('§');
                inColorCode = true;
            } else if (inColorCode) {
                // É o caractere do código de cor
                result.append(c);
                inColorCode = false;
            } else {
                // É um caractere normal, aplicar maiúsculo
                result.append(Character.toUpperCase(c));
            }
        }
        
        return result.toString();
    }

    /* --------------------------
                Rank
     ---------------------------- */
    public Rank getRank() {
        return getRankInfo().getRank();
    }

    public RankInfo getRankInfo() {
        return context.getRankInfo();
    }

    public RankType getRankType() {
        return getRank().getType();
    }

    public List<Rank> getAllRanks() {
        List<Rank> list = new ArrayList<>(Collections.singletonList(getRank()));

        list.addAll(getAvailableRanks());

        return list;
    }

    public List<Rank> getAvailableRanks() {
        return getRankInfo().getAvailableRanks();
    }

    public boolean hasRank(RankType type) {
        RankType current = getRankType();
        return current != null && current.ordinal() >= type.ordinal();
    }

    public boolean hasAvailableRank(RankType type) {
        return getAvailableRanks().stream().anyMatch(rank -> rank.getType() != null && rank.getType().equals(type));
    }

    public boolean hasRank(Rank rank) {
        return hasRank(rank.getType());
    }

    public boolean hasOnlyRank(RankType rank) {
        RankType type = getRank().getType();
        return type != null && type.equals(rank);
    }

    public boolean isVIP() {
        return hasRank(RankType.VIP) || isStaffer();
    }

    public boolean isStaffer() {
        return hasRank(RankType.HELPER);
    }

    public void setRank(Rank rank) {
        RankInfo info = getRankInfo();

        // Se o rank atual for nulo, tratar como MEMBER
        RankType currentType = getRankType();
        if (currentType == null) {
            info.setRank(Rank.builder().type(RankType.MEMBER).build());
            currentType = RankType.MEMBER;
        }

        // Evitar adicionar o mesmo rank mais de uma vez
        if (currentType.equals(rank.getType()) || hasAvailableRank(rank.getType())) {
            return;
        }

        // Se o rank atual for menor que o novo, substituir o novo no lugar do atual, e adicionar o atual na lista de ranks
        if (currentType.ordinal() < rank.getType().ordinal()) {
            Rank current = info.getRank();

            List<Rank> rankList = new ArrayList<>(info.getAvailableRanks());

            rankList.removeIf(search -> search.getType().equals(rank.getType()) || search.getType().equals(current.getType()));
            rankList.add(current);

             RankInfo newInfo = new RankInfo();
 
             newInfo.setRank(rank);
             newInfo.setAvailableRanks(rankList);
             newInfo.touchRankTimestamp();
 
             context.setRankInfo(newInfo);
             saveContext(context);

            JsonObject json = new JsonObject();

            json.addProperty("id", id.toString());
            json.addProperty("rank", Core.GSON.toJson(rank));

            Core.getRedis().publish(Constant.REDIS_ACCOUNT_RANK_UPDATE_CHANNEL, json.toString());
        } else {
            // Se o rank atual for maior ou igual ao novo, apenas adicionar o novo na lista de ranks
            info.getAvailableRanks().add(rank);

            context.setRankInfo(info);
            saveContext(context);
        }
    }

    public void removeRank(RankType rankType) {
        Rank current = getRank();

        Core.message("Rank atual: " + current,
                "");

        RankInfo info = getRankInfo(), newInfo = new RankInfo();

        Core.message("Info atual: " + info,
                "",
                "Nova info: " + newInfo);

        List<Rank> rankList = new ArrayList<>(info.getAvailableRanks());

        Core.message("Lista de ranks (ANT UP): " + rankList);

        rankList.removeIf(rank -> rank.getType() != null && rank.getType().equals(rankType));

        Core.message("Lista de ranks (POS UP): " + rankList);

        if (rankType.equals(current.getType())) {
            Rank highest = rankList.stream().filter(rank -> !rank.hasExpired())
                    .max(Comparator.comparing(rank -> rank.getType().ordinal()))
                    .orElse(Rank.builder().type(RankType.MEMBER).build());

            Core.message("Maior rank: " + highest);

            rankList.removeIf(rank -> rank.getType().equals(highest.getType()));

            Core.message("Outro lista de ranks: " + rankType);

            newInfo.setRank(highest);

            Core.message("New info UP: " + newInfo);

            JsonObject json = new JsonObject();

            json.addProperty("id", id.toString());
            json.addProperty("rank", Core.GSON.toJson(highest));

            Core.message("Json Received: " + json.toString());

            Core.getRedis().publish(Constant.REDIS_ACCOUNT_RANK_UPDATE_CHANNEL, json.toString());
        } else {
            newInfo.setRank(getRank());

            Core.message("New info (NAO E MESMO CARGO): " + newInfo);
        }

        newInfo.setAvailableRanks(rankList);

        Core.message("New info (Updated): " + newInfo);

        context.setRankInfo(newInfo);
        saveContext(context);

        Core.message("Rank info context: " + context.getRankInfo());
    }

    protected void refreshRankInfo() {
        boolean requireUpdate = false;

        RankInfo info = context.getRankInfo();

        Rank actualRank = getRank();

        if (actualRank.hasExpired()) {
            Rank member = Rank.builder().type(RankType.MEMBER).build();

            Rank rankToSet = !getAvailableRanks().isEmpty()
                    ? getAvailableRanks().stream().filter(rank -> !rank.hasExpired())
                    .max(Comparator.comparing(rank -> rank.getType().ordinal())).orElse(member)
                    : member;

            info.setRank(rankToSet);

            requireUpdate = true;
        }

        if (!getAvailableRanks().isEmpty()) {
            Iterator<Rank> iterator = getAvailableRanks().iterator();

            while (iterator.hasNext()) {
                Rank rank = iterator.next();

                if (rank != null && rank.hasExpired()) {
                    iterator.remove();

                    requireUpdate = true;
                }
            }
        }

        if (requireUpdate)
            saveContext(context);
    }

    /* --------------------------
                Rota
     ---------------------------- */
    public RouteContext getRoute() {
        return context.getRoute();
    }

    public ArcadeRouteContext getArcadeRoute() {
        return getRoute().getArcade();
    }

    public Server getServer() {
        return Core.getServerData().of(getRoute().getServerPort());
    }

    public ServerType getServerType() {
        return getRoute().getServerType();
    }

    public ServerType getLastServer() {
        return getRoute().getLastServer();
    }

    public boolean inServer(ServerType serverType) {
        ServerType current = getServerType();
        return current != null && current.equals(serverType);
    }

    public boolean inServer(int serverId) {
        return getServer().getId() == serverId;
    }

    public void setRoute(RouteContext route) {
        context.setRoute(route);
        saveContext(context);
    }

    public void setRoute(Room arena, String teamId, ArcadeState state, Join join) {
        Server server = Core.getServerData().local();

        setRoute(RouteContext.builder()
                .senderId(id)
                .serverType(server.getType())
                .serverId(server.getId())
                .serverPort(server.getPort())
                .arcade(ArcadeRouteContext.builder()
                        .arcade(arena.getArcade().getCategory())
                        .roomId(arena.getId())
                        .mapId(arena.getMap().getId())
                        .type(arena.getType())
                        .slot(arena.getSlot())
                        .serverId(server.getId())
                        .teamId(teamId)
                        .state(state)
                        .arenaIdentifier(arena.getIdentifier())
                        .maxPlayers(arena.getMaxPlayers())
                        .join(join)
                        .build())
                .updatedAt(System.currentTimeMillis())
                .build());
    }

    public void setRoute(Room arena, Join join) {
        Server server = Core.getServerData().local();

        setRoute(RouteContext.builder()
                .senderId(id)
                .serverType(server.getType())
                .serverId(server.getId())
                .serverPort(server.getPort())
                .arcade(ArcadeRouteContext.builder()
                        .arcade(arena.getArcade().getCategory())
                        .roomId(arena.getId())
                        .mapId(arena.getMap().getId())
                        .type(arena.getType())
                        .serverId(server.getId())
                        .arenaIdentifier(arena.getIdentifier())
                        .slot(arena.getSlot())
                        .maxPlayers(arena.getMaxPlayers())
                        .join(join)
                        .build())
                .updatedAt(System.currentTimeMillis())
                .build());
    }

    public void setArcadeRoute(ArcadeRouteContext arcadeRoute) {
        RouteContext route = getRoute();

        route.setArcade(arcadeRoute);
        context.setRoute(route);

        saveContext(context);
    }

    public void setArcadeState(ArcadeState state) {
        RouteContext route = getRoute();
        ArcadeRouteContext arcadeRoute = getArcadeRoute();

        arcadeRoute.setState(state);
        route.setArcade(arcadeRoute);

        context.setRoute(route);
        saveContext(context);
    }

    public boolean hasReconnect() {
        return Core.getReconnectData().of(id) != null;
    }

    public void redirect(RouteContext route) {
        if (hasParty() && !getParty().isAuthor(id) && route.getArcade() != null && !route.getArcade().isLinked() && route.getServerType().isArcade()) {
            send("§cApenas o dono da party pode procurar salas.");
            return;
        }

        if (route.getSenderId() == null) {
            send("§cO remetente da rota não pode ser nulo.");
            return;
        }

        // Limpar rota antiga antes de verificar
        RouteContext oldRoute = getRoute();
        Core.getRouteData().delete(id);

        // Verificar se está realmente conectado na mesma sala (apenas se estiver procurando uma sala específica)
        if (route.getArcade() != null && route.getArcade().getRoomId() > 0 && oldRoute != null && oldRoute.getArcade() != null) {
            // Se está procurando uma sala arcade e já está na mesma sala
            if (oldRoute.getArcade().getRoomId() == route.getArcade().getRoomId() &&
                oldRoute.getArcade().getMapId() == route.getArcade().getMapId() &&
                Core.getPlatform().isOnlinePlayer(id)) {
                send("§cVocê já está conectado nesta sala.");
                return;
            }
        }

        Core.getRouteData().save(route);

        new RouteSearchMessage(this, route).send();
    }

    public void redirect(ArcadeRouteContext route) {
        redirect(RouteContext.builder()
                .senderId(id)
                .serverType(route.getArcade().getServer())
                .arcade(route)
                .build());
    }

    public void redirect(Room arena, Join join) {
        ArcadeRouteContext route = ArcadeRouteContext.builder()
                .arcade(arena.getArcade().getCategory())
                .roomId(arena.getId())
                .mapId(arena.getMap().getId())
                .slot(arena.getSlot())
                .serverId(Core.getServerId())
                .arenaIdentifier(arena.getIdentifier())
                .state(ArcadeState.ALIVE)
                .maxPlayers(arena.getMaxPlayers())
                .join(join)
                .build();

        redirect(route);

        /* Enviar os jogadores da party para a sala */
        if (hasParty() && getParty().isAuthor(id)) {
            route.setLink(getParty().getMembersId());

            getParty().redirect(route);
        }
    }

    public void redirect(ServerType type) {
        redirect(RouteContext.builder()
                .senderId(id)
                .serverType(type)
                .build());
    }

    public void redirectToHub() {
        ServerType serverType = getServerType();
        if (serverType == null) {
            redirect(ServerType.HUB);
            return;
        }
        ServerType lobby = serverType.getServerLobby();
        redirect(lobby != null ? lobby : ServerType.HUB);
    }

    /* --------------------------
            Party
    ---------------------------- */
    public Party getParty() {
        if (context.getPartyIdentifier() == null || context.getPartyIdentifier().isEmpty()) {
            return null;
        }
        try {
            return Core.getPartyData().of(context.getPartyIdentifier());
        } catch (Exception e) {
            Core.getLogger().warning("[Account] Erro ao carregar party para " + getName() + ": " + e.getMessage());
            // Limpar o identifier da party corrompida
            context.setPartyIdentifier(null);
            saveContext(context);
            return null;
        }
    }

    public boolean hasParty() {
        try {
            Party party = getParty();
            return party != null && party.isMember(id);
        } catch (Exception e) {
            // Se houver erro, considerar que não tem party
            return false;
        }
    }

    public boolean isPartyOwner() {
        try {
            Party party = getParty();
            return party != null && party.isAuthor(id);
        } catch (Exception e) {
            return false;
        }
    }

    public void setParty(String identifier) {
        context.setPartyIdentifier(identifier);
        saveContext(context);
    }

    public void resetParty() {
        setParty("...");
    }

    /* Cosmetic Methods */
    public List<CosmeticEntry> getCollectibleEntries() {
        return context.getCollectibles();
    }

    public List<Collectible> getCollectibles() {
        return Core.getCollectibleController().list().stream()
                .filter(collectible -> collectible != null && (collectible.isFree() 
                        || isStaffer()
                        || collectible.hasAccess(getRank().getType())
                        || getCollectibleEntries().stream().anyMatch(entry -> entry.getIdentifier().equalsIgnoreCase(collectible.getIdentifier()))))
                .collect(Collectors.toList());
    }

    public List<Collectible> getCollectibles(CollectibleCategory category) {
        return getCollectibles().stream()
                .filter(collectible -> collectible.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    public List<Collectible> getCollectibles(CollectibleCategory category, CollectibleRarity rarity) {
        return getCollectibles().stream()
                .filter(collectible -> collectible.getCategory().equals(category) && collectible.getRarity().equals(rarity))
                .collect(Collectors.toList());
    }

    public int getTotalCollectibles(CollectibleCategory category, CollectibleRarity rarity) {
        return getCollectibles(category, rarity).size();
    }

    public Collectible getCollectible(String identifier) {
        return Core.getCollectibleController().of(identifier);
    }

    public boolean hasCollectible(String identifier) {
        return getCollectible(identifier) != null;
    }

    public List<String> getActiveCollectibles() {
        return context.getActiveCollectibles();
    }

    public boolean isUsingCollectible(Collectible collectible) {
        return getActiveCollectibles().contains(collectible.getIdentifier());
    }

    public void activateCollectible(Collectible collectible) {
        List<String> list = getActiveCollectibles();

        if (list.contains(collectible.getIdentifier())) return;

        Iterator<String> iterator = list.iterator();

        while (iterator.hasNext()) {
            String id = iterator.next();

            String category = id.split(":")[0];

            if (collectible.getCategory().name().equalsIgnoreCase(category))
                iterator.remove();
        }

        list.add(collectible.getIdentifier());
        saveContext(context);
    }

    public void removeActiveCollectible(Collectible collectible) {
        getActiveCollectibles().remove(collectible.getIdentifier());
        saveContext(context);
    }

    public void setCollectible(String identifier) {
        getCollectibleEntries().add(new CosmeticEntry(identifier));
        saveContext(context);
    }

    /* Favorite Collectibles */
    public List<String> getFavoriteCollectibles() {
        return context.getFavoriteCollectibles();
    }

    public boolean isFavoriteCollectible(Collectible collectible) {
        return getFavoriteCollectibles().contains(collectible.getIdentifier());
    }

    public void addFavoriteCollectible(Collectible collectible) {
        String id = collectible.getIdentifier();
        List<String> favorites = getFavoriteCollectibles();
        if (!favorites.contains(id)) {
            favorites.add(id);
            saveContext(context);
        }
    }

    public void removeFavoriteCollectible(Collectible collectible) {
        String id = collectible.getIdentifier();
        List<String> favorites = getFavoriteCollectibles();
        if (favorites.remove(id)) {
            saveContext(context);
        }
    }

    public Map<String, List<Integer>> getFavoriteMaps() {
        return context.getFavoriteMaps();
    }

    public List<Integer> getFavoriteMaps(String arcadeName) {
        return context.getFavoriteMaps().getOrDefault(arcadeName.toLowerCase(), new ArrayList<>());
    }

    public boolean isFavoriteMap(String arcadeName, int mapId) {
        return getFavoriteMaps(arcadeName).contains(mapId);
    }

    public void addFavoriteMap(String arcadeName, int mapId) {
        String key = arcadeName.toLowerCase();
        List<Integer> favorites = context.getFavoriteMaps().computeIfAbsent(key, k -> new ArrayList<>());
        if (!favorites.contains(mapId)) {
            favorites.add(mapId);
            saveContext(context);
        }
    }

    public void removeFavoriteMap(String arcadeName, int mapId) {
        String key = arcadeName.toLowerCase();
        List<Integer> favorites = context.getFavoriteMaps().get(key);
        if (favorites != null && favorites.remove((Integer) mapId)) {
            if (favorites.isEmpty()) {
                context.getFavoriteMaps().remove(key);
            }
            saveContext(context);
        }
    }

    public void toggleFavoriteMap(String arcadeName, int mapId) {
        if (isFavoriteMap(arcadeName, mapId)) {
            removeFavoriteMap(arcadeName, mapId);
        } else {
            addFavoriteMap(arcadeName, mapId);
        }
    }

    /* Toggle Methods */
    public Toggle getToggle() {
        return context.getToggle();
    }

    public void setToggle(Toggle toggle) {
        context.setToggle(toggle);
        saveContext(context);
    }

    public void resetToggle() {
        setToggle(new Toggle());
    }

    public TagPrefix getTagPrefix() {
        return getToggle().getTagPrefix();
    }

    public void setTagPrefix(TagPrefix prefix) {
        getToggle().setTagPrefix(prefix);
        setToggle(getToggle());
    }

    public boolean isUsingTagPrefix(TagPrefix prefix) {
        return getTagPrefix().equals(prefix);
    }

    /* Cooldown Methods */
    public void setCooldown(String key, long expiresAt) {
        context.getCooldown().put(key.toLowerCase(), (System.currentTimeMillis() + expiresAt));
        saveContext(context);
    }

    public long getCooldown(String key) {
        return context.getCooldown().get(key.toLowerCase());
    }

    public String getFormattedCooldown(String key) {
        return TimeUtil.formatCooldown(getCooldown(key));
    }

    public boolean hasCooldown(String key) {
        key = key.toLowerCase();

        if (context.getCooldown().containsKey(key)) {
            if (getCooldown(key) > System.currentTimeMillis())
                return true;

            context.getCooldown().remove(key);
            saveContext(context);
            return false;
        }

        return false;
    }

    /* Fake Methods */
    public Fake getFake() {
        return context.getFake();
    }

    public boolean isUsingFake() {
        return getFake().isValid();
    }

    public void setFake(String nickname) {
        Fake fake = getFake();

        fake.setLastNick(fake.getNick());
        fake.setNick(nickname);

        fake.setUpdatedAt(System.currentTimeMillis());

        context.setFake(fake);
        saveContext(context);
    }

    public void resetFake() {
        this.setFake("");
    }

    /* Permission Methods */
    public List<Permission> getPermissions() {
        return context.getPermissions();
    }

    public boolean hasPermission(String key) {
        return (getRank().getType() != null && getRank().getType().getPermissions().stream().anyMatch(permission -> permission.equalsIgnoreCase(key)))
                || getAvailableRanks().stream().anyMatch(rank -> rank.getType() != null && rank.getType().getPermissions().stream().anyMatch(permission -> permission.equalsIgnoreCase(key)))
                || getPermissions().stream().anyMatch(permission -> permission.getKey().equalsIgnoreCase(key));
    }

    public void setPermission(Permission permission) {
        getPermissions().add(permission);
        saveContext(context);
    }

    public void removePermission(String key) {
        getPermissions().removeIf(permission -> permission.getKey().equalsIgnoreCase(key));
        saveContext(context);
    }

    protected void refreshPermissions() {
        Iterator<Permission> iterator = getPermissions().iterator();

        boolean update = false;
        while (iterator.hasNext()) {
            Permission permission = iterator.next();

            if (permission.hasExpired()) {
                iterator.remove();
                update = true;
            }
        }

        if (update)
            saveContext(context);
    }

    /* Block Methods */
    public List<Block> getBlocks() {
        return context.getBlocks();
    }

    public void setBlock(Account account) {
        getBlocks().add(new Block(account.getId()));
        saveContext(context);
    }

    public boolean hasBlock(Account account) {
        return getBlocks().stream().anyMatch(block -> block.getId().equals(account.getId()));
    }

    public void removeBlock(Account account) {
        if (!hasBlock(account)) return;

        getBlocks().removeIf(block -> block.getId().equals(account.getId()));
        saveContext(context);
    }

    public Block getBlock(UUID id) {
        return getBlocks().stream().filter(block -> block.getId().equals(id)).findFirst().orElse(null);
    }

    /* Auth Metadata */
    public AuthMetadata getAuth() {
        return context.getAuth();
    }

    public String getPassword() {
        return getAuth().getPassword();
    }

    public String getLastPassword() {
        return getAuth().getLastPassword();
    }

    public List<String> getRecentPasswords() {
        return getAuth().getRecentPasswords();
    }

    public boolean isRecentPassword(String password) {
        return getRecentPasswords().contains(password);
    }

    public void setPassword(String password) {
        AuthMetadata auth = getAuth();

        if (!isAuthState(AuthState.OK))
            auth.setState(AuthState.OK);

        auth.setLastPassword(auth.getPassword());
        auth.setPassword(password);

        auth.setTimestamp(System.currentTimeMillis());

        if (!isRecentPassword(password))
            auth.getRecentPasswords().add(password);

        context.setAuth(auth);
        saveContext(context);
    }

    public boolean samePassword(String password) {
        return getPassword().equals(password);
    }

    public AuthState getAuthState() {
        return getAuth().getState();
    }

    public boolean isAuthState(AuthState state) {
        return getAuthState().equals(state);
    }

    /* Skin Metadata */
    public SkinMetadata getSkinMetadata() {
        return context.getSkin();
    }

    public Skin getDefaultProfileSkin() {
        return Core.getSkinCacheData().of(this);
    }

    public void setDefaultProfileSkin() {
        Core.getPlatform().runAsync(() -> {
            Skin skin = getDefaultProfileSkin();

            if (!getSkin().isValid() || (getSkin().getType().equals(SkinType.PROFILE) && !skin.equals(getSkin())))
                setSkin(skin);
        });
    }

    public Skin getSkin() {
        return getSkinMetadata().getSkin();
    }

    public Skin getLastSkin() {
        return getSkinMetadata().getLast();
    }

    public boolean hasLastSkin() {
        return !getLastSkin().equals(getSkin());
    }

    public void setSkin(Skin skin) {
        SkinMetadata data = getSkinMetadata();

        data.setLast(getSkin());
        data.setSkin(skin);

        data.setUpdatedAt(System.currentTimeMillis());

        context.setSkin(data);
        saveContext(context);
    }

    public boolean isUsingProfileSkin() {
        return getSkin().getId().equals(id);
    }

    public boolean hasSkin(Skin skin) {
        return getSkin().equals(skin);
    }

    public boolean hasSkin(String displayName) {
        return getSkin().getDisplayName().equalsIgnoreCase(displayName);
    }

    /* Tag Methods */
    public Tag getTag() {
        return context.getTag();
    }

    public String getTagWithCustomColor() {
        Tag tag = getTag();
        if (tag.equals(Tag.MAX_PLUS)) {
            char colorChar = context.getMaxPlusColor();
            // MAX fica roxo (padrão), + fica na cor escolhida
            String result = "§5§lMAX§" + colorChar + "§l+ §5" + ChatColor.getByChar(colorChar) + getName();
            // Truncar para no máximo 16 caracteres (limite do Minecraft)
            if (result.length() > 16) {
                // Tenta manter o nome visível truncando o prefixo se necessário
                String prefix = "§5§lMAX§" + colorChar + "§l+ §5";
                String name = ChatColor.getByChar(colorChar) + getName();
                int maxNameLength = 16 - prefix.length();
                if (maxNameLength > 0) {
                    result = prefix + name.substring(0, Math.min(name.length(), maxNameLength));
                } else {
                    result = prefix.substring(0, 16);
                }
            }
            return result;
        }
        return tag.getByPrefix(getTagPrefix());
    }

    public Tag getDefaultTag() {
        Rank rank = getRank();
        if (rank != null && rank.getType() != null) {
            Tag tag = Tag.of(rank.getType());
            if (tag != null) return tag;
        }
        return Tag.MEMBER;
    }

    public boolean isUsingTag(Tag tag) {
        return getTag().equals(tag);
    }

    public void setTag(Tag tag) {
        context.setTag(tag);
        saveContext(context);
    }

    public char getMaxPlusColor() {
        return context.getMaxPlusColor();
    }

    public void setMaxPlusColor(char color) {
        context.setMaxPlusColor(color);
        saveContext(context);
    }

    public int getMaxPlusLevel() {
        return context.getMaxPlusLevel();
    }

    public void setMaxPlusLevel(int level) {
        context.setMaxPlusLevel(level);
        saveContext(context);
    }

    public boolean hasTag(Tag tag) {
        if (tag.isRole(TagRole.COLORED)) return false;

        if (tag.isRole(TagRole.SPECIAL) && !(hasPermission(tag.getPermission()) || hasRank(RankType.ADMIN)))
            return false;

        if (tag.isRole(TagRole.VIP) && !(hasRank(tag.getSource()) || isStaffer())) return false;

        if (tag.isRole(TagRole.ONLY_THESE) && !tag.isOnlyThese(id)) return false;

        if (tag.isRole(TagRole.STAFF) && !hasRank(RankType.ADMIN) && !hasOnlyRank(tag.getSource())) return false;

        return hasRank(tag.getSource());
    }

    public boolean isAllowColoredChat() {
        if (isUsingFake()) return false;

        return getTag().ordinal() >= Tag.VIP.ordinal();
    }

    /* Join Message Methods */
    public JoinMessageMetadata getJoinMessageMetadata() {
        return context.getJoinMessage();
    }

    public JoinMessage getSelectedJoinMessageEnum() {
        return getJoinMessageMetadata().getSelectedJoinMessage();
    }

    public String getJoinMessage() {
        return getJoinMessageMetadata().getMessage(getRank().getType());
    }

    public void setSelectedJoinMessage(JoinMessage message) {
        getJoinMessageMetadata().setSelectedMessage(message);
        saveContext(context);
    }

    public boolean hasAccessToJoinMessage(JoinMessage message) {
        return message != null && (isStaffer() || message.hasAccess(getRank().getType()));
    }

    public List<JoinMessage> getAvailableJoinMessages() {
        if (isStaffer()) {
            return getJoinMessageMetadata().getAllMessages();
        }
        return getJoinMessageMetadata().getAvailableMessages(getRank().getType());
    }

    /* Friend Methods */
    public FriendMetadata getFriendMetadata() {
        return context.getFriend();
    }

    public List<Friend> getFriends() {
        return new ArrayList<>(getFriendMetadata().getFriends().values());
    }

    public List<Friend> getOnlineFriends() {
        return getFriends().stream().filter(friend -> {
            Account account = Core.getAccountData().of(friend.getId(), false);

            return account != null && account.isOnline() && isFriend(account);
        }).collect(Collectors.toList());
    }

    public void notifyFriends(String message) {
        getOnlineFriends().forEach(friend -> {
            Account account = Core.getAccountController().of(friend.getId());

            if (account != null && !account.inServer(ServerType.AUTH))
                account.sendGlobal("§6[AMIGOS] §e" + message);
        });
    }

    public void notifyFriends() {
        List<Friend> onlines = getOnlineFriends();

        if (!onlines.isEmpty()) {
            send("§6[AMIGOS] §eVocê tem §b" + onlines.size() + " amigo" + (onlines.size() > 1 ? "s" : "") + "§e online!");

            onlines.forEach(friend -> {
                Account account = Core.getAccountController().of(friend.getId());

                if (account != null && !account.inServer(ServerType.AUTH))
                    account.sendGlobal("§6[AMIGOS] §b" + name + "§e entrou.");
            });
        }
    }

    public boolean isFriend(Account account) {
        return isFriend(account.getId());
    }

    public boolean isFriend(UUID id) {
        return context.getFriend().getFriends().containsKey(id);
    }

    public Friend getFriend(UUID id) {
        return context.getFriend().getFriends().get(id);
    }

    public Friend getFriend(String name) {
        return getFriends().stream().filter(friend -> friend.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void addFriend(Account account) {
        context.getFriend().getFriends().put(account.getId(), new Friend(account));
        saveContext(context);
    }

    public void removeFriend(Account account) {
        if (!isFriend(account)) return;

        context.getFriend().getFriends().remove(account.getId());
        saveContext(context);
    }

    public void updateFriend(Friend friend) {
        if (!isFriend(friend.getId())) return;

        context.getFriend().getFriends().put(friend.getId(), friend);
        saveContext(context);
    }

    public List<FriendRequest> getFriendsRequests() {
        return getFriendMetadata().getRequests();
    }

    public boolean hasFriendRequest(Account account) {
        return getFriendsRequests().stream().anyMatch(request -> request.getSender().equals(account.getId()));
    }

    public FriendRequest getFriendRequest(UUID sender) {
        return getFriendsRequests().stream()
                .filter(request -> request.getSender().equals(sender))
                .findFirst()
                .orElse(null);
    }

    public void addFriendRequest(Account account) {
        getFriendsRequests().add(new FriendRequest(account.getId()));
        saveContext(context);
    }

    public void removeFriendRequest(UUID sender) {
        getFriendsRequests().removeIf(request -> request.getSender().equals(sender));
        saveContext(context);
    }

    /* Mutable Methods */
    public boolean isPremium() {
        return context.isPremium();
    }

    public void setPremium(boolean premium) {
        context.setPremium(premium);
        saveContext(context);
    }

    public boolean isOnline() {
        return context.isOnline();
    }

    public void setOnline(boolean online) {
        context.setOnline(online);
        saveContext(context);
    }

    public void updateLastLogin() {
        context.setLastLogin(System.currentTimeMillis());
        saveContext(context);
    }

    public String getIpAddress() {
        return context.getIpAddress();
    }

    public void setIpAddress(String ipAddress) {
        context.setIpAddress(ipAddress);
        saveContext(context);
    }

    public boolean hasLastMessage() {
        return context.getLastMessage() != null && !context.getLastMessage().equals(Constant.DEFAULT_ID);
    }

    public void setLastMessage(UUID lastMessage) {
        context.setLastMessage(lastMessage);
        saveContext(context);
    }

    /* Economy */
    public int getGolds() {
        return context.getGolds();
    }

    public void setGolds(int golds) {
        context.setGolds(golds);
        saveContext(context);
    }

    public void addGolds(int golds) {
        if (golds > 0)
            setGolds(getGolds() + golds);
    }

    public void removeGolds(int golds) {
        if (golds > 0 && getGolds() >= golds)
            setGolds(getGolds() - golds);
    }

    public int getLevel() {
        return context.getLevel();
    }

    public void addLevel() {
        context.setLevel(getLevel() + 1);
        saveContext(context);
    }

    /* Medal System */
    public List<Medal> getMedals() {
        return context.getMedals();
    }

    public void addMedal(Medal medal) {
        getMedals().add(medal);
        saveContext(context);
    }

    public void removeMedal(Medal medal) {
        getMedals().remove(medal);
        saveContext(context);
    }

    public Medal getMedal() {
        return context.getMedal();
    }

    public void setMedal(Medal medal) {
        context.setMedal(medal);
        saveContext(context);
    }

    public boolean hasMedal(Medal medal) {
        // Staffers (helper para cima) têm todas as medalhas
        if (isStaffer()) {
            return true;
        }
        // Verificar se a medalha está nas medalhas padrão do rank
        if (getRankType().getDefaultMedals().contains(medal)) {
            return true;
        }
        return getMedals().contains(medal) || hasPermission(medal.getPermission());
    }

    public boolean isUsingMedal(Medal medal) {
        return getMedal().equals(medal);
    }

    /* ClanTag Color Methods */
    public List<String> getAllowedClanTagColors() {
        return context.getAllowedClanTagColors();
    }

    public void addClanTagColor(String colorCode) {
        List<String> colors = getAllowedClanTagColors();
        if (!colors.contains(colorCode)) {
            colors.add(colorCode);
            saveContext(context);
        }
    }

    public void removeClanTagColor(String colorCode) {
        List<String> colors = getAllowedClanTagColors();
        if (colors.remove(colorCode)) {
            saveContext(context);
        }
    }

    public boolean hasClanTagColorPermission(String colorCode) {
        return getAllowedClanTagColors().contains(colorCode);
    }

    /* Parkour Methods */
    public long getParkourBestTime() {
        return context.getParkourBestTime();
    }

    public void setParkourBestTime(long time) {
        context.setParkourBestTime(time);
        saveContext(context);
    }

    public boolean hasParkourRecord() {
        return getParkourBestTime() > 0;
    }

    public String getFormattedParkourTime() {
        if (!hasParkourRecord()) {
            return "Nenhum";
        }
        return formatTime(getParkourBestTime());
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        long millis = (milliseconds % 1000) / 100;

        if (minutes > 0) {
            return String.format("%dm %02d.%ds", minutes, seconds, millis);
        } else {
            return String.format("%d.%ds", seconds, millis);
        }
    }

    /* Custom Title Methods */
    public String getCustomTitle() {
        return context.getCustomTitle();
    }

    public void setCustomTitle(String title) {
        context.setCustomTitle(title);
        saveContext(context);
    }

    public boolean hasCustomTitle() {
        return getCustomTitle() != null && !getCustomTitle().isEmpty();
    }
}
