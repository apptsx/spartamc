package com.minecraft.core.backend.database.redis;

import com.minecraft.core.Core;
import com.minecraft.core.backend.database.Database;
import com.minecraft.core.backend.database.DatabaseCredentials;
import com.minecraft.core.util.list.JsonUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Getter
@RequiredArgsConstructor
public class RedisDatabase implements Database {

    private final DatabaseCredentials credential;
    private JedisPool pool;

    @Override
    public void load() {
        Instant now = Instant.now();

        Core.getLogger().info("Conectando ao Redis...");

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(128);
        config.setMaxIdle(32);
        config.setMinIdle(8);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        config.setTestWhileIdle(true);
        config.setBlockWhenExhausted(true);
        config.setMaxWaitMillis(5000);
        config.setNumTestsPerEvictionRun(3);
        config.setTimeBetweenEvictionRunsMillis(60000);
        config.setMinEvictableIdleTimeMillis(300000);

        String host = credential.getHost() != null && !credential.getHost().isEmpty() ? credential.getHost() : "127.0.0.1";
        if ("localhost".equalsIgnoreCase(host)) host = "127.0.0.1";
        int port = credential.getPort();

        if (credential.getPassword() != null && !credential.getPassword().isEmpty()) {
            pool = new JedisPool(config, host, port, 5000, credential.getPassword());
        } else {
            pool = new JedisPool(config, host, port, 5000);
        }

        try (Jedis jedis = pool.getResource()) {
            jedis.ping();
            Core.getLogger().info("Conexão com Redis efetuada com sucesso. (Tempo: " + Duration.between(now, Instant.now()).toMillis() + "ms)");
        } catch (Exception e) {
            Core.getLogger().severe("Erro ao conectar ao Redis: " + e.getMessage());
            if (pool != null) {
                try { pool.destroy(); } catch (Exception ignored) {}
                pool = null;
            }
        }
    }

    @Override
    public void unload() {
        if (isAvailable()) pool.destroy();
    }

    @Override
    public boolean isAvailable() {
        return pool != null && !pool.isClosed();
    }

    public JedisPool getPool() {
        return pool;
    }


    public boolean exists(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.exists(key);
        }
    }

    public void save(String key, Object object) {
        if (!isAvailable()) return;
        try (Jedis jedis = pool.getResource()) {
            Map<String, String> fields = JsonUtil.objectToMap(object);
            fields.values().removeIf(Objects::isNull);
            jedis.hmset(key, fields);
        } catch (Exception e) {
            Core.getLogger().severe("[Redis] Erro ao salvar " + key + ": " + e.getMessage());
        }
    }

    public void saveDefault(String key, Object value) {
        try (Jedis jedis = pool.getResource()) {
            if (jedis.exists(key)) return;
            jedis.set(key, value.toString());
        }
    }

    public void saveDefault(String key, Object value, int expires) {
        try (Jedis jedis = pool.getResource()) {
            if (jedis.exists(key)) return;
            jedis.setex(key, expires, value.toString());
        }
    }

    public Object getDefault(String key) {
        try (Jedis jedis = pool.getResource()) {
            if (!jedis.exists(key)) return null;
            return jedis.get(key);
        }
    }

    public void update(String key, Object object) {
        if (!isAvailable()) {
            Core.getLogger().warning("[Redis] Tentativa de atualizar com Redis indisponível: " + key);
            return;
        }
        
        try (Jedis jedis = pool.getResource()) {
            if (!jedis.exists(key)) {
                Core.getLogger().warning("[Redis] Tentativa de atualizar chave inexistente: " + key);
                return;
            }
            Map<String, String> fields = JsonUtil.objectToMap(object);
            fields.values().removeIf(Objects::isNull);
            jedis.hmset(key, fields);
        } catch (Exception e) {
            Core.getLogger().severe("[Redis] Erro ao atualizar " + key + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void flush() {
        try (Jedis jedis = pool.getResource()) {
            jedis.flushAll();
        }
    }

    public void publish(String channel, String message) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, message);
        }
    }

    public void save(String key, Object object, int expire) {
        save(key, object);
        cache(key, expire);
    }

    public void delete(String key) {
        if (!isAvailable()) return;
        try (Jedis jedis = pool.getResource()) {
            jedis.del(key);
        } catch (Exception e) {
            Core.getLogger().severe("[Redis] Erro ao deletar " + key + ": " + e.getMessage());
        }
    }

    public void cache(String key, int seconds) {
        try (Jedis jedis = pool.getResource()) {
            jedis.expire(key, seconds);
        }
    }

    public void persist(String key) {
        try (Jedis jedis = pool.getResource()) {
            jedis.persist(key);
        }
    }

    public int ttl(String key) {
        try (Jedis jedis = pool.getResource()) {
            return (int) jedis.ttl(key);
        }
    }

    public void delete(String key, String... fields) {
        try (Jedis jedis = pool.getResource()) {
            jedis.hdel(key, fields);
        }
    }

    public <T> T load(String key, Class<T> tClass) {
        if (!exists(key)) return null;
        try (Jedis jedis = pool.getResource()) {
            Map<String, String> fields = jedis.hgetAll(key);
            if (fields == null || fields.isEmpty()) return null;
            fields.values().removeIf(Objects::isNull);
            return JsonUtil.mapToObject(fields, tClass);
        }
    }

    public <T> List<T> loadAll(String key, Class<T> type) {
        List<T> list = new ArrayList<>();
        if (!isAvailable()) return list;

        try (Jedis jedis = pool.getResource()) {
            Set<String> keys = jedis.keys(key + "*");
            if (keys == null || keys.isEmpty()) return list;

            for (String keyName : keys) {
                try {
                    Map<String, String> fields = jedis.hgetAll(keyName);
                    if (fields == null || fields.isEmpty()) continue;
                    fields.values().removeIf(Objects::isNull);
                    T object = JsonUtil.mapToObject(fields, type);
                    if (object != null) list.add(type.cast(object));
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            Core.getLogger().severe("[Redis] Erro ao carregar lista " + key + ": " + e.getMessage());
        }

        return list;
    }

    public void removeAll(String key) {
        if (!isAvailable()) return;
        try (Jedis jedis = pool.getResource()) {
            Set<String> keys = jedis.keys(key + "*");
            if (keys != null && !keys.isEmpty()) keys.forEach(jedis::del);
        } catch (Exception e) {
            Core.getLogger().severe("[Redis] Erro ao remover chaves " + key + ": " + e.getMessage());
        }
    }

    public int getNumberKeys(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.smembers(key).size();
        }
    }
}
