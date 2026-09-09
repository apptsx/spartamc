package com.minecraft.lobby.menu.shop.orders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

public class OrderManager {

    private static final Map<String, List<Order>> playerOrders = new HashMap<>();

    public static void addOrder(Player player, Order order) {
        playerOrders.computeIfAbsent(player.getName(), k -> new ArrayList<>()).add(order);
    }

    public static List<Order> getOrders(Player player) {
        return playerOrders.getOrDefault(player.getName(), new ArrayList<>());
    }

    public static Order getLastOrder(Player player) {
        List<Order> orders = getOrders(player);
        if (orders.isEmpty()) return null;
        return orders.get(orders.size() - 1);
    }
}
