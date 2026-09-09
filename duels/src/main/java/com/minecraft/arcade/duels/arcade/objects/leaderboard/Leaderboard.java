package com.minecraft.arcade.duels.arcade.objects.leaderboard;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.bukkit.api.hologram.Hologram;
import com.minecraft.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.member.context.stats.ArcadeStats;
import com.minecraft.core.member.list.duels.DuelMember;
import com.minecraft.core.util.Util;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Leaderboard extends LeaderboardHologram {

    private final ArcadeCategory arcade;

    public Leaderboard(ArcadeCategory arcade, String field, Player host, Room arena, int maxLines) {
        super(field, host, arena, maxLines);

        this.arcade = arcade;
    }

    @Override
    public void handle() {
        HologramClient hologram = getHologram();

        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil(100.0 / itemsPerPage);

        hologram.writeProperty("page", 0);

        // soup_winStreak, soup_wins, search: stats.soup.winStreak, stats.soup.wins
        String field = "stats." + arcade.name().split("_")[1].toLowerCase() + getName().toLowerCase();

        int currentPage = hologram.getInt("page");

        Collection<DuelMember> members = new ArrayList<>(Core.getDuelsData().ranking(field, 100));

        List<String> lines = new ArrayList<>();

        lines.add(String.format("§b§lTOP 100 §7(%s/%s)", currentPage + 1, totalPages));
        lines.add("§e§l" + arcade.getName().toUpperCase() + " " + getName().toUpperCase());

        lines.add("");

        int start = currentPage * itemsPerPage;

        List<DuelMember> memberList = new ArrayList<>(members);

        for (int i = start; i < start + itemsPerPage; i++) {
            if (i < memberList.size()) {
                DuelMember member = memberList.get(i);

                Account account = Core.getAccountData().of(member.getId(), false);

                ArcadeStats stats = member.getStats(arcade);

                Object value = null;

                switch (getName().toLowerCase()) {
                    case "winstreak":
                        value = Util.formatNumber(stats.getWinStreak());
                        break;
                    case "wins":
                        value = Util.formatNumber(stats.getWins());
                        break;
                }


                if (value != null)
                    lines.add(String.format("§e%s. %s §7- §e%s",
                            i + 1,
                            account.getRank().getColor() + account.getName(),
                            value
                    ));
            } else {
                lines.add(String.format("§e%s. §7...", i + 1));
            }
        }

        lines.add("§a§lClique para ver mais!");

        hologram.setText(lines);

        hologram.setTouch((player, touch) -> {
            int nextPage = (hologram.getInt("page") + 1) % totalPages;
            hologram.writeProperty("page", nextPage);

            updateHologram(memberList, itemsPerPage, totalPages, nextPage);
            player.playSound(player.getLocation(), Sound.CLICK, 1.8f, 2f);
        });
    }

    private void updateHologram(List<DuelMember> memberList, int itemsPerPage, int totalPages, int nextPage) {
        Hologram hologram = getHologram();

        List<String> newLines = new ArrayList<>();

        newLines.add(String.format("§b§lTOP 100 §7(%s/%s)", nextPage + 1, totalPages));
        newLines.add("§e§l" + arcade.getName().toUpperCase() + " " + getName().toUpperCase());

        newLines.add("");

        int startIdx = nextPage * itemsPerPage;

        for (int i = startIdx; i < startIdx + itemsPerPage; i++) {
            if (i < memberList.size()) {
                DuelMember member = memberList.get(i);

                Account account = Core.getAccountData().of(member.getId(), false);

                if (account == null) {
                    newLines.add(String.format("§e%s. ...", i + 1));
                    continue;
                }

                ArcadeStats stats = member.getStats(arcade);

                Object value = null;

                switch (getName().toLowerCase()) {
                    case "winstreak":
                        value = Util.formatNumber(stats.getWinStreak());
                        break;
                    case "wins":
                        value = Util.formatNumber(stats.getWins());
                        break;
                }


                if (value != null)
                    newLines.add(String.format("§e%s. %s §7- §e%s",
                            i + 1,
                            account.getRank().getColor() + account.getName(),
                            value
                    ));
            } else {
                newLines.add(String.format("§e%s. §7...", i + 1));
            }
        }

        newLines.add("§a§lClique para ver mais!");

        hologram.setText(newLines);
    }
}