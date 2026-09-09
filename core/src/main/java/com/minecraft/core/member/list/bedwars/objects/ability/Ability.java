package com.minecraft.core.member.list.bedwars.objects.ability;

import com.minecraft.core.member.list.bedwars.objects.ability.enums.AbilityType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ability {

    private final AbilityType type;
    private long matchStartTime; // Tempo de início da partida quando a habilidade foi ativada

    public Ability(AbilityType type) {
        this.type = type;
        this.matchStartTime = -1L; // -1 indica que ainda não foi ativada na partida
    }

    public Ability(AbilityType type, long matchStartTime) {
        this.type = type;
        this.matchStartTime = matchStartTime;
    }

    /**
     * Ativa a habilidade com o tempo de início da partida
     */
    public void activate(long matchStartTime) {
        this.matchStartTime = matchStartTime;
    }

    /**
     * Verifica se a habilidade está ativa baseado no tempo de início da partida
     */
    public boolean isActive() {
        // Se não foi ativada ainda, não está ativa
        if (matchStartTime < 0) {
            return false;
        }

        // Para NO_FALL, verificar se ainda está dentro de 2 minutos (120000ms) desde o início da partida
        if (type == AbilityType.NO_FALL) {
            long elapsed = System.currentTimeMillis() - matchStartTime;
            return elapsed < 120000; // 2 minutos
        }
        
        // Outras habilidades são ativas durante toda a partida (se foram ativadas)
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Ability ability = (Ability) obj;
        return type == ability.type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}

