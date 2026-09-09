package com.minecraft.core.backend.data.list.api;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.punishment.Punishment;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.backend.database.mysql.MySQLDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PunishmentData {

    private final MySQLDatabase mysql;

    public PunishmentData(MySQLDatabase mysql) {
        this.mysql = mysql;
        
        migrateAndIndex(mysql);
    }
    
    public static void migrateAndIndex(MySQLDatabase mysql) {
        try (Connection conn = mysql.getConnection()) {
            try {
                try (PreparedStatement alter = conn.prepareStatement(
                        "ALTER TABLE punishments ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'BAN'"
                )) {
                    alter.execute();
                }
            } catch (SQLException ignored) {}
            
            try {
                try (PreparedStatement idx1 = conn.prepareStatement(
                        "CREATE INDEX idx_punishments_player ON punishments(player)"
                )) {
                    idx1.execute();
                }
            } catch (SQLException ignored) {}
            
            try {
                try (PreparedStatement idx2 = conn.prepareStatement(
                        "CREATE INDEX idx_punishments_author ON punishments(author)"
                )) {
                    idx2.execute();
                }
            } catch (SQLException ignored) {}
            
            try {
                try (PreparedStatement idx3 = conn.prepareStatement(
                        "CREATE INDEX idx_punishments_category ON punishments(category)"
                )) {
                    idx3.execute();
                }
            } catch (SQLException ignored) {}
        
        } catch (SQLException e) {
            Core.getLogger().severe("[PunishmentData] Erro ao migrar tabela 'punishments': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void save(Punishment punishment) {
        String checkSql = "SELECT id FROM punishments WHERE id = ?";
        
        try (Connection conn = mysql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            
            stmt.setString(1, punishment.getId());
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                String insertSql = "INSERT INTO punishments (id, player, author, category, data) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, punishment.getId());
                    insertStmt.setString(2, punishment.getPlayer().toString());
                    insertStmt.setString(3, punishment.getAuthor().toString());
                    insertStmt.setString(4, punishment.getCategory().name());
                    insertStmt.setString(5, Core.GSON.toJson(punishment));
                    insertStmt.executeUpdate();
                }
                
                Account player = Core.getAccountData().of(punishment.getPlayer());
                
                if (player != null && !player.getId().equals(punishment.getAuthor()))
                    player.addPunishmentToHistory(punishment);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Punishment punishment) {
        String sql = "DELETE FROM punishments WHERE id = ?";
        
        try (Connection conn = mysql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, punishment.getId());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void update(Punishment punishment, String field) {
        String sql = "UPDATE punishments SET data = ? WHERE id = ?";
        
        try (Connection conn = mysql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, Core.GSON.toJson(punishment));
            stmt.setString(2, punishment.getId());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean has(UUID player, PunishmentCategory category) {
        return of(player, category) != null;
    }

    public Punishment of(String ip, PunishmentCategory category) {
        Punishment punishment = listAssets(category).stream()
                .filter(search -> search.getIp() != null && search.getIp().equalsIgnoreCase(ip) && search.isValid())
                .findFirst()
                .orElse(null);

        if (punishment != null && punishment.hasExpired()) {
            punishment.revoke(Constant.DEFAULT_ID, "Expirado");

            Account target = Core.getAccountData().of(punishment.getPlayer(), false);

            if (target != null)
                target.updatePunishment(punishment);

            return null;
        }

        return punishment;
    }

    public Punishment of(UUID player, PunishmentCategory category) {
        return list(category).stream()
                .filter(punishment -> punishment.getPlayer().equals(player) && punishment.isValid())
                .findFirst()
                .orElse(null);
    }

    public Punishment of(String id) {
        return list().stream()
                .filter(punishment -> punishment.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public List<Punishment> listAssets(PunishmentCategory category) {
        return list(category).stream().filter(punishment -> punishment.isValid() && !punishment.getRevocation().isValid()).collect(Collectors.toList());
    }

    public List<Punishment> list() {
        List<Punishment> punishments = new ArrayList<>();
        String sql = "SELECT data FROM punishments";
        
        try (Connection conn = mysql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String jsonData = rs.getString("data");
                Punishment punishment = Core.GSON.fromJson(jsonData, Punishment.class);
                punishments.add(punishment);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return punishments;
    }

    public List<Punishment> list(PunishmentCategory category) {
        return list().stream().filter(punishment -> punishment.getCategory().equals(category)).collect(Collectors.toList());
    }

    public List<Account> getTopPunishers(int limit) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT author, COUNT(*) as count FROM punishments GROUP BY author ORDER BY count DESC LIMIT ?";
        
        try (Connection conn = mysql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String authorId = rs.getString("author");
                Account account = Core.getAccountData().of(UUID.fromString(authorId));
                if (account != null) {
                    accounts.add(account);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return accounts;
    }

    public int count(UUID player, PunishmentCategory category) {
        String sql = "SELECT COUNT(*) as count FROM punishments WHERE player = ? AND category = ?";
        
        try (Connection conn = mysql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, player.toString());
            stmt.setString(2, category.name());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
}
