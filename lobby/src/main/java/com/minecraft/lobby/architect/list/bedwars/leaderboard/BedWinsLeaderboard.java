package com.minecraft.lobby.architect.list.bedwars.leaderboard;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
import com.minecraft.core.bukkit.api.hologram.leaderboard.period.LeaderboardPeriod;
import com.minecraft.core.bukkit.api.hologram.touch.Touch;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.member.Member;
import com.minecraft.core.arcade.category.ArcadeCategory;
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

public class BedWinsLeaderboard extends LeaderboardHologram {

    public BedWinsLeaderboard(Player player, Location location) {
        super("wins", player, location, 10);
    }

    @Override
    public void handle() {
        HologramClient hologram = getHologram();

        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil(100.0 / itemsPerPage);

        hologram.writeProperty("page", 0);

        int currentPage = hologram.getInt("page");

        String mode = String.valueOf(hologram.getProperty("mode", "GERAL"));
        LeaderboardPeriod period = (LeaderboardPeriod) hologram.getProperty("period", null);
        ArcadeCategory category = mapCategory(mode);

        Stream<BedMember> stream = Core.getBedWarsData().ranking("metadata.statsList", 100).stream();

//        if (period != null) {
//            if (period.equals(LeaderboardPeriod.WEEK))
//                stream = stream.sorted(Comparator.comparing(Member::getWeekEloXp));
//            else if (period.equals(LeaderboardPeriod.MONTH))
//                stream = stream.sorted(Comparator.comparing(Member::getMonthEloXp));
//        }

        List<BedMember> members = stream
                .filter(member -> Core.getAccountData().of(member.getId()) != null)
                .sorted((a, b) -> {
                    if (category != null) {
                        return Integer.compare(b.getTotalWins(category), a.getTotalWins(category));
                    }
                    return Integer.compare(b.getTotalWins(), a.getTotalWins());
                })
                .collect(Collectors.toList());

        List<String> lines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP 100 §7(%d/%d)", currentPage + 1, totalPages),
                "§e§lVITÓRIAS" + formatPeriodSuffix(period) + formatModeSuffix(mode),
                ""
        ));

        int start = currentPage * itemsPerPage;

        List<BedMember> memberList = new ArrayList<>(members);

        for (int i = start; i < start + itemsPerPage; i++) {
            if (i < memberList.size()) {
                BedMember member = memberList.get(i);

                Account account = Core.getAccountData().of(member.getId());

                if (account == null || account.getRank() == null || account.getRank().getType() == null) {
                    lines.add(String.format("§e%s. §7...", i + 1));
                    continue;
                }

                lines.add(String.format("§e%s. %s §7- %s",
                        i + 1,
                        account.getFormattedDisplayName() + (account.hasClan() ? account.getClanTag() : ""),
                        Util.formatNumber(category != null ? member.getTotalWins(category) : member.getTotalWins())
                ));
            } else {
                lines.add(String.format("§e%s. §7...", i + 1));
            }
        }

        lines.add("§6§lClique para ver mais!");

        hologram.setSpaciousText(lines);

        hologram.setTouch((player, touch) -> {
            int nextPage = (hologram.getInt("page") + 1) % totalPages;
            hologram.writeProperty("page", nextPage);
            updateHologram(memberList, itemsPerPage, totalPages, nextPage);
            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 2.0f);
        });
    }

    // Method to update hologram text based on page
    private void updateHologram(List<BedMember> memberList, int itemsPerPage, int totalPages, int nextPage) {
        HologramClient hologram = getHologram();

        String mode = String.valueOf(hologram.getProperty("mode", "GERAL"));
        LeaderboardPeriod period = (LeaderboardPeriod) hologram.getProperty("period", null);
        ArcadeCategory category = mapCategory(mode);

        List<String> newLines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP 100 §7(%d/%d)", nextPage + 1, totalPages),
                "§e§lVITÓRIAS" + formatPeriodSuffix(period) + formatModeSuffix(mode),
                ""
        ));

        int startIdx = nextPage * itemsPerPage;

        for (int i = startIdx; i < startIdx + itemsPerPage; i++) {
            if (i < memberList.size()) {
                BedMember member = memberList.get(i);
                Account account = Core.getAccountData().of(member.getId());

                if (account == null || account.getRank() == null || account.getRank().getType() == null) {
                    newLines.add(String.format("§e%s. §7...", i + 1));
                    continue;
                }

                newLines.add(String.format("§e%s. %s §7- %s",
                        i + 1,
                        account.getFormattedDisplayName() + (account.hasClan() ? account.getClanTag() : ""),
                        Util.formatNumber(category != null ? member.getTotalWins(category) : member.getTotalWins())
                ));
            } else {
                newLines.add(String.format("§e%s. §7...", i + 1));
            }
        }

        newLines.add("§6§lClique para ver mais!");

        hologram.setSpaciousText(newLines);
    }

    private ArcadeCategory mapCategory(String mode) {
        if (mode == null) return null;
        switch (mode.toUpperCase()) {
            case "SOLO":
                return ArcadeCategory.BEDWARS_SOLO;
            case "DUPLAS":
                return ArcadeCategory.BEDWARS_DUO;
            case "TRIOS":
                return ArcadeCategory.BEDWARS_TRIO;
            case "QUARTETOS":
                return ArcadeCategory.BEDWARS_QUARTET;
            case "1V1":
                return ArcadeCategory.BEDWARS_VERSUS_SOLO;
            case "2V2":
                return ArcadeCategory.BEDWARS_VERSUS_DUO;
            case "3V3":
                return ArcadeCategory.BEDWARS_VERSUS_TRIO;
            case "4V4":
                return ArcadeCategory.BEDWARS_VERSUS_QUARTET;
            default:
                return null; // Geral
        }
    }

    private String formatPeriodSuffix(LeaderboardPeriod period) {
        if (period == null) {
            return " TOTAIS";
        }
        switch (period) {
            case DAY:
                return " DIÁRIAS";
            case WEEK:
                return " SEMANAIS";
            case MONTH:
                return " MENSAIS";
            default:
                return " TOTAIS";
        }
    }

    private String formatModeSuffix(String mode) {
        if (mode == null || mode.equalsIgnoreCase("GERAL")) return "";
        switch (mode.toUpperCase()) {
            case "SOLO":
                return " SOLO";
            case "DUPLAS":
                return " DUPLAS";
            case "TRIOS":
                return " TRIOS";
            case "QUARTETOS":
                return " QUARTETOS";
            case "1V1":
                return " 1V1";
            case "2V2":
                return " 2V2";
            case "3V3":
                return " 3V3";
            case "4V4":
                return " 4V4";
            default:
                return "";
        }
    }

}