package com.minecraft.core.bukkit.command.single;

import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.backend.database.redis.message.types.route.arcade.ArenaSearchMessage;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.server.type.ServerType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayCommand implements CommandInheritor {

    private final List<ArcadeCategory> arcadeList = ArcadeCategory.list(arcade -> !arcade.getServer().equals(ServerType.HUB));

    private final List<ServerType> serverList = ServerType.list(server -> !server.isArcade() && server.isShown());

    @Completer(name = "play", subCommands = {"jogar"})
    public List<String> playCompleter(BukkitCommandContext context) {
        String[] args = context.getArgs();

        List<String> arcadeNames = arcadeList.stream().map(arcade -> arcade.name().toLowerCase()).collect(Collectors.toList()),
                names = new ArrayList<>(serverList.stream().map(server -> server.name().toLowerCase()).collect(Collectors.toList()));

        names.addAll(arcadeNames);

        List<String> list = new ArrayList<>();

        if (args.length > 0) {
            String name = args[0].toLowerCase();

            for (String search : names) {
                if (search.toLowerCase().startsWith(name))
                    list.add(search);
            }
        } else
            list.addAll(names);

        return list;
    }

    @Command(name = "play", aliases = {"jogar"})
    public void play(BukkitCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getLabel() + " (modo).");
            return;
        }

        ServerType server = ServerType.of(args[0]);

        if (server != null && !server.isArcade())
            account.redirect(server);
        else {
            ArcadeCategory arcade = ArcadeCategory.of(args[0]);

            if (arcade == null) {
                account.send("§cO modo de jogo solicitado não foi encontrado.");
                return;
            }

            ArcadeRouteContext route = ArcadeRouteContext.builder()
                    .arcade(arcade)
                    .slot(arcade.getSlots().get(0))
                    .build();

            new ArenaSearchMessage(account.getId(), route).send();
        }
    }
}
