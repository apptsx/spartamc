package com.minecraft.arcade.bedwars.arcade.arena.leaderboard;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
import com.minecraft.core.bukkit.api.hologram.leaderboard.period.LeaderboardPeriod;
import com.minecraft.core.bukkit.api.hologram.touch.Touch;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.member.Member;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.util.Util;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BedLevelLeaderboard extends LeaderboardHologram {

    public BedLevelLeaderboard(Player player, Location location) {
        super("level", player, location, 10);
    }

    @Override
    public void handle() {
        HologramClient hologram = getHologram();

        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil(100.0 / itemsPerPage);

        hologram.writeProperty("page", 0);

        int currentPage = hologram.getInt("page");

        LeaderboardPeriod period = (LeaderboardPeriod) hologram.getProperty("period", LeaderboardPeriod.DAY);

        Stream<BedMember> stream = Core.getBedWarsData().ranking("context.level", 100).stream();

        // Para todos os períodos (DAY, WEEK, MONTH), ordenar por nível (decrescente) e depois por XP do nível (decrescente)
        stream = stream.sorted(Comparator.comparing(Member::getLevel)
                .thenComparing(Member::getLevelXp)
                .reversed());

        stream = stream.filter(member -> Core.getAccountData().of(member.getId()) != null);

        List<BedMember> members = stream.collect(Collectors.toList());

        List<String> lines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP 100 §7(%d/%d)", currentPage + 1, totalPages),
                "§e§lNÍVEL",
                ""
        ));

        int start = currentPage * itemsPerPage;

        List<BedMember> memberList = new ArrayList<>(members);

        for (int i = start; i < start + itemsPerPage; i++) {
            if (i < memberList.size()) {
                BedMember member = memberList.get(i);

                Account account = Core.getAccountData().of(member.getId());

                lines.add(String.format("§e%s. %s §7- %s",
                        i + 1,
                        account.getTag().getColor() + account.getName() + (account.hasClan() ? account.getClanTag() : ""),
                        member.getLevelId()
                ));
            } else {
                lines.add(String.format("§e%s. §7...", i + 1));
            }
        }

        lines.addAll(Arrays.asList(
                "§6§lClique para trocar!",
                handlePeriodLine(period)
        ));

        hologram.setSpaciousText(lines);

        hologram.setTouch((player, touch) -> {
            if (player.isSneaking() && touch == Touch.RIGHT) {
                LeaderboardPeriod next = ((LeaderboardPeriod) hologram.getProperty("period", LeaderboardPeriod.DAY)).next();
                hologram.writeProperty("period", next);
                player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
                handle();
            } else {
                int nextPage = (hologram.getInt("page") + 1) % totalPages;
                hologram.writeProperty("page", nextPage);
                updateHologram(memberList, itemsPerPage, totalPages, nextPage);
                player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
            }
        });
    }

    private void updateHologram(List<BedMember> memberList, int itemsPerPage, int totalPages, int nextPage) {
        HologramClient hologram = getHologram();

        LeaderboardPeriod period = (LeaderboardPeriod) hologram.getProperty("period", LeaderboardPeriod.DAY);

        List<String> newLines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP 100 §7(%d/%d)", nextPage + 1, totalPages),
                "§e§lNÍVEL",
                ""));

        int startIdx = nextPage * itemsPerPage;

        for (int i = startIdx; i < startIdx + itemsPerPage; i++) {
            if (i < memberList.size()) {
                BedMember member = memberList.get(i);
                Account account = Core.getAccountData().of(member.getId());

                if (account == null) {
                    newLines.add(String.format("§e%s. ...", i + 1));
                    continue;
                }

                // Para todos os períodos (DAY, WEEK, MONTH), exibir apenas o nível
                newLines.add(String.format("§e%s. %s §7- %s",
                        i + 1,
                        account.getTag().getColor() + account.getName() + (account.hasClan() ? account.getClanTag() : ""),
                        member.getLevelId()
                ));
            } else {
                newLines.add(String.format("§e%s. §7...", i + 1));
            }
        }

        newLines.addAll(Arrays.asList(
                "§6§lClique para trocar!",
                handlePeriodLine(period)
        ));

        hologram.setSpaciousText(newLines);
    }

    protected String handlePeriodLine(LeaderboardPeriod period) {
        StringBuilder builder = new StringBuilder();

        int index = 0;
        boolean end = false;
        for (LeaderboardPeriod search : LeaderboardPeriod.values()) {
            if (index >= LeaderboardPeriod.values().length) end = true;

            boolean using = period.equals(search);

            builder.append((using ? "§a§l" : "§7")).append(search.getName())
                    .append(end ? "" : " ");

            index++;
        }

        return builder.toString();
    }
}

