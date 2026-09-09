package com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.armor;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
public enum ArmorProtection {

    NONE("", 0),

    ONE("I", 2),
    TWO("II", 4),
    THREE("III", 8),
    FOUR("IV", 16);

    private final String tag;
    private final int price;

    public String getName() {
        return "Proteção " + tag;
    }

    public boolean isHighest() {
        return this == FOUR;
    }

    public int getAdjustedPrice(Arena arena) {
        if (arena.getSlot().ordinal() < Slot.TRIO.ordinal()) return price;

        switch (this) {
            case ONE:
                return 5;
            case TWO:
                return 10;
            case THREE:
                return 20;
            case FOUR:
                return 30;
            default:
                return price;
        }
    }

    public void applyEnchantment(Player player) {
        BukkitUtil.enchantItemByType(player, BukkitUtil.EquipmentType.ARMOR, Enchantment.PROTECTION_ENVIRONMENTAL, ordinal());
    }
}