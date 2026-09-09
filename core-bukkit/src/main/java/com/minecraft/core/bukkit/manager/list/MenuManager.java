package com.minecraft.core.bukkit.manager.list;

import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.controller.Controller;

import java.util.UUID;

public class MenuManager extends Controller<UUID, Menu> {

    @Override
    public void save(Menu menu) {
        getCache().put(menu.getPlayer().getUniqueId(), menu);
    }
}
