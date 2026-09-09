package com.minecraft.arcade.pvp.arcade.list.arena.objects.controller;

import com.minecraft.arcade.pvp.PvP;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitCategory;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list.None;
import com.minecraft.arcade.pvp.user.factory.list.ArenaUser;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.util.list.loader.ClassLoader;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class KitController {

    @Getter
    private final static Set<Kit> list = new HashSet<>();

    static {
        for (Class<?> kitClass : ClassLoader.getClassesForPackage(PvP.getInstance(), Constant.SOURCE_DIR + ".arcade.pvp.arcade.list.arena.objects.kit.list")) {
            if (Kit.class.isAssignableFrom(kitClass)) {
                try {
                    Kit kit = (Kit) kitClass.newInstance();

                    list.add(kit);
                } catch (Exception e) {
                    Core.getLogger().log(Level.WARNING, "Não foi possível instanciar o kit " + kitClass.getName() + "!", e);
                }
            }
        }
    }

    public static List<Kit> getKitsUserHave(Player player, KitCategory category) {
        return list.stream().filter(kit -> isReleased(player, kit, category)).collect(Collectors.toList());
    }

    public static List<Kit> getKitsUserDontHave(Player player) {
        return list.stream().filter(kit -> !isReleased(player, kit, KitCategory.SECONDARY)).collect(Collectors.toList());
    }

    public static boolean isReleased(Player player, Kit kit, KitCategory category) {
        if (category.equals(KitCategory.PRIMARY)) return true;

        return kit.hasKit(player);
    }

    public static void exchangeKit(ArenaUser user, Kit kit, KitCategory category) {
        Account account = user.getAccount();

        if (user.isUsingKit(kit, category)) {
            account.send("§cO kit " + kit.getName() + " já foi selecionado.");
            return;
        }

        if (!isReleased(account.player(), kit, category)) {
            account.send("§cVocê não pode usar o kit " + kit.getName() + "!");
            return;
        }

        Kit current = category.equals(KitCategory.PRIMARY) ? user.getSecondary() : user.getPrimary();

        if (current != null && (kit.isRestricted(current.getClass()) || current.isRestricted(kit.getClass()))) {
            account.send("§cO kit " + kit.getName() + " é incompatível com o kit " + current.getName() + ".");
            return;
        }

        if (category.equals(KitCategory.PRIMARY))
            user.setPrimary(kit);
        else
            user.setSecondary(kit);

        user.getArcade().handleSidebar(user);

        account.title("§6" + kit.getName(), "§eSelecionado!");

        account.send("§eO kit §6" + kit.getName() + "§e foi selecionado.");
        account.sound(Sound.LEVEL_UP);
    }

    public static Kit empty() {
        return of(None.class);
    }

    public static Kit of(String name) {
        return list.stream().filter(kit -> kit.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static Kit of(Class<? extends Kit> kitClass) {
        return list.stream().filter(kit -> kitClass.isAssignableFrom(kit.getClass())).findFirst().orElse(null);
    }

    public static Kit of(Predicate<Kit> filter) {
        return list.stream().filter(filter).findFirst().orElse(null);
    }
}
