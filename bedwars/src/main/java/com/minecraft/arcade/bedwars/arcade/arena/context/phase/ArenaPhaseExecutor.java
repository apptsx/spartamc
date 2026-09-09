package com.minecraft.arcade.bedwars.arcade.arena.context.phase;

import com.minecraft.arcade.bedwars.BedWars;
import com.minecraft.arcade.bedwars.arcade.Arcade;
import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.arcade.arena.context.objects.event.Event;
import com.minecraft.arcade.bedwars.arcade.arena.context.objects.top.Top;
import com.minecraft.arcade.bedwars.arcade.arena.context.objects.top.user.TopUser;
import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.map.area.Cuboid;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.arcade.slime.SlimeWorldController;
import com.minecraft.core.bukkit.api.title.TitleAnimation;
import com.minecraft.core.bukkit.api.vanish.Vanish;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.util.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.minecraft.core.util.list.StringUtil.StringHelper.makeCenteredMessage;

public class ArenaPhaseExecutor {

    /**
     * Mapeia o nome do time para a tag correspondente.
     * Ex: CYAN -> AQUA (pois não existe tag CYAN, apenas AQUA)
     */
    public static Tag getTeamTag(String typeName) {
        if (typeName == null) return null;
        
        // Mapear CYAN para AQUA (pois a tag é AQUA, não CYAN)
        String mappedName = typeName.equalsIgnoreCase("CYAN") ? "AQUA" : typeName;
        
        Tag tag = Tag.of(mappedName);
        
        // Se ainda não encontrou, tentar com alguns nomes alternativos conhecidos
        if (tag == null) {
            switch (typeName.toUpperCase()) {
                case "CYAN" -> tag = Tag.AQUA;
                case "RED" -> tag = Tag.RED;
                case "BLUE" -> tag = Tag.BLUE;
                case "PINK" -> tag = Tag.PINK;
                case "ORANGE" -> tag = Tag.ORANGE;
                case "YELLOW" -> tag = Tag.YELLOW;
                case "GREEN" -> tag = Tag.GREEN;
                case "AQUA" -> tag = Tag.AQUA;
                case "GRAY", "GREY" -> tag = Tag.GRAY;
                default -> tag = null;
            }
        }
        
        return tag;
    }

    public static void execute(Arena arena, RoomPhase phase) {
        List<User> users = arena.getUsers().stream().filter(user -> !Vanish.has(user.getPlayer())).collect(Collectors.toList());

        Arcade arcade = arena.getArcade();

        switch (phase) {
            case WAITING: {
                arena.setTime(0);
                
                // Resetar flags quando voltar para WAITING
                arena.getContext().setIgnoreMinPlayers(false);
                arena.getContext().setShouldCountStatistics(true);

                users.forEach(arcade::handleSidebar);
                break;
            }
            case STARTING: {
                arena.setTime(5);

                users.forEach(arcade::handleSidebar);
                break;
            }
            case PLAYING: {
                arena.setTime(0);
                arena.setFullTime(0);
                
                // Resetar a flag de ignorar jogadores mínimos quando a partida começa
                arena.getContext().setIgnoreMinPlayers(false);

                // Remover leaderboards de todos os jogadores de forma assíncrona para evitar delay
                CompletableFuture.runAsync(() -> {
                    users.forEach(user -> {
                        Player player = user.getAccount().player();
                        if (player != null && player.isOnline()) {
                            arena.removeLeaderboards(player);
                        }
                    });
                });

                arena.getContext().setEvent(new Event());
                arena.spawnGenerators();

                /* Definir times */

                List<User> playersWithoutTeam = users.stream()
                        .filter(user -> user != null && user.isPlayer() && user.getTeam() == null)
                        .collect(Collectors.toList());

                List<Team> availableTeams = arena.getTeamList().stream()
                        .filter(team -> team != null && team.getBase() != null)
                        .collect(Collectors.toList());

                Core.getLogger().info("[BedWars] Distribuição de times: " + playersWithoutTeam.size() + " jogadores sem time, " + availableTeams.size() + " times disponíveis");

                if (availableTeams.isEmpty()) {
                    Core.getLogger().warning("[BedWars] Nenhum time disponível com base configurada!");
                    return;
                }
                
                // Validar mínimo de 2 jogadores (total de jogadores na arena)
                int totalPlayers = arena.getTotalPlayers();
                if (totalPlayers < 2) {
                    Core.getLogger().warning("[BedWars] Não é possível iniciar partida com menos de 2 jogadores! (Atual: " + totalPlayers + ")");
                    return;
                }
                
                // Se não há jogadores sem time, pula a distribuição automática
                if (playersWithoutTeam.isEmpty()) {
                    Core.getLogger().info("[BedWars] Todos os jogadores já têm time atribuído.");
                } else {
                    
                    // Calcular quantidade de times baseado no maxPlayers de cada time
                    // Se maxPlayers = 1 (Solo), precisa de 1 time por jogador
                    // Se maxPlayers = 2 (Dupla), precisa de ceil(totalPlayers / 2) times
                    // Se maxPlayers = 3 (Trio), precisa de ceil(totalPlayers / 3) times
                    // Se maxPlayers = 4 (Quarteto), precisa de ceil(totalPlayers / 4) times
                    
                    int playersToDistribute = playersWithoutTeam.size();
                    int maxPlayersPerTeam = availableTeams.get(0).getMaxPlayers();
                    int teamsToUse;
                    
                    Core.getLogger().info("[BedWars] MaxPlayers por time: " + maxPlayersPerTeam);
                    
                    if (maxPlayersPerTeam == 1) {
                        // Solo: 1 time por jogador
                        teamsToUse = Math.min(playersToDistribute, availableTeams.size());
                    } else {
                        // Dupla/Trio/Quarteto: dividir jogadores entre times
                        teamsToUse = Math.max(2, Math.min(availableTeams.size(), (int) Math.ceil((double) playersToDistribute / maxPlayersPerTeam)));
                    }
                
                List<Team> activeTeams = availableTeams.subList(0, teamsToUse);
                
                Core.getLogger().info("[BedWars] Usando " + teamsToUse + " times para " + playersToDistribute + " jogadores sem time (maxPlayers por time: " + maxPlayersPerTeam + ")");

                Map<String, List<User>> partyGroups = new HashMap<>();
                List<User> soloPlayers = new ArrayList<>();

                for (User user : playersWithoutTeam) {
                    Account account = user.getAccount();

                    if (account.hasParty()) {
                        String partyId = account.getParty().getIdentifier();
                        partyGroups.computeIfAbsent(partyId, k -> new ArrayList<>()).add(user);
                    } else {
                        soloPlayers.add(user);
                    }
                }

                for (List<User> partyMembers : partyGroups.values()) {
                    // Encontrar time com mais espaço disponível
                    Team bestTeam = activeTeams.stream()
                            .filter(team -> !team.isFull())
                            .max(Comparator.comparingInt(team -> team.getMaxPlayers() - team.getMembers().size()))
                            .orElse(null);

                    if (bestTeam == null) {
                        Core.getLogger().warning("[BedWars] Nenhum time disponível para party de " + partyMembers.size() + " jogadores");
                        break;
                    }

                    int availableSlots = bestTeam.getMaxPlayers() - bestTeam.getMembers().size();

                    if (partyMembers.size() <= availableSlots) {
                        for (User user : partyMembers) {
                            user.setTeam(bestTeam);
                            bestTeam.getMembers().add(user.getPlayer().getUniqueId());
                            Core.getLogger().info("[BedWars] Jogador de party " + user.getPlayer().getName() + " atribuído ao time " + bestTeam.getTypeName());
                        }
                    } else {
                        int membersToAdd = Math.min(partyMembers.size(), availableSlots);

                        for (int i = 0; i < membersToAdd; i++) {
                            User user = partyMembers.get(i);
                            user.setTeam(bestTeam);
                            bestTeam.getMembers().add(user.getPlayer().getUniqueId());
                            Core.getLogger().info("[BedWars] Jogador de party " + user.getPlayer().getName() + " atribuído ao time " + bestTeam.getTypeName());
                        }

                        for (int i = membersToAdd; i < partyMembers.size(); i++) {
                            soloPlayers.add(partyMembers.get(i));
                            Core.getLogger().info("[BedWars] Jogador de party " + partyMembers.get(i).getPlayer().getName() + " movido para solo players (party muito grande)");
                        }
                    }
                }

                Collections.shuffle(soloPlayers);

                for (User user : soloPlayers) {
                    Team targetTeam = activeTeams.stream()
                            .filter(team -> !team.isFull())
                            .min(Comparator.comparingInt(t -> t.getMembers().size()))
                            .orElse(null);

                    if (targetTeam != null) {
                        user.setTeam(targetTeam);
                        targetTeam.getMembers().add(user.getPlayer().getUniqueId());
                        Core.getLogger().info("[BedWars] Jogador " + user.getPlayer().getName() + " atribuído ao time " + targetTeam.getTypeName());
                    } else {
                        Core.getLogger().warning("[BedWars] Não foi possível atribuir time para o jogador " + user.getPlayer().getName());
                    }
                }
                
                // Log final da distribuição
                Core.getLogger().info("[BedWars] Distribuição completa. Times ativos: " + teamsToUse);
                for (Team team : activeTeams) {
                    Core.getLogger().info("[BedWars] Time " + team.getTypeName() + ": " + team.getMembers().size() + " jogadores");
                }
                }



                /* Atualizar dados dos jogadores */
                users.forEach(user -> {
                    Player player = user.getPlayer();

                    Team team = user.getTeam();
                    
                    // Validar se o jogador tem um time
                    if (team == null) {
                        Core.getLogger().warning("[BedWars] ERRO: Jogador " + player.getName() + " não tem time atribuído!");
                        return;
                    }

                    Location base = team.getBase();
                    
                    if (base == null) {
                        Core.getLogger().warning("[BedWars] ERRO: Time " + team.getTypeName() + " não tem base configurada!");
                        return;
                    }

                    Core.getLogger().info("[BedWars] Teleportando " + player.getName() + " para base do time " + team.getTypeName() + 
                        " em " + base.getBlockX() + ", " + base.getBlockY() + ", " + base.getBlockZ());
                    
                    player.teleport(base);

                    Tag tag = getTeamTag(team.getTypeName());

                    if (tag != null) {
                        Core.getLogger().info("[BedWars] Aplicando tag " + tag.getName() + " para " + player.getName());
                        user.setTag(tag);
                    } else {
                        Core.getLogger().warning("[BedWars] AVISO: Tag não encontrada para time " + team.getTypeName() + ", usando tag padrão do jogador");
                        user.setTag(user.getAccount().getTag());
                    }

                    user.setMatches();

                    arcade.load(user);

                    arena.handleEntities(user);

                    user.getAccount().setRoute(arena, team.getCodeId(), user.getState(), user.getJoin());
                    
                    // Ativar habilidades quando a partida começa
                    user.activateAbilitiesOnMatchStart();
                });

                arena.getAllUsers().stream().filter(UserModel::isVanish).forEach(user -> {
                    Player player = user.getPlayer();


                    user.setState(ArcadeState.DEAD);

                    arcade.handleSidebar(user);

                    Core.getPlatform().runSync(() -> arcade.handleHotbar(user), 5L);

                    arena.getPlayers().stream().filter(search -> !search.equals(player)).findFirst().ifPresent(player::teleport);
                });

                handleDestroyColiseum(arena);

                // Remover camas de times sem jogadores (raio de 3 blocos)
                arena.clearEmptyTeamBeds();
                arena.getAllUsers().forEach(arena::updateSidebarTeams);

                // Raio removido do início da partida - agora aparece apenas em kills de VIPs

                arena.send("", "§4§lAVISO: §cAliar-se com outros jogadores é proibido e resulta em punição!", "");
                arena.sound(Sound.LEVEL_UP);

                arena.getAllUsers().forEach(arena::hideAndShow);

                Core.getLogger().info("[" + arcade.getName() + "/" + arena.getIdentifier() + "/" + arena.getMap().getName() + "]" + " A partida foi iniciada com " + arena.getTotalPlayers() + " jogadores!");

                break;
            }
            case ENDING: {
                arena.setTime(15);

                arcade.updateStatisticalData((Team) arena.getWinner(), arena.getLosers().stream().map(team -> (Team) team).collect(Collectors.toList()));

                users.forEach(user -> {
                    Player player = user.getPlayer();

                    if (player != null) {
                        // Executar load no thread do servidor para garantir que os itens sejam setados corretamente
                        Core.getPlatform().runSync(() -> {
                            // O handleHotbar já limpa o inventário e seta os itens corretos
                            arcade.load(user);
                        });
                    } else {
                        CompletableFuture.runAsync(() -> arcade.load(user));
                    }

                    handleEndMessage(user, arena);
                });

                if (arena.hasWinner()) {
                    Team winner = (Team) arena.getWinner();

                    List<TeamPreset> losers = arena.getLosers().stream().map(team -> (TeamPreset) team).collect(Collectors.toList());

                    // Enviar títulos (Vitória / Derrota)
                    sendEndGameTitles(winner, losers, winner.getColor() + (arena.isSlot(Slot.SOLO) ? winner.getPlayers().stream().map(HumanEntity::getName).findFirst().orElse(winner.getName()) : winner.getName()) + "§e venceu!");

                    // Criar mensagem de vitória
                    String winnerName = winner.getColor() + (arena.isSlot(Slot.SOLO) ? winner.getPlayers().stream().map(HumanEntity::getName).findFirst().orElse(winner.getName()) : winner.getName());

                    Core.getPlatform().runSync(() -> {
                        for (Player winnerPlayer : winner.getPlayers()) {
                            if (winnerPlayer == null) continue;

                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                if (onlinePlayer == winnerPlayer || winner.getPlayers().stream().noneMatch(search -> search.getUniqueId().equals(onlinePlayer.getUniqueId())))
                                    continue;

                                System.out.println("Escondeu " + onlinePlayer.getName() + " de " + winnerPlayer.getName());

                                winnerPlayer.hidePlayer(onlinePlayer);
                            }
                        }
                    });

                    arena.send(winnerName + "§e venceu a partida.");
                }

                arena.sound(Sound.ENDERDRAGON_GROWL, 13, 1);
                break;
            }
            case RESETTING: {
                users.forEach(user -> {
                    Player player = user.getPlayer();

                    if (player != null) player.performCommand("playagain");
                });

                SlimeWorldController.unloadWorld(arena.getWorld(), () -> arcade.removeArena(arena));

                Core.getPlatform().runSync(() -> {
                    SlimeWorldController.unloadWorld(arena.getWorld(), () -> {
                        arcade.removeArena(arena);
                        arcade.copyArena(arena);
                    });
                }, 30);

                break;
            }
        }
    }

    private static void sendEndGameTitles(TeamPreset winner, List<TeamPreset> losers, String subTitle) {
        winner.getPlayers().forEach(player -> TitleAnimation.sendWinFrame(player, subTitle));
        losers.forEach(loser -> loser.getPlayers().forEach(player -> TitleAnimation.sendDefeatFrame(player, subTitle)));
    }

    protected static void handleDestroyColiseum(Arena arena) {
        Location spawn = arena.getLocation("spawn");

        Instant now = Instant.now();

        Objects.requireNonNull(spawn, "O spawn da arena \"" + arena.getIdentifier() + "\" não foi encontrado.");

        // Dividir em partes para processar em chunks
        List<Block> blockList = Cuboid.getBlocksFromCenter(spawn, 25, 30, 5).stream().filter(block -> block != null && !block.isEmpty()).distinct().collect(Collectors.toList());

        int chunkSize = 100; // Número de blocos processados por execução

        AtomicInteger currentIndex = new AtomicInteger(0);

        // Início da tarefa escalonada
        new BukkitRunnable() {
            @Override
            public void run() {
                int startIndex = currentIndex.getAndAdd(chunkSize);
                if (startIndex >= blockList.size()) {
                    cancel(); // Cancelar a tarefa quando terminar
                    Core.scan("Coliseu/" + arena.getIdentifier(), now);
                    return;
                }

                int endIndex = Math.min(startIndex + chunkSize, blockList.size());
                for (int i = startIndex; i < endIndex; i++) {
                    Block block = blockList.get(i);

                    block.setType(Material.AIR, false);
                    block.getState().update(true, false);
                    
                    block.getWorld().getNearbyEntities(block.getLocation(), 2, 0, 2).stream()
                        .filter(entity -> entity instanceof Item)
                        .forEach(org.bukkit.entity.Entity::remove);
                }
            }
        }.runTaskTimer(BedWars.getInstance(), 0, 1);
    }

    protected static void handleEndMessage(User user, Arena arena) {
        String separatorBar = "§a--------------------------------------";

        Team winner = (Team) arena.getWinner();

        if (winner == null) return;

        ArcadeCategory arcade = arena.getArcade().getCategory();

        List<String> messageList = new ArrayList<>(Arrays.asList("", separatorBar, makeCenteredMessage((arena.isType(Type.CASUAL) ? "§6" : "§5") + "§l " + arcade.getServer().getName().toUpperCase() + " " + arcade.getName().toUpperCase()), makeCenteredMessage("§eVencedor: " + winner.getColor() + "Time " + winner.getName()), ""));

        // Renderizar tops
        for (Top top : arena.getContext().getTops()) {
            if (top == null || top.getUsers().isEmpty()) continue;

            messageList.add(makeCenteredMessage("§b§lTOP " + top.getType().getName().toUpperCase()));

            List<TopUser> topList = top.getTopThird();

            for (TopUser topUser : topList) {
                Account account = topUser.getAccount();

                if (account == null) continue;

                int index = topList.indexOf(topUser) + 1;

                String tagPrefix = account.getTag().getColor() + (account.getTag().ordinal() > Tag.PARTNER.ordinal() ? "§o" : "");

                String color = index == 1 ? "§a" : index == 2 ? "§e" : "§c";

                // 1º CassioMartim - 5
                messageList.add(makeCenteredMessage(color + index + "º " + tagPrefix + account.getNickname() + " §7- " + Util.formatNumber(topUser.getValue())));
            }

            messageList.add("");
        }
        messageList.addAll(Arrays.asList(separatorBar, ""));

        Account account = user.getAccount();

        account.send(messageList);

        /* Mensagens individuais */
        TextComponent playAgainText = new TextComponent("§eDeseja jogar novamente? ");

        TextComponent playAgainButton = new TextComponent("§b§lCLIQUE AQUI");
        playAgainButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/playagain"));

        playAgainText.addExtra(playAgainButton);
        playAgainText.addExtra("\n");

        account.send(playAgainText);

        winner.getPlayers().forEach(player -> TitleAnimation.sendWinFrame(player, ""));
        BedMember member = user.getMember();

        if (arena.inTop(user)) {
            int xpForBeingAtTop = Core.RANDOM.ints(120, 250).findFirst().orElse(120);

            member.addLevelXp(xpForBeingAtTop);
            user.setXpGain(xpForBeingAtTop);

            account.send("§b+" + xpForBeingAtTop + " XP");
        }

        account.send("§eVocê reivindicou §b" + Util.formatNumber(user.getXpGain()) + " de XP§e nessa partida!");

        if (arena.isType(Type.CASUAL)) {
            String current = member.getLevelId(), next = member.getNextLevelId();

            final int xp = member.getLevelXp(), minXp = member.getMinLevelXp();

            String[] barAndPercentage = buildBarAndPercentage(xp, minXp);

            account.send(current + barAndPercentage[0] + next + barAndPercentage[1]);
        }
    }

    protected static String[] buildBarAndPercentage(int current, int max) {
        return new String[]{" §8[" + Util.createProgressBar(ChatColor.GREEN, ChatColor.GRAY, "|", current, max, 50) + "§8] ", " §7(" + Util.formatNumberWithLetter(current) + "/" + Util.formatNumberWithLetter(max) + ") " + Util.percentage(current, max) + "%"};
    }
}
