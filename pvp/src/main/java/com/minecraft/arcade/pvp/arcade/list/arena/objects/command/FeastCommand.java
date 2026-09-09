package com.minecraft.arcade.pvp.arcade.list.arena.objects.command;

import com.minecraft.arcade.pvp.arcade.list.arena.Arena;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.feast.Feast;
import com.minecraft.arcade.pvp.user.User;
import com.minecraft.arcade.pvp.user.factory.list.ArenaUser;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeastCommand implements CommandInheritor {

    @Completer(name = "feast", rank = RankType.MODPLUS)
    public List<String> completer(BukkitCommandContext context) {
        List<String> list = new ArrayList<>();

        List<String> subCommands = Arrays.asList("tempo", "spawn", "destruct");

        String[] args = context.getArgs();

        if (args.length > 0 && !args[0].isEmpty()) {
            String filter = args[0].toLowerCase();

            for (String subCommand : subCommands) {
                if (subCommand.startsWith(filter))
                    list.add(subCommand);
            }
        } else {
            list.addAll(subCommands);
        }

        return list;
    }

    @Command(name = "feast", rank = RankType.MODPLUS)
    public void feast(BukkitCommandContext context) {
        Account account = context.getAccount();

        User user = (User) User.of(account.getId());

        if (!user.getArcade().isCategory(ArcadeCategory.PVP_ARENA) && !(user instanceof ArenaUser)) return;

        String[] args = context.getArgs();

        String label = "/" + context.getLabel();

        if (args.length == 0) {
            account.send("§cComo usar " + label + ":",
                    "§c* " + label + " tempo (valor) - §eAltere o tempo do feast.",
                    "§c* " + label + " spawn - §eGere o feast.",
                    "§c* " + label + " destruct - §eDestrua o feast.");
            return;
        }

        String cmdLabel = label + " " + args[0].toLowerCase();

        Arena arena = (Arena) user.getArcade();

        Feast feast = arena.getFeast(user.getArena());

        switch (args[0].toLowerCase()) {
            case "tempo": {
                if (args.length == 1) {
                    account.send("§cUso: " + cmdLabel + " [tempo].");
                    return;
                }

                if (!Util.isNumber(args[1])) {
                    account.send("§cSomente números são válidos.");
                    return;
                }

                int time = Integer.parseInt(args[1]);

                if (time < 0) {
                    account.send("§cO tempo não pode ser negativo.");
                    return;
                }

                feast.setTime(time);
                arena.updateFeast(user.getArena(), feast);

                account.send("§aO tempo do feast foi alterado para §f" + TimeUtil.time(time) + "§a.");
                break;
            }

            case "spawn": {

                if (feast.isSpawned()) {
                    account.send("§cO feast já foi spawnado.");
                    return;
                }

                feast.spawn();
                break;
            }

            case "destruct": {

                if (!feast.isSpawned()) {
                    account.send("§cO feast não foi spawnado.");
                    return;
                }

                feast.destruct();
                break;
            }

            default: {
                account.send("§cComo usar " + label + ":",
                        "§c* " + label + " tempo (valor) - §eAltere o tempo do feast.",
                        "§c* " + label + " spawn - §eGere o feast.",
                        "§c* " + label + " destruct - §eDestrua o feast.");
                break;
            }
        }
    }
}
