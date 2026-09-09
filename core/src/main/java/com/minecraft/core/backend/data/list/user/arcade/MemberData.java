package com.minecraft.core.backend.data.list.user.arcade;

import com.minecraft.core.Core;
import com.minecraft.core.backend.database.mysql.MySQLDatabase;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import com.minecraft.core.controller.list.MemberController;
import com.minecraft.core.member.Member;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MemberData<T extends Member> {

    private final MySQLDatabase mysql;
    private final RedisDatabase redis;
    private final String tableName;

    private final MemberController controller;

    private final Type type;
    private final Class<T> generic;

    private final String MEMBER_KEY_FORMAT;

    protected final static List<String> MEMBER_TABLE_NAMES = Arrays.asList(
            "members_duels", "members_bedwars", "members_pvp", "members_skywars", "members_hungergames");

    @SuppressWarnings("unchecked")
    public MemberData(MySQLDatabase mysql, RedisDatabase redis, String tableName) {
        this.mysql = mysql;
        this.tableName = tableName;
        this.redis = redis;
        this.controller = Core.getMemberController();

        this.type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.generic = (Class<T>) type;

        MEMBER_KEY_FORMAT = "member:" + tableName + ":";
        
        createTable(mysql, tableName);
    }
    
    public static void createTable(MySQLDatabase mysql, String tableName) {
        try (Connection conn = mysql.getConnection()) {
            String createTableSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    "id CHAR(36) PRIMARY KEY," +
                    "account_id CHAR(36) NOT NULL," +
                    "data JSON NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")";
            
            try (PreparedStatement stmt = conn.prepareStatement(createTableSql)) {
                stmt.execute();
            }

            try {
                try (PreparedStatement idx = conn.prepareStatement(
                    "CREATE INDEX idx_" + tableName + "_account ON " + tableName + "(account_id)"
                )) {
                    idx.execute();
                }
            } catch (SQLException ignored) {}

            Core.getLogger().info("[MemberData] Tabela '" + tableName + "' verificada/criada com sucesso.");

        } catch (SQLException e) {
            Core.getLogger().severe("[MemberData] Erro ao criar tabela '" + tableName + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<Member> listAll(UUID id) {
        List<Member> members = new ArrayList<>();

        for (String tableName : MEMBER_TABLE_NAMES) {
            try {
                String sql = "SELECT data FROM " + tableName + " WHERE id = ?";
                
                try (Connection conn = Core.getMysql().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    
                    stmt.setString(1, id.toString());
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        String jsonData = rs.getString("data");
                        members.add(Core.GSON.fromJson(jsonData, Member.class));
                    }
                }

            } catch (Exception e) {
                System.err.println("Erro ao acessar a tabela " + tableName + "!");
                e.printStackTrace();
            }
        }

        return members;
    }

    public T save(T member) {
        String checkSql = "SELECT data FROM " + tableName + " WHERE id = ?";
        
        try (Connection conn = mysql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            
            stmt.setString(1, member.getId().toString());
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                String insertSql = "INSERT INTO " + tableName + " (id, account_id, data) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, member.getId().toString());
                    insertStmt.setString(2, member.getId().toString());
                    insertStmt.setString(3, Core.GSON.toJson(member));
                    insertStmt.executeUpdate();
                }
                
                controller.save(member);
            } else {
                String jsonData = rs.getString("data");
                member = Core.GSON.fromJson(jsonData, type);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return member;
    }

    public T of(UUID id, boolean storeInRedis) {
        Member member = controller.of(id, generic);

        if (member == null) {
            String sql = "SELECT data FROM " + tableName + " WHERE id = ?";
            
            try (Connection conn = mysql.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, id.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String jsonData = rs.getString("data");
                    member = Core.GSON.fromJson(jsonData, generic);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return generic.cast(member);
    }

    public T of(UUID id) {
        return of(id, false);
    }

    public synchronized void update(T member, String field) {
        try {
            String sql = "UPDATE " + tableName + " SET data = ? WHERE id = ?";
            
            try (Connection conn = mysql.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, Core.GSON.toJson(member));
                stmt.setString(2, member.getId().toString());
                stmt.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Core.getLogger().severe("[Member-Update] Não foi possível atualizar o campo \"" + field + "\" no membro de " + member.getName() + "!");
        }
    }

    public void startExpiration(UUID id) {
        controller.remove(id, generic);

        redis.cache(MEMBER_KEY_FORMAT + id, 300);
    }

    public void cancelExpiration(UUID id) {
        redis.persist(MEMBER_KEY_FORMAT + id);
    }

    public List<T> list() {
        List<T> members = new ArrayList<>();
        String sql = "SELECT data FROM " + tableName;
        
        try (Connection conn = mysql.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String jsonData = rs.getString("data");
                T member = Core.GSON.fromJson(jsonData, generic);
                members.add(member);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return members;
    }

    public Collection<T> ranking(String field, int limit) {
        List<T> members = new ArrayList<>();
        
        try (Connection conn = mysql.getConnection()) {
            String sql = "SELECT data FROM " + tableName + " ORDER BY CAST(JSON_EXTRACT(data, ?) AS UNSIGNED) DESC LIMIT ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "$." + field);
                stmt.setInt(2, limit * 2);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    String jsonData = rs.getString("data");
                    T member = Core.GSON.fromJson(jsonData, generic);
                    members.add(member);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return members;
    }
}
