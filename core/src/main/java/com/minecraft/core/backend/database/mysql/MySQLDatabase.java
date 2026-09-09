package com.minecraft.core.backend.database.mysql;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.backend.database.Database;
import com.minecraft.core.backend.database.DatabaseCredentials;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

@Getter
public class MySQLDatabase implements Database {

    private final DatabaseCredentials credentials;
    private HikariDataSource dataSource;

    public MySQLDatabase(DatabaseCredentials credentials) {
        this.credentials = credentials;
    }

    public MySQLDatabase(boolean local) {
        this(DatabaseCredentials.builder()
                .host(System.getProperty("mysql.host", "localhost"))
                .port(Integer.parseInt(System.getProperty("mysql.port", "3306")))
                .database(System.getProperty("mysql.database", com.minecraft.core.Constant.DATABASE_NAME))
                .user(System.getProperty("mysql.username", "root"))
                .password(System.getProperty("mysql.password", ""))
                .build());
    }

    @Override
    public void load() {
        Instant now = Instant.now();
        Core.getLogger().info("Conectando ao MySQL...");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Core.getLogger().severe("Driver MySQL não encontrado!");
            return;
        }

        if (!createDatabaseIfNotExists()) {
            Core.getLogger().severe("Não foi possível criar/conectar ao banco de dados.");
            return;
        }

        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                HikariConfig config = getHikariConfig();

                dataSource = new HikariDataSource(config);
                
                try (Connection conn = dataSource.getConnection()) {
                    if (conn.isValid(5)) {
                        Core.getLogger().info("Conexão com MySQL efetuada com sucesso. (Tempo médio: " + Duration.between(now, Instant.now()).toMillis() + "ms)");
                        createTablesIfNotExists(conn);
                        break;
                    }
                }
                
            } catch (Exception e) {
                Core.getLogger().warning("Erro ao estabelecer conexão com o MySQL. Tentativa " + (i + 1) + " de " + maxRetries);
                e.printStackTrace();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (dataSource == null || dataSource.isClosed()) {
            Core.getLogger().severe("Não foi possível efeturar a conexão com o MySQL após " + maxRetries +
                " tentativas. Verifique as credenciais.");
        }
    }

    private boolean createDatabaseIfNotExists() {
        String jdbcUrl = String.format(
            "jdbc:mysql://%s:%d/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=30000",
            credentials.getHost(),
            credentials.getPort()
        );

        try (Connection conn = java.sql.DriverManager.getConnection(jdbcUrl, credentials.getUser(), credentials.getPassword())) {
            conn.createStatement().executeUpdate("CREATE DATABASE IF NOT EXISTS `" + credentials.getDatabase() + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            Core.getLogger().info("Banco de dados '" + credentials.getDatabase() + "' verificado/criado com sucesso.");
            return true;
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao criar banco de dados: " + e.getMessage());
            return false;
        }
    }

    private void createTablesIfNotExists(Connection conn) {
        String[][] tables = {
            {"accounts", "CREATE TABLE IF NOT EXISTS accounts (id CHAR(36) PRIMARY KEY, name VARCHAR(16) NOT NULL, data JSON NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"},
            {"punishments", "CREATE TABLE IF NOT EXISTS punishments (id VARCHAR(255) PRIMARY KEY, player CHAR(36) NOT NULL, author CHAR(36) NOT NULL, category VARCHAR(20) NOT NULL DEFAULT 'BAN', data JSON NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"},
            {"clans", "CREATE TABLE IF NOT EXISTS clans (id CHAR(36) PRIMARY KEY, name VARCHAR(255) NOT NULL, tag VARCHAR(10) NOT NULL, data JSON NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)"},
            {"seasons", "CREATE TABLE IF NOT EXISTS seasons (id INT PRIMARY KEY, data JSON NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)"},
            {"link_codes", "CREATE TABLE IF NOT EXISTS link_codes (id INT AUTO_INCREMENT PRIMARY KEY, code VARCHAR(10) NOT NULL, player_id VARCHAR(36), player_name VARCHAR(16), created_at BIGINT, expires_at BIGINT, used TINYINT(1) DEFAULT 0, used_at BIGINT, linked_discord_id VARCHAR(20), linked_discord_name VARCHAR(37))"},
            {"server_whitelists", "CREATE TABLE IF NOT EXISTS server_whitelists (id INT AUTO_INCREMENT PRIMARY KEY, server_identifier VARCHAR(50) UNIQUE NOT NULL, enabled BOOLEAN DEFAULT FALSE, players JSON, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)"}
        };

        for (String[] table : tables) {
            try {
                conn.createStatement().executeUpdate(table[1]);
                Core.getLogger().info("Tabela '" + table[0] + "' verificada/criada com sucesso.");
            } catch (SQLException e) {
                Core.getLogger().warning("Erro ao criar tabela " + table[0] + ": " + e.getMessage());
            }
        }

        String[][] migrations = {
            {"link_codes", "ALTER TABLE link_codes ADD COLUMN IF NOT EXISTS player_name VARCHAR(16) AFTER player_id"},
            {"link_codes", "ALTER TABLE link_codes ADD COLUMN IF NOT EXISTS used TINYINT(1) DEFAULT 0 AFTER expires_at"},
            {"link_codes", "ALTER TABLE link_codes ADD COLUMN IF NOT EXISTS used_at BIGINT AFTER used"},
            {"link_codes", "ALTER TABLE link_codes ADD COLUMN IF NOT EXISTS linked_discord_id VARCHAR(20) AFTER used_at"},
            {"link_codes", "ALTER TABLE link_codes ADD COLUMN IF NOT EXISTS linked_discord_name VARCHAR(37) AFTER linked_discord_id"},
            {"server_whitelists", "ALTER TABLE server_whitelists ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at"},
        };

        for (String[] migration : migrations) {
            try {
                conn.createStatement().executeUpdate(migration[1]);
                Core.getLogger().info("Migração '" + migration[0] + "' aplicada: " + migration[1]);
            } catch (SQLException ignored) {}
        }
    }

    private HikariConfig getHikariConfig() {
        HikariConfig config = new HikariConfig();

        String jdbcUrl = String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=30000&socketTimeout=30000&autoReconnect=true",
            credentials.getHost(),
            credentials.getPort(),
            credentials.getDatabase()
        );

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(credentials.getUser());
        config.setPassword(credentials.getPassword());
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPoolName("MySQLPool");
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        return config;
    }

    @Override
    public void unload() {
        if (isAvailable()) {
            dataSource.close();
            Core.getLogger().info("Conexão com MySQL encerrada.");
        }
    }

    @Override
    public boolean isAvailable() {
        return dataSource != null && !dataSource.isClosed();
    }

    public Connection getConnection() throws SQLException {
        if (!isAvailable()) {
            throw new SQLException("DataSource não está disponível");
        }
        return dataSource.getConnection();
    }
}
