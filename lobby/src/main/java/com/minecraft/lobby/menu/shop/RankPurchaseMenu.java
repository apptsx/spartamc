package com.minecraft.lobby.menu.shop;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.lobby.menu.shop.coupon.CouponAnvilMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.Arrays;

public class RankPurchaseMenu extends Menu {

    private final String rankName;
    private final double rankPrice;
    private final String rankDisplayName;
    private final String[] rankLore;

    public RankPurchaseMenu(Player player, Menu last, String rankName, double price, String displayName, String[] lore) {
        super(player, "Comprar: " + displayName, last, 3);
        this.rankName = rankName;
        this.rankPrice = price;
        this.rankDisplayName = displayName;
        this.rankLore = lore;
    }

    @Override
    public void handle() {
        clear();

        // Fileira 1 (slots 0-8): Vazia

        // Fileira 2 (slots 9-17)
        // Slot 11: Concreto verde - Inserir cupom
        addItem(11, Item.of(Material.STAINED_CLAY, 13, "§aInserir cupom")
                .flags(ItemFlag.values())
                .lore(Arrays.asList(
                        "§7Insira um cupom",
                        "§7de desconto"
                ))
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new CouponAnvilMenu(getPlayer(), this, rankName, rankPrice, rankDisplayName, rankLore, false).open();
                }));

        // Slot 13: Item do rank
        Material rankMaterial;
        int rankData;
        switch (rankName.toLowerCase()) {
            case "vip":
                rankMaterial = Material.INK_SACK;
                rankData = 10;
                break;
            case "max":
                rankMaterial = Material.INK_SACK;
                rankData = 9;
                break;
            case "max+":
                rankMaterial = Material.INK_SACK;
                rankData = 5;
                break;
            default:
                rankMaterial = Material.EMERALD;
                rankData = 0;
        }

        addItem(13, Item.of(rankMaterial, rankData, rankDisplayName)
                .flags(ItemFlag.values())
                .lore(rankLore));

        // Slot 15: Concreto vermelho - Sem cupom
        addItem(15, Item.of(Material.STAINED_CLAY, 14, "§cSem cupom")
                .flags(ItemFlag.values())
                .lore(Arrays.asList(
                        "§7Prossiga sem um",
                        "§7cupom de desconto"
                ))
                .click(event -> {
                    sound(MenuSound.DONE);
                    showLoadingAndGenerate();
                }));

        display();
    }

    private void showLoadingAndGenerate() {
        Player p = getPlayer();

        clear();
        for (int i = 0; i < 27; i++) {
            addItem(i, Item.of(Material.STAINED_GLASS_PANE, 15, " "));
        }

        int[] ring = {5, 4, 3, 12, 21, 22, 23, 14};

        addItem(13, Item.of(Material.WATCH, "§aGerando QR Code...")
                .lore("§7Aguarde enquanto geramos", "§7seu pagamento PIX."));

        display();

        final int[] animIndex = {0};
        final boolean[] done = {false};
        final org.bukkit.scheduler.BukkitTask[] taskRef = new org.bukkit.scheduler.BukkitTask[1];

        taskRef[0] = org.bukkit.Bukkit.getScheduler().runTaskTimer(
                org.bukkit.Bukkit.getPluginManager().getPlugin("Lobby"),
                new Runnable() {
                    @Override
                    public void run() {
                        if (done[0]) {
                            taskRef[0].cancel();
                            return;
                        }
                        // Limpar todos
                        for (int slot : ring) {
                            addItem(slot, Item.of(Material.STAINED_GLASS_PANE, 15, " "));
                        }
                        // Só 1 verde
                        addItem(ring[animIndex[0]], Item.of(Material.STAINED_GLASS_PANE, 5, " "));
                        display();
                        animIndex[0] = (animIndex[0] + 1) % ring.length;
                    }
                },
                0L, 3L
        );

        // Geração do PIX assíncrona
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(
                org.bukkit.Bukkit.getPluginManager().getPlugin("Lobby"),
                new Runnable() {
                    @Override
                    public void run() {
                        CouponAnvilMenu menu = new CouponAnvilMenu(p, null, rankName, rankPrice, rankDisplayName, rankLore, true);
                        final org.bukkit.inventory.ItemStack mapItem = menu.generatePIXItem(rankPrice);

                        org.bukkit.Bukkit.getScheduler().runTask(
                                org.bukkit.Bukkit.getPluginManager().getPlugin("Lobby"),
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        done[0] = true;
                                        taskRef[0].cancel();
                                        p.closeInventory();

                                        if (mapItem != null) {
                                            // Coloca no slot selecionado da hotbar
                                            int selected = p.getInventory().getHeldItemSlot();
                                            p.getInventory().setItem(selected, mapItem);
                                            p.updateInventory();

                                            // Camera olhando para baixo
                                            org.bukkit.Location loc = p.getLocation().clone();
                                            loc.setPitch(90);
                                            loc.setYaw(loc.getYaw());
                                            p.teleport(loc);
                                        }
                                    }
                                }
                        );
                    }
                }
        );
    }

}
