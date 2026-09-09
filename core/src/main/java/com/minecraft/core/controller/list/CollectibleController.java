package com.minecraft.core.controller.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.nms.NMSContract;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.core.api.collectible.type.balloon.BalloonCollectible;
import com.minecraft.core.api.collectible.type.banner.BannerCollectible;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.type.CompanionType;
import com.minecraft.core.api.collectible.type.cape.CapeCollectible;
import com.minecraft.core.api.collectible.type.emotion.EmotionCollectible;
import com.minecraft.core.api.collectible.type.hat.HatCollectible;
import com.minecraft.core.api.collectible.type.particle.ParticleCollectible;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.collectible.type.trail.TrailCollectible;
import com.minecraft.core.controller.Controller;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.loader.ClassLoader;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class CollectibleController extends Controller<String, Collectible> {

    @Override
    public void save(Collectible collectible) {
        getCache().put(collectible.getIdentifier(), collectible);
    }

    public void handle(Object instance, String path) {
        Instant now = Instant.now();

        Core.getLogger().info("Registrando colecionáveis...");

        boolean nmsInitialized = NMSContract.handle();
        if (!nmsInitialized) {
        }

        List<Class<? extends Collectible>> prohibitedClasses = Arrays.asList(
                Collectible.class,
                EmotionCollectible.class,
                BalloonCollectible.class,
                CompanionCollectible.class,
                HatCollectible.class,
                ArtifactCollectible.class,
                BannerCollectible.class,
                ParticleCollectible.class,
                TitleCollectible.class,
                CapeCollectible.class,
                com.minecraft.core.api.collectible.type.joinmessage.JoinMessageCollectible.class,
                TrailCollectible.class
        );

        List<Class<?>> allClasses = ClassLoader.getClassesForPackage(instance, path);
        Core.getLogger().info("[CollectibleController] Encontradas " + allClasses.size() + " classes no pacote " + path);
        
        int loadedCount = 0;
        for (Class<?> cosmeticClass : allClasses) {
            if (prohibitedClasses.contains(cosmeticClass)) continue;

            if (Collectible.class.isAssignableFrom(cosmeticClass)) {
                try {
                    Collectible collectible = (Collectible) cosmeticClass.newInstance();
                    // Nota: algumas classes (EmotionCollectible, ArtifactCollectible, CompanionCollectible) 
                    // chamam save(this) no próprio construtor, então save() aqui pode ser redundante,
                    // mas não faz mal chamar novamente
                    save(collectible);
                    loadedCount++;
                } catch (Exception e) {
                    Core.getLogger().log(Level.SEVERE, "Não foi possível iniciar o cosmético " + cosmeticClass.getSimpleName(), e);
                }
            }
        }
        
        Core.getLogger().info("[CollectibleController] " + loadedCount + " cosméticos carregados via reflection. Cache: " + size() + " itens.");

        /* Caso a parte, cabeças, companheiros... */
        HatCollectible.handle();
        ArtifactCollectible.handle();
        CompanionType.handle();
        com.minecraft.core.api.collectible.type.joinmessage.JoinMessageCollectible.handle();

        if (!isEmpty())
            Core.getLogger().info(size() + " colecionáveis foram carregados. (Tempo médio: " + Util.formatInstant(now) + ")");
    }

    public Collectible of(CollectibleCategory category, String name) {
        return list(category).stream().filter(collectible -> collectible != null && collectible.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<Collectible> list(CollectibleCategory category) {
        return filter(collectible -> collectible.getCategory().equals(category));
    }

    public List<Collectible> list(CollectibleCategory category, CollectibleRarity rarity) {
        return filter(collectible -> collectible.getCategory().equals(category) && collectible.getRarity().equals(rarity));
    }
}
