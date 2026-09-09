package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.BedCollectible;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.BedCollectibleController;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

@Getter
@AllArgsConstructor
public enum BedCollectibleType {

    BED_DESTRUCTION("Destruição de Cama", Item.of(Material.BED),
            asList("§7Altere o efeito especial", "§7ao destruir uma cama.")),
    FINAL_KILL("Abate Final", Item.of(Material.DIAMOND_SWORD),
            asList("§7Altere o efeito especial", "§7ao finalizar um jogador.")),
    SELLER_SKIN("Skin dos Vendedores", Item.of(Material.SKULL_ITEM, 3)
            .skullByBase64("ewogICJ0aW1lc3RhbXAiIDogMTcxOTQzODg3Mzk2MywKICAicHJvZmlsZUlkIiA6ICI5ZTA5YzM4ZGUzZTY0MDA2OTAwYzAwZTJiOTQ3ZTQwMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ4RFRPTUFTX1lUIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RiY2EzNzAzMTliYThkZjY4M2YzOGUwOTQxNDg3ZTJhNzBjNGE2YzE0ZGFiZWYwYWEzNGUzZDBiNmYwNTk5MjIiCiAgICB9CiAgfQp9"),
            asList("§7Altere a skin dos", "§7vendedores da ilha."));

    private final String name;
    private final Item icon;

    private final List<String> lore;

    public static List<BedCollectibleType> list() {
        return new ArrayList<>(Arrays.asList(values()));
    }

    public List<BedCollectible> getItems() {
        return BedCollectibleController.list(this);
    }

    public int getItemCount() {
        return (int) getItems().stream().filter(collectible -> collectible.getRarity() != null).count();
    }
}
