package com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.haste;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.core.arcade.room.slot.Slot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Getter
@AllArgsConstructor
public enum Haste {

    ONE("I", 2),
    TWO("II", 4);

    private final String tag;
    private final int price;

    public String getName() {
        return "Pressa " + tag;
    }

    public boolean isHighest() {
        return this == TWO;
    }

    public int getAdjustedPrice(Arena arena) {
        if (arena.getSlot().ordinal() < Slot.TRIO.ordinal()) return price;

        switch (this) {
            case ONE:
                return 4;
            case TWO:
                return 6;
            default:
                return price;
        }
    }

    public void applyEnchantment(Player player) {

        if (player.hasPotionEffect(PotionEffectType.FAST_DIGGING))
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);

        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, ordinal()));
    }
}
