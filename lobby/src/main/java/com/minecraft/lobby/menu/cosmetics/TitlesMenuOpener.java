package com.minecraft.lobby.menu.cosmetics;

import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.lobby.user.User;
import org.bukkit.entity.Player;

public class TitlesMenuOpener implements com.minecraft.core.bukkit.menu.server.collectible.TitlesMenuOpener {
    @Override
    public void openTitlesMenu(Player player) {
        UserModel model = User.of(player.getUniqueId());
        if (model instanceof User) {
            new TitlesMenu(player, (User) model);
        }
    }
}
