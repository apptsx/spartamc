package com.minecraft.core.bukkit.menu.account.stats;

import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.api.hologram.leaderboard.period.LeaderboardPeriod;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.Member;
import com.minecraft.core.member.context.stats.ArcadeStats;
import com.minecraft.core.member.list.pvp.PvPMember;
import com.minecraft.core.member.list.pvp.stats.list.ArenaStats;
import com.minecraft.core.member.list.pvp.stats.list.LavaStats;
import com.minecraft.core.member.list.pvp.stats.list.MLGStats;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StatsInfoMenu extends Menu {

    private final Member member;
    private final ServerType server;

    @Setter
    private LeaderboardPeriod period = LeaderboardPeriod.DAY;

    public StatsInfoMenu(Player player, Account target, ServerType server, Menu last) {
        super(player, "Estatísticas " + LeaderboardPeriod.DAY.getName(), last, 6);

        this.member = target.loadMember(server);
        this.server = server;
    }

    protected int getStatistic(Supplier<Integer> general, Supplier<Integer> month, Supplier<Integer> week) {
        return period.equals(LeaderboardPeriod.MONTH) ? month.get() : period.equals(LeaderboardPeriod.WEEK) ? week.get() : general.get();
    }

    protected Item buildStatistic(ArcadeCategory arcade) {
        ServerType server = arcade.getServer();

        Item item = Item.of(Material.getMaterial(arcade.getIconId()), "§a" + arcade.getName())
                .flags(ItemFlag.values());

        List<String> lore = new ArrayList<>(Arrays.asList("§8" + arcade.getStyle().getName(), ""));

        if (server.equals(ServerType.PVP)) {
            PvPMember pvpMember = (PvPMember) member;

            switch (arcade) {
                case PVP_ARENA:
                case PVP_FPS: {
                    ArenaStats stats = arcade.equals(ArcadeCategory.PVP_ARENA) ? pvpMember.getArenaStats() : pvpMember.getFpsStats();

                    int kills = getStatistic(stats::getKills, stats::getMonthKills, stats::getWeekKills),
                            deaths = getStatistic(stats::getDeaths, stats::getMonthDeaths, stats::getWeekDeaths),
                            streak = getStatistic(stats::getKillStreak, stats::getMonthKillStreak, stats::getWeekKillStreak);

                    lore.addAll(Arrays.asList(
                            "§7Kills: §a" + Util.formatNumber(kills),
                            "§7Deaths: §a" + Util.formatNumber(deaths),
                            "",
                            "§7Killstreak: §a" + Util.formatNumber(streak),
                            "§7Melhor Streak: §a" + Util.formatNumber(stats.getBestKillStreak())
                    ));
                    break;
                }

                case PVP_MLG: {
                    MLGStats stats = pvpMember.getMlgStats();

                    lore.addAll(Arrays.asList(
                            "§7Dif. Fácil: §a" + Util.formatNumber(stats.getHits(MLGStats.MLGLevel.EASY)),
                            "§7Dif. Médio: §e" + Util.formatNumber(stats.getHits(MLGStats.MLGLevel.MEDIUM)),
                            "§7Dif. Difícil: §c" + Util.formatNumber(stats.getHits(MLGStats.MLGLevel.HARD)),
                            "§7Dif. Extremo: §4" + Util.formatNumber(stats.getHits(MLGStats.MLGLevel.EXTREME))
                    ));
                    break;
                }

                case PVP_LAVA: {
                    LavaStats stats = pvpMember.getLavaStats();

                    lore.addAll(Arrays.asList(
                            "§7Dif. Fácil: §a" + Util.formatNumber(stats.getLevel(LavaStats.LavaLevel.EASY)),
                            "§7Dif. Médio: §e" + Util.formatNumber(stats.getLevel(LavaStats.LavaLevel.MEDIUM)),
                            "§7Dif. Difícil: §c" + Util.formatNumber(stats.getLevel(LavaStats.LavaLevel.HARD)),
                            "§7Dif. Extremo: §4" + Util.formatNumber(stats.getLevel(LavaStats.LavaLevel.EXTREME))
                    ));
                    break;
                }
            }

        } else {
            ArcadeStats stats = member.getStats(arcade);

            int wins = getStatistic(stats::getWins, stats::getMonthWins, stats::getWeekWins),
                    defeats = getStatistic(stats::getDefeats, stats::getMonthDefeats, stats::getWeekDefeats),
                    matches = getStatistic(stats::getMatches, stats::getMonthMatches, stats::getWeekMatches),
                    kills = getStatistic(stats::getKills, stats::getMonthKills, stats::getWeekKills),
                    assists = getStatistic(stats::getAssists, stats::getMonthAssists, stats::getWeekAssists),
                    winStreak = getStatistic(stats::getWinStreak, stats::getMonthWinStreak, stats::getWeekWinStreak),
                    killStreak = getStatistic(stats::getKillStreak, stats::getMonthKillStreak, stats::getWeekKillStreak);

            lore.addAll(Arrays.asList(
                    "§7Partidas jogadas: §a" + Util.formatNumber(matches),
                    "",
                    "§7Vitórias: §a" + Util.formatNumber(wins),
                    "§7Derrotas: §a" + Util.formatNumber(defeats),
                    "§7Winstreak: §a" + Util.formatNumber(winStreak),
                    "",
                    "§7Abates: §a" + Util.formatNumber(kills),
                    "§7Assistências: §a" + Util.formatNumber(assists),
                    "§7Killstreak: §a" + Util.formatNumber(killStreak),
                    ""
            ));
        }

        item.lore(lore);

        return item;
    }

    @Override
    public void handle() {
        clear();

        if (member == null)
            addErrorButton("§cNão foi possível carregar sua conta...");
        else {

            if (server.isArcade()) {
                List<ArcadeCategory> allArcades = ArcadeCategory.of(server);
                
                // Filtrar apenas Bedwars Solo/Duo, Duels, PvP, The Bridge Solo, HungerGames
                List<ArcadeCategory> arcades = allArcades.stream()
                    .filter(a -> {
                        if (server.equals(ServerType.BEDWARS)) {
                            return a.name().contains("BEDWARS") && !a.name().contains("VERSUS");
                        } else if (server.equals(ServerType.DUELS)) {
                            return a.name().contains("DUELS") && !a.name().contains("VERSUS");
                        } else if (server.equals(ServerType.PVP)) {
                            return a.name().equals("PVP_ARENA") || a.name().equals("PVP_FPS");
                        } else if (server.equals(ServerType.THE_BRIDGE)) {
                            return a.name().contains("THE_BRIDGE");
                        } else if (server.equals(ServerType.HUNGERGAMES) || server.equals(ServerType.HGMIX)) {
                            return a.name().contains("HUNGERGAMES");
                        }
                        return false;
                    })
                    .collect(Collectors.toList());

                if (arcades.isEmpty())
                    addErrorButton("§cNão foi possível listar os modos...");
                else {
                    int[] rowSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
                    
                    int index = 0;
                    for (ArcadeCategory arcade : arcades) {
                        if (index < rowSlots.length) {
                            addItem(rowSlots[index], buildStatistic(arcade));
                            index++;
                        }
                    }

                    List<String> periodLore = new ArrayList<>(Collections.singletonList(""));

                    for (LeaderboardPeriod period : LeaderboardPeriod.values())
                        periodLore.add("§7- " + (this.period.equals(period) ? "§a" : "") + period.getName());

                    periodLore.add("");
                    periodLore.add("§eClique para alternar!");

                    addItem(49, Item.of(Material.BREWING_STAND_ITEM, "§bFiltrar por:", periodLore)
                            .click(event -> {
                                setPeriod(period.next());
                                sound(MenuSound.PAGINATED);

                                setTitle("Estatísticas " + period.getName());
                                updateTitle();

                                handle();
                            }));
                }
            } else {
                addErrorButton("§cEstatísticas em breve para " + server.getName());
            }

        }

        if (isReturnable())
            addBackButton();

        display();
    }
}