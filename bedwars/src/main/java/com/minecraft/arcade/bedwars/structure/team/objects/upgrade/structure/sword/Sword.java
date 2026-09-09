package com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.sword;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
public enum Sword {

    ONE("I", 4),
    TWO("II", 16),
    THREE("III", 32);

    private final String tag;
    private final int price;

    public String getName() {
        return "Afiação " + tag;
    }

    public boolean isHighest() {
        return this == THREE;
    }

    public int getAdjustedPrice(Arena arena) {
        if (arena.getSlot().ordinal() < Slot.TRIO.ordinal()) return price;

        switch (this) {
            case ONE:
                return 8;
            case TWO:
                return 30;
            default:
                return price;
        }
    }

    public void applyEnchantment(Player player) {
        BukkitUtil.enchantItemByType(player, BukkitUtil.EquipmentType.SWORD, Enchantment.DAMAGE_ALL, ordinal() + 1);
    }
}
