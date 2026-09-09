package com.minecraft.core.member.list.duels.metadata.objects.collectible.objects;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.DuelCollectible;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.objects.type.DuelCollectibleType;
import com.minecraft.core.util.list.loader.ClassLoader;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DuelCollectibleController {

    @Getter
    private static final Set<DuelCollectible> list = new HashSet<>();

    public static List<DuelCollectible> list(Predicate<DuelCollectible> filter) {
        return list.stream().filter(filter).collect(Collectors.toList());
    }

    public static boolean contains(DuelCollectible collectible) {
        return list.stream().anyMatch(search -> search.getType().equals(collectible.getType()) && search.getName().equalsIgnoreCase(collectible.getName()));
    }

    public static List<DuelCollectible> list(DuelCollectibleType type) {
        return list(collectible -> collectible.getType().equals(type));
    }

    public static DuelCollectible of(String name, DuelCollectibleType type) {
        return list(type).stream().filter(collectible -> collectible.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static void handle(JavaPlugin plugin) {
        for (Class<?> collectibleClass : ClassLoader.getClassesForPackage(plugin, Constant.SOURCE_DIR + ".core.member.list.duels.metadata.objects.collectible.objects.list")) {
            if (DuelCollectible.class.isAssignableFrom(collectibleClass)) {
                try {
                    DuelCollectible collectible = (DuelCollectible) collectibleClass.newInstance();

                    if (!contains(collectible))
                        list.add(collectible);

                } catch (Exception e) {
                    Core.getLogger().log(Level.WARNING, "[Duel-Collectible] Não foi possível carregar o colecionável " + collectibleClass.getSimpleName(), e);
                }
            }
        }
    }

    public static void disable() {
        list.clear();
    }
}
