package com.minecraft.arcade.duels.arcade.arena;

import com.minecraft.arcade.duels.Duels;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.feature.ArcadeFeature;
import com.minecraft.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.arcade.slime.SlimeWorldController;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.firework.FireworkApi;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.member.context.stats.ArcadeStats;
import com.minecraft.core.member.list.duels.DuelMember;
import com.minecraft.core.util.list.DateUtil;
import com.minecraft.arcade.duels.arcade.Arcade;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.api.skin.Skin;
import com.minecraft.core.arcade.ArcadeHolder;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.arcade.duels.arcade.arena.phase.PhaseAction;
import com.minecraft.arcade.duels.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

public class Arena extends Room {

    public Arena(int id, ArcadeHolder arcade, Map map, Slot slot) {
        super(id, arcade, map, slot);

        super.setPhase(RoomPhase.WAITING);
    }

    public List<TeamPreset> getTeamList() {
        return getTeams().stream().map(team -> (TeamPreset) team).collect(Collectors.toList());
    }

    @Override
    public Arcade getArcade() {
        return (Arcade) super.getArcade();
    }

    @Override
    public boolean canBeCount() {
        return getMatchUsers().size() >= getMaxPlayers();
    }

    @Override
    public boolean isFull() {
        return getMatchUsers().size() >= getMaxPlayers();
    }

    public void setSkinChangeByPhase(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account == null) {
            Core.getLogger().info("Não foi possível alterar a skin de " + player.getName() + ". Conta não encontrada!");
            return;
        }

        for (Player target : getPlayers()) {
            if (target == null || target.equals(player) || !target.getWorld().equals(player.getWorld())) continue;

            Account other = Core.getAccountController().of(target.getUniqueId());

            ProtocolHandler.changePlayerSkin(player, isPhase(RoomPhase.WAITING) ? Skin.unknown() : account.getSkin(), target);

            if (other != null)
                ProtocolHandler.changePlayerSkin(target, isPhase(RoomPhase.WAITING) ? Skin.unknown() : other.getSkin(), player);

        }
    }

    @Override
    public void join(Player player) {
        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();

        Join join = user.getJoin();

        getPlayers().add(player);

        player.setWalkSpeed(0.2f);
        player.updateInventory();

        // Aplicando tag
        user.setGainXp(0);
        Tag tagToSet = !join.equals(Join.PLAYER) ? Tag.SPECTATOR : Tag.MASKED;
        user.setTag(tagToSet);
        
        // Para jogadores MASKED, usar formato obfuscado no TAB e nametag
        if (tagToSet == Tag.MASKED) {
            String obfuscatedName = "§f§k" + account.getNickname() + "§r§f";
            player.setDisplayName(obfuscatedName);
            player.setPlayerListName(obfuscatedName);
        } else {
            // Atualizar o TagManager para outras tags
            TagManager.updateTag(account, tagToSet);
        }

        if (!user.getArena().equals(this))
            user.setArena(this);

        /* Entrou como jogador na partida */
        if (join.equals(Join.PLAYER)) {
            // Definindo time
            for (TeamPreset team : getTeamList()) {
                if (team == null || team.isFull()) continue;

                user.setTeam(team);
                team.getMembers().add(player.getUniqueId());
                break;
            }

            player.teleport(getLocation("spawn"));

            setSkinChangeByPhase(player);
            BukkitCore.getManager().getCooldown().resetCooldown(player);

            send(account.getTag().getColor() + getRandomizedName(account.getNickname()) + "§r§e entrou na partida. §7(" + getMatchUsers().size() + "/" + getMaxPlayers() + ")");
        } else {
            /* Entrou em Vanish ou Spectator na partida */
            handleVanish(player);
        }

        Arcade arcade = getArcade();

        arcade.join(user);

        account.setRoute(this, join);

        hideAndShow(user);

        account.title("");
    }

    @Override
    public void quit(Player player) {
        User user = (User) User.of(player.getUniqueId());

        DuelMember member = user.getMember();

        /* Efetuando derrota caso for jogador, e sair durante uma partida */
        if (user.isPlayer() && isPhase(RoomPhase.PLAYING)) {
            ArcadeStats stats = member.getStats(getArcade().getCategory());

            if (stats != null) {
                stats.setDefeats();
                member.updateStats(stats);
            }
        }

        getPlayers().remove(player);

        if (user.isPlayer()) {
            TeamPreset team = user.getTeam();

            // Remover do time
            if (team != null) {
                team.getMembers().remove(player.getUniqueId());

                if (isPhase(RoomPhase.PLAYING))
                    send(team.getColor() + getRandomizedName(user.getAccount().getNickname()) + "§r§e saiu da partida. §7(" + getMatchUsers().size() + "/" + getMaxPlayers() + ")");
            }

            // Remover metadata
            player.removeMetadata("trapped", Duels.getPlugin(Duels.class));

            BukkitCore.getManager().getHologram().removeClients(player);
        }
    }

    public void timer() {
        Arcade arcade = getArcade();

        // Se a sala for customizada, e ja tiver 10s de criada e não tiver jogadores, remover a sala.
        if ((System.currentTimeMillis() - getCreatedAt()) >= 5000
                && isType(Type.CUSTOM) && !isPhase(RoomPhase.RESETTING) && getPlayers().isEmpty()) {
            setSuperPhase(RoomPhase.RESETTING);

            SlimeWorldController.unloadWorld(getWorld(), () -> {
                arcade.unloadArena(this);

                Core.message("[Custom/" + getIdentifier() + "] A arena customizada foi deletada.");
            });
        }

        // Aguardando jogadores, e partir para o Starting...
        if (isPhase(RoomPhase.WAITING) && isFullTeams())
            setPhase(RoomPhase.STARTING);

        // Iniciando partida
        if (isPhase(RoomPhase.STARTING)) {
            // Times cheios, contabilizar...
            if (isFullTeams()) {
                setTime(getTime() - 1);

                if (getTime() == 10) {
                    send("§ePrepare-se! A partida inicia em §c" + getTime() + "§e segundos!");
                    sound(Sound.CLICK);

                    title("§c§l" + getTime(), "§ePrepare-se!");
                }

                if (arcade.hasFeature(ArcadeFeature.WITHOUT_MOVING_STARTING)) {

                    if (getTime() <= 3 && getTime() >= 1) {
                        send("§a" + getTime() + "...");
                        title("", "§a" + getTime() + "...");

                        float pitch = getTime() == 3 ? 1f : getTime() == 2 ? 1.2f : 0.9f;

                        sound(Sound.NOTE_PLING, 1, pitch);
                    }

                    if (getTime() == 0) {
                        send("§a§lVAI!");
                        title("", "§a§lVAI!");

                        sound(Sound.LEVEL_UP);
                    }

                } else if (getTime() <= 5 && getTime() >= 1) {
                    send("§eA partida inicia em §c" + getTime() + "§e segundo" + (getTime() > 1 ? "s" : "") + "!");
                    title("", (getTime() > 3 ? "§c" : getTime() > 1 ? "§e" : "§a") + getTime());

                    sound(Sound.NOTE_PLING);
                }

                // Iniciar partida
                if (getTime() <= 0) {
                    setPhase(RoomPhase.PLAYING);

                    sound(Sound.LEVEL_UP);
                }
            } else
                // Voltar para Aguardando...
                setPhase(RoomPhase.WAITING);
        }

        // Partida em andamento
        if (isPhase(RoomPhase.PLAYING)) {
            setFullTime(getFullTime() + 1);

            // Lidar com condições da partida
            if (canBeCount()) {
                for (Player player : getPlayers()) {
                    User user = (User) User.of(player.getUniqueId());

                    if (user == null || !user.isPlayer() || user.inState(ArcadeState.ALIVE)) continue;

                    Account account = user.getAccount();

                    int time = user.getRespawnTime();

                    if (time > 0)
                        user.setRespawnTime(time - 1);

                    if (time >= 1 && time <= 5) {
                        account.title("", "§c" + time + "...");

                        account.sound(Sound.NOTE_PLING);
                    }

                    if (time == 0) {
                        Location spawn = user.getTeam().getBase();

                        if (spawn != null)
                            player.teleport(spawn);

                        account.sound(Sound.SUCCESSFUL_HIT);

                        user.setState(ArcadeState.ALIVE);

                        hideAndShow(user);

                        arcade.handleHotbar(player, this);

                        account.title("", "§aRenasceu!");
                    }
                }
            } else {
                // Encerrar a partida (Jogadores Insuficientes)
                getTeams().stream().filter(team -> !team.getMembers().isEmpty()).findFirst().ifPresent(this::setWinner);

                send("§4§lAVISO §cNão há jogadores suficientes para continuar!");
                setPhase(RoomPhase.ENDING);
            }
        }

        // Partida encerrando
        if (isPhase(RoomPhase.ENDING)) {
            if (getTime() > 0)
                setTime(getTime() - 1);

            if (getPlayers().isEmpty() || getTime() == 0)
                setPhase(RoomPhase.RESETTING);

            if (hasWinner())
                getWinner().getPlayers().forEach(player -> Core.getPlatform().runSync(() -> FireworkApi.random(player.getLocation())));
        }

        updateTimer();
    }

    @Override
    public void setPhase(RoomPhase phase) {
        if (isPhase(phase)) return;

        super.setPhase(phase);
        PhaseAction.handle(this, phase);

        // Quando a partida começar, revelar os nomes reais
        if (phase == RoomPhase.PLAYING) {
            for (Player player : getPlayers()) {
                User user = (User) User.of(player.getUniqueId());
                if (user == null || !user.isPlayer()) continue;

                Account account = user.getAccount();
                if (account == null) continue;

                String realName = account.getNickname();
                player.setDisplayName(realName);
                player.setPlayerListName(realName);

                // Também atualizar para os outros jogadores
                for (Player target : getPlayers()) {
                    if (target.equals(player)) continue;
                    target.showPlayer(player);
                }
            }
        }
    }

    public void setSuperPhase(RoomPhase phase) {
        super.setPhase(phase);
    }

    public void hideAndShow(User user) {
        Player player = user.getAccount().player();

        // Primeiro, esconda todos os jogadores
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target == null || target.equals(player)) continue;

            player.hidePlayer(target);
            target.hidePlayer(player);
        }

        // Agora, aplique a lógica personalizada para mostrar os jogadores corretamente
        for (Player target : getPlayers()) {
            if (target == null || target.equals(player)) continue;

            User other = (User) User.of(target.getUniqueId());

            if (user.isVanish()) {
                player.showPlayer(target);

                if (other.isVanish())
                    target.showPlayer(player);

                return;
            }

            if (user.isSpectator() && !other.isVanish()) {
                player.showPlayer(target);

                if (other.isSpectator())
                    target.showPlayer(player);

                return;
            }

            if (user.isPlayer()) {
                if (isPhase(RoomPhase.ENDING) && other.isPlayer()) {
                    player.showPlayer(target);
                    target.showPlayer(player);
                }

                if (other.isPlayer()) {
                    if (other.inState(ArcadeState.ALIVE) && user.inState(ArcadeState.ALIVE)) {
                        player.showPlayer(target);
                        target.showPlayer(player);
                    } else if (!other.inState(ArcadeState.ALIVE) && !user.inState(ArcadeState.ALIVE)) {
                        player.showPlayer(target);
                        target.showPlayer(player);
                    }
                }
            }
        }
    }

    public void createTimer(Sidebar sidebar) {
        String prefix = isPhase(RoomPhase.STARTING) ? "Inicia em: " : isPhase(RoomPhase.ENDING) ? "Acaba em: "
                : isPhase(RoomPhase.WAITING) ? "§7Aguardando..." : "Tempo: ";

        String suffix = isPhase(RoomPhase.WAITING) ? ""
                : (isPhase(RoomPhase.STARTING) || isPhase(RoomPhase.ENDING)) ? "§a" + getTime() + "s"
                : "§7" + getTimeFormat();

        sidebar.addRow("time", prefix + suffix);
    }

    public void updateTimer() {
        for (Player player : getPlayers()) {
            User user = (User) User.of(player.getUniqueId());

            if (user == null) continue;

            Sidebar sidebar = user.getSidebar();

            sidebar.updateRow("players", "Players: §a" + getMatchUsers().size() + "/" + getMaxPlayers());
            sidebar.updateRow("id", "§7" + DateUtil.getCurrentDate() + " §8" + user.getArena().getIdentifier());

            String prefix = isPhase(RoomPhase.STARTING) ? "Inicia em: " : isPhase(RoomPhase.ENDING) ? "Acaba em: "
                    : isPhase(RoomPhase.WAITING) ? "§7Aguardando..." : "Tempo: ";

            String suffix = isPhase(RoomPhase.WAITING) ? ""
                    : (isPhase(RoomPhase.STARTING) || isPhase(RoomPhase.ENDING)) ? "§a" + getTime() + "s"
                    : "§7" + getTimeFormat();

            sidebar.updateRow("time", prefix + suffix);
        }
    }

    public void pullBack(Player player) {
        Vector pushDirection = player.getLocation().getDirection().setY(0).normalize().multiply(-0.7);
        pushDirection.setY(0.6);

        player.setVelocity(pushDirection);
    }

    public TeamPreset getTeamByBed(Location blockLocation) {
        return getTeamList().stream().filter(team -> team.isYourBed(blockLocation)).findFirst().orElse(null);
    }

    public List<User> getMatchUsers() {
        return UserModel.getList().stream()
                .map(model -> (User) model)
                .filter(user -> getPlayers().stream().anyMatch(player -> user.getAccount().getId().equals(player.getUniqueId()) && user.isPlayer()))
                .collect(Collectors.toList());
    }

    public List<User> getAliveUsers(TeamPreset team) {
        return getMatchUsers().stream()
                .filter(user -> user.getTeam().equals(team) && user.inState(ArcadeState.ALIVE))
                .collect(Collectors.toList());
    }

    public boolean isValid(Player player) {
        User user = (User) User.of(player.getUniqueId());

        return isPhase(RoomPhase.PLAYING) && user != null && user.isPlayer();
    }

    public String getRandomizedName(String name) {
        StringBuilder result = new StringBuilder("§r§7");
        for (char c : name.toCharArray()) {
            if (Core.RANDOM.nextBoolean()) {
                result.append(Character.toUpperCase(c));
            } else {
                result.append(Character.toLowerCase(c));
            }
            if (Core.RANDOM.nextInt(3) == 0) {
                result.append("§k§r§7");
            }
        }
        return result.append("§r").toString();
    }
}
