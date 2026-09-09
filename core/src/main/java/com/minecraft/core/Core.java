package com.minecraft.core;

import com.minecraft.core.api.mojang.MojangApi;
import com.minecraft.core.backend.data.list.api.*;
import com.minecraft.core.backend.data.list.server.RouteData;
import com.minecraft.core.backend.data.list.server.SeasonData;
import com.minecraft.core.backend.data.list.server.ServerData;
import com.minecraft.core.backend.data.list.user.AccountData;
import com.minecraft.core.backend.data.list.user.arcade.list.*;
import com.minecraft.core.backend.database.DatabaseCredentials;
import com.minecraft.core.backend.database.config.DatabaseConfig;
import com.minecraft.core.backend.database.mysql.MySQLDatabase;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import com.minecraft.core.controller.list.AccountController;
import com.minecraft.core.controller.list.CollectibleController;
import com.minecraft.core.controller.list.JoinMessageController;
import com.minecraft.core.controller.list.MemberController;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.type.ServerType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Logger;

public class Core {

    @Getter @Setter private static Platform platform;
    @Getter @Setter private static Logger logger;
    @Getter @Setter private static JavaPlugin javaPlugin;
    @Getter @Setter private static MySQLDatabase mysql;
    @Getter @Setter private static RedisDatabase redis;
    @Getter @Setter private static ServerData serverData;
    @Getter @Setter private static SeasonData seasonData;
    @Getter @Setter private static ReportData reportData;
    @Getter @Setter private static AccountData accountData;
    @Getter @Setter private static RouteData routeData;
    @Getter @Setter private static ClanData clanData;
    @Getter @Setter private static ArcadeData arcadeData;
    @Getter @Setter private static DuelsData duelsData;
    @Getter @Setter private static PvPData pvpData;
    @Getter @Setter private static BedWarsData bedWarsData;
    @Getter @Setter private static EggWarsData eggWarsData;
    @Getter @Setter private static SkyWarsData skyWarsData;
    @Getter @Setter private static TheBridgeData theBridgeData;
    @Getter @Setter private static HungerGamesData hungerGamesData;
    @Getter @Setter private static PartyData partyData;
    @Getter @Setter private static PunishmentData punishmentData;
    @Getter @Setter private static RedirectData redirectData;
    @Getter @Setter private static ReconnectData reconnectData;
    @Getter @Setter private static SkinCacheData skinCacheData;
    @Getter @Setter private static ServerType serverType = ServerType.BUNGEE;
    @Getter @Setter private static int serverId = 0, serverPort = 25565;
    @Getter private final static AccountController accountController = new AccountController();
    @Getter private final static MemberController memberController = new MemberController();
    @Getter private final static CollectibleController collectibleController = new CollectibleController();
    @Getter private final static JoinMessageController joinMessageController = new JoinMessageController();

    public static Gson GSON = new GsonBuilder()
            .registerTypeAdapter(com.minecraft.core.account.context.objects.rank.type.RankType.class, 
                new com.google.gson.JsonSerializer<com.minecraft.core.account.context.objects.rank.type.RankType>() {
                    @Override
                    public com.google.gson.JsonElement serialize(com.minecraft.core.account.context.objects.rank.type.RankType src, java.lang.reflect.Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
                        return new com.google.gson.JsonPrimitive(src.name());
                    }
                })
            .registerTypeAdapter(com.minecraft.core.account.context.objects.rank.type.RankType.class, 
                new com.google.gson.JsonDeserializer<com.minecraft.core.account.context.objects.rank.type.RankType>() {
                    @Override
                    public com.minecraft.core.account.context.objects.rank.type.RankType deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT, com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
                        if (json == null || json.isJsonNull()) return com.minecraft.core.account.context.objects.rank.type.RankType.MEMBER;
                        try {
                            return com.minecraft.core.account.context.objects.rank.type.RankType.valueOf(json.getAsString());
                        } catch (IllegalArgumentException e) {
                            getLogger().warning("RankType inválido: " + json.getAsString() + ", usando MEMBER como padrão");
                            return com.minecraft.core.account.context.objects.rank.type.RankType.MEMBER;
                        }
                    }
                })
            .registerTypeAdapter(com.minecraft.core.account.context.objects.rank.Rank.class, new com.minecraft.core.account.context.objects.rank.Rank.RankDeserializer())
            .create();
    public static JsonParser PARSER = new JsonParser();
    public static Random RANDOM = new Random();
    public static MojangApi MOJANG_API = new MojangApi();

    public static Server getLocalServer() {
        return serverData.local();
    }

    public static boolean isUntouchable(UUID id) {
        return Constant.DEVELOPERS_IDS.contains(id);
    }

    public static void load(Platform platform, ServerData serverData, Logger logger) {
        logger.info("Iniciando Core...");

        setPlatform(platform);
        setLogger(logger);

        TimeZone.setDefault(TimeZone.getTimeZone("GMT-3"));

        DatabaseConfig dbConfig = new DatabaseConfig();

        DatabaseCredentials mysqlCreds = dbConfig.getMysql();
        if (mysqlCreds == null) {
            mysqlCreds = DatabaseCredentials.builder()
                    .host(System.getenv().getOrDefault("MYSQL_HOST", "localhost"))
                    .port(Integer.parseInt(System.getenv().getOrDefault("MYSQL_PORT", "3306")))
                    .user(System.getenv().getOrDefault("MYSQL_USER", "root"))
                    .password(System.getenv().getOrDefault("MYSQL_PASSWORD", ""))
                    .database(System.getenv().getOrDefault("MYSQL_DATABASE", Constant.DATABASE_NAME))
                    .build();
            logger.warning("config.json nao encontrado, usando variaveis de ambiente MYSQL_*.");
        }

        DatabaseCredentials redisCreds = dbConfig.getRedis();
        if (redisCreds == null) {
            redisCreds = DatabaseCredentials.builder()
                    .host(System.getenv().getOrDefault("REDIS_HOST", "127.0.0.1"))
                    .port(Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379")))
                    .password(System.getenv().getOrDefault("REDIS_PASSWORD", ""))
                    .build();
            logger.warning("config.json nao encontrado, usando variaveis de ambiente REDIS_*.");
        }

        System.setProperty("mysql.host", mysqlCreds.getHost());
        System.setProperty("mysql.port", String.valueOf(mysqlCreds.getPort()));
        System.setProperty("mysql.username", mysqlCreds.getUser());
        System.setProperty("mysql.password", mysqlCreds.getPassword());
        System.setProperty("mysql.database", mysqlCreds.getDatabase());

        logger.info("MySQL: " + mysqlCreds.getHost() + ":" + mysqlCreds.getPort() + "/" + mysqlCreds.getDatabase());
        logger.info("Redis: " + redisCreds.getHost() + ":" + redisCreds.getPort());

        MySQLDatabase mysql = new MySQLDatabase(mysqlCreds);
        mysql.load();

        System.setProperty("redis.host", redisCreds.getHost());
        System.setProperty("redis.port", String.valueOf(redisCreds.getPort()));
        System.setProperty("redis.password", redisCreds.getPassword());

        RedisDatabase redis = new RedisDatabase(redisCreds);
        redis.load();

        setMysql(mysql);
        setRedis(redis);

        setServerData(serverData);
        setSeasonData(new SeasonData(mysql));

        setRouteData(new RouteData(redis));
        setArcadeData(new ArcadeData(redis));
        setReportData(new ReportData(redis));
        setClanData(new ClanData(mysql, redis));
        setPartyData(new PartyData(redis));
        setPunishmentData(new PunishmentData(mysql));
        setRedirectData(new RedirectData(redis));
        setReconnectData(new ReconnectData(redis));
        setSkinCacheData(new SkinCacheData(redis));

        setAccountData(new AccountData(mysql, redis));

        setDuelsData(new DuelsData(mysql, redis));
        setPvpData(new PvPData(mysql, redis));
        setBedWarsData(new BedWarsData(mysql, redis));
        setEggWarsData(new EggWarsData(mysql, redis));
        setSkyWarsData(new SkyWarsData(mysql, redis));
        setTheBridgeData(new TheBridgeData(mysql, redis));
        setHungerGamesData(new HungerGamesData(mysql, redis));

        logger.info("Core iniciado com sucesso.");
        logger.info("MySQL conectado em: " + System.getProperty("mysql.host") + ":" + System.getProperty("mysql.port"));
        logger.info("Redis conectado em: " + System.getProperty("redis.host") + ":" + System.getProperty("redis.port"));
        logger.info("Título de jogadores sincronizados via Redis.");
    }

    public static void unload() {
        logger.info("Cancelando serviços...");

        if (serverData != null) {
            serverData.stop();
        }

        if (mysql != null) {
            mysql.unload();
        }
        if (redis != null) {
            redis.unload();
        }

        logger.info("Serviços cancelados com sucesso.");
    }

    public static void scan(String title, Instant started) {
        logger.info("[Scan/" + title + "] Examined with " + Duration.between(started, Instant.now()).toMillis() + " ms!");
    }

    public static void message(String... texts) {
        for (String text : texts)
            logger.info(text);
    }
}
