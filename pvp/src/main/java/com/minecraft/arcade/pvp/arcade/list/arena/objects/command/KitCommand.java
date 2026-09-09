package com.minecraft.arcade.pvp.arcade.list.arena.objects.command;

import com.minecraft.arcade.pvp.arcade.list.arena.objects.controller.KitController;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.enums.KitCategory;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.menu.KitMenu;
import com.minecraft.arcade.pvp.user.User;
import com.minecraft.arcade.pvp.user.factory.list.ArenaUser;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KitCommand implements CommandInheritor {

    @Completer(name = "kit")
    public List<String> kitCompleter(BukkitCommandContext context) {
        return handleKitCompleter(context, KitCategory.PRIMARY);
    }

    @Completer(name = "kit2")
    public List<String> kit2Completer(BukkitCommandContext context) {
        return handleKitCompleter(context, KitCategory.SECONDARY);
    }

    @Command(name = "kit")
    public void kit(BukkitCommandContext context) {
        handleKitCommand(context, KitCategory.PRIMARY);
    }

    @Command(name = "kit2")
    public void kit2(BukkitCommandContext context) {
        handleKitCommand(context, KitCategory.SECONDARY);
    }

    protected void handleKitCommand(BukkitCommandContext context, KitCategory category) {
        Player player = context.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (!(user.getArcade().isCategory(ArcadeCategory.PVP_ARENA) && user instanceof ArenaUser)) return;

        if (!user.isProtected()) {
            player.sendMessage("§cVocê não selecionar kits fora do spawn!");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            new KitMenu(player, category).handle();
            return;
        }

        Kit kit = KitController.of(args[0]);

        if (kit == null) {
            player.sendMessage("§cO kit solicitado não foi encontrado.");
            return;
        }

        KitController.exchangeKit((ArenaUser) user, kit, category);
    }

    protected List<String> handleKitCompleter(BukkitCommandContext context, KitCategory category) {
        Player player = context.getPlayer();

        List<String> list = new ArrayList<>();

        User user = (User) User.of(player.getUniqueId());

        if (!(user.getArcade().isCategory(ArcadeCategory.PVP_ARENA) && user instanceof ArenaUser)) return list;

        List<String> names = KitController.getKitsUserHave(player, category).stream().map(Kit::getName).collect(Collectors.toList());

        String[] args = context.getArgs();

        if (args.length > 0 && !args[0].isEmpty()) {
            String filter = args[0].toLowerCase();

            for (String name : names) {
                if (name.toLowerCase().startsWith(filter))
                    list.add(name);
            }
        } else
            list.addAll(names);

        return list;
    }
}
