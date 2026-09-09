package com.minecraft.core.bukkit.command.single;

import com.minecraft.core.account.Account;
import com.minecraft.core.api.viewer.MenuViewer;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.bukkit.menu.server.arcade.viewer.ViewerMenu;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;

public class MenuViewerCommand implements CommandInheritor {

    @Command(name = "mvw")
    public void menuViewer(BukkitCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getLabel() + " (id).");
            return;
        }

//        if (args[0].equalsIgnoreCase("test")) {
//            MenuViewer viewer = new MenuViewer(account.getNickname(),
//                    Constant.DUELS_SIMULATOR_BASE64,
//                    "Simulator 1v1",
//                    18.5);
//
//            TextComponent text = new TextComponent("§aInventário gerado! ID: §f" + viewer.getId());
//            text.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mvw " + viewer.getId()));
//
//            account.send(text);
//            return;
//        }

        MenuViewer viewer = MenuViewer.of(args[0]);

        if (viewer == null) {
            account.send("§cO ID informado não foi encontrado.");
            return;
        }

        new ViewerMenu(context.getPlayer(), viewer).handle();
    }
}
