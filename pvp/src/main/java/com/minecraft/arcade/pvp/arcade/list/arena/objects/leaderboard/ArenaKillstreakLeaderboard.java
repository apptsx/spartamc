package com.minecraft.arcade.pvp.arcade.list.arena.objects.leaderboard;

import com.minecraft.arcade.pvp.arcade.arena.Arena;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.member.list.pvp.PvPMember;
import com.minecraft.core.util.Util;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArenaKillstreakLeaderboard extends LeaderboardHologram {

    public ArenaKillstreakLeaderboard(Player host, Arena arena) {
        super("killstreak", host, arena, 100);
    }

    @Override
    public void handle() {
        HologramClient model = getHologram();

        final int itemsPerPage = 10,
                totalPages = (int) Math.ceil(100.0 / itemsPerPage);

        int page = model.getInt("page", 0);

        List<String> textLines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP %s §7(%s/%s)", getMaxLines(), page + 1, totalPages),
                "§c§lKILLSTREAK",
                ""
        ));

        List<PvPMember> memberList = new ArrayList<>(Core.getPvpData().ranking("stats.arena.bestKillStreak", getMaxLines()));

        handleUserInfo(memberList, page, model, textLines);

        model.setTouch((player, touch) -> {
            int next = (model.getInt("page", 0) + 1) % totalPages;

            model.writeProperty("page", next);

            handleClick(memberList, totalPages, next);

            player.playSound(player.getLocation(), Sound.CLICK, 1.8f, 2f);
        });
    }

    protected void handleClick(List<PvPMember> memberList, int totalPages, int nextPage) {
        HologramClient model = getHologram();

        List<String> textLines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP %s §7(%s/%s)", getMaxLines(), nextPage + 1, totalPages),
                "§c§lKILLSTREAK",
                ""
        ));

        handleUserInfo(memberList, nextPage, model, textLines);
    }

    private void handleUserInfo(List<PvPMember> memberList, int nextPage, HologramClient model, List<String> textLines) {
        int start = nextPage * 10;
        for (int i = start; i < start + 10; i++) {
            int id = i + 1;

            if (i < memberList.size()) {
                PvPMember member = memberList.get(i);

                if (member == null) continue;

                Account account = Core.getAccountData().of(member.getId());

                if (account == null) continue;

                int killstreak = member.getArenaStats().getKillStreak();

                textLines.add(String.format("§e%s. %s §7- §c%s",
                        id,
                        account.getRank().getColor() + account.getName() + (account.hasClan() ? account.getClanTag() : ""),
                        Util.formatNumber(killstreak)));

            } else
                textLines.add(String.format("§e%s. §7...", id));
        }

        textLines.add("§e§o" + com.minecraft.core.Constant.SERVER_DOMAIN);

        model.setSpaciousText(textLines);
    }
}