package com.minecraft.core.backend.database.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minecraft.core.backend.database.DatabaseCredentials;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Getter
public class DatabaseConfig {

    private static final JsonParser PARSER = new JsonParser();

    private final DatabaseCredentials mysql;
    private final DatabaseCredentials redis;

    public DatabaseConfig(File configFile) {
        JsonObject json = loadJson(configFile);
        this.mysql = parseMySql(json);
        this.redis = parseRedis(json);
    }

    public DatabaseConfig() {
        this(findConfigFile());
    }

    private static File findConfigFile() {
        String configPath = System.getProperty("sparta.config.path");
        if (configPath != null) {
            return new File(configPath);
        }
        File shared = new File("../" + com.minecraft.core.Constant.CONFIG_DIR_NAME + "/config.json");
        if (shared.exists()) {
            return shared;
        }
        return new File("config.json");
    }

    private JsonObject loadJson(File configFile) {
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                return PARSER.parse(reader).getAsJsonObject();
            } catch (Exception e) {
                System.err.println("[DatabaseConfig] Erro ao ler " + configFile.getPath() + ": " + e.getMessage());
            }
        }

        InputStream in = getClass().getClassLoader().getResourceAsStream("config.json");
        if (in != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                return PARSER.parse(reader).getAsJsonObject();
            } catch (Exception e) {
                System.err.println("[DatabaseConfig] Erro ao ler config.json do classpath: " + e.getMessage());
            }
        }

        return null;
    }

    private DatabaseCredentials parseMySql(JsonObject json) {
        if (json == null || !json.has("mysql")) return null;
        JsonObject mysql = json.getAsJsonObject("mysql");
        return DatabaseCredentials.builder()
                .host(getString(mysql, "host", "localhost"))
                .port(getInt(mysql, "port", 3306))
                .user(getString(mysql, "user", "root"))
                .password(getString(mysql, "password", ""))
                .database(getString(mysql, "database", com.minecraft.core.Constant.DATABASE_NAME))
                .build();
    }

    private DatabaseCredentials parseRedis(JsonObject json) {
        if (json == null || !json.has("redis")) return null;
        JsonObject redis = json.getAsJsonObject("redis");
        return DatabaseCredentials.builder()
                .host(getString(redis, "host", "127.0.0.1"))
                .port(getInt(redis, "port", 6379))
                .password(getString(redis, "password", ""))
                .build();
    }

    private String getString(JsonObject obj, String key, String def) {
        if (obj.has(key) && !obj.get(key).isJsonNull())
            return obj.get(key).getAsString();
        return def;
    }

    private int getInt(JsonObject obj, String key, int def) {
        if (obj.has(key) && !obj.get(key).isJsonNull())
            return obj.get(key).getAsInt();
        return def;
    }
}
