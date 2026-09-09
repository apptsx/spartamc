package com.minecraft.core.backend.data.list.user;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.backend.data.template.UserTemplate;
import com.minecraft.core.backend.database.mysql.MySQLDatabase;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import com.minecraft.core.backend.database.redis.message.types.account.AccountUpdateMessage;
import com.minecraft.core.controller.list.AccountController;
import com.minecraft.core.util.list.JsonUtil;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class AccountData implements UserTemplate<Account> {

    private final MySQLDatabase mysql;
    private final RedisDatabase redis;

    private final AccountController controller;

    private final String ACCOUNT_KEY_FORMAT = "account:";

    public AccountData(MySQLDatabase mysql, RedisDatabase redis) {
        this.mysql = mysql;
        this.redis = redis;
        this.controller = Core.getAccountController();
        createIndexes(mysql);
    }

    public static void createIndexes(MySQLDatabase mysql) {
        try (Connection conn = mysql.getConnection()) {
            try {
                try (PreparedStatement idx1 = conn.prepareStatement(
                        "CREATE INDEX idx_accounts_name ON accounts(name)"
                )) {
                    idx1.execute();
                }
            } catch (SQLException ignored) {}

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Account save(Account account) {
        String sql = "SELECT data FROM accounts WHERE id = ?";
        
        try (Connection conn = mysql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, account.getId().toString());
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                String insertSql = "INSERT INTO accounts (id, name, data) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, account.getId().toString());
                    insertStmt.setString(2, account.getName());
                    insertStmt.setString(3, Core.GSON.toJson(account));
                    insertStmt.executeUpdate();
                }
                
                controller.save(account);
            } else {
                String jsonData = rs.getString("data");
                account = Core.GSON.fromJson(jsonData, Account.class);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return account;
    }

    @Override
    public void delete(Account account) {
        String sql = "DELETE FROM accounts WHERE id = ?";
        
        try (Connection conn = mysql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, account.getId().toString());
            int deleted = stmt.executeUpdate();
            
            if (deleted > 0) {
                controller.remove(account.getId());
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            Core.getLogger().severe("[Account-Delete] Erro ao deletar conta de " + account.getName());
        }
    }

    public void clearCache() {
        controller.getCache().clear();

        redis.removeAll(ACCOUNT_KEY_FORMAT);
    }

    @Override
    public void update(Account account, String field) {
        try {
            JsonObject tree = JsonUtil.jsonTree(account);
            String sql = "UPDATE accounts SET data = ? WHERE id = ?";

            try (Connection conn = mysql.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, Core.GSON.toJson(account));
                stmt.setString(2, account.getId().toString());

                int updated = stmt.executeUpdate();

                if (updated > 0) {
                    new AccountUpdateMessage(account, field, tree.get(field)).send();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startExpiration(Account account) {
        controller.remove(account.getId());

        redis.cache(ACCOUNT_KEY_FORMAT + account.getId(), 80);
    }

    public void removeCache(Account account) {
        controller.remove(account.getId());

        redis.delete(ACCOUNT_KEY_FORMAT + account.getId());
    }

    @Override
    public void cancelExpiration(Account account) {
        redis.persist(ACCOUNT_KEY_FORMAT + account.getId());
    }

    @Override
    public Account of(UUID id, boolean storeInRedis) {
        Account account = controller.of(id);

        if (account == null) {
            String sql = "SELECT data FROM accounts WHERE id = ?";
            
            try (Connection conn = mysql.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, id.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String jsonData = rs.getString("data");
                    account = Core.GSON.fromJson(jsonData, Account.class);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return account;
    }

    @Override
    public Account of(String name) {
        Account account = controller.of(name);

        if (account == null) {
            String sql = "SELECT data FROM accounts WHERE LOWER(name) = LOWER(?)";
            
            try (Connection conn = mysql.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String jsonData = rs.getString("data");
                    account = Core.GSON.fromJson(jsonData, Account.class);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return account;
    }

    public List<Account> getAlts(String ipAddress) {
        List<Account> accounts = new ArrayList<>();
        
        for (Account account : list()) {
            if (account.getIpAddress() != null && account.getIpAddress().equals(ipAddress)) {
                accounts.add(account);
            }
        }
        
        return accounts;
    }

    public boolean isExceededAccountLimit(String name, String ipAddress) {
        final int altSize = getAlts(ipAddress).size(),
                accountLimit = 3;

        Account existingAccount = of(name);

        return existingAccount == null && altSize >= accountLimit;
    }

    @Override
    public List<Account> list() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT data FROM accounts";
        
        try (Connection conn = mysql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String jsonData = rs.getString("data");
                Account account = Core.GSON.fromJson(jsonData, Account.class);
                accounts.add(account);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return accounts;
    }

    @Override
    public Collection<Account> ranking(String field, int limit) {
        List<Account> accounts = new ArrayList<>();
        
        try (Connection conn = mysql.getConnection()) {
            String sql = "SELECT data FROM accounts ORDER BY CAST(JSON_EXTRACT(data, ?) AS UNSIGNED) DESC LIMIT ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "$." + field);
                stmt.setInt(2, limit * 2);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    String jsonData = rs.getString("data");
                    Account account = Core.GSON.fromJson(jsonData, Account.class);
                    accounts.add(account);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return accounts;
    }
}
