package com.minecraft.lobby.menu.shop;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.lobby.menu.shop.orders.Order;
import com.minecraft.lobby.menu.shop.orders.OrderManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.List;

public class MeusPedidosMenu extends Menu {

    public MeusPedidosMenu(Player player, Menu last) {
        super(player, "Meus Pedidos", last, 6);
    }

    @Override
    public void handle() {
        clear();

        // Botão voltar
        addItem(45, Item.of(Material.ARROW, "§cVoltar")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    getLast().handle();
                }));

        // Buscar pedidos
        List<Order> orders = OrderManager.getOrders(getPlayer());

        if (orders.isEmpty()) {
            addItem(22, Item.of(Material.PAPER, "§cNenhum pedido encontrado")
                    .flags(ItemFlag.values())
                    .lore("§7Voce ainda nao possui pedidos."));
        } else {
            int slot = 9;
            for (Order order : orders) {
                if (slot >= 45) break;

                addItem(slot, Item.of(Material.PAPER, "§ePedido #" + order.getId())
                        .flags(ItemFlag.values())
                        .lore(new java.util.ArrayList<>(java.util.Arrays.asList(
                                "§7Produto: §f" + order.getProductName(),
                                "§7Preço: §eR$ " + String.format("%.2f", order.getPrice()),
                                "§7Status: " + order.getStatusColor() + order.getStatus(),
                                "§7Data: §f" + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date(order.getTimestamp()))
                        ))));

                slot++;
            }
        }

        display();
    }
}
