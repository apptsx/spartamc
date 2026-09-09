package com.minecraft.lobby.command;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.lobby.menu.shop.ShopMenu;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandInheritor {

    @Command(name = "loja", aliases = {"shop", "store"})
    public void shop(BukkitCommandContext context) {
        Player player = context.getPlayer();
        new ShopMenu(player).handle();
    }
}