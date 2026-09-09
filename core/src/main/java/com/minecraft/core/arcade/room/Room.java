package com.minecraft.core.arcade.room;

import com.google.gson.JsonObject;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.redirect.Redirect;
import com.minecraft.core.arcade.ArcadeHolder;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.feature.ArcadeFeature;
import com.minecraft.core.arcade.room.cabin.Cabin;
import com.minecraft.core.arcade.room.custom.RoomCustom;
import com.minecraft.core.arcade.room.event.ArenaPhaseEvent;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.map.location.SignedLocation;
import com.minecraft.core.arcade.room.map.location.SyntheticLocation;
import com.minecraft.core.arcade.room.map.rollback.RollbackBlock;
import com.minecraft.core.arcade.room.map.rollback.pattern.Pattern;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.team.Team;
import com.minecraft.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.core.arcade.room.team.preset.TeamPresetType;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.style.ArcadeStyle;
import com.minecraft.core.backend.database.redis.message.types.account.AccountVanishMessage;
import com.minecraft.core.util.list.StringUtil;
import com.minecraft.core.util.list.TimeUtil;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.github.paperspigot.Title;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class Room {

    private final int id;
    private final String hashCode;

    private final ArcadeHolder arcade;
    private final Map map;

    private Type type = Type.CASUAL;
    private final Slot slot;

    private final Set<Player> players = new CopyOnWriteArraySet<>();
    private final List<Team> teams = new ArrayList<>();

    private final Set<Cabin> cabins = new HashSet<>();
    private final Set<UUID> reservations = new HashSet<>();

    private final Set<RollbackBlock> rollbackBlocks = new CopyOnWriteArraySet<>();

    private final java.util.Map<String, Object> properties = new HashMap<>();

    private final long createdAt = System.currentTimeMillis();

    private RoomPhase phase = RoomPhase.WAITING, lastPhase = RoomPhase.WAITING;

    private Team winner;
    private RoomCustom custom;

    private int time = 0, fullTime = 0, minPlayers, maxPlayers;

    public Room(int id, ArcadeHolder arcade, Map map, Slot slot) {
        this.id = id;
        this.hashCode = StringUtil.generateLetterCode(4);

        this.arcade = arcade;
        this.map = map;

        this.slot = slot;
        this.maxPlayers = slot.getMaxPlayers();
    }

    public Room(int id, ArcadeHolder arcade, Map map, Slot slot, Type type) {
        this(id, arcade, map, slot);

        this.type = type;
    }

    public abstract void join(Player player);

    public abstract void quit(Player player);

    @Override
    public boolean equals(Object roomObj) {
        if (roomObj == null || roomObj.getClass() != getClass()) return false;

        Room room = (Room) roomObj;

        return room.getId() == id && room.getHashCode().equalsIgnoreCase(hashCode);
    }

    /* Room Methods */
    public void handleBorder() {
        if (map.getConfig().has("border")) {
            World world = getWorld();

            JsonObject json = map.getConfig().getAsJsonObject("border");

            Location spawn = getLocation("spawn");

            double centerX = json.has("center_x") ? json.get("center_x").getAsDouble() : spawn.getX(),
                    centerZ = json.has("center_z") ? json.get("center_z").getAsDouble() : spawn.getZ(),
                    width = json.has("width") ? json.get("width").getAsDouble() : 100;

            WorldBorder border = world.getWorldBorder();

            border.setCenter(centerX, centerZ);
            border.setSize(width);
        }
    }

    public boolean isAvailable() {
        return !isFull() && (isPhase(RoomPhase.WAITING) || isPhase(RoomPhase.STARTING));
    }

    public boolean isType(Type type) {
        return this.type.equals(type);
    }

    public boolean isPhase(RoomPhase phase) {
        return this.phase.equals(phase);
    }

    public void setPhase(RoomPhase phase) {
        this.lastPhase = this.phase;
        this.phase = phase;

        new ArenaPhaseEvent(this, phase).callEvent();
    }

    public boolean isStyle(ArcadeStyle style) {
        return arcade.getCategory().getStyle().equals(style);
    }

    public boolean isCategory(ArcadeCategory category) {
        return arcade.isCategory(category);
    }

    public boolean isPreGame() {
        return isPhase(RoomPhase.WAITING) || isPhase(RoomPhase.STARTING);
    }

    public String getIdentifier() {
        return hashCode + id;
    }

    public boolean isCustom() {
        return type.equals(Type.CUSTOM) && custom != null;
    }

    public String getModeName() {
        return arcade.getCategory().getName() + " " + slot.getId();
    }

    public boolean hasWinner() {
        return winner != null;
    }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    public boolean isSlot(Slot slot) {
        return this.slot.equals(slot);
    }

    public Team getLoser() {
        return teams.stream().filter(team -> !team.equals(winner)).findFirst().orElse(null);
    }

    public boolean isPlayer(UUID id) {
        return players.stream().anyMatch(player -> player.getUniqueId().equals(id));
    }

    public int getTotalPlayers() {
        return players.size();
    }

    public boolean isFullTeams() {
        return teams.stream().allMatch(Team::isFull);
    }

    public boolean isValidTeamDistribution() {
        if (teams.isEmpty()) return true;

        int full = (int) teams.stream().filter(Team::isFull).count(),
                notFull = (int) teams.stream().filter(team -> !team.isFull() && !team.getMembers().isEmpty()).count();

        return full != 1 || notFull < 1;
    }

    public String getTimeFormat() {
        return TimeUtil.time(fullTime);
    }

    public boolean canBeCount() {
        return false;
    }

    public boolean isFiltered(int mapId) {
        return map.getId() > 0 && map.getId() == mapId;
    }

    public List<Account> getAccounts() {
        return new ArrayList<>(Core.getAccountController().filter(account -> players.stream().anyMatch(player -> account.getId().equals(player.getUniqueId()))));
    }

    /* Location Configuration */
    public void loadConfigurations() {

        // Buscando customização
        if (hasProperty("custom")) {
            UUID author = UUID.fromString((String) getProperty("author"));

            String mapListJson = (String) getProperty("mapList");

            List<Map> mapList = Arrays.asList(Core.GSON.fromJson(mapListJson, Map[].class));

            int teamSize = (int) (hasProperty("team_size") ? getProperty("team_size") : slot.getMaxPlayers() / 2);

            this.custom = new RoomCustom(author, mapList, teamSize);

            /* Configurando jogadores mínimos e máximos */
            teams.forEach(team -> team.setMaxPlayers(teamSize));

            setMinPlayers(teamSize * teams.size());
            setMaxPlayers(teamSize * teams.size());
        }
    }

    public void send(String... message) {
        getPlayers().forEach(player -> player.sendMessage(message));
    }

    public void send(BaseComponent... message) {
        getPlayers().forEach(player -> player.sendMessage(message));
    }

    public void sound(Sound sound) {
        sound(sound, 1, 1);
    }

    public void sound(Sound sound, float volume, float pitch) {
        getPlayers().forEach(player -> player.playSound(player.getLocation(), sound, volume, pitch));
    }

    public void title(String title, String subTitle) {
        getPlayers().forEach(player -> player.sendTitle(new Title(title, subTitle, 0, 20, 60)));
    }

    public void title(String title) {
        title(title, "");
    }

    public World getWorld() {
        String[] mapSplit = map.getName().split(" ");
        String mapName = mapSplit.length > 1 ? mapSplit[0] + "-" + mapSplit[1] : map.getName();

        return Bukkit.getWorld(arcade.getName().toLowerCase() + "-" + mapName.toLowerCase() + "-" + id);
    }

    public void handleVanish(Player player) {
        Redirect redirect = Core.getRedirectData().of(player.getUniqueId());

        if (redirect != null) {
            Player target = Bukkit.getPlayer(redirect.getTarget());

            player.setFlying(true);

            if (target != null) {
                player.teleport(target);

                player.sendMessage(new String[]{
                        "",
                        "§cVocê entrou no servidor de " + target.getName(),
                        "§cBoa observação!",
                        ""});
            } else
                player.sendMessage("§cNão foi possível localizar o seu alvo.");

            AccountVanishMessage message = new AccountVanishMessage(player.getUniqueId());

            message.setRedirect(redirect);
            message.send();

            Core.getRedirectData().delete(player.getUniqueId());
        }
    }

    /* Properties */
    public void writeProperty(String name, Object value) {
        properties.put(name.toLowerCase(), value);
    }

    public Object getProperty(String name) {
        return properties.get(name.toLowerCase());
    }

    public void removeProperty(String name) {
        properties.remove(name.toLowerCase());
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name.toLowerCase());
    }

    /* Location */
    public boolean hasLocation(String key) {
        return map.hasLocation(key.toLowerCase());
    }

    public List<SignedLocation> getLocations() {
        return map.getLocations();
    }

    public List<SignedLocation> getLocationsByName(String key) {
        return map.getLocations().stream()
                .filter(location -> location.getName().startsWith(key))
                .collect(Collectors.toList());
    }

    public List<SignedLocation> getSignedLocations(Predicate<SignedLocation> predicate) {
        return getLocations().stream().filter(predicate).collect(Collectors.toList());
    }

    public List<Location> getLocations(Predicate<SignedLocation> predicate) {
        return getLocations().stream()
                .filter(predicate)
                .map(location -> location.getSynthetic().of(getWorld()))
                .collect(Collectors.toList());
    }

    public List<Location> getLocations(String... keys) {
        List<Location> list = new ArrayList<>();

        for (String key : keys) {
            if (!hasLocation(key)) continue;

            Location location = map.getLocation(getWorld(), key.toLowerCase());

            list.add(location);
        }

        return list;
    }

    public Location getLocation(String key) {
        if (!map.hasLocation(key)) return null;

        SignedLocation signed = map.getLocation(key.toLowerCase());

        return signed.getSynthetic().of(getWorld());
    }

    public SyntheticLocation getSynthetic(String name) {
        if (!map.hasLocation(name)) return null;

        SignedLocation signed = map.getLocation(name.toLowerCase());

        return signed.getSynthetic();
    }

    /* Team Configuration */
    public List<Team> getLosers() {
        return teams.stream().filter(team -> hasWinner() && !team.equals(winner)).collect(Collectors.toList());
    }

    public Team buildTeam(TeamPresetType preset) {
        Location location = getLocation(preset.name().toLowerCase() + "_spawn");

        if (location == null)
            location = getLocation(preset.getName().toLowerCase() + "_spawn");

        return new TeamPreset(preset, this, location, slot.getMaxPlayers() / 2);
    }

    public List<Team> buildTeamList() {
        return Arrays.stream(TeamPresetType.values())
                .map(this::buildTeam)
                .collect(Collectors.toList());
    }

    public List<Team> buildTeamList(ChatColor... colors) {
        List<Team> teamList = buildTeamList();

        if (colors == null || colors.length == 0)
            return teamList;

        Set<ChatColor> colorSet = new HashSet<>(Arrays.asList(colors));

        return teamList.stream()
                .filter(team -> colorSet.contains(team.getColor()))
                .collect(Collectors.toList());
    }

    /* Rollback */
    public void addRollBack(Block block, Pattern pattern, RollbackBlock.RollbackType type) {
        rollbackBlocks.add(new RollbackBlock(block, pattern, type));
    }

    public void addRollBack(Block block, RollbackBlock.RollbackType type) {
        this.addRollBack(block, Pattern.of(block.getType(), block.getData()), type);
    }

    public RollbackBlock getRollback(Block block) {
        return rollbackBlocks.stream().filter(b -> block.equals(b.getBlock())).findFirst().orElse(null);
    }

    public boolean isReversible(Block block) {
        return getRollback(block) != null;
    }

    /* Cabin */
    public void resetCabins(JavaPlugin plugin) {
        cabins.forEach(cabin -> cabin.delete(plugin));
        cabins.clear();
    }

    public Cabin createCabin(Location spawn, DyeColor color) {
        Cabin cabin = new Cabin("cabin-" + arcade.getName().toLowerCase() + "-" + cabins.size() + 1, spawn);

        if (color != null)
            cabin.setColor(color);

        if (cabin.build())
            cabins.add(cabin);

        return cabin;
    }

    public Cabin getCabin(String identifier) {
        return cabins.stream().filter(cabin -> cabin.getIdentifier().equalsIgnoreCase(identifier)).findFirst().orElse(null);
    }

    public boolean hasCabins() {
        return !cabins.isEmpty() && arcade.hasFeature(ArcadeFeature.CABINS);
    }

    /* Reservations */
    public void setReservations(List<UUID> reservations) {
        this.reservations.addAll(reservations);
    }

    public void removeReservation(UUID uuid) {
        this.reservations.remove(uuid);
    }

    public boolean isReserved() {
        return !reservations.isEmpty();
    }

    public boolean hasReservation(UUID uuid) {
        return this.reservations.contains(uuid);
    }

    public boolean hasReservations(List<UUID> list) {
        return this.reservations.stream().anyMatch(list::contains);
    }

    public int getTotalReservations() {
        return reservations.size();
    }
}
