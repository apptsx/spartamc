package com.minecraft.arcade.pvp.arcade.list.lava.leaderboard;

import com.minecraft.arcade.pvp.arcade.arena.Arena;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
import com.minecraft.core.bukkit.api.hologram.row.HologramRow;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.member.list.pvp.PvPMember;
import com.minecraft.core.member.list.pvp.stats.list.LavaStats;
import com.minecraft.core.util.Util;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LavaLevelLeaderboard extends LeaderboardHologram {

    private final List<LavaStats.LavaLevel> levelList;

    private final transient String DIFFICULTY_MODE_KEY = "leaderboard-mode";

    public LavaLevelLeaderboard(Player host, Arena arena) {
        super("levels", host, arena, 100);

        this.levelList = new ArrayList<>(Arrays.asList(LavaStats.LavaLevel.values()));
    }

    @Override
    public void handle() {
        HologramClient model = getHologram();

        /* Criando Tabela baseado no Usuário */
        final int itemsPerPage = 10,
                totalPages = (int) Math.ceil(100.0 / itemsPerPage);

        int page = model.getInt("page", 0);

        /* Efetuando criação de linhas do modelo */
        List<String> textLines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP %s §7(%s/%s)", getMaxLines(), page + 1, totalPages),
                "§e§lNÍVEIS",
                ""
        ));

        List<PvPMember> memberList = new ArrayList<>(Core.getPvpData().ranking("stats.lava", getMaxLines()));

        handleUserInfo(memberList, itemsPerPage, page, model, textLines);

        model.setTouch((player, touch) -> {
            int next = (model.getInt("page", 0) + 1) % totalPages;

            model.writeProperty("page", next);

            handleClick(memberList, itemsPerPage, totalPages, next);

            player.playSound(player.getLocation(), Sound.CLICK, 1.8f, 2f);
        });

        buildPeriodSelector();
    }

    protected void handleClick(List<PvPMember> memberList, int itemsPerPage, int totalPages, int nextPage) {
        HologramClient model = getHologram();

        /* Efetuando criação de linhas do modelo */
        List<String> textLines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP %s §7(%s/%s)", getMaxLines(), nextPage + 1, totalPages),
                "§e§lNÍVEIS",
                ""
        ));

        handleUserInfo(memberList, itemsPerPage, nextPage, model, textLines);

        model.space();
    }

    private void handleUserInfo(List<PvPMember> memberList, int itemsPerPage, int nextPage, HologramClient model, List<String> textLines) {
        int start = nextPage * itemsPerPage;
        for (int i = start; i < start + itemsPerPage; i++) {
            int id = i + 1;

            if (i < memberList.size()) {
                PvPMember member = memberList.get(i);

                if (member == null) continue;

                Account account = Core.getAccountData().of(member.getId());

                if (account == null) continue;

                int level = member.getLavaStats().getLevel(getLevel());

                textLines.add(String.format("§e%s. %s §7- §e%s",
                        id,
                        account.getRank().getColor() + account.getName() + (account.hasClan() ? account.getClanTag() : ""),
                        Util.formatNumber(level)));

            } else
                textLines.add(String.format("§e%s. §7...", id));
        }

        textLines.add("§e§o" + com.minecraft.core.Constant.SERVER_DOMAIN);

        model.setSpaciousText(textLines);
    }

    protected void buildPeriodSelector() {
        HologramClient model = getHologram();

        /* Adicionar linha de troca de períodos */
        HologramRow lastRow = model.getLastRow();

        if (lastRow != null && getManager().notExistsClient(getHost(), "lb_period")) {
            Location lastRowLocation = lastRow.getLocation().clone();

            Vector direction = lastRowLocation.getDirection().normalize(); // Direção normalizada

            double offset = -3.3; // Distância para mover para a esquerda
            Location levelLocation = lastRowLocation.add(direction.getZ() * offset, 2, -direction.getX() * offset);

            HologramClient level = getManager().spawnClient(getHost(), "lb_period", levelLocation);

            List<String> lines = new ArrayList<>(Arrays.asList(
                    "§b§lDIFICULDADE ATUAL",
                    ""
            ));

            levelList.forEach(search -> lines.add((withPeriod(search) ? "§a" : "§7") + search.getName()));

            lines.addAll(Arrays.asList("", "§6§lClique para trocar!"));

            level.setText(lines);

            level.setTouch((player, touch) -> {
                LavaStats.LavaLevel next = getLevel().next();

                model.writeProperty("page", 0);
                model.writeProperty(DIFFICULTY_MODE_KEY, next);

                List<String> newLines = new ArrayList<>(Arrays.asList(
                        "§b§lDIFICULDADE ATUAL",
                        ""
                ));

                levelList.forEach(search -> newLines.add((withPeriod(search) ? "§a" : "§7") + search.getName()));

                newLines.addAll(Arrays.asList("", "§6§lClique para trocar!"));

                level.setText(newLines);

                handle();
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.8f, 2f);
            });

            model.writeProperty("lb_period", level);
        }
    }


    protected LavaStats.LavaLevel getLevel() {
        return (LavaStats.LavaLevel) getHologram().getProperty(DIFFICULTY_MODE_KEY, LavaStats.LavaLevel.EASY);
    }

    protected boolean withPeriod(LavaStats.LavaLevel level) {
        return getLevel().equals(level);
    }
}
