package com.minecraft.core.backend.data.list.api;

import com.minecraft.core.Core;
import com.minecraft.core.backend.database.mysql.MySQLDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class LinkData {

    private final MySQLDatabase mysql;

    public LinkData(MySQLDatabase mysql) {
        this.mysql = mysql;
        createTable(mysql);
    }

    public static void createTable(MySQLDatabase mysql) {
        try (Connection conn = mysql.getConnection()) {
            String createTableSql = "CREATE TABLE IF NOT EXISTS link_codes (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "code VARCHAR(10) NOT NULL," +
                    "player_id VARCHAR(36)," +
                    "player_name VARCHAR(16)," +
                    "created_at BIGINT," +
                    "expires_at BIGINT," +
                    "used TINYINT(1) DEFAULT 0," +
                    "used_at BIGINT," +
                    "linked_discord_id VARCHAR(20)," +
                    "linked_discord_name VARCHAR(37)," +
                    "INDEX idx_code (code)," +
                    "INDEX idx_player (player_id)" +
                    ")";

            try (PreparedStatement stmt = conn.prepareStatement(createTableSql)) {
                stmt.execute();
            }

            String[] alterStatements = {
                "ALTER TABLE link_codes ADD COLUMN IF NOT EXISTS player_name VARCHAR(16) AFTER player_id",
                "ALTER TABLE link_codes ADD COLUMN IF NOT EXISTS used TINYINT(1) DEFAULT 0 AFTER expires_at",
                "ALTER TABLE link_codes ADD COLUMN IF NOT EXISTS used_at BIGINT AFTER used",
                "ALTER TABLE link_codes ADD COLUMN IF NOT EXISTS linked_discord_id VARCHAR(20) AFTER used_at",
                "ALTER TABLE link_codes ADD COLUMN IF NOT EXISTS linked_discord_name VARCHAR(37) AFTER linked_discord_id"
            };

            for (String alterSql : alterStatements) {
                try (PreparedStatement stmt = conn.prepareStatement(alterSql)) {
                    stmt.execute();
                } catch (SQLException ignored) {}
            }

            Core.getLogger().info("[LinkData] Tabela 'link_codes' verificada/criada com sucesso.");

        } catch (SQLException e) {
            Core.getLogger().severe("[LinkData] Erro ao criar tabela 'link_codes': " + e.getMessage());
        }
    }

    public String generateCode(UUID playerId) {
        String code = generateRandomCode(6);
        long createdAt = System.currentTimeMillis();
        long expiresAt = createdAt + (15 * 60 * 1000);

        try (Connection conn = mysql.getConnection()) {
            String sql = "INSERT INTO link_codes (player_id, code, created_at, expires_at) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerId.toString());
                stmt.setString(2, code);
                stmt.setLong(3, createdAt);
                stmt.setLong(4, expiresAt);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            Core.getLogger().severe("[LinkData] Erro ao gerar código: " + e.getMessage());
        }

        return code;
    }

    public String useCode(String code) {
        try (Connection conn = mysql.getConnection()) {
            String selectSql = "SELECT player_id FROM link_codes WHERE code = ? AND used = 0 AND expires_at > ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setString(1, code);
                stmt.setLong(2, System.currentTimeMillis());
                var rs = stmt.executeQuery();
                if (rs.next()) {
                    String playerId = rs.getString("player_id");

                    String updateSql = "UPDATE link_codes SET used = 1, used_at = ? WHERE code = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setLong(1, System.currentTimeMillis());
                        updateStmt.setString(2, code);
                        updateStmt.executeUpdate();
                    }

                    return playerId;
                }
            }
        } catch (SQLException e) {
            Core.getLogger().severe("[LinkData] Erro ao usar código: " + e.getMessage());
        }
        return null;
    }

    public void cleanupExpired() {
        try (Connection conn = mysql.getConnection()) {
            String sql = "DELETE FROM link_codes WHERE expires_at < ? AND used = 0";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, System.currentTimeMillis());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            Core.getLogger().severe("[LinkData] Erro ao limpar códigos expirados: " + e.getMessage());
        }
    }

    private String generateRandomCode(int length) {
        StringBuilder code = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(Core.RANDOM.nextInt(chars.length())));
        }
        return code.toString();
    }
}