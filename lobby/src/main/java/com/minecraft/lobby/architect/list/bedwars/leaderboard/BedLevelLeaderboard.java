package com.minecraft.lobby.architect.list.bedwars.leaderboard;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
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
import java.util.stream.Stream;

public class BedLevelLeaderboard extends LeaderboardHologram {

    public BedLevelLeaderboard(Player player, Location location) {
        super("level", player, location, 10);
    }

    @Override
    public void handle() {
        HologramClient hologram = getHologram();

        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil(100.0 / itemsPerPage); // 100 items, 10 per page

        hologram.writeProperty("page", 0);

        int currentPage = hologram.getInt("page");

        Stream<BedMember> stream = Core.getBedWarsData().ranking("context.level", 100).stream();

        // Para todos os períodos (DAY, WEEK, MONTH), ordenar por nível (decrescente) e depois por XP do nível (decrescente)
        stream = stream.sorted(Comparator.comparing(Member::getLevel)
                .thenComparing(Member::getLevelXp)
                .reversed());

        stream = stream.filter(member -> Core.getAccountData().of(member.getId()) != null);

        List<BedMember> members = stream.toList();

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

                if (account == null || account.getRank() == null || account.getRank().getType() == null) {
                    lines.add(String.format("§e%s. §7...", i + 1));
                    continue;
                }

                // Para todos os períodos (DAY, WEEK, MONTH), exibir apenas o nível
                lines.add(String.format("§e%s. %s §7- %s",
                        i + 1,
                        account.getFormattedDisplayName() + (account.hasClan() ? account.getClanTag() : ""),
                        member.getLevelId()
                ));
            } else {
                lines.add(String.format("§e%s. §7...", i + 1));
            }
        }

        lines.add("§6§lClique para ver mais!");

        hologram.setSpaciousText(lines);

        hologram.setTouch((player, touch) -> {
            // Clique normal = Trocar página
            int nextPage = (hologram.getInt("page") + 1) % totalPages;
            hologram.writeProperty("page", nextPage);
            updateHologram(memberList, itemsPerPage, totalPages, nextPage);
            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 2.0f);
        });
    }

    // Method to update hologram text based on page
    private void updateHologram(List<BedMember> memberList, int itemsPerPage, int totalPages, int nextPage) {
        HologramClient hologram = getHologram();

        List<String> newLines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP 100 §7(%d/%d)", nextPage + 1, totalPages),
                "§e§lNÍVEL",
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

                // Para todos os períodos (DAY, WEEK, MONTH), exibir apenas o nível
                newLines.add(String.format("§e%s. %s §7- %s",
                        i + 1,
                        account.getFormattedDisplayName() + (account.hasClan() ? account.getClanTag() : ""),
                        member.getLevelId()
                ));
            } else {
                newLines.add(String.format("§e%s. §7...", i + 1));
            }
        }

        newLines.add("§6§lClique para ver mais!");

        hologram.setSpaciousText(newLines);
    }
}