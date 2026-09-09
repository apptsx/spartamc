package com.minecraft.core.api.collectible.type.particle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParticleType {
    
    SIMPLES("Simples", "§7Partículas simples ao redor", org.bukkit.Material.SNOW_BALL),
    ESPIRAL("Espiral", "§7Partículas em formato espiral", org.bukkit.Material.EMERALD),
    ESPIRAL_DUPLO("Espiral Duplo", "§7Dois espirais girando", org.bukkit.Material.NETHER_STAR),
    CIRCULAR("Circular", "§7Partículas em círculo ao redor", org.bukkit.Material.EYE_OF_ENDER);
    
    private final String name;
    private final String description;
    private final org.bukkit.Material icon;
    
}

