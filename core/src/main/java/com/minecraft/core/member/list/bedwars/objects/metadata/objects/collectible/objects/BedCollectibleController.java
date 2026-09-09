package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.BedCollectible;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.BedDestroyCollectible;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.SellerCollectible;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import com.minecraft.core.util.list.loader.ClassLoader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BedCollectibleController {

    @Getter
    private static final Set<BedCollectible> list = new HashSet<>();

    public static List<BedCollectible> list(Predicate<BedCollectible> filter) {
        return list.stream().filter(filter).collect(Collectors.toList());
    }

    public static boolean contains(BedCollectible collectible) {
        return list.stream().anyMatch(search -> search.getType().equals(collectible.getType()) && search.getName().equalsIgnoreCase(collectible.getName()));
    }

    public static List<BedCollectible> list(BedCollectibleType type) {
        return list(collectible -> collectible.getType().equals(type));
    }

    public static BedCollectible of(String name, BedCollectibleType type) {
        return list(type).stream().filter(collectible -> collectible.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static void handle(JavaPlugin plugin) {
        List<Class<? extends BedCollectible>> prohibitedClasses = Arrays.asList(SellerCollectible.class, BedDestroyCollectible.class);

        for (Class<?> collectibleClass : ClassLoader.getClassesForPackage(plugin,
                Constant.SOURCE_DIR + ".core.member.list.bedwars.objects.metadata.objects.collectible.objects.list")) {
            if (prohibitedClasses.contains(collectibleClass)) continue;

            if (BedCollectible.class.isAssignableFrom(collectibleClass)) {
                try {
                    BedCollectible collectible = (BedCollectible) collectibleClass.newInstance();

                    if (!contains(collectible))
                        list.add(collectible);

                } catch (Exception e) {
                    Core.getLogger().log(Level.WARNING, "[BW-Collectible] Não foi possível carregar o colecionável " + collectibleClass.getSimpleName(), e);
                }
            }
        }
    }

    public static void disable() {
        list.clear();
    }
}
