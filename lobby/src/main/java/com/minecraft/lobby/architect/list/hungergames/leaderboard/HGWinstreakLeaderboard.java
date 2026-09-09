package com.minecraft.lobby.architect.list.hungergames.leaderboard;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.member.list.hungergames.HungerGamesMember;
import com.minecraft.core.util.Util;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HGWinstreakLeaderboard extends LeaderboardHologram {

    public HGWinstreakLeaderboard(Player player, Location location) {
        super("winstreak", player, location, 10);
        handle();
    }

    @Override
    public void handle() {
        HologramClient hologram = getHologram();

        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil(100.0 / itemsPerPage);

        hologram.writeProperty("page", 0);

        int currentPage = hologram.getInt("page");

        List<HungerGamesMember> members = Core.getHungerGamesData().ranking("context.stats", 100).stream()
                .filter(member -> Core.getAccountData().of(member.getId()) != null)
                .sorted((a, b) -> Integer.compare(b.getStats(ArcadeCategory.HUNGERGAMES).getHighWinStreak(), a.getStats(ArcadeCategory.HUNGERGAMES).getHighWinStreak()))
                .collect(Collectors.toList());

        List<String> lines = new ArrayList<>(Arrays.asList(
                "§b§lTOP 100 HG",
                "§6§lMAIOR WIN",
                ""
        ));

        int start = currentPage * itemsPerPage;

        for (int i = start; i < start + itemsPerPage; i++) {
            if (i < members.size()) {
                HungerGamesMember member = members.get(i);
                Account account = Core.getAccountData().of(member.getId());

                if (account == null || account.getRank() == null) {
                    lines.add(String.format("§e%s. §7...", i + 1));
                    continue;
                }

                int winstreak = member.getStats(ArcadeCategory.HUNGERGAMES).getHighWinStreak();
                String winstreakColor = getWinstreakColor(winstreak);

                lines.add(String.format("§e%s. %s §7- %s%s",
                        i + 1,
                        account.getFormattedDisplayName() + (account.hasClan() ? account.getClanTag() : ""),
                        winstreakColor,
                        Util.formatNumber(winstreak)
                ));
            } else {
                lines.add(String.format("§e%s. §7...", i + 1));
            }
        }

        lines.add("§6§o" + com.minecraft.core.Constant.SERVER_DOMAIN);

        hologram.setSpaciousText(lines);

        hologram.setTouch((player, touch) -> {
            int nextPage = (hologram.getInt("page") + 1) % totalPages;
            hologram.writeProperty("page", nextPage);
            updateHologram(members, itemsPerPage, nextPage);
            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 2.0f);
        });
    }

    private void updateHologram(List<HungerGamesMember> members, int itemsPerPage, int nextPage) {
        HologramClient hologram = getHologram();

        List<String> newLines = new ArrayList<>(Arrays.asList(
                "§b§lTOP 100 HG",
                "§6§lMAIOR WIN",
                ""
        ));

        int startIdx = nextPage * itemsPerPage;

        for (int i = startIdx; i < startIdx + itemsPerPage; i++) {
            if (i < members.size()) {
                HungerGamesMember member = members.get(i);
                Account account = Core.getAccountData().of(member.getId());

                if (account == null || account.getRank() == null) {
                    newLines.add(String.format("§e%s. §7...", i + 1));
                    continue;
                }

                int winstreak = member.getStats(ArcadeCategory.HUNGERGAMES).getHighWinStreak();
                String winstreakColor = getWinstreakColor(winstreak);

                newLines.add(String.format("§e%s. %s §7- %s%s",
                        i + 1,
                        account.getFormattedDisplayName() + (account.hasClan() ? account.getClanTag() : ""),
                        winstreakColor,
                        Util.formatNumber(winstreak)
                ));
            } else {
                newLines.add(String.format("§e%s. §7...", i + 1));
            }
        }

        newLines.add("§6§o" + com.minecraft.core.Constant.SERVER_DOMAIN);

        hologram.setSpaciousText(newLines);
    }

    private String getWinstreakColor(int winstreak) {
        if (winstreak >= 100) return "§6§l";
        if (winstreak >= 50) return "§e";
        if (winstreak >= 25) return "§a";
        if (winstreak >= 10) return "§b";
        return "§f";
    }
}
