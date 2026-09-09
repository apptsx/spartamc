package com.minecraft.arcade.duels.arcade;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.api.redirect.Redirect;
import com.minecraft.core.arcade.ArcadeHolder;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.feature.ArcadeFeature;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.arcade.slime.SlimeWorldController;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.bukkit.menu.server.arcade.mode.duels.DuelsMenu;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.member.list.duels.DuelMember;
import com.minecraft.core.member.context.stats.ArcadeStats;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.DateUtil;
import com.minecraft.core.util.list.StringUtil;
import com.minecraft.arcade.duels.Duels;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.arcade.objects.leaderboard.Leaderboard;
import com.minecraft.arcade.duels.arcade.objects.style.SidebarStyle;
import com.minecraft.arcade.duels.user.User;
import com.grinderwolf.swm.plugin.SWMPlugin;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Setter
@IgnoreEvent
public abstract class Arcade extends ArcadeHolder {

    private final File mapsDirectory;

    @Getter
    private final List<Material> blocksAllowedToBreak;

    @Getter
    private SidebarStyle style;

    @Getter
    private int maxScore;

    public Arcade(String mapsDirectory, Integer minRooms, Integer maxRooms, ArcadeCategory category) {
        super(mapsDirectory, minRooms, maxRooms, category);

        this.mapsDirectory = new File(Duels.getInstance().getDataFolder().getAbsolutePath() + "/maps");

        this.blocksAllowedToBreak = new ArrayList<>();
    }

    @Override
    public boolean load() {
        Core.getLogger().info("Iniciando " + getName() + "...");

        if (!loadMaps(mapsDirectory)) return false;

        List<Map> availableMaps = getMaps();

        if (availableMaps.isEmpty()) {
            Core.getLogger().warning("Nenhum mapa disponível para " + getName() + ".");
            return false;
        }

        int numRooms = getMinRooms();
        int mapIndex = 0;

        for (int i = 0; i < numRooms; i++) {
            Map map = availableMaps.get(mapIndex);

            if (setupRoom(map, getCategory().getSlots().get(0), Type.CASUAL) == null) return false;

            mapIndex = (mapIndex + 1) % availableMaps.size();
        }

        Core.getLogger().info("Modo de jogo " + getName() + " iniciado com sucesso.");
        return true;
    }

    @Override
    public void unload() {
        Core.getLogger().info("Encerrando jogo " + getName() + "...");

        getRooms().clear();

        Core.getLogger().info("O jogo " + getName() + " foi desligado com sucesso!");
    }

    @Override
    public Arena setupRoom(Map map, Slot slot, Type type) {
        Instant now = Instant.now();

        int id = (getId().getAndIncrement() + 1);

        Arena arena = new Arena(id, this, map, slot);

        arena.setType(type);

        Core.getLogger().info("[" + getName() + "/" + id + "] Criando arena...");

        String[] mapSplit = map.getName().split(" ");

        String mapName = mapSplit.length > 1 ? mapSplit[0] + "-" + mapSplit[1] : map.getName();

        String templateName = getName().toLowerCase() + "-" + mapName.toLowerCase(),
                worldName = templateName + "-" + id;

        handleArenaData(arena, id, templateName, worldName, now);

        return arena;
    }

    @Override
    public Arena setupRoom(Map map, Slot slot) {
        return setupRoom(map, slot, Type.CASUAL);
    }

    public void unloadArena(Arena arena) {
        arena.getProperties().clear();

        getRooms().remove(arena);
    }

    public void copySourceArena(Arena source) {
        Instant now = Instant.now();

        int id = source.getId();

        Map map = source.getMap();
        Slot slot = source.getSlot();

        Arena arena = new Arena(id, this, map, slot);

        arena.setType(source.getType());
        arena.getProperties().putAll(source.getProperties());

        Core.getLogger().info("[" + getName() + "/" + id + "] Copiando arena...");

        String[] mapSplit = map.getName().split(" ");

        String mapName = mapSplit.length > 1 ? mapSplit[0] + "-" + mapSplit[1] : map.getName();

        String templateName = getName().toLowerCase() + "-" + mapName.toLowerCase(),
                worldName = templateName + "-" + id;

        handleArenaData(arena, id, templateName, worldName, now);
    }

    protected void handleArenaData(Arena arena, int id, String templateName, String worldName, Instant now) {
        Slot slot = arena.getSlot();

        SlimeWorldController.cloneWorldFromTemplate(SWMPlugin.getInstance(), templateName, worldName, () -> {
            final int maxPlayers = slot.getMaxPlayers();

            List<TeamPreset> teamList = arena.buildTeamList(ChatColor.RED, ChatColor.BLUE).stream()
                    .map(team -> (TeamPreset) team)
                    .collect(Collectors.toList());

            teamList.forEach(team -> team.setMaxPlayers(maxPlayers / 2));

            arena.setMinPlayers(maxPlayers);
            arena.setMaxPlayers(maxPlayers);

            arena.getTeams().addAll(teamList);

            handleProperties(arena);

            arena.handleBorder();

            getRooms().add(arena);

            Core.getLogger().info("[" + getName() + "/" + id + "] Arena criada com sucesso. (Tempo médio: " + Util.formatInstant(now) + ")");
        });
    }

    public void checkAndGenerateArenas() {
        int totalArenas = getRooms().size();
        int occupiedArenas = (int) getRooms().stream().filter(room -> !room.isAvailable()).count();

        if (occupiedArenas >= (totalArenas / 2)) {
            final int NUM_ARENAS_TO_GENERATE = 3;

            Core.getLogger().info("[" + getName() + "] Gerando " + NUM_ARENAS_TO_GENERATE + " arenas...");

            int loaded = 0;
            for (int i = 0; i < NUM_ARENAS_TO_GENERATE; i++) {
                Map randomMap = getRandomMap();

                if (randomMap != null) {
                    setupRoom(randomMap, randomMap.getArcade().getSlots().get(0), Type.CASUAL);
                    loaded++;
                }
            }
        }
    }

    @Override
    public Arena findBestArena(UUID sender, ArcadeRouteContext route) {
        Account account = Core.getAccountData().of(sender);

        Redirect redirect = Core.getRedirectData().of(sender);

        if (redirect != null) {
            return (Arena) getRooms().stream()
                    .filter(room -> room.isCategory(route.getArcade()) && room.getId() == route.getRoomId() && room.isFiltered(route.getMapId()))
                    .findFirst()
                    .orElse(null);
        }

        Stream<Arena> stream = getRooms().stream().map(room -> (Arena) room)
                .filter(arena -> arena.isAvailable() && arena.getSlot().equals(route.getSlot()))
                .collect(Collectors.toList()).stream();

        if (route.getType() != null)
            stream = stream.filter(arena -> arena.getType().equals(route.getType()));

        if (route.hasMap())
            stream = stream.filter(arena -> arena.isFiltered(route.getMapId()));

        return stream.filter(arena -> {
            System.out.println("[Search] Filtrando " + arena.getIdentifier() + " para " + sender);

            if (account.isPartyOwner() && route.isLinked() && !arena.hasReservations(route.getLink())) {
                arena.setReservations(route.getLink());

                System.out.println("[PT-Route] " + arena.getIdentifier() + " recebeu +" + route.getLink().size() + " reservas. " +
                        "Total de reservas: " + arena.getTotalReservations());
            }

            if (account.isPartyOwner() && arena.isReserved() && arena.hasReservation(sender)) {
                Core.getLogger().info("[PT-Route] O dono " + account.getNickname() + " entrou em " + arena.getIdentifier() + ".");
                return true;
            }

            if (arena.isReserved() && arena.hasReservation(sender) && !arena.isFull()) {
                arena.removeReservation(sender);

                System.out.println("[PT-Route] " + account.getNickname() + " entrou em " + arena.getIdentifier() + ".");
                return true;
            }

            return arena.isAvailable();
        }).sorted((a1, a2) -> Integer.compare(a2.getTotalPlayers(), a1.getTotalPlayers())).findFirst().orElseGet(() -> createArenaOnDemand(route));
    }

    private Arena createArenaOnDemand(ArcadeRouteContext route) {
        Core.getLogger().info("[" + getName() + "] Nenhuma arena disponível. Criando arena sob demanda...");

        Map randomMap = getRandomMap();

        if (randomMap == null) {
            Core.getLogger().warning("[" + getName() + "] Nenhum mapa disponível para criar arena!");
            return null;
        }

        Slot slot = route.getSlot() != null ? route.getSlot() : getCategory().getSlots().get(0);
        Type type = route.getType() != null ? route.getType() : Type.CASUAL;

        Arena arena = setupRoom(randomMap, slot, type);

        if (arena != null) {
            Core.getLogger().info("[" + getName() + "] Arena criada sob demanda: " + arena.getIdentifier());
        }

        return arena;
    }

    public void handleSidebar(User user) {
        Sidebar sidebar = user.getSidebar();

        Arena arena = user.getArena();

        DuelMember member = user.getMember();

        sidebar.clear();
        sidebar.addRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + arena.getIdentifier()
                + (arena.isType(Type.CUSTOM) ? " (C)" : ""));
        sidebar.blankRow();

        sidebar.addRow("mode", "Modo: §a" + arena.getModeName());
        sidebar.addRow("map", "Mapa: §a" + arena.getMap().getName());
        sidebar.blankRow();

        if (arena.isPhase(RoomPhase.PLAYING)) {
            handleTeamStyle(arena, sidebar);

            if (user.isPlayer()) {
                ArcadeStats stats = member.getStats(getCategory());
                sidebar.addRow("winstreak", "Winstreak: §a" + Util.formatNumber(stats.getWinStreak()));
            } else {
                sidebar.blankRow();
                sidebar.addRow("state", user.isVanish() ? "§cMODO VANISH" : "§eMODO ESPECTADOR");
            }

        } else {
            if (user.isPlayer())
                arena.createTimer(sidebar);
            else
                sidebar.addRow("state", user.isVanish() ? "§cMODO VANISH" : "§eMODO ESPECTADOR");

            sidebar.blankRow();
            sidebar.addRow("players", "Jogadores: §a" + arena.getMatchUsers().size() + "/" + arena.getMaxPlayers());
        }

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(user.getAccount());
    }

    public void handleTeamStyle(Arena arena, Sidebar sidebar) {
        List<TeamPreset> teamList = arena.getTeamList();

        switch (getStyle()) {
            case LATENCY: {
                teamList.forEach(team -> {
                    if (arena.isSlot(Slot.SOLO)) {
                        team.getPlayers().forEach(player -> sidebar.addRow(team.getCodeId(),
                                team.getColor() + player.getName() + ": §7" + Util.formatNumber(player.spigot().getPing()) + "ms"));
                    }
                });
                break;
            }

            case BED: {
                teamList.forEach(team -> sidebar.addRow(team.getCodeId(), team.getBracketsName() + ": " + team.getBedName(arena.getAliveUsers(team).size())));
                break;
            }

            case POINT: {
                teamList.forEach(team -> sidebar.addRow(team.getCodeId(), team.getBracketsName() + ": "
                        + (maxScore <= 5 ? Util.createProgressBar(team.getColor(), ChatColor.GRAY, "⬤", team.getScore(), maxScore, maxScore)
                        : "§7" + Util.formatNumber(team.getScore()))));
                break;
            }
        }

        sidebar.blankRow();
    }

    public void updateTeamStyle(Arena arena, Sidebar sidebar) {
        List<TeamPreset> teamList = arena.getTeamList();

        switch (getStyle()) {
            case LATENCY: {
                teamList.forEach(team -> {
                    if (arena.isSlot(Slot.SOLO)) {
                        team.getPlayers().forEach(player -> sidebar.updateRow(team.getCodeId(),
                                team.getColor() + player.getName() + ": §7" + Util.formatNumber(player.spigot().getPing()) + "ms"));
                    }
                });
                break;
            }

            case BED: {
                teamList.forEach(team -> sidebar.updateRow(team.getCodeId(), team.getBracketsName() + ": " + team.getBedName(arena.getAliveUsers(team).size())));
                break;
            }

            case POINT: {
                teamList.forEach(team -> sidebar.updateRow(team.getCodeId(), team.getBracketsName() + ": "
                        + (maxScore <= 5 ? Util.createProgressBar(team.getColor(), ChatColor.GRAY, "⬤", team.getScore(), maxScore, maxScore)
                        : "§7" + Util.formatNumber(team.getScore()))));
                break;
            }
        }
    }

    public boolean isValid(Player player) {
        User user = (User) User.of(player.getUniqueId());

        return user != null && user.isPlayer() && user.inState(ArcadeState.ALIVE) && user.getArcade().isCategory(getCategory());
    }

    public void start(Player player) {
    }

    public void handleProperties(Arena arena) {

    }

    public void timer(Arena arena) {

    }

    public void join(User user) {
        Player player = user.getAccount().player();

        Arena arena = user.getArena();

        handleSidebar(user);
        handleHotbar(player, arena);

        if (user.isPlayer())
            CompletableFuture.runAsync(() -> {
                new Leaderboard(getCategory(), "winStreak", player, arena, 10).handle();
                new Leaderboard(getCategory(), "wins", player, arena, 10).handle();
            });
    }

    public void handleDeath(User user, User killer, DeathCause cause) {
        Player player = user.getAccount().player();

        Arena arena = user.getArena();
        Arcade arcade = arena.getArcade();

        TeamPreset team = user.getTeam();

        user.setCombat(null);

        // Partida 1v1
        if (killer != null) {
            killer.setKills(killer.getKills() + 1);

            killer.setCombat(null);
        }

        if (arena.isSlot(Slot.SOLO)) {

            sendDeathMessage(arena, user, killer, cause);

            /* Jogo com camas */
            if (arcade.getStyle().equals(SidebarStyle.BED)) {
                if (team.hasBed())
                    user.setState(ArcadeState.RESPAWN);
                else {
                    arena.getTeams().stream().filter(winner -> !winner.equals(team)).findFirst().ifPresent(arena::setWinner);

                    arena.setPhase(RoomPhase.ENDING);
                    user.setState(ArcadeState.DEAD);
                }
            } else if (arcade.getStyle().equals(SidebarStyle.POINT)) {
                /* Jogos de pontuação */
                TeamPreset winner = (TeamPreset) arena.getTeams().stream().filter(found -> !found.equals(team)).findFirst().orElse(null);

                if (winner == null) {
                    Core.getLogger().log(Level.SEVERE, "Não foi possível encontrar o time pontuador da sala " + arena.getIdentifier() + " (" + arcade.getName() + ")");
                    return;
                }

                if (arcade.hasFeature(ArcadeFeature.SCORE_DEATH)) {
                    winner.setScore(winner.getScore() + 1);

                    if (winner.getScore() >= maxScore) {
                        arena.setWinner(winner);
                        arena.setPhase(RoomPhase.ENDING);

                        user.setState(ArcadeState.DEAD);
                    } else
                        arena.setPhase(RoomPhase.RESTARTING);
                } else {
                    arcade.handleHotbar(player, arena);

                    player.teleport(team.getBase());
                }

            } else {
                arena.getTeams().stream().filter(winner -> !winner.equals(team)).findFirst().ifPresent(arena::setWinner);

                arena.setPhase(RoomPhase.ENDING);
                user.setState(ArcadeState.DEAD);
            }
        }

        BukkitCore.getManager().getCooldown().resetCooldown(player);

        if (player.getHealth() < player.getMaxHealth())
            player.setHealth(player.getMaxHealth());

        if (!cause.equals(DeathCause.VOID) && !arcade.hasFeature(ArcadeFeature.NOT_PULL_BACK))
            arena.pullBack(player);
    }

    public void handlePoint(User user, Arena arena, DeathCause cause, TeamPreset winner, TeamPreset loser) {

        if (arena.getAliveUsers(loser).size() > 1) {
            user.setState(ArcadeState.DEAD);
            sendDeathMessage(arena, user, user.inCombat() ? (User) User.of(user.getCombat().getTarget().getUniqueId()) : null, cause);
            return;
        }

        Account account = user.getAccount();
        Player player = account.player();

        winner.setScore(winner.getScore() + 1);

        TextComponent message = new TextComponent("§6§m------------------------------------------");

        message.addExtra("\n\n");

        message.addExtra(StringUtil.StringHelper.makeCenteredMessage(winner.getColor() + account.getNickname()
                + " §7(§a" + Util.formatNumber(player.getHealth()) + "§c❤§7)§e marcou! §7(§6" + winner.getScore() + "º Ponto§7)" + "\n"));

        message.addExtra(StringUtil.StringHelper.makeCenteredMessage(winner.getColor().toString() + winner.getScore() + " §7- "
                + loser.getColor().toString() + loser.getScore() + "\n\n"));

        message.addExtra("§6§m------------------------------------------\n\n");

        arena.send(message);

        if (winner.getScore() >= maxScore) {
            arena.setWinner(winner);
            arena.setPhase(RoomPhase.ENDING);

            loser.getPlayers().forEach(loserPlayer -> {
                User loserUser = (User) User.of(loserPlayer.getUniqueId());

                if (loserUser != null)
                    loserUser.setState(ArcadeState.DEAD);
            });
        } else
            arena.setPhase(RoomPhase.RESTARTING);
    }

    public void sendDeathMessage(Arena arena, User user, User killer, DeathCause cause) {
        boolean hasKiller = killer != null;

        if (hasKiller) {
            Account account = killer.getAccount();

            account.sound(Sound.SUCCESSFUL_HIT);
        }

        Account account = user.getAccount();

        arena.send(user.getTeam().getColor() + arena.getRandomizedName(account.getNickname()) + " §r§e" +
                (cause.equals(DeathCause.PLAYER) ? (hasKiller ? "foi morto por " + killer.getTeam().getColor() + arena.getRandomizedName(killer.getAccount().getNickname()) + "§r" : "morreu")
                        : (hasKiller ? "foi jogado no void por " + killer.getTeam().getColor() + arena.getRandomizedName(killer.getAccount().getNickname()) + "§r" : "caiu no void"))
                + "§e.");
    }


    public void updateStatisticalData(TeamPreset winner, List<TeamPreset> losers) {
        for (UUID id : winner.getMembers()) {
            User user = (User) User.of(id);

            if (user == null) continue;

            DuelMember member = user.getMember();

            ArcadeStats stats = member.getStats(getCategory());

            int gainXp = Core.RANDOM.ints(9, 18).findFirst().orElse(9);

            stats.setWins();
            member.updateStats(stats);

            user.setGainXp(user.getGainXp() + gainXp);
            user.getAccount().send("§b+" + gainXp + " XP");
        }

        for (TeamPreset loser : losers) {
            for (UUID id : loser.getMembers()) {
                User user = (User) User.of(id);

                if (user == null) continue;

                DuelMember member = user.getMember();

                ArcadeStats stats = member.getStats(getCategory());
                stats.setDefeats();
                member.updateStats(stats);
            }
        }
    }

    public enum DeathCause {
        PLAYER, VOID
    }

    public void handleHotbar(Player player, Arena arena) {
    }

    public void handleDefaultHotbar(Player player) {
        User user = (User) User.of(player.getUniqueId());

        Arena arena = user.getArena();

        PlayerInventory inv = player.getInventory();

        player.getInventory().clear();

        if (user.isPlayer() && arena.isPreGame())
            handleWaitingHotbar(inv);

        if (user.inState(ArcadeState.DEAD) || arena.isPhase(RoomPhase.ENDING))
            handleDeathHotbar(inv);

        if (!user.inState(ArcadeState.RESPAWN))
            inv.setItem(8, Item.of(Material.ACACIA_DOOR_ITEM, "§cVoltar").interact(event -> event.getPlayer().performCommand("hub")));
    }

    public void handleWaitingHotbar(PlayerInventory inv) {
        inv.setItem(4, Item.of(Material.EMERALD, "§aMenu do Practice")
                .interact(event -> new DuelsMenu(event.getPlayer()).handle()));
    }

    public void handleDeathHotbar(PlayerInventory inv) {
        inv.setItem(0, Item.of(Material.PAPER, "§aJogar Novamente")
                .interact(event -> event.getPlayer().performCommand("playagain")));
    }
}
