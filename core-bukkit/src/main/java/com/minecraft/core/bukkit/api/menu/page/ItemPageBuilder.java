package com.minecraft.core.bukkit.api.menu.page;

@FunctionalInterface
public interface ItemPageBuilder<T> {
    void accept(T item, int slot);
}
