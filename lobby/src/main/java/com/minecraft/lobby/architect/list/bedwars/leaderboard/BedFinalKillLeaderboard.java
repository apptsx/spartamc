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

public class BedFinalKillLeaderboard extends LeaderboardHologram {

    private List<BedMember> memberList;

    public BedFinalKillLeaderboard(Player player, Location location) {
        super("kills", player, location, 10);
    }

    @Override
    public void handle() {
        HologramClient hologram = getHologram();

        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil(100.0 / itemsPerPage);

        hologram.writeProperty("page", 0);

        int currentPage = hologram.getInt("page");

        List<BedMember> members = Core.getBedWarsData().ranking("metadata.statsList", 100)
                .stream()
                .filter(member -> Core.getAccountData().of(member.getId()) != null)
                .sorted((a, b) -> Integer.compare(b.getTotalFinalKills(), a.getTotalFinalKills()))
                .collect(Collectors.toList());

        memberList = new ArrayList<>(members);

        List<String> lines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP 100 §7(%d/%d)", currentPage + 1, totalPages),
                "§e§lKILLS FINAIS",
                ""
        ));

        int start = currentPage * itemsPerPage;

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
                        Util.formatNumber(member.getTotalFinalKills())
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
            updateHologram(itemsPerPage, totalPages, nextPage);
            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 2.0f);
        });
    }

    private void updateHologram(int itemsPerPage, int totalPages, int nextPage) {
        HologramClient hologram = getHologram();

        List<String> newLines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP 100 §7(%d/%d)", nextPage + 1, totalPages),
                "§e§lKILLS FINAIS",
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
                        Util.formatNumber(member.getTotalFinalKills())
                ));
            } else {
                newLines.add(String.format("§e%s. §7...", i + 1));
            }
        }

        newLines.add("§6§lClique para ver mais!");

        hologram.setSpaciousText(newLines);
    }

}