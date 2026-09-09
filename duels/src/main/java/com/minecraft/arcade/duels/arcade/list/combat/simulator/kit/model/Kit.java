package com.minecraft.arcade.duels.arcade.list.combat.simulator.kit.model;

import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.core.bukkit.manager.list.CooldownManager;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.arcade.duels.arcade.list.combat.simulator.kit.model.list.Neo;
import com.minecraft.arcade.duels.user.User;
import com.minecraft.arcade.duels.user.factory.list.SimulatorUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.List;

@Getter
@RequiredArgsConstructor
@IgnoreEvent
public abstract class Kit implements Listener {

    private final String name;
    private final Style style;

    private final ItemStack icon;

    private final long cooldown;

    private final List<String> lore;
    private final List<ItemStack> items;

    @Getter
    @AllArgsConstructor
    public enum Style {

        STRATEGY("Estratégia"),
        COMBAT("Combate"),
        MOVEMENT("Movimentação");

        private final String name;
    }

    public boolean isUsing(Player player) {
        User userObj = (User) User.of(player.getUniqueId());

        if (userObj == null || !userObj.getArcade().isCategory(ArcadeCategory.DUELS_SIMULATOR)) return false;

        SimulatorUser user = (SimulatorUser) userObj;

        return user.isUsingKit(this);
    }

    public boolean isInvincible(Player player) {
        User userObj = (User) User.of(player.getUniqueId());

        if (userObj == null || !userObj.getArcade().isCategory(ArcadeCategory.DUELS_SIMULATOR)) return false;

        SimulatorUser user = (SimulatorUser) userObj;

        return user.getKit().getName().equalsIgnoreCase(Neo.class.getSimpleName());
    }

    public boolean isKitItem(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) return false;

        return !items.isEmpty() && items.stream().anyMatch(item -> item.getType() == stack.getType());
    }

    public void applyCooldown(Player player) {
        BukkitCore.getManager().getCooldown().addCooldown(player.getUniqueId(), "kit-" + name, cooldown);
    }

    public boolean hasCooldown(Player player) {
        CooldownManager manager = BukkitCore.getManager().getCooldown();

        String name = getName();

        if (manager.hasCooldown(player, "kit-" + name)) {
            Cooldown cooldown = manager.getCooldown(player, "kit-" + name);
            if (cooldown == null) return false;

            player.sendMessage("§cAguarde " + new DecimalFormat("#.#").format(cooldown.getRemaining())
                    + "s para usar o kit " + name + " novamente.");
            return true;
        }

        return false;
    }
}
