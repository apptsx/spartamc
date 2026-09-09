package com.minecraft.core.bukkit.api.sidebar;

import com.minecraft.core.Constant;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.sidebar.row.SidebarRow;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.Scroller;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class Sidebar {

    protected transient final BukkitCore bukkit = BukkitCore.getInstance();

    private final Player owner;

    private final Scoreboard scoreboard;
    private final Objective objective;

    private String title;
    private Scroller animation;

    private final List<SidebarRow> rows;

    private int rowIndex, rowCount;
    @Setter
    private boolean showing, isAnimated = true;

    public void setAnimation(Scroller animation) {
        this.animation = animation;
    }

    public Sidebar(Player owner, String title) {
        this.owner = owner;

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective objective = scoreboard.getObjective(bukkit.getName().toLowerCase());

        if (objective == null)
            objective = scoreboard.registerNewObjective(bukkit.getName().toLowerCase(), "dummy");

        this.objective = objective;

        this.title = title;

        this.rows = new CopyOnWriteArrayList<>();

        this.rowIndex = 0;
        this.rowCount = 0;

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(Util.color(title.length() > 32 ? title.substring(0, 32) : title));

        owner.setScoreboard(scoreboard);
    }

    public void display() {
        rowIndex = 0;

        for (SidebarRow row : rows) {
            if (row == null) continue;

            String lineText = Util.color(row.getPrefix() + row.getSuffix());
            if (lineText.length() > 40) {
                lineText = lineText.substring(0, 40); // Truncate to fit within maximum scoreboard length
            }

            int slot = rows.size() - rowIndex;

            Score score = objective.getScore(lineText);
            score.setScore(slot);

            row.setSlot(slot);
            row.setScore(score);
            rowIndex += 1;
        }

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        this.showing = true;

        if (isAnimated())
            this.animation = new Scroller(ChatColor.stripColor(title).toUpperCase(), "§6§l", "§f§l", "§e§l");

        BukkitCore.getManager().getSidebar().save(this);
    }

    public void clear() {
        scoreboard.getEntries().forEach(scoreboard::resetScores);
        rows.clear();
        rowIndex = 0;
        rowCount = 0;
    }

    public void reset() {
        clear();
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
    }

    public void hide() {
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        scoreboard.getEntries().forEach(scoreboard::resetScores);
        showing = false;
        rowIndex = 0;
    }

    public void setTitle(String title) {
        this.title = title;

        String displayTitle = title;
        if (title.contains("§")) {
            displayTitle = title;
        } else {
            displayTitle = Constant.SERVER_COLOR + ChatColor.BOLD.toString() + title;
        }

        objective.setDisplayName(Util.color(displayTitle.length() > 32 ? displayTitle.substring(0, 32) : displayTitle));
    }

    public boolean hasRow(String field) {
        return rows.stream().anyMatch(row -> row.getField().equalsIgnoreCase(field));
    }

    public SidebarRow getRow(String field) {
        return rows.stream().filter(row -> row.getField().equalsIgnoreCase(field)).findFirst().orElse(null);
    }

    public void addRow(String field, String text) {
        if (!hasRow(field)) {
            String truncatedText = text.length() > 40 ? text.substring(0, 40) : text; // Truncate if necessary
            SidebarRow row = new SidebarRow(field, truncatedText);
            rows.add(row);
            rowCount++;
        }
    }

    public void updateRow(SidebarRow row) {
        updateRow(row.getField(), row.getPrefix());
    }

    public void updateRow(String field, String text) {
        if (hasRow(field)) {
            SidebarRow row = getRow(field);

            if (row != null) {
                String truncatedText = text.length() > 40 ? text.substring(0, 40) : text; // Truncate if necessary

                if (!truncatedText.equals(row.getPrefix())) {
                    scoreboard.resetScores(row.getPrefix());

                    row.setPrefix(truncatedText);
                    row.setSuffix("");

                    Score score = objective.getScore(truncatedText);
                    score.setScore(row.getSlot());

                    row.setScore(score);
                }
            }
        }
    }

    public void removeRow(String field) {
        if (hasRow(field)) {
            SidebarRow row = getRow(field);

            if (row != null) {
                scoreboard.resetScores(row.getPrefix());
                rows.remove(row);
                rowCount--;
            }
        }
    }

    public void blankRow() {
        addRow(randomColor(rowCount), randomColor(rowCount));
    }

    public void addWebsiteRow(String color) {
        addRow("website",color + Constant.SERVER_DOMAIN);
    }

    public void addWebsiteRow() {
        addWebsiteRow("§e§o");
    }

    protected String randomColor(int row) {
        return ChatColor.values()[row].toString() + ChatColor.RESET;
    }
}