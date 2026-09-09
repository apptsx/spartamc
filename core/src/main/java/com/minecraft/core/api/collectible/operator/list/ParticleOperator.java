package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.particle.ParticleCollectible;
import com.minecraft.core.api.collectible.type.particle.ParticleType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParticleOperator extends CollectibleOperator {

    private static final Map<String, ParticleType> particleTypes = new HashMap<>();
    private static final Map<UUID, Location> lastPlayerLocations = new HashMap<>();
    
    private BukkitTask task;

    public ParticleOperator(Player host, ParticleCollectible particle) {
        super(host, CollectibleCategory.PARTICLE);

        handle(particle);
    }

    @Override
    public void handle(Collectible collectible) {
        ParticleCollectible particle = (ParticleCollectible) collectible;

        // Cancelar task anterior se existir ANTES de continuar
        if (task != null) {
            task.cancel();
            task = null;
        }

        // Obter o tipo selecionado, usando SIMPLES como padrão se não houver tipo salvo
        ParticleType type = getParticleType(getHost().getUniqueId(), particle.getIdentifier());
        if (type == null) {
            type = ParticleType.SIMPLES;
            setParticleType(getHost().getUniqueId(), particle.getIdentifier(), type);
        }

        final ParticleType finalType = type; // Variável final para usar no lambda

        // Criar nova task para exibir partículas continuamente
        task = Bukkit.getScheduler().runTaskTimer(Core.getJavaPlugin(), () -> {
            if (getHost() != null && getHost().isOnline()) {
                particle.display(getHost(), finalType);
                // Armazenar última posição após exibir (para próxima verificação)
                updateLastLocation(getHost());
            } else {
                cancel();
            }
        }, 0L, particle.getInterval());

        setCollectible(particle);
    }
    
    public static ParticleType getParticleType(UUID playerId, String collectibleId) {
        String key = playerId.toString() + ":" + collectibleId;
        return particleTypes.get(key); // Retorna null se não existir
    }
    
    public static void setParticleType(UUID playerId, String collectibleId, ParticleType type) {
        String key = playerId.toString() + ":" + collectibleId;
        particleTypes.put(key, type);
    }

    @Override
    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        // Remover o tipo do mapa quando o operador é cancelado
        if (getCollectible() != null) {
            particleTypes.remove(getHost().getUniqueId().toString() + ":" + getCollectible().getIdentifier());
        }
        // Remover última localização
        lastPlayerLocations.remove(getHost().getUniqueId());
    }
    
    private void updateLastLocation(Player player) {
        lastPlayerLocations.put(player.getUniqueId(), player.getLocation().clone());
    }
    
    public static boolean hasPlayerMoved(Player player) {
        Location lastLocation = lastPlayerLocations.get(player.getUniqueId());
        if (lastLocation == null) {
            // Primeira vez - inicializar a posição e considerar como parado
            lastPlayerLocations.put(player.getUniqueId(), player.getLocation().clone());
            return false;
        }
        
        Location currentLocation = player.getLocation();
        final double threshold = 0.1;
        
        boolean moved = !lastLocation.getWorld().equals(currentLocation.getWorld()) ||
                Math.abs(lastLocation.getX() - currentLocation.getX()) > threshold ||
                Math.abs(lastLocation.getY() - currentLocation.getY()) > threshold ||
                Math.abs(lastLocation.getZ() - currentLocation.getZ()) > threshold;
        
        return moved;
    }
}
