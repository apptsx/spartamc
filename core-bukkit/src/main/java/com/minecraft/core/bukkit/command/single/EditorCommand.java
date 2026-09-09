package com.minecraft.core.bukkit.command.single;

import com.minecraft.core.Constant;
import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.feature.ArcadeFeature;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.bukkit.menu.server.arcade.editor.ArcadeEditingMenu;
import com.minecraft.core.bukkit.menu.server.arcade.editor.ArcadeEditorMenu;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EditorCommand implements CommandInheritor {

    @Completer(name = "editor")
    public List<String> editorCompleter(BukkitCommandContext context) {
        List<String> list = new ArrayList<>(),
                serverList = ServerType.list(ServerType::isArcade).stream().map(type -> type.name().toLowerCase()).collect(Collectors.toList());

        String[] args = context.getArgs();

        if (args.length == 1) {
            String serverName = args[0].toLowerCase();

            if (serverName.isEmpty())
                list.addAll(serverList);
            else {
                for (String server : serverList) {
                    if (server.toLowerCase().startsWith(serverName))
                        list.add(server);
                }
            }

        } else if (args.length == 2) {
            ServerType serverType = ServerType.of(args[0]);

            List<String> modeList = serverType != null
                    ? ArcadeCategory.of(serverType).stream().filter(arcade -> arcade.hasFeature(ArcadeFeature.EDITABLE_MENU)).map(arcade -> arcade.name().toLowerCase()).collect(Collectors.toList())
                    : ArcadeCategory.list().stream().filter(arcade -> arcade.hasFeature(ArcadeFeature.EDITABLE_MENU)).map(arcade -> arcade.name().toLowerCase()).collect(Collectors.toList());

            String modeName = args[1].toLowerCase();

            if (modeName.isEmpty())
                list.addAll(modeList);
            else {
                for (String mode : modeList) {
                    if (mode.toLowerCase().startsWith(modeName))
                        list.add(mode);
                }
            }
        }

        return list;
    }

    @Command(name = "editor")
    public void editorMenu(BukkitCommandContext context) {
        Account account = context.getAccount();

        Player player = context.getPlayer();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getLabel() + " (servidor) (modo).");
            return;
        }

        ServerType server = ServerType.of(args[0]);

        if (server == null) {
            account.send(String.format(Constant.SERVER_NOT_FOUND_MESSAGE, args[0]));
            return;
        }

        if (!server.isArcade()) {
            account.send("§cO servidor " + server.getName() + " não possui suporte para jogos.");
            return;
        }

        if (args.length == 1) {
            new ArcadeEditorMenu(player, server, null).handle();
            return;
        }

        ArcadeCategory arcade = ArcadeCategory.of(context.getMessage(1, args));

        if (arcade == null) {
            account.send("§cO modo de jogo solicitado não foi encontrado.");
            return;
        }

        if (!arcade.hasFeature(ArcadeFeature.EDITABLE_MENU)) {
            account.send("§cO modo de jogo " + arcade.getName() + " não possui inventários customizados.");
            return;
        }

        if (!arcade.getServer().equals(server)) {
            account.send("§cO servidor " + server.getName() + " não suporta o modo de jogo " + arcade.getName() + ".");
            return;
        }

        new ArcadeEditingMenu(context.getPlayer(), arcade, null).handle();
    }
}
