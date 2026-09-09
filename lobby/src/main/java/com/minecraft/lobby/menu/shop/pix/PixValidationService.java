package com.minecraft.lobby.menu.shop.pix;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.Rank;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.lobby.menu.shop.orders.Order;
import com.minecraft.lobby.menu.shop.orders.OrderManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class PixValidationService {

    private final JavaPlugin plugin;
    private BukkitTask validationTask;

    public PixValidationService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        validationTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    List<Order> orders = OrderManager.getOrders(player);
                    for (Order order : orders) {
                        if (!order.getStatus().equals("PENDING")) continue;
                        try {
                            boolean paid = MercadoPagoAPI.checkPayment("payment_" + order.getId());
                            if (paid) {
                                order.setStatus("PAID");
                                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                                    @Override
                                    public void run() {
                                        player.sendMessage("§aPagamento confirmado! Seu " + order.getProductName() + " foi ativado!");
                                        ativarRank(player, order.getProductName());
                                    }
                                });
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Erro ao validar PIX para " + player.getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }, 20L * 60, 20L * 60);
    }

    private void ativarRank(Player player, String productName) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) {
            player.sendMessage("§cErro ao ativar rank: conta não encontrada.");
            return;
        }

        RankType rankType;
        switch (productName) {
            case "§aVIP":
                rankType = RankType.VIP;
                break;
            case "§dMax":
                rankType = RankType.MAX;
                break;
            case "§5Max§5+":
                rankType = RankType.MAX_PLUS;
                break;
            default:
                player.sendMessage("§cRank desconhecido: " + productName);
                return;
        }

        account.setRank(Rank.builder().type(rankType).build());
        account.saveContext(account.getContext());

        player.sendMessage("§aParabéns! Agora você é " + rankType.getColoredName() + "§a!");
        plugin.getLogger().info("Rank " + rankType + " ativado para " + player.getName());
    }

    public void stop() {
        if (validationTask != null) {
            validationTask.cancel();
        }
    }
}
