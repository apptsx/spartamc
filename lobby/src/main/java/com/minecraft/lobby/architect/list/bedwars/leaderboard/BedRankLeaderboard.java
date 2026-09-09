package com.minecraft.lobby.architect.list.bedwars.leaderboard;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.member.Member;
import com.minecraft.core.member.list.bedwars.BedMember;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BedRankLeaderboard extends LeaderboardHologram {

    public BedRankLeaderboard(Player player, Location location) {
        super("competitive_rank", player, location, 10);
    }

    @Override
    public void handle() {
        HologramClient hologram = getHologram();

        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil(100.0 / itemsPerPage);

        hologram.writeProperty("page", 0);

        int currentPage = hologram.getInt("page");

        Stream<BedMember> stream = Core.getBedWarsData().ranking("context.eloXp", 100).stream();

        stream = stream.sorted(Comparator.comparingInt(Member::getEloXp).reversed());

        stream = stream.filter(member -> Core.getAccountData().of(member.getId()) != null);

        List<BedMember> members = stream.collect(Collectors.toList());

        List<String> lines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP 100 §7(%d/%d)", currentPage + 1, totalPages),
                "§e§lRANK COMPETITIVO",
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

                lines.add(String.format("§e%s. §8- §7(%s) %s",
                        i + 1,
                        member.getElo().getName(),
                        account.getFormattedDisplayName() + (account.hasClan() ? account.getClanTag() : "")
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

    private void updateHologram(List<BedMember> memberList, int itemsPerPage, int totalPages, int nextPage) {
        HologramClient hologram = getHologram();

        List<String> newLines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP 100 §7(%d/%d)", nextPage + 1, totalPages),
                "§e§lRANK COMPETITIVO",
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

                newLines.add(String.format("§e%s. §8- §7(%s) %s",
                        i + 1,
                        member.getElo().getName(),
                        account.getFormattedDisplayName() + (account.hasClan() ? account.getClanTag() : "")
                ));
            } else {
                newLines.add(String.format("§e%s. §7...", i + 1));
            }
        }

        newLines.add("§6§lClique para ver mais!");

        hologram.setSpaciousText(newLines);
    }
}
