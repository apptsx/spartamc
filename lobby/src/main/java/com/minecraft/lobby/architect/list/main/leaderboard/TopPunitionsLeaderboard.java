package com.minecraft.lobby.architect.list.main.leaderboard;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.bukkit.api.hologram.touch.Touch;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TopPunitionsLeaderboard extends LeaderboardHologram {

    public TopPunitionsLeaderboard(Player player, Location location) {
        super("top_punitions", player, location, 15);
        HologramClient hologram = getHologram();
        if (hologram != null) {
            setupNavigation();
        }
        handle();
    }

    @Override
    public void handle() {
        HologramClient hologram = getHologram();
        if (hologram == null) return;

        Map<UUID, Long> punitionCounts = Core.getPunishmentData().list().stream()
                .collect(Collectors.groupingBy(p -> p.getAuthor(), Collectors.counting()));

        List<Account> staffAccounts = Core.getAccountController().list().stream()
                .filter(account -> {
                    RankType rank = account.getRankType();
                    return rank == RankType.HELPER || rank == RankType.TRIAL ||
                           rank == RankType.MOD || rank == RankType.MODPLUS ||
                           rank == RankType.ADMIN || rank == RankType.CHEFE;
                })
                .sorted((a1, a2) -> {
                    long c1 = punitionCounts.getOrDefault(a1.getId(), 0L);
                    long c2 = punitionCounts.getOrDefault(a2.getId(), 0L);
                    if (c1 != c2) return Long.compare(c2, c1);
                    return a1.getRankType().ordinal() - a2.getRankType().ordinal();
                })
                .limit(10)
                .collect(Collectors.toList());

        List<String> lines = new ArrayList<>();

        lines.add("§e§lTOP 10");
        lines.add("§6§lPUNIÇÕES");
        lines.add("");

        for (int i = 0; i < Math.min(10, staffAccounts.size()); i++) {
            Account account = staffAccounts.get(i);
            int position = i + 1;

            Long count = punitionCounts.getOrDefault(account.getId(), 0L);

            String rankColor = account.getRank().getColor().toString();
            String staffName = rankColor + "§o" + account.getName();
            String clanTag = account.hasClan() ? account.getClanTag() : "";
            String punishments = "§b" + count;

            lines.add(String.format("§e%s. %s%s §7- %s",
                    position,
                    staffName,
                    clanTag,
                    punishments
            ));
        }

        for (int i = staffAccounts.size(); i < 10; i++) {
            lines.add("§e" + (i + 1) + ". §7Ninguém");
        }

        lines.add("");
        lines.add("§6§o" + com.minecraft.core.Constant.SERVER_DOMAIN);

        hologram.setSpaciousText(lines);
    }

    @Override
    protected void setupNavigation() {
        HologramClient hologram = getHologram();
        if (hologram == null) return;

        hologram.setTouch((player, touch) -> {
            // TOP 10 não tem múltiplas páginas
        });
    }
}
