package com.minecraft.core.member.list.eggwars.objects.enums;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.eggwars.EggMember;
import com.minecraft.core.member.list.eggwars.objects.enums.item.EggWarsItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum EggShop {

    FAVORITE("Favoritos", Item.of(Material.NETHER_STAR)),
    BLOCKS("Blocos", Item.of(Material.WOOL)),
    COMBAT("Combate", Item.of(Material.STONE_SWORD)),
    ARMOR("Armadura", Item.of(Material.CHAINMAIL_BOOTS)),
    TOOLS("Ferramentas", Item.of(Material.STONE_PICKAXE)),
    ARTILLERY("Artilharia", Item.of(Material.BOW)),
    POTION("Poções", Item.of(Material.BREWING_STAND_ITEM)),
    UTIL("Utilidades", Item.of(Material.TNT));

    private final String name;
    private final ItemStack icon;

    public List<EggWarsItem> items() {
        return Arrays.stream(EggWarsItem.values())
                .filter(item -> item.getShop().equals(this))
                .collect(Collectors.toList());
    }

    public List<EggWarsItem> getItemsList(EggMember member) {
        Predicate<EggWarsItem> predicate = this == FAVORITE
                ? member::isFavoriteItem
                : item -> item.getShop().equals(this);

        return Stream.of(EggWarsItem.values())
                .filter(predicate)
                .collect(Collectors.toList());
    }
}

