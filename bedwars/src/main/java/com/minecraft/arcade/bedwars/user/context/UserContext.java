package com.minecraft.arcade.bedwars.user.context;

import com.minecraft.core.member.list.bedwars.objects.ability.Ability;
import com.minecraft.core.member.list.bedwars.objects.ability.enums.AbilityType;
import com.minecraft.arcade.bedwars.user.context.objects.tracker.UserTracker;
import com.minecraft.core.api.reconnect.Reconnect;
import com.minecraft.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserContext {

    private UserTracker tracker = new UserTracker();

    private BedWarsItem pickaxeItem, axeItem, armorItem, swordItem;
    private Reconnect reconnect;

    private int xpGain, kills, deaths, finalKill, bedDestruction;
    
    // Sistema de moedas
    private int coins = 0;
    
    // Sistema de habilidades
    private Set<Ability> abilities = new HashSet<>();

    private boolean purchasedShears, purchasedPortableShop;

    private long milkTime = -1L, protectionTime = -1L;
    
    private ItemStack permanentSword;
    
    // Rastreamento de expiração de habilidades
    private boolean noFallWasActive = false; // Para rastrear se NO_FALL estava ativa no último check

    public boolean hasReconnect() {
        return reconnect != null;
    }

    public void downGradePickaxe(Player player) {
        if (pickaxeItem == null || pickaxeItem.equals(BedWarsItem.TOOLS_WOOD_PICKAXE)) return;

        BedWarsItem previousPick = BedWarsItem.of(item -> item.getStack().getType().name().endsWith("_PICKAXE")
                && item.ordinal() == (pickaxeItem.ordinal() - 1));

        BukkitUtil.replaceItemByType(player, pickaxeItem.getStack().getType(), previousPick.getStack());

        this.pickaxeItem = previousPick;
    }

    public void downGradeAxe(Player player) {
        if (axeItem == null || axeItem.equals(BedWarsItem.TOOLS_WOOD_AXE)) return;

        BedWarsItem previousAxe = BedWarsItem.of(item -> item.getStack().getType().name().endsWith("_AXE")
                && item.ordinal() == (axeItem.ordinal() - 1));

        BukkitUtil.replaceItemByType(player, axeItem.getStack().getType(), previousAxe.getStack());

        this.axeItem = previousAxe;
    }
    
    public boolean hasPermanentSword() {
        return permanentSword != null;
    }
    
    // Métodos de moedas
    public void addCoins(int amount) {
        this.coins += amount;
    }
    
    public void removeCoins(int amount) {
        this.coins -= amount;
    }
    
    public boolean hasCoins(int amount) {
        return coins >= amount;
    }
    
    // Métodos de habilidades
    public boolean hasAbility(AbilityType type) {
        return abilities.stream().anyMatch(ability -> ability.getType() == type);
    }
    
    public void addAbility(Ability ability) {
        abilities.add(ability);
    }
    
    public Ability getAbility(AbilityType type) {
        return abilities.stream()
                .filter(ability -> ability.getType() == type)
                .findFirst()
                .orElse(null);
    }
}
