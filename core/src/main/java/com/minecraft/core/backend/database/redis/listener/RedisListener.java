package com.minecraft.core.backend.database.redis.listener;

import com.minecraft.core.Core;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.logging.Level;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class RedisListener {

    private final RedisDatabase redis;
    private final JedisPubSub pubSub;

    private final String[] channels;
    
    private Jedis subscriptionJedis;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread subscriptionThread;

    public RedisListener(JedisPubSub pubSub, String... channels) {
        this.redis = Core.getRedis();

        this.pubSub = pubSub;

        this.channels = channels;
    }

    public void load() {
        if (running.get()) {
            Core.getLogger().warning("RedisListener já está em execução. Ignorando chamada duplicada.");
            return;
        }

        running.set(true);
        
        subscriptionThread = new Thread(() -> {
            int retryCount = 0;
            final int maxRetries = 5;
            final long retryDelay = 3000;
            
            while (running.get() && retryCount < maxRetries) {
                try {
                    JedisPool pool = redis.getPool();
                    if (pool == null || pool.isClosed()) {
                        retryCount++;
                        if (retryCount < maxRetries && running.get()) {
                            try { Thread.sleep(retryDelay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); running.set(false); break; }
                        }
                        continue;
                    }
                    
                    subscriptionJedis = pool.getResource();
                    Core.getLogger().info("Iniciando subscription nos canais Redis: " + String.join(", ", channels));
                    subscriptionJedis.subscribe(pubSub, channels);
                    break;
                    
                } catch (Exception e) {
                    retryCount++;
                    if (running.get() && retryCount < maxRetries) {
                        Core.getLogger().log(Level.WARNING, "Erro ao registrar canais do Redis (tentativa " + retryCount + "/" + maxRetries + "). " + e.getMessage());
                        
                        if (subscriptionJedis != null) {
                            try {
                                if (subscriptionJedis.isConnected()) {
                                    pubSub.unsubscribe();
                                }
                            } catch (Exception ignored) {
                            }
                            
                            try {
                                subscriptionJedis.close();
                            } catch (Exception ignored) {
                            }
                            
                            subscriptionJedis = null;
                        }
                        
                        if (retryCount < maxRetries && running.get()) {
                            try {
                                Thread.sleep(retryDelay);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                running.set(false);
                                break;
                            }
                        }
                    }
                }
            }
            
            if (retryCount >= maxRetries && running.get()) {
                Core.getLogger().severe("Não foi possível estabelecer conexão Redis após " + maxRetries + " tentativas.");
            }
            
            if (subscriptionJedis != null) {
                try {
                    subscriptionJedis.close();
                } catch (Exception ignored) {
                }
                subscriptionJedis = null;
            }
        }, "RedisListener-Thread");
        
        subscriptionThread.setDaemon(true);
        subscriptionThread.start();
    }
    
    public void unload() {
        if (!running.get()) {
            return;
        }
        
        running.set(false);
        
        Core.getLogger().info("Encerrando RedisListener...");
        
        // Interromper subscription
        try {
            if (pubSub != null) {
                pubSub.unsubscribe();
            }
        } catch (Exception e) {
            // Ignorar se já não está subscribed ou se houver outro erro
            Core.getLogger().log(Level.FINE, "Erro ao fazer unsubscribe do Redis (pode ser normal se já estava desconectado)", e);
        }
        
        // Fechar conexão Jedis
        if (subscriptionJedis != null) {
            try {
                if (subscriptionJedis.isConnected()) {
                    subscriptionJedis.disconnect();
                }
            } catch (Exception ignored) {
            }
            
            try {
                subscriptionJedis.close();
            } catch (Exception ignored) {
            }
            
            subscriptionJedis = null;
        }
        
        // Aguardar thread terminar (com timeout)
        if (subscriptionThread != null && subscriptionThread.isAlive()) {
            try {
                subscriptionThread.interrupt();
                subscriptionThread.join(2000); // Aguardar até 2 segundos
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        Core.getLogger().info("RedisListener encerrado.");
    }
}