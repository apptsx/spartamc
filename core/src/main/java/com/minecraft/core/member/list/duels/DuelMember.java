package com.minecraft.core.member.list.duels;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.member.Member;
import com.minecraft.core.member.context.menu.MenuMetadata;
import com.minecraft.core.member.context.stats.ArcadeStats;
import com.minecraft.core.member.list.duels.metadata.DuelMetadata;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.DuelCollectible;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.objects.DuelCollectibleController;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.objects.type.DuelCollectibleType;
import com.minecraft.core.server.type.ServerType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class DuelMember extends Member {

    private DuelMetadata metadata;

    public DuelMember(UUID id, String name) {
        super(id, name);


        // Adicionando inventários customizados
        this.addMenu(
                new MenuMetadata(ArcadeCategory.DUELS_SIMULATOR, Constant.DUELS_SIMULATOR_BASE64),
                new MenuMetadata(ArcadeCategory.DUELS_UHC, Constant.DUELS_UHC_BASE64),
                new MenuMetadata(ArcadeCategory.DUELS_FIREBALL, Constant.DUELS_FIREBALL_BASE64),
                new MenuMetadata(ArcadeCategory.DUELS_THE_BRIDGE, Constant.DUELS_THE_BRIDGE_BASE64),
                new MenuMetadata(ArcadeCategory.DUELS_GLADIATOR, Constant.DUELS_GLADIATOR_BASE64),
                new MenuMetadata(ArcadeCategory.DUELS_NO_DEBUFF, Constant.DUELS_NO_DEBUFF_BASE64)
        );

        ArcadeCategory.of(ServerType.DUELS).forEach(arcade -> this.addMultipleStats(new ArcadeStats(arcade)));

        DuelMetadata metadata = new DuelMetadata();

        DuelCollectibleType.list().forEach(type -> {
            DuelCollectible none = DuelCollectibleController.of("Nenhuma", type);

            if (none != null)
                metadata.getCollectibles().put(type, none.getName().toLowerCase());
        });

        this.metadata = metadata;
    }

    @Override
    public void save(String... fields) {
        for (String field : fields)
            Core.getDuelsData().update(this, field);
    }

    /* Metadata */
    public void saveMetadata(DuelMetadata metadata) {
        this.metadata = metadata;
        save("metadata");
    }

    /* Collectibles */
    public List<DuelCollectible> getCollectibles() {
        return DuelCollectibleController.list(collectible -> collectible.hasPermission(getAccount()));
    }

    public List<DuelCollectible> getCollectibles(DuelCollectibleType type) {
        return getCollectibles().stream().filter(collectible -> collectible.getRarity() == null || collectible.getType().equals(type)).collect(Collectors.toList());
    }

    public int getCollectibleCount(DuelCollectibleType type) {
        return (int) getCollectibles(type).stream().filter(collectible -> collectible.getRarity() != null).count();
    }

    public List<DuelCollectible> getActiveCollectibles() {
        List<DuelCollectible> list = new ArrayList<>();

        metadata.getCollectibles().forEach((type, name) -> {
            DuelCollectible collectible = DuelCollectibleController.of(name, type);

            if (collectible != null)
                list.add(collectible);
        });

        return list;
    }

    public DuelCollectible getActiveCollectible(DuelCollectibleType type) {
        return getActiveCollectibles().stream().filter(collectible -> collectible.getType().equals(type)).findFirst().orElse(null);
    }

    public boolean isActivateCollectible(DuelCollectible collectible) {
        return getActiveCollectibles().stream().anyMatch(search -> search.getType().equals(collectible.getType()) && search.getName().equalsIgnoreCase(collectible.getName()));
    }

    public void setCollectible(DuelCollectible collectible) {
        getActiveCollectibles().removeIf(search -> search.getType().equals(collectible.getType()));

        metadata.getCollectibles().put(collectible.getType(), collectible.getName().toLowerCase());
        saveMetadata(metadata);
    }
}
