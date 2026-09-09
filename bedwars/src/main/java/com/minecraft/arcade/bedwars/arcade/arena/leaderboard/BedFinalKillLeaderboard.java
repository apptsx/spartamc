package com.minecraft.arcade.bedwars.arcade.arena.leaderboard;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.util.Util;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BedFinalKillLeaderboard extends LeaderboardHologram {

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

        List<String> lines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP 100 §7(%d/%d)", currentPage + 1, totalPages),
                "§e§lKILLS FINAIS",
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
            updateHologram(memberList, itemsPerPage, totalPages, nextPage);
            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
        });
    }

    private void updateHologram(List<BedMember> memberList, int itemsPerPage, int totalPages, int nextPage) {
        HologramClient hologram = getHologram();

        List<String> newLines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP 100 §7(%d/%d)", nextPage + 1, totalPages),
                "§e§lKILLS FINAIS",
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

                newLines.add(String.format("§e%s. %s §7- %s",
                        i + 1,
                        account.getTag().getColor() + account.getName() + (account.hasClan() ? account.getClanTag() : ""),
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

