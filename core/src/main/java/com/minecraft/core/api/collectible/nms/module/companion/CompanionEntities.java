package com.minecraft.core.api.collectible.nms.module.companion;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.nms.v1_8_R3.module.companion.CompanionSlime;
import com.minecraft.core.api.collectible.operator.list.CompanionOperator;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class CompanionEntities {

    private static final Map<Class<? extends CompanionCollectible>, Class<? extends CompanionEntity>> TYPES = new HashMap<>();

    private static final Map<UUID, CompanionSlime> SLIMES = new HashMap<>();

    public static void setCompanionType(Class<? extends CompanionCollectible> type, Class<? extends CompanionEntity> entity) {
        TYPES.put(type, entity);
    }

    public static void setTypes(CompanionEntityModel... models) {
        for (CompanionEntityModel model : models) {
            TYPES.put(model.getCollectible(), model.getEntity());
        }
    }

    public static void saveOwner(Player player, CompanionSlime slime) {
        SLIMES.put(player.getUniqueId(), slime);
    }

    public static CompanionSlime getByOwner(Player player) {
        return SLIMES.get(player.getUniqueId());
    }

    public static void removeByOwner(Player player) {
        SLIMES.remove(player.getUniqueId());
    }

    public static CompanionEntity createForType(Class<? extends CompanionCollectible> clazz, CompanionOperator companion) {
        Class<? extends CompanionEntity> entityClass = TYPES.get(clazz);
        if (entityClass == null) {
            throw new IllegalArgumentException("Tipo de companion desconhecido: " + clazz.getSimpleName());
        }

        try {
            return (CompanionEntity) entityClass.getConstructors()[0].newInstance(companion);
        } catch (Exception ex) {
            Core.getLogger().log(Level.WARNING, "createForType(" + clazz.getSimpleName() + "): ", ex);
        }

        return null;
    }

    public static boolean existsForType(Class<? extends CompanionEntity> type) {
        return TYPES.containsKey(type);
    }

    @Getter
    @AllArgsConstructor
    public static class CompanionEntityModel {
        private final Class<? extends CompanionCollectible> collectible;
        private final Class<? extends CompanionEntity> entity;
    }
}