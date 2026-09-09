package com.minecraft.core.member.list.duels.metadata.objects.collectible.objects.type;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.DuelCollectible;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.objects.DuelCollectibleController;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

@Getter
@AllArgsConstructor
public enum DuelCollectibleType {

    EFFECT_KILL("Efeito de Abate", Item.of(Material.SKULL_ITEM, 3).skullByUrl("439c3df7a628af8d751ecca197642cdc1a07c30e3289b2d3261f7a65cf395b"),
            asList("§7Ao abater um jogador,", "§7surge um efeito especial."));

    private final String name;
    private final Item icon;

    private final List<String> lore;

    public static List<DuelCollectibleType> list() {
        return new ArrayList<>(Arrays.asList(values()));
    }

    public List<DuelCollectible> getItems() {
        return DuelCollectibleController.list(this);
    }

    public int getItemCount() {
        return (int) getItems().stream().filter(collectible -> collectible.getRarity() != null).count();
    }
}
