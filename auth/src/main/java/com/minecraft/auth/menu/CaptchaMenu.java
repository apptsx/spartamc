package com.minecraft.auth.menu;

import com.minecraft.auth.user.User;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class CaptchaMenu extends Menu {

    public CaptchaMenu(Player player) {
        super(player, "Clique no ícone Azul", 3);
    }

    @Override
    public void handle() {
        clear();

        User user = (User) User.of(getPlayer().getUniqueId());

        for (int i = 0; i < getTotalSlots(); i++)
            addItem(i, Item.of(Material.SKULL_ITEM, 3, " ")
                    .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTIwZmNhNzM4ZjIwYjM3ZDQ0OWI1NGFjOTlhZDZjNDlmYzllYjdkMzVmZjQzYmIwNDg1MjJhZTc4NTBkZjZmNCJ9fX0=")
                    .click(event -> {
                        sound(MenuSound.ERROR);

                        user.setTotalAttempts(user.getTotalAttempts() - 1);

                        if (user.getTotalAttempts() == 0)
                            getPlayer().kickPlayer(Constant.SERVER_TITLE + "\n\n§cVocê falhou a verificação."
                                    + "\n§cTente entrar novamente mais tarde!"
                                    + "\n\n§cEm caso de problemas, contate: §e" + Constant.SERVER_DISCORD);
                    }));

        addItem(Core.RANDOM.nextInt(getTotalSlots()), Item.of(Material.SKULL_ITEM, 3, " ")
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjEwZTM3NGNkYzJiYTk1YmI3MmYxYTAzNmM3N2RhMzUwOTkzNWExYWJkMjRiNjhjNmIzNTkxNjkwYjEwM2ZlZCJ9fX0=")
                .click(event -> {
                    user.setCaptcha(false);
                    user.resetTime();

                    if (user.getTotalAttempts() < 3)
                        user.setTotalAttempts(3);

                    close();
                    sound(MenuSound.SUCCESS);
                }));

        display();
    }
}
