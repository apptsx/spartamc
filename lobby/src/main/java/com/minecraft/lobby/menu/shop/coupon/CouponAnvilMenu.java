package com.minecraft.lobby.menu.shop.coupon;

import com.minecraft.lobby.menu.shop.RankPurchaseMenu;
import com.minecraft.lobby.menu.shop.orders.Order;
import com.minecraft.lobby.menu.shop.orders.OrderManager;
import com.minecraft.lobby.menu.shop.pix.MercadoPagoAPI;
import com.minecraft.lobby.menu.shop.pix.PixData;
import com.minecraft.lobby.menu.shop.qrcode.ImageCreator;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryAnvil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CouponAnvilMenu {

    private final Player player;
    private final RankPurchaseMenu lastMenu;
    private final String rankName;
    private final double rankPrice;
    private final String rankDisplayName;
    private final String[] rankLore;
    private final boolean skipCoupon;

    private ContainerAnvil anvilContainer;
    private BukkitRunnable updateTask;
    private boolean taskCancelled = false;
    private boolean processed = false;

    private static final Map<String, Double> VALID_COUPONS = new HashMap<>();

    static {
        VALID_COUPONS.put("BETA15",0.15);
    }

    public CouponAnvilMenu(Player player, RankPurchaseMenu lastMenu, String rankName,
                           double rankPrice, String rankDisplayName, String[] rankLore, boolean skipCoupon) {
        this.player = player;
        this.lastMenu = lastMenu;
        this.rankName = rankName;
        this.rankPrice = rankPrice;
        this.rankDisplayName = rankDisplayName;
        this.rankLore = rankLore;
        this.skipCoupon = skipCoupon;
    }

    public void open() {
        if (skipCoupon) {
            generatePIX(rankPrice);
            return;
        }

        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        anvilContainer = new ContainerAnvil(entityPlayer.inventory, entityPlayer.world,
                new BlockPosition(0, 0, 0), entityPlayer) {
            @Override
            public boolean a(EntityHuman entityhuman) {
                return true;
            }
        };

        int containerId = entityPlayer.nextContainerCounter();

        entityPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(
                containerId, "minecraft:anvil", new ChatMessage("Inserir Cupom"), 0));

        entityPlayer.activeContainer = anvilContainer;
        entityPlayer.activeContainer.windowId = containerId;
        entityPlayer.activeContainer.addSlotListener(entityPlayer);

        ItemStack paper = new ItemStack(org.bukkit.Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName("§eDigite o cupom...");
        paper.setItemMeta(meta);

        anvilContainer.getBukkitView().getTopInventory().setItem(0, paper);

        CouponAnvilListener.activeMenus.put(player.getUniqueId(), this);

        updateTask = new BukkitRunnable() {
            private String lastText = "";

            @Override
            public void run() {
                if (taskCancelled || !player.isOnline()
                        || player.getOpenInventory().getType() != org.bukkit.event.inventory.InventoryType.ANVIL) {
                    cancel();
                    return;
                }

                try {
                    String currentText = "";
                    try {
                        Field renameField = ContainerAnvil.class.getDeclaredField("renameText");
                        renameField.setAccessible(true);
                        String renameText = (String) renameField.get(anvilContainer);
                        if (renameText != null && !renameText.isEmpty()) {
                            currentText = renameText.toUpperCase();
                        }
                    } catch (Exception ignored) {}

                    if (currentText.isEmpty()) {
                        ItemStack input = anvilContainer.getBukkitView().getTopInventory().getItem(0);
                        if (input != null && input.hasItemMeta() && input.getItemMeta().hasDisplayName()) {
                            currentText = org.bukkit.ChatColor.stripColor(input.getItemMeta().getDisplayName()).toUpperCase();
                        }
                    }

                    if (!currentText.equals(lastText)) {
                        lastText = currentText;
                        ItemStack result = new ItemStack(org.bukkit.Material.PAPER);
                        ItemMeta resultMeta = result.getItemMeta();

                        if (!currentText.isEmpty() && !currentText.equals("§EDIGITE O CUPOM...")) {
                            if (VALID_COUPONS.containsKey(currentText)) {
                                resultMeta.setDisplayName("§a✓ " + currentText);
                            } else {
                                resultMeta.setDisplayName("§c✗ Cupom inválido");
                            }
                        } else {
                            result = null;
                        }

                        anvilContainer.getBukkitView().getTopInventory().setItem(2, result);

                        EntityPlayer ep = ((CraftPlayer) player).getHandle();
                        anvilContainer.a();
                        ep.updateInventory(anvilContainer);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        updateTask.runTaskTimer(Bukkit.getPluginManager().getPlugin("Lobby"),0L, 1L);
    }

    public void handleClick(InventoryClickEvent event) {
        if (!(event.getClickedInventory() instanceof CraftInventoryAnvil)) return;

        CraftInventoryAnvil anvil = (CraftInventoryAnvil) event.getClickedInventory();

        if (event.getSlot() == 2) {
            ItemStack result = anvil.getItem(2);
            if (result != null && result.getType() == org.bukkit.Material.PAPER) {
                ItemMeta meta = result.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String displayName = org.bukkit.ChatColor.stripColor(meta.getDisplayName());
                    if (displayName.startsWith("§a✓ ")) {
                        String couponCode = displayName.substring(4).trim();
                        event.setCancelled(true);
                        processed = true;

                        anvil.setItem(0, null);
                        anvil.setItem(1, null);
                        anvil.setItem(2, null);
                        player.setItemOnCursor(null);
                        player.closeInventory();

                        if (VALID_COUPONS.containsKey(couponCode)) {
                            double finalPrice = rankPrice * (1 - VALID_COUPONS.get(couponCode));
                            generatePIX(finalPrice);
                        } else {
                            player.sendMessage("§cCupom não encontrado!");
                            new RankPurchaseMenu(player, lastMenu, rankName, rankPrice, rankDisplayName, rankLore).handle();
                        }
                    } else {
                        player.sendMessage("§cCupom inválido!");
                    }
                }
            }
        }
    }

    public void handleDrag(InventoryDragEvent event) {
        if (event.getInventory() instanceof CraftInventoryAnvil) {
            event.setCancelled(true);
        }
    }

    public void handleClose(InventoryCloseEvent event) {
        if (processed) return;

        taskCancelled = true;
        if (updateTask != null) updateTask.cancel();

        if (event.getInventory() instanceof CraftInventoryAnvil) {
            CraftInventoryAnvil anvil = (CraftInventoryAnvil) event.getInventory();
            anvil.setItem(0, null);
            anvil.setItem(1, null);
            anvil.setItem(2, null);
        }

        player.setItemOnCursor(null);
        player.updateInventory();
        CouponAnvilListener.activeMenus.remove(player.getUniqueId());

        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("Lobby"),
                () -> new RankPurchaseMenu(player, lastMenu, rankName, rankPrice, rankDisplayName, rankLore).handle(), 2L);
    }

    public ItemStack generatePIXItem(double price) {
        try {
            Order order = new Order(rankDisplayName, price);
            OrderManager.addOrder(player, order);

            PixData pixData = MercadoPagoAPI.createPixPayment(player.getName(), rankDisplayName, price, order.getId());

            if (pixData == null || pixData.getQrCode() == null || pixData.getQrCode().isEmpty()) {
                player.sendMessage("§cErro ao gerar cobrança PIX!");
                return null;
            }

            java.awt.image.BufferedImage qrImage = ImageCreator.generateQR(pixData.getQrCode());
            ItemStack mapItem = ImageCreator.generateMap(qrImage, player);
            org.bukkit.inventory.meta.ItemMeta meta = mapItem.getItemMeta();
            meta.setDisplayName("§aPIX - " + rankDisplayName);
            mapItem.setItemMeta(meta);

            player.sendMessage("§aCobrança PIX gerada! §eR$ " + String.format("%.2f", price));
            player.sendMessage("§7Abra o mapa para ler o QR Code com seu banco.");
            player.sendMessage("§7ID do Pedido: §f" + order.getId());

            return mapItem;

        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao gerar cobrança PIX: " + e.getMessage());
            return null;
        }
    }

    public void generatePIX(double price) {
        ItemStack mapItem = generatePIXItem(price);
        if (mapItem == null) return;

        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i) == null) {
                player.getInventory().setItem(i, mapItem);
                break;
            }
        }
        if (!player.getInventory().contains(mapItem)) {
            player.getInventory().addItem(mapItem);
        }
    }
}
