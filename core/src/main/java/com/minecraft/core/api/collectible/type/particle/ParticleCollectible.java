package com.minecraft.core.api.collectible.type.particle;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public abstract class ParticleCollectible extends Collectible {
    
    public ParticleCollectible(String name, CollectibleRarity rarity, List<RankType> ranks, long releasedAt) {
        super(name, CollectibleCategory.PARTICLE, rarity, ranks, releasedAt);
    }

    public abstract void display(Player player, ParticleType type);
    
    // Método auxiliar para renderizar partícula simples no pé do jogador
    protected void renderSimple(Player player, Location baseLoc, Effect effect, int amount) {
        // Renderizar no pé do jogador (y = 0 em relação ao chão)
        Location footLoc = player.getLocation();
        footLoc.setY(player.getLocation().getY());
        
        for (int i = 0; i < amount; i++) {
            double offsetX = (Math.random() - 0.5) * 0.3;
            double offsetZ = (Math.random() - 0.5) * 0.3;
            Location particleLoc = footLoc.clone().add(offsetX, 0.1, offsetZ);
            player.getWorld().playEffect(particleLoc, effect, 0);
        }
    }
    
    // Método auxiliar para verificar se o jogador está parado
    protected boolean isPlayerStationary(Player player) {
        // Usar o método do ParticleOperator para verificar movimento baseado em posição
        return !com.minecraft.core.api.collectible.operator.list.ParticleOperator.hasPlayerMoved(player);
    }
    
    // Método auxiliar para renderizar partícula em espiral envolta do jogador
    protected void renderSpiral(Player player, Location baseLoc, Effect effect, double radius, int points) {
        // Só renderizar se o jogador estiver parado
        if (!isPlayerStationary(player)) {
            return;
        }
        
        long time = System.currentTimeMillis();
        int totalSpiralPoints = points * 6; // Total de pontos na espiral (mais pontos = espiral mais suave)
        
        // Calcular quantas partículas devem ser visíveis baseado no tempo
        // Cada partícula aparece uma a uma, criando o efeito de espiral sendo formada
        double t = (time / 60.0) % 1.0; // 0.0 a 1.0, ciclo completo a cada 6 segundos (60 * 100ms)
        int visiblePoints = (int) (t * totalSpiralPoints);
        
        // Renderizar todas as partículas desde o início até a posição atual
        // Isso cria o efeito de espiral sendo formada progressivamente
        for (int i = 0; i < visiblePoints; i++) {
            double spiralProgress = i / (double) totalSpiralPoints; // 0.0 a 1.0
            
            // Calcular ângulo: 2 voltas completas da espiral
            double angle = spiralProgress * Math.PI * 2 * 2;
            
            // Calcular altura: de 0 a 2 (do pé até acima da cabeça)
            double height = spiralProgress * 2.0;
            
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location particleLoc = baseLoc.clone().add(x, height - 1.0, z); // Ajustar para centro do jogador
            player.getWorld().playEffect(particleLoc, effect, 0);
        }
    }
    
    // Método auxiliar para renderizar espiral duplo
    protected void renderDoubleSpiral(Player player, Location baseLoc, Effect effect, double radius, int points) {
        // Só renderizar se o jogador estiver parado
        if (!isPlayerStationary(player)) {
            return;
        }
        
        long time = System.currentTimeMillis();
        double rotationSpeed = 50.0;
        double heightSpeed = 100.0;
        double baseAngle1 = (time / rotationSpeed) % (Math.PI * 2);
        double baseAngle2 = baseAngle1 + Math.PI; // Espiral oposta
        double baseHeight = (time / heightSpeed) % 2.0;
        
        int spiralPoints = points * 2;
        
        // Primeira espiral
        for (int i = 0; i < spiralPoints; i++) {
            double t = i / (double) spiralPoints;
            double angle = baseAngle1 + (t * Math.PI * 2 * 2);
            double height = baseHeight + (t * 2.0);
            if (height > 2.0) height -= 2.0;
            
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location particleLoc = baseLoc.clone().add(x, height - 1.0, z);
            player.getWorld().playEffect(particleLoc, effect, 0);
        }
        
        // Segunda espiral (oposta)
        for (int i = 0; i < spiralPoints; i++) {
            double t = i / (double) spiralPoints;
            double angle = baseAngle2 + (t * Math.PI * 2 * 2);
            double height = baseHeight + (t * 2.0);
            if (height > 2.0) height -= 2.0;
            
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location particleLoc = baseLoc.clone().add(x, height - 1.0, z);
            player.getWorld().playEffect(particleLoc, effect, 0);
        }
    }
    
    // Método auxiliar para renderizar círculo envolta do jogador
    protected void renderCircular(Player player, Location baseLoc, Effect effect, double radius, int points) {
        // Só renderizar se o jogador estiver parado
        if (!isPlayerStationary(player)) {
            return;
        }
        
        long time = System.currentTimeMillis();
        double rotationSpeed = 100.0;
        double baseAngle = (time / rotationSpeed) % (Math.PI * 2);
        
        // Usar mais pontos para círculo mais suave (mínimo 16)
        int circlePoints = Math.max(points, 16);
        
        // Altura do círculo (no meio do jogador)
        double height = 1.0;
        
        for (int i = 0; i < circlePoints; i++) {
            double angle = baseAngle + (i * Math.PI * 2 / circlePoints);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location particleLoc = baseLoc.clone().add(x, height, z);
            player.getWorld().playEffect(particleLoc, effect, 0);
        }
    }

    public long getInterval() {
        return 5L;
    }
}
