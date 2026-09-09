package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.controller.KitController;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list.Caster;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list.Neo;
import com.minecraft.arcade.pvp.user.User;
import com.minecraft.arcade.pvp.user.factory.list.ArenaUser;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.assignment.Assignment;
import com.minecraft.core.account.context.objects.permission.Permission;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.core.bukkit.manager.list.CooldownManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class Kit {

    private final String name;
    private final ItemStack icon;

    private final KitStyle style;

    private final List<String> description;

    private List<RankType> ranks = new ArrayList<>();
    private List<ItemStack> specialItems = new ArrayList<>();

    private List<Class<? extends Kit>> restrictedKits = new ArrayList<>();

    private int price = 0;
    private long cooldown;

    @Override
    public boolean equals(Object kitObj) {
        if (kitObj == null) return false;
        if (kitObj.getClass() != getClass()) return false;

        Kit kit = (Kit) kitObj;

        return kit.getName().equalsIgnoreCase(name);
    }

    public boolean isFree() {
        return price <= 0 || ranks.isEmpty() || ranks.contains(RankType.MEMBER);
    }

    public boolean isEmpty() {
        return name.equalsIgnoreCase(KitController.empty().getName());
    }

    public boolean withSpecial(ItemStack object) {
        return specialItems.stream().anyMatch(special -> special.isSimilar(object));
    }

    public void setRanks(RankType... ranks) {
        this.ranks.addAll(Arrays.asList(ranks));
    }

    public void setSpecialItems(ItemStack... specialItems) {
        this.specialItems.addAll(Arrays.asList(specialItems));
    }

    @SafeVarargs
    public final void setRestrictedKits(Class<? extends Kit>... kits) {
        this.restrictedKits.addAll(Arrays.asList(kits));
    }

    public boolean isRestricted(Class<? extends Kit> kitClass) {
        return restrictedKits.contains(kitClass);
    }

    public boolean isAllow(Player player) {
        User user = (User) User.of(player.getUniqueId());

        return user != null && !user.isProtected() && user.getArcade().isCategory(ArcadeCategory.PVP_ARENA) && user instanceof ArenaUser;
    }

    public boolean isUsingKit(Player player) {
        if (!isAllow(player)) return false;

        ArenaUser user = (ArenaUser) ArenaUser.of(player.getUniqueId());

        return user.isUsingKit(this);
    }

    public boolean isNeo(Player player) {
        if (!isAllow(player)) return false;

        ArenaUser user = (ArenaUser) ArenaUser.of(player.getUniqueId());

        return user.isUsingKit(Neo.class);
    }

    public String getPermission() {
        return "arcade.pvp.kit." + name.toLowerCase();
    }

    public boolean hasKit(Player player) {
        if (isFree()) return true;

        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account == null) return false;

        if (ranks.stream().anyMatch(rank -> account.getRank().getType().ordinal() >= rank.ordinal())) return true;

        return account.hasPermission(getPermission());
    }

    public void setKit(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account != null)
            account.setPermission(new Permission(getPermission(), Assignment.AUTO, Constant.DEFAULT_ID, -1));
    }

    public boolean hasCooldown(Player player) {
        CooldownManager manager = BukkitCore.getManager().getCooldown();

        String name = getName().toLowerCase();

        if (manager.hasCooldown(player, "kit-" + name)) {
            Cooldown cooldown = manager.getCooldown(player, "kit-" + name);

            if (cooldown == null) return false;

            player.sendMessage("§cAguarde " + new DecimalFormat("#.#").format(cooldown.getRemaining()) + "s para usar o kit " + getName() + " novamente.");
            return true;
        }

        return false;
    }

    public void applyCooldown(Player player) {
        ArenaUser user = (ArenaUser) ArenaUser.of(player.getUniqueId());

        if (user == null) return;

        long cooldown = getCooldown();

        if (user.isUsingKit(Caster.class))
            cooldown = cooldown / 2;

        BukkitCore.getManager().getCooldown().addCooldown(player.getUniqueId(), "kit-" + name, cooldown);
    }
}
