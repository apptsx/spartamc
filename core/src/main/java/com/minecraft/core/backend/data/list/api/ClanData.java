package com.minecraft.core.backend.data.list.api;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.clan.request.ClanRequest;
import com.minecraft.core.api.clan.Clan;
import com.minecraft.core.backend.database.mysql.MySQLDatabase;
import com.minecraft.core.backend.database.redis.RedisDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClanData {

    private final MySQLDatabase mysql;
    private final RedisDatabase redis;

    private final String CLAN_KEY_FORMAT = "clan:",
            CLAN_REQUEST_KEY_FORMAT = "clan-request:";

    public ClanData(MySQLDatabase mysql, RedisDatabase redis) {
        this.mysql = mysql;
        this.redis = redis;
        
        createIndexes(mysql);
    }
    
    public static void createIndexes(MySQLDatabase mysql) {
        try (Connection conn = mysql.getConnection()) {
            try {
                try (PreparedStatement idx1 = conn.prepareStatement(
                    "CREATE INDEX idx_clans_name ON clans(name)"
                )) {
                    idx1.execute();
                }
            } catch (SQLException ignored) {}

            try {
                try (PreparedStatement idx2 = conn.prepareStatement(
                    "CREATE INDEX idx_clans_tag ON clans(tag)"
                )) {
                    idx2.execute();
                }
            } catch (SQLException ignored) {}

        } catch (SQLException e) {
            Core.getLogger().severe("[ClanData] Erro ao criar indices: " + e.getMessage());
        }
    }

    public void save(Clan clan) {
        String checkSql = "SELECT id FROM clans WHERE id = ?";
        
        try (Connection conn = mysql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            
            stmt.setString(1, clan.getId().toString());
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                String insertSql = "INSERT INTO clans (id, name, tag, data) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, clan.getId().toString());
                    insertStmt.setString(2, clan.getName());
                    insertStmt.setString(3, clan.getTag());
                    insertStmt.setString(4, Core.GSON.toJson(clan));
                    insertStmt.executeUpdate();
                }
            }

            redis.save(CLAN_KEY_FORMAT + clan.getId().toString(), clan);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Clan of(UUID id, boolean storeInRedis) {
        Clan clan = redis.load(CLAN_KEY_FORMAT + id, Clan.class);

        if (clan == null) {
            String sql = "SELECT data FROM clans WHERE id = ?";
            
            try (Connection conn = mysql.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, id.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String jsonData = rs.getString("data");
                    clan = Core.GSON.fromJson(jsonData, Clan.class);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (clan != null && storeInRedis)
                redis.save(CLAN_KEY_FORMAT + id, clan);
        }

        return clan;
    }

    public Clan of(UUID id) {
        return of(id, false);
    }

    public Clan getByName(String name) {
        Clan clan = listInRedis().stream().filter(search -> search.getName().equalsIgnoreCase(name)).findFirst().orElse(null);

        if (clan == null) {
            String sql = "SELECT data FROM clans WHERE name = ?";
            
            try (Connection conn = mysql.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String jsonData = rs.getString("data");
                    clan = Core.GSON.fromJson(jsonData, Clan.class);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return clan;
    }

    public Clan getByTag(String tag) {
        Clan clan = listInRedis().stream().filter(search -> search.getTag().equalsIgnoreCase(tag)).findFirst().orElse(null);

        if (clan == null) {
            String sql = "SELECT data FROM clans WHERE tag = ?";
            
            try (Connection conn = mysql.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, tag);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String jsonData = rs.getString("data");
                    clan = Core.GSON.fromJson(jsonData, Clan.class);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return clan;
    }

    public void update(Clan clan, String field) {
        String sql = "UPDATE clans SET data = ?, name = ?, tag = ? WHERE id = ?";
        
        try (Connection conn = mysql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, Core.GSON.toJson(clan));
            stmt.setString(2, clan.getName());
            stmt.setString(3, clan.getTag());
            stmt.setString(4, clan.getId().toString());
            stmt.executeUpdate();
            
            redis.update(CLAN_KEY_FORMAT + clan.getId().toString(), clan);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Clan clan) {
        String sql = "DELETE FROM clans WHERE id = ?";
        
        try (Connection conn = mysql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, clan.getId().toString());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        redis.delete(CLAN_KEY_FORMAT + clan.getId().toString());
    }

    public List<Clan> listInRedis() {
        return redis.loadAll(CLAN_KEY_FORMAT, Clan.class);
    }

    public List<Clan> list() {
        List<Clan> clans = new ArrayList<>();
        String sql = "SELECT data FROM clans";
        
        try (Connection conn = mysql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String jsonData = rs.getString("data");
                Clan clan = Core.GSON.fromJson(jsonData, Clan.class);
                clans.add(clan);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return clans;
    }

    public List<ClanRequest> getRequests() {
        return redis.loadAll(CLAN_REQUEST_KEY_FORMAT, ClanRequest.class);
    }

    public boolean hasRequest(Clan clan, UUID receiver) {
        return getRequests().stream().anyMatch(request -> request.getId().equals(clan.getId()) && request.getReceiver().equals(receiver));
    }

    public ClanRequest getRequest(UUID author, UUID receiver) {
        return getRequests().stream().filter(request -> request.getAuthor().equals(author) && request.getReceiver().equals(receiver))
                .findFirst().orElse(null);
    }

    public void addRequest(ClanRequest request) {
        redis.save(CLAN_REQUEST_KEY_FORMAT + request.getIdentifier(), request, 300);
    }

    public void removeRequest(ClanRequest request) {
        redis.delete(CLAN_REQUEST_KEY_FORMAT + request.getIdentifier());
    }
}
