package com.minecraft.arcade.bedwars.arcade.arena;

import com.google.gson.JsonObject;
import com.minecraft.arcade.bedwars.arcade.Arcade;
import com.minecraft.arcade.bedwars.arcade.arena.context.ArenaContext;
import com.minecraft.arcade.bedwars.arcade.arena.context.objects.event.Event;
import com.minecraft.arcade.bedwars.arcade.arena.context.objects.event.phase.EventPhase;
import com.minecraft.arcade.bedwars.arcade.arena.context.objects.top.Top;
import com.minecraft.arcade.bedwars.arcade.arena.context.objects.top.enums.TopCategory;
import com.minecraft.arcade.bedwars.arcade.arena.context.objects.top.enums.TopType;
import com.minecraft.arcade.bedwars.arcade.arena.context.objects.top.user.TopUser;
import com.minecraft.arcade.bedwars.arcade.arena.context.phase.ArenaPhaseExecutor;
import com.minecraft.arcade.bedwars.menu.shop.ShopMenu;
import com.minecraft.arcade.bedwars.menu.upgrade.UpgradeMenu;
import com.minecraft.arcade.bedwars.structure.generator.objects.level.GeneratorLevel;
import com.minecraft.arcade.bedwars.structure.generator.objects.type.forge.ForgeGenerator;
import com.minecraft.arcade.bedwars.structure.generator.objects.type.ore.list.DiamondGenerator;
import com.minecraft.arcade.bedwars.structure.generator.objects.type.ore.list.EmeraldGenerator;
import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.arcade.bedwars.structure.generator.objects.type.ore.OreGenerator;
import com.minecraft.arcade.bedwars.structure.team.objects.forge.Forge;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.arcade.bedwars.user.context.UserContext;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.medal.Medal;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.api.punishment.Punishment;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.api.reconnect.Reconnect;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.map.area.Cuboid;
import com.minecraft.core.arcade.room.map.location.SyntheticLocation;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.team.preset.TeamPresetType;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.arcade.slime.SlimeWorldController;
import com.minecraft.core.bukkit.api.firework.FireworkApi;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.api.npc.type.client.NpcClient;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.manager.list.HologramManager;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.member.list.bedwars.objects.enums.BedOre;
import com.minecraft.core.member.list.bedwars.objects.enums.BedShop;
import com.minecraft.core.member.list.bedwars.objects.enums.BedSkin;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.SellerCollectible;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.list.Default;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.list.Mirror;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.list.Random;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.arcade.bedwars.arcade.arena.leaderboard.BedLevelLeaderboard;
import com.minecraft.arcade.bedwars.arcade.arena.leaderboard.BedWinsLeaderboard;
import com.minecraft.arcade.bedwars.arcade.arena.leaderboard.BedFinalKillLeaderboard;
import com.minecraft.arcade.bedwars.arcade.arena.leaderboard.BedWinstreakLeaderboard;
import com.minecraft.core.util.list.TimeUtil;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
public class Arena extends Room {

    private final ArenaContext context;

    public Arena(int id, Arcade arcade, Map map, Slot slot, Type type) {
        super(id, arcade, map, slot, type);

        this.context = new ArenaContext();
    }

    public void unload() {
        getUsers().forEach(user -> user.getAccount().redirectToHub());

        if (getPlayers().isEmpty()) {
            Arcade arcade = getArcade();

            SlimeWorldController.unloadWorld(getWorld(), () -> arcade.removeArena(this));
        }
    }

    public void reload(Player author) {
        Arcade arcade = getArcade();

        Arena arena = arcade.setupRoom(getMap(), getSlot(), getType());

        arena.setCustom(getCustom());

        Core.getPlatform().runSync(() -> {
            if (arena.getWorld() != null) {
                send("§b" + author.getName() + "§e reiniciou a sala.");

                getUsers().forEach(user -> {
                    user.setArena(arena);

                    arena.join(user.getPlayer());
                });

                if (getPlayers().isEmpty())
                    SlimeWorldController.unloadWorld(getWorld(), () -> arcade.removeArena(this));
            }
        }, 10L);
    }

    @Override
    public void join(Player player) {
        User user = (User) User.of(player.getUniqueId());

        UserContext context = user.getContext();

        Arcade arcade = user.getArcade();

        Account account = user.getAccount();

        getPlayers().add(player);

        player.getEnderChest().clear();
        
        // Limpar armadura ao entrar na arena (sala de espera)
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        Location spawn = getLocation("spawn");

        if (user.isPlayer()) {
            if (context.hasReconnect()) {
                Reconnect reconnect = context.getReconnect();
                Team team = getTeamList().stream()
                        .filter(search -> search.getCodeId().equalsIgnoreCase(reconnect.getTeamId()))
                        .findFirst()
                        .orElse(null);

                if (team != null) {
                    team.setBedExpiresAt(-1L);
                    user.setTeam(team);
                    
                    if (!team.getMembers().contains(player.getUniqueId()))
                        team.getMembers().add(player.getUniqueId());

                    if (team.hasBed()) {
                        // Garantir que o jogador seja definido como PLAYER ao reconectar
                        user.setJoin(Join.PLAYER);
                        user.setState(ArcadeState.RESPAWN);
                        
                        // Verificar se o time tem o upgrade de renascimento rápido
                        boolean hasQuickRespawn = team.getUpgrade() != null && team.getUpgrade().isQuickRespawn();
                        int respawnTime = hasQuickRespawn ? 3 : 5;
                        user.setRespawnTime(respawnTime);
                        
                        Tag teamTag = com.minecraft.arcade.bedwars.arcade.arena.context.phase.ArenaPhaseExecutor.getTeamTag(team.getTypeName());
                        user.setTag(teamTag != null ? teamTag : account.getTag());
                        
                        Location spawnLoc = team.getBase();
                        if (spawnLoc != null)
                            player.teleport(spawnLoc);
                        
                        account.title("§e§lRECONECTANDO", "§7Você renascerá em §e" + respawnTime + " segundos§7!");
                        player.sendMessage("§aVocê reconectou! Renascendo em " + respawnTime + " segundos...");
                        send(team.getColor() + account.getNickname() + "§7 reconectou.");
                    } else {
                        user.setState(ArcadeState.DEAD);
                        user.setTag(Tag.SPECTATOR);
                        
                        Location spawnLoc = team.getBase();
                        if (spawnLoc != null)
                            player.teleport(spawnLoc.clone().add(0, 20, 0));
                        
                        player.sendMessage("§cO seu time foi eliminado enquanto você estava fora.");
                        account.title("§c§lELIMINADO", "§eO seu time foi eliminado!");
                        send("§7" + account.getNickname() + "§e reconectou (time eliminado).");
                    }
                    
                    TagManager.updateTag(account);
                    Core.getPlatform().runSync(() -> handleEntities(user), 5L);
                    Core.getPlatform().runSync(() -> getUsers().forEach(this::updateSidebarTeams), 3L);
                    account.setRoute(this, team.getCodeId(), user.getState(), user.getJoin());
                } else {
                    user.setTeam(null);
                    user.setTag(account.getTag());
                    user.setState(ArcadeState.ALIVE);
                    player.teleport(spawn);
                    handleLeaderboards(user);
                }

                Core.getReconnectData().delete(player.getUniqueId());
            } else {
                user.setTeam(null);
                user.setTag(account.getTag());
                user.setState(ArcadeState.ALIVE);
                player.teleport(spawn);
                handleLeaderboards(user);

                Tag tag = user.getTag();
                send(tag.getColor() + (tag.ordinal() >= Tag.PARTNER.ordinal() ? "§o" : "") + account.getNickname()
                        + "§e entrou na sala. (§b" + getTotalPlayers() + "§e/§b" + getMaxPlayers() + "§e)");
            }
        } else {
            user.setTag(Tag.SPECTATOR);
            handleVanish(player);
            player.teleport(spawn);
        }

        hideAndShow(user);

        arcade.load(user);

        account.setRoute(this, user.getTeam() != null ? user.getTeam().getCodeId() : "", user.getState(), user.getJoin());
    }

    @Override
    public void quit(Player player) {
        User user = (User) User.of(player.getUniqueId());

        if (user.isPlayer() && !user.inState(ArcadeState.DEAD)) {
            Team team = user.getTeam();

            if (team != null) {
                if (isPhase(RoomPhase.PLAYING)) {
                    boolean hasBed = team.hasBed();
                    
                    if (hasBed) {
                        ArcadeRouteContext route = ArcadeRouteContext.builder()
                                .arcade(getArcade().getCategory())
                                .roomId(getId())
                                .mapId(getMap().getId())
                                .slot(getSlot())
                                .serverId(Core.getServerId())
                                .arenaIdentifier(getIdentifier())
                                .state(ArcadeState.ALIVE)
                                .maxPlayers(getMaxPlayers())
                                .teamId(team.getCodeId())
                                .build();
                        
                        Reconnect reconnect = new Reconnect(player.getUniqueId(), route);
                        Core.getReconnectData().save(reconnect);
                        
                        Core.getLogger().info("[BedWars] Reconexão criada para " + player.getName() + " (expira em 3 minutos)");
                    } else {
                        team.getMembers().remove(player.getUniqueId());
                        Core.getLogger().info("[BedWars] " + player.getName() + " removido do time (sem cama)");
                    }
                    
                    Core.getPlatform().runSync(() -> {
                        send(team.getColor() + player.getName() + "§7 desconectou.");

                        if (team.getPlayers().isEmpty() && team.hasBed()) {
                            team.setBedExpiresAt(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(3));
                            Core.getLogger().info("[BedWars] Time " + team.getTypeName() + " ficou vazio. Cama expira em 3 minutos.");
                        }

                        if (!hasBed) {
                            user.setState(ArcadeState.DEAD);
                            getArcade().handleDefeatStats(user);
                        }
                        
                        long alivePlayers = getUsers().stream()
                                .filter(u -> u.isPlayer() && !u.inState(ArcadeState.DEAD))
                                .count();
                        
                        if (alivePlayers == 0) {
                            Core.getLogger().info("[BedWars] Todos os jogadores desconectaram. Fechando partida...");
                            send("§cTodos os jogadores desconectaram. A partida será encerrada.");
                            Core.getPlatform().runSync(this::unload, 60L);
                            return;
                        }
                        
                        getUsers().forEach(this::updateSidebarTeams);
                    }, 3L);
                } else if (isPhase(RoomPhase.WAITING) || isPhase(RoomPhase.STARTING)) {
                    team.getMembers().remove(player.getUniqueId());
                    
                    Account account = user.getAccount();
                    Tag tag = user.getTag();
                    send(tag.getColor() + (tag.ordinal() >= Tag.PARTNER.ordinal() ? "§o" : "") + account.getNickname()
                            + "§e saiu da sala. §7(§b" + getTotalPlayers() + "§7/§b" + getMaxPlayers() + "§7)");
                }
            }
        }

        getPlayers().remove(player);
    }

    public void handlePulse() {
        if (isPhase(RoomPhase.WAITING) && canBeCount())
            setPhase(RoomPhase.STARTING);

        if (isPhase(RoomPhase.STARTING)) {
            if (context.isCounterEnabled()) {
                if (canBeCount()) {
                    if (isFull() && getTime() > 10)
                        setTime(10);

                    setTime(getTime() - 1);

                    final int time = getTime();

                    if (time == 10) {
                        title("§c§l" + getTime(), "§csegundos!");

                        sound(Sound.CLICK);
                    }

                    if (time <= 5 && time >= 1) {
                        send("§eA partida inicia em §c" + time + "§e segundo" + (time > 1 ? "s" : "") + "!");
                        title((time <= 3 ? "§a" : "§c") + time, "§ePrepare-se!");

                        sound(Sound.CLICK);
                    }

                    if (time <= 0)
                        setPhase(RoomPhase.PLAYING);
                } else if (!isCustom() && !context.isIgnoreMinPlayers()) {
                    setPhase(RoomPhase.WAITING);
                }
            }
        }

        if (isPhase(RoomPhase.PLAYING)) {
            setFullTime(getFullTime() + 1);

            checkTeamBeds();
            updateGenerators();
            
            Event event = getEvent();

            if (context.isCounterEnabled()) {
                if (event != null) {
                    int eventTime = event.getTime();

                    if (eventTime > 0)
                        event.setTime(eventTime - 1);

                    if (eventTime == 0) {
                        event.handle(this);
                        updateEventSidebar(event);
                    } else
                        updateEventSidebar(event);
                }
            }

            for (Player player : getPlayers()) {
                User user = (User) User.of(player.getUniqueId());

                if (user == null || !user.isPlayer() || !user.inState(ArcadeState.RESPAWN) || user.getRespawnTime() < 0)
                    continue;

                Account account = user.getAccount();

                Arcade arcade = user.getArena().getArcade();

                int time = user.getRespawnTime();

                user.setRespawnTime(time - 1);

                if (time >= 1 && time <= 5) {
                    account.title("§c§lVOCÊ MORREU!", "§eRenascendo em §c" + time + "§e segundo" + (time > 1 ? "s" : "") + "!",
                            60, 100);

                    account.send("§c" + time + "...");
                    account.sound(Sound.WOOD_CLICK);
                }

                if (time == 0) {
                    final ArcadeState state = user.getState();

                    if (!state.equals(ArcadeState.ALIVE))
                        account.sound(Sound.LEVEL_UP);

                    user.setState(ArcadeState.ALIVE);

                    if (user.inState(ArcadeState.ALIVE))
                        arcade.handleHotbar(user);

                    hideAndShow(user);

                    Location spawn = user.getTeam().getBase();

                    if (spawn != null)
                        player.teleport(spawn);

                    account.title("", "§aRenasceu!");
                }
            }
            
            long teamsCompeting = getTeamList().stream()
                .filter(team -> !team.getMembers().isEmpty() && (team.hasBed() || !team.getPlayers().isEmpty()))
                .count();
            
            if (teamsCompeting <= 1) {
                if (context.isShouldCountStatistics()) {
                    send("§4§lAVISO: §cApenas um time restante!");

                    Team winner = getTeamList().stream()
                        .filter(team -> !team.getMembers().isEmpty() && (team.hasBed() || !team.getPlayers().isEmpty()))
                        .findFirst()
                        .orElse(null);

                    setWinner(winner);
                    setPhase(RoomPhase.ENDING);
                }
            }
        }

        if (isPhase(RoomPhase.ENDING)) {
            setTime(getTime() - 1);

            if (getTime() <= 0 || getPlayers().isEmpty())
                setPhase(RoomPhase.RESETTING);

            if (hasWinner())
                getWinner().getPlayers().forEach(player -> Core.getPlatform().runSync(() -> FireworkApi.random(player.getLocation())));
        }

        getAllUsers().forEach(user -> updateSidebarTime(user.getSidebar()));
    }

    protected void updateEventSidebar(Event event) {
        EventPhase phase = event.getPhase();

        getAllUsers().forEach(user -> {
            Sidebar sidebar = user.getSidebar();

            if (sidebar != null && sidebar.hasRow("event")) {
                sidebar.updateRow("event", phase.getName() + " em §a" + TimeUtil.time(event.getTime()));
            }
        });
    }

    public void chat(ChatType type, User user, String message) {
        Account account = user.getAccount();

        // Verificar se o jogador está mutado
        Punishment mute = account.getActivePunishment(PunishmentCategory.MUTE);
        if (mute != null) {
            String timeMessage = mute.isTemporary() 
                ? " §7(Expira em: §f" + com.minecraft.core.util.list.TimeUtil.formatTime(mute.getExpiresAt(), com.minecraft.core.util.list.TimeUtil.TimeFormat.SHORT) + "§7)"
                : "";
            account.send("§cVocê está mutado! Motivo: §f" + mute.getCause() + timeMessage);
            return;
        }

        BedMember member = user.getMember();

        String rank = member.getLevelId() + " ";

        Team team = user.getTeam();

        String prefix = !user.inState(ArcadeState.ALIVE) ? "§7[MORTO] " : "";

        if (Objects.requireNonNull(type) == ChatType.TEAM) {
            List<User> usersToSend = !user.inState(ArcadeState.DEAD)
                    ? getTeamUsers(team)
                    : getAllUsers().stream().filter(search -> !search.inState(ArcadeState.ALIVE)).collect(Collectors.toList());

            usersToSend.forEach(teamMember -> {
                Player player = teamMember.getPlayer();

                if (player != null) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(!user.inState(ArcadeState.DEAD) ? team.getBrackets() + " " : "")
                            .append(prefix).append(rank);
                    
                    // Adicionar medalha se não for NONE
                    if (account.getMedal() != null && !account.getMedal().equals(Medal.NONE)) {
                        builder.append(account.getMedal().getColoredSymbol()).append(" ");
                    }
                    
                    if (account.getTag() == Tag.MAX_PLUS) {
                        char colorChar = account.getMaxPlusColor();
                        int level = account.getMaxPlusLevel();
                        String maxColor = level == 2 ? "§b" : level == 3 ? "§6" : "§5";
                        builder.append(maxColor).append("§lMAX§").append(colorChar).append("§l+ ").append(maxColor);
                    } else {
                        builder.append(account.getTag().getByPrefix(teamMember.getAccount().getTagPrefix()));
                    }
                    builder.append(account.getNickname())
                            .append(": §f")
                            .append(message);
                    
                    player.sendMessage(builder.toString());
                }
            });

        } else {
            List<User> usersToSend = !user.inState(ArcadeState.DEAD)
                    ? getUsers()
                    : getAllUsers().stream().filter(search -> !search.inState(ArcadeState.ALIVE)).collect(Collectors.toList());

            usersToSend.forEach(target -> {
                Player player = target.getPlayer();

                if (player != null) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(!isSlot(Slot.SOLO) ? "§6[G] " : "")
                            .append(team != null && isPhase(RoomPhase.PLAYING) ?
                                    (!user.inState(ArcadeState.DEAD) ? team.getBrackets() + " " : "") : "")
                            .append(prefix).append(rank);
                    
                    // Adicionar medalha se não for NONE
                    if (account.getMedal() != null && !account.getMedal().equals(Medal.NONE)) {
                        builder.append(account.getMedal().getColoredSymbol()).append(" ");
                    }
                    
                    if (account.getTag() == Tag.MAX_PLUS) {
                        char colorChar = account.getMaxPlusColor();
                        int level = account.getMaxPlusLevel();
                        String maxColor = level == 2 ? "§b" : level == 3 ? "§6" : "§5";
                        builder.append(maxColor).append("§lMAX§").append(colorChar).append("§l+ ").append(maxColor);
                    } else {
                        builder.append(account.getTag().getByPrefix(target.getAccount().getTagPrefix()));
                    }
                    builder.append(account.getNickname())
                            .append(": §f")
                            .append(message);
                    
                    player.sendMessage(builder.toString());
                }
            });
        }
    }

    public enum ChatType {
        TEAM, GLOBAL
    }

    public Arcade getArcade() {
        return (Arcade) super.getArcade();
    }

    @Override
    public void setPhase(RoomPhase phase) {
        if (isPhase(phase)) return;

        super.setPhase(phase);
        ArenaPhaseExecutor.execute(this, phase);
    }

    @Override
    public boolean canBeCount() {
        if (!context.isCounterEnabled()) return false;
        
        int totalPlayers = getTotalPlayers();
        if (totalPlayers < 2) {
            return false;
        }
        
        if (context.isIgnoreMinPlayers()) {
            return true;
        }
        
        return totalPlayers >= getMinPlayers();
    }

    public boolean isProtectedArea(Location center) {
        // Verificar geradores de minério
        for (OreGenerator generator : getGenerators()) {
            Location location = generator.getLocation();

            if (location != null && center.getWorld().equals(location.getWorld()) && center.distance(location) <= 3) {
                return true;
            }
        }

        // Verificar área de proteção das bases dos times
        JsonObject area = getMap().getConfig().get("area_protection").getAsJsonObject();

        int frontAndBack = area.get("frontAndBack").getAsInt(),
                up = area.get("up").getAsInt(),
                side = area.get("side").getAsInt();

        for (Team team : getTeamList()) {
            Location base = team.getBase();
            
            if (base == null) continue;

            SyntheticLocation synthetic = getSynthetic(team.getTypeName().toLowerCase() + "_spawn");

            boolean axisX = synthetic != null && synthetic.isAxisX();

            if (Cuboid.isProtectedArea(base, center, frontAndBack, up, side, axisX))
                return true;
        }

        return false;
    }

    public void handleEntities(User user) {
        Player player = user.getPlayer();

        BedMember member = user.getMember();

        Account account = user.getAccount();

        Team userTeam = user.getTeam();

        for (Team team : getTeamList()) {
            if (team.equals(userTeam)) continue;

            BedSkin skin = BedSkin.of(team.getName());
            if (skin == null) {
                TeamPresetType preset = TeamPresetType.of(team.getColor());
                if (preset != null) {
                    String code = preset.name();
                    skin = BedSkin.of(code);
                    if (skin == null) {
                        skin = switch (code) {
                            case "PINK" -> BedSkin.PURP;
                            case "WHITE" -> BedSkin.WHIT;
                            case "YEL", "YELLOW" -> BedSkin.YEL;
                            case "ORAN", "ORANGE" -> BedSkin.ORAN;
                            case "PURPLE" -> BedSkin.PURP;
                            default -> skin;
                        };
                    }
                }
            }

            if (skin == null) {
                skin = BedSkin.BLUE;
            }

            handleTeamNpc(player, team, skin.getValue(), skin.getSignature());
        }

        String value, signature;

        SellerCollectible seller = (SellerCollectible) member.getActiveCollectible(BedCollectibleType.SELLER_SKIN);

        if (seller != null && seller.isNotEqual(Default.class)) {
            if (!seller.isNotEqual(Random.class)) {
                seller = (SellerCollectible) member.getRandomCollectible(BedCollectibleType.SELLER_SKIN);

                if (seller == null)
                    seller = (SellerCollectible) member.getCollectible(BedCollectibleType.SELLER_SKIN, "Default");
            }

            value = seller.isNotEqual(Mirror.class) ? seller.getValue() : account.getSkin().getValue();
            signature = seller.isNotEqual(Mirror.class) ? seller.getSignature() : account.getSkin().getSignature();
        } else {
            BedSkin bedSkin = BedSkin.of(userTeam.getName());
            if (bedSkin == null) {
                TeamPresetType preset = TeamPresetType.of(userTeam.getColor());
                if (preset != null) {
                    String code = preset.name();
                    bedSkin = BedSkin.of(code);
                    if (bedSkin == null) {
                        bedSkin = switch (code) {
                            case "PINK" -> BedSkin.PURP;
                            case "WHITE" -> BedSkin.WHIT;
                            case "YEL", "YELLOW" -> BedSkin.YEL;
                            case "ORAN", "ORANGE" -> BedSkin.ORAN;
                            case "PURPLE" -> BedSkin.PURP;
                            default -> bedSkin;
                        };
                    }
                }
            }

            if (bedSkin == null) bedSkin = BedSkin.BLUE;

            value = bedSkin.getValue();
            signature = bedSkin.getSignature();
        }

        handleTeamNpc(player, userTeam, value, signature);
    }

    protected void handleTeamNpc(Player player, Team team, String value, String signature) {
        Location shopLocation = getLocation("npc_shop_" + team.getTypeName().toLowerCase());

        if (shopLocation != null) {
            NpcClient shop = getArcade().getManager().getNpc().spawnClient(player, shopLocation, value, signature);

            shop.setAction((target, action) -> {
                if (ableToInteract(target))
                    new ShopMenu(target, BedShop.FAVORITE).handle();
            });

            shop.display();
        }

        Location upgradeLocation = getLocation("npc_upgrade_" + team.getTypeName().toLowerCase());

        if (upgradeLocation != null) {
            NpcClient upgrade = getArcade().getManager().getNpc().spawnClient(player, upgradeLocation, value, signature);

            upgrade.setAction((target, action) -> {
                if (ableToInteract(target))
                    new UpgradeMenu(target).handle();
            });

            upgrade.display();
        }
    }

    protected boolean ableToInteract(Player player) {
        User user = (User) User.of(player.getUniqueId());

        return user != null && user.isPlayer() && user.inState(ArcadeState.ALIVE);
    }

    public Event getEvent() {
        return context.getEvent();
    }

    public List<Team> getTeamList() {
        return getTeams().stream().map(team -> (Team) team).collect(Collectors.toList());
    }

    @Override
    public Team buildTeam(TeamPresetType preset) {
        Location location = getLocation(preset.name().toLowerCase() + "_spawn");
        
        if (location == null) {
            location = getLocation(preset.getName().toLowerCase() + "_spawn");
        }
        
        if (location == null) {
            location = getLocation(preset.name().toLowerCase() + "_bed");
        }

        if (location != null) {
            Core.getLogger().info("[BedWars] Time " + preset.getName() + " criado com spawn em " + 
                location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        } else {
            Core.getLogger().warning("[BedWars] Time " + preset.getName() + " criado SEM SPAWN! Localizações tentadas: " +
                preset.name().toLowerCase() + "_spawn, " + preset.getName().toLowerCase() + "_spawn, " + preset.name().toLowerCase() + "_bed");
        }

        return new Team(preset, this, location, getSlot().getMaxPlayers() / 2);
    }

    public List<Team> buildTeams() {
        return Arrays.stream(TeamPresetType.values())
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .map(this::buildTeam)
                .collect(Collectors.toList());
    }

    public List<Team> buildTeams(ChatColor... colors) {
        List<Team> teamList = buildTeams();

        if (colors == null || colors.length == 0)
            return teamList;

        Set<ChatColor> colorSet = new HashSet<>(Arrays.asList(colors));

        return teamList.stream()
                .filter(team -> colorSet.contains(team.getColor()))
                .collect(Collectors.toList());
    }

    public Team getTeam(Location bedLocation) {
        return getTeamList().stream().filter(team -> team.isYourBed(bedLocation)).findFirst().orElse(null);
    }

    public void clearEmptyTeamBeds() {
        for (Team team : getTeamList()) {
            if (!team.getMembers().isEmpty()) continue;

            Location bed = team.getBedLocation();
            if (bed == null) continue;

            int bx = bed.getBlockX(), by = bed.getBlockY(), bz = bed.getBlockZ();
            World world = bed.getWorld();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        Block block = world.getBlockAt(bx + x, by + y, bz + z);
                        block.setType(Material.AIR);
                    }
                }
            }

            Core.getLogger().info("[BedWars] Cama do time " + team.getTypeName() + " removida (time vazio) em " +
                bx + ", " + by + ", " + bz);

            team.setBedLocation(null);
            Core.getLogger().info("[BedWars] Time " + team.getTypeName() + " marcado como sem cama (time vazio)");
        }
    }

    public List<Team> getAliveTeams() {
        return getTeamList().stream().filter(team -> !team.isDead()).collect(Collectors.toList());
    }

    public List<User> getAllUsers() {
        return User.getList().stream().filter(user -> isPlayer(user.getAccount().getId()))
                .map(user -> (User) user).collect(Collectors.toList());
    }

    public List<User> getUsers() {
        return User.getList().stream()
                .filter(user -> user != null && user.isPlayer() && isPlayer(user.getAccount().getId()))
                .map(user -> (User) user)
                .collect(Collectors.toList());
    }

    public List<User> getAliveUsers() {
        return getUsers().stream().filter(user -> user.inState(ArcadeState.ALIVE)).collect(Collectors.toList());
    }

    public List<User> getAliveUsers(Team team) {
        return getAliveUsers().stream().filter(user -> user.getTeam() != null && user.getTeam().equals(team)).collect(Collectors.toList());
    }

    public List<User> getTeamUsers(Team team) {
        return getUsers().stream().filter(user -> user.getTeam() != null && user.getTeam().equals(team)).collect(Collectors.toList());
    }

    @Override
    public int getTotalPlayers() {
        return getUsers().size();
    }

    public boolean hasTeamsWithBed() {
        return getTeamList().stream().anyMatch(Team::hasBed);
    }

    public void hideAndShow(User user) {
        Player mainPlayer = user.getAccount().player();

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target == null || target.equals(mainPlayer)) continue;

            target.hidePlayer(mainPlayer);
            mainPlayer.hidePlayer(target);
        }

        for (Player targetPlayer : getPlayers()) {
            if (targetPlayer == null || mainPlayer.equals(targetPlayer))
                continue;

            User target = (User) User.of(targetPlayer.getUniqueId());

            if (target == null || !target.getArena().equals(user.getArena())) continue;

            if (user.isVanish()) {
                mainPlayer.showPlayer(targetPlayer);

                if (target.isVanish())
                    targetPlayer.showPlayer(mainPlayer);
            }

            if (user.isSpectator() && !target.isVanish()) {
                mainPlayer.showPlayer(targetPlayer);

                if (target.isSpectator())
                    targetPlayer.showPlayer(mainPlayer);
            }

            if (user.isPlayer()) {
                if (isPhase(RoomPhase.ENDING) && target.isPlayer()) {
                    mainPlayer.showPlayer(targetPlayer);
                    targetPlayer.showPlayer(mainPlayer);
                }

                if (target.isPlayer() && !target.inState(ArcadeState.ALIVE) && !user.inState(ArcadeState.ALIVE))
                    mainPlayer.showPlayer(targetPlayer);

                if (target.isPlayer() && target.inState(ArcadeState.ALIVE))
                    mainPlayer.showPlayer(targetPlayer);

                if (user.inState(ArcadeState.ALIVE))
                    targetPlayer.showPlayer(mainPlayer);
            }
        }
    }

    public void renderSidebarTeams(User user) {
        Sidebar sidebar = user.getSidebar();
        Team userTeam = user.getTeam();

        for (Team team : getTeamList()) {
            if (team.getBase() == null || team.getMembers().isEmpty()) continue;
            if (team.isDead()) continue;

            String firstLetter = team.getName().substring(0, 1).toUpperCase();
            String status = team.getBedName(getAliveUsers(team).size());
            
            sidebar.addRow(team.getCodeId(), team.getColor() + "§l" + firstLetter + " " + team.getColor() + team.getName() + ": "
                    + status
                    + (user.isPlayer() && team.equals(userTeam) && !user.inState(ArcadeState.DEAD)
                    ? " §7(VOCÊ)" : ""));
        }
    }
    
    public void updateSidebarTeams(User user) {
        Sidebar sidebar = user.getSidebar();
        Team userTeam = user.getTeam();

        sidebar.removeRow("website");

        for (Team team : getTeamList()) {
            sidebar.removeRow(team.getCodeId());
        }

        for (Team team : getTeamList()) {
            if (team.getBase() == null || team.getMembers().isEmpty()) continue;
            if (team.isDead()) continue;

            String firstLetter = team.getName().substring(0, 1).toUpperCase();
            String status = team.getBedName(getAliveUsers(team).size());
            
            sidebar.addRow(team.getCodeId(), team.getColor() + "§l" + firstLetter + " " + team.getColor() + team.getName() + ": "
                    + status
                    + (user.isPlayer() && team.equals(userTeam) && !user.inState(ArcadeState.DEAD)
                    ? " §7(VOCÊ)" : ""));
        }
        
        sidebar.blankRow();
        sidebar.addWebsiteRow();
        sidebar.display();
    }


    public void renderSidebarTime(Sidebar sidebar) {
        String prefix = isPhase(RoomPhase.STARTING)
                ? "Inicia em §a"
                : isPhase(RoomPhase.ENDING)
                ? "§cFim de jogo!"
                : "§7Aguardando...";

        String suffix = isPhase(RoomPhase.STARTING)
                ? TimeUtil.formatTime(getTime())
                : "";

        sidebar.addRow("time", prefix + suffix);
        sidebar.blankRow();
    }

    public void updateSidebarTime(Sidebar sidebar) {
        if (isPhase(RoomPhase.PLAYING)) {
            return;
        }

        String prefix = isPhase(RoomPhase.STARTING)
                ? "Inicia em §a"
                : isPhase(RoomPhase.ENDING)
                ? "§cFim de jogo!"
                : "§7Aguardando...";

        String suffix = isPhase(RoomPhase.STARTING)
                ? TimeUtil.formatTime(getTime())
                : "";

        if (sidebar.hasRow("time")) {
            sidebar.updateRow("time", prefix + suffix);
        }
        if (sidebar.hasRow("players")) {
            sidebar.updateRow("players", "Players: §a" + getTotalPlayers() + "/" + getMaxPlayers());
        }
    }

    public Top getTop(TopType type) {
        return context.getTop(type);
    }

    public boolean inTop(User user) {
        return context.getTops().stream().anyMatch(top -> top.inTop(user.getAccount().getId()));
    }

    public void verifyTop(TopType type, UUID id, int value) {
        Top top = getTop(type);

        if (top != null)
            top.setTop(id, value);
    }

    public TopUser getTopUser(TopType type, UUID id) {
        Top top = getTop(type);

        if (top == null) {
            Core.getLogger().warning("O top " + type.name() + " é nulo.");
            return null;
        }

        return top.getUser(id);
    }

    public TopUser getTopUser(TopType type, TopCategory category) {
        Top top = getTop(type);

        if (top == null) {
            Core.getLogger().warning("O top " + type.name() + " é nulo.");
            return null;
        }

        return top.getUser(category);
    }

    public boolean isFinalKill(User user) {
        Team team = user.getTeam();
        
        if (team == null) {
            Core.getLogger().warning("[BedWars] isFinalKill: Jogador " + user.getPlayer().getName() + " não tem time!");
            return true;
        }

        return !team.hasBed() && getAliveUsers(team).size() <= 1;
    }

    public List<OreGenerator> getGenerators() {
        return context.getGenerators();
    }

    public void spawnGenerators() {
        getLocationsByName("ore_generator_" + BedOre.DIAMOND.name().toLowerCase())
                .forEach(signed -> getGenerators().add(new DiamondGenerator(getTotalGenerators(BedOre.DIAMOND) + 1, this)));

        getLocationsByName("ore_generator_" + BedOre.EMERALD.name().toLowerCase())
                .forEach(signed -> getGenerators().add(new EmeraldGenerator(getTotalGenerators(BedOre.EMERALD) + 1, this)));

        // Spawnar forge para TODOS os times, independente de terem jogadores
        for (Team team : getTeamList()) {
            if (team == null) continue;

            Forge forge = team.getForge();

            if (forge != null) {
                forge.spawn();
                Core.getLogger().info("[BedWars] Forge spawnado para time " + team.getTypeName() + 
                    " (Jogadores: " + team.getMembers().size() + ")");
            } else {
                Core.getLogger().warning("[BedWars] Forge não encontrado para time " + team.getTypeName());
            }
        }
    }

    public void checkTeamBeds() {
        for (Team team : getTeamList()) {
            if (team.hasExpired()) {
                Core.getPlatform().runSync(() -> {
                    team.destructBed();

                    Core.getReconnectData().list().stream()
                        .filter(reconnect -> reconnect.getRoute().getArenaIdentifier() != null 
                            && reconnect.getRoute().getArenaIdentifier().equals(getIdentifier())
                            && reconnect.getTeamId() != null
                            && reconnect.getTeamId().equalsIgnoreCase(team.getCodeId()))
                        .forEach(reconnect -> Core.getReconnectData().delete(reconnect.getSender()));

                    getUsers().forEach(this::updateSidebarTeams);
                });

                send("",
                     "§f§lCAMA DESTRUÍDA §f» §7A " + team.getColor() + "Cama do " + team.getName() + 
                     "§7 expirou após §c3 minutos §7sem jogadores online.",
                     "");
                
                sound(Sound.ENDERDRAGON_GROWL, 13, 1);
            }
        }
    }

    public void updateGenerators() {
        for (Team team : getTeamList()) {
            if (team == null) continue;

            Forge forge = team.getForge();

            for (ForgeGenerator generator : forge.getGenerators()) {
                if (generator != null && generator.hasPendentUpdate()) {
                    generator.resetTime();

                    Core.getPlatform().runSync(generator::spawn);
                }
            }
        }

        for (OreGenerator generator : getGenerators()) {
            HologramServer hologram = generator.getHologram();

            if (hologram == null) continue;

            generator.setTime(generator.getTime() - 1);

            int time = generator.getTime();

            if (time <= 0) {
                Core.getPlatform().runSync(generator::spawn);

                generator.setTime(generator.getUpdateTime());
            }

            int lastRow = hologram.getRows().size() - 2;

            if (time <= 0)
                time = 1;

            hologram.setText(0, "§eNível §c" + generator.getLevel().getTag());
            hologram.setText(lastRow, "§eGera em §c" + time + "§e segundo" + (time > 1 ? "s" : ""));
        }
    }

    public void upgradeGenerators(BedOre ore, int updateTime) {
        for (OreGenerator generator : getGenerators(ore)) {
            GeneratorLevel next = GeneratorLevel.values()[generator.getLevel().ordinal() + 1];

            generator.setLevel(next);

            generator.setUpdateTime(updateTime);
            generator.setTime(updateTime);
        }
    }

    public GeneratorLevel getGeneratorsLevel(BedOre ore) {
        OreGenerator generator = getGenerators(ore).stream().findFirst().orElse(null);

        return generator != null ? generator.getLevel() : GeneratorLevel.NONE;
    }

    public List<OreGenerator> getGenerators(BedOre ore) {
        return getGenerators().stream().filter(generator -> generator.getOre().equals(ore)).collect(Collectors.toList());
    }

    public OreGenerator getGenerator(BedOre ore, int id) {
        return getGenerators(ore).stream().filter(generator -> generator.getId() == id).findFirst().orElse(null);
    }

    public int getTotalGenerators() {
        return getGenerators().size();
    }

    public int getTotalGenerators(BedOre ore) {
        return getGenerators(ore).size();
    }

    public void handleLeaderboards(User user) {
        Player player = user.getAccount().player();
        if (player == null || !player.isOnline()) return;

        HologramManager manager = BukkitCore.getManager().getHologram();

        Location levelLoc = getLocation("leaderboard_level");
        if (levelLoc != null && manager.notExistsClient(player, "leaderboard_level")) {
            new BedLevelLeaderboard(player, levelLoc).handle();
        }

        Location winsLoc = getLocation("leaderboard_wins");
        if (winsLoc != null && manager.notExistsClient(player, "leaderboard_wins")) {
            new BedWinsLeaderboard(player, winsLoc).handle();
        }

        Location killLoc = getLocation("leaderboard_kill");
        if (killLoc != null && manager.notExistsClient(player, "leaderboard_kills")) {
            new BedFinalKillLeaderboard(player, killLoc).handle();
        }

        Location winstreakLoc = getLocation("leaderboard_winstreak");
        if (winstreakLoc != null && manager.notExistsClient(player, "leaderboard_winstreak")) {
            new BedWinstreakLeaderboard(player, winstreakLoc).handle();
        }
    }

    public void removeLeaderboards(Player player) {
        HologramManager manager = BukkitCore.getManager().getHologram();
        String[] leaderboardTags = {"leaderboard_level", "leaderboard_rank", "leaderboard_wins", "leaderboard_kills", "leaderboard_winstreak"};
        
        for (String tag : leaderboardTags) {
            HologramClient client = manager.getClient(player, tag);
            if (client != null) {
                manager.removeClient(client);
            }
        }
    }
}
