package com.minecraft.lobby.menu.shop;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.Arrays;

public class ShopMenu extends Menu {

    public ShopMenu(Player player) {
        super(player, "Loja " + com.minecraft.core.Constant.SERVER_NAME, 6);
    }

    @Override
    public void handle() {
        clear();

        // Fileira 1 (slots 0-8)
        // Slot 0: Vazio
        
        // Slot 1: Ranks (Esmeralda)
        addItem(1, Item.of(Material.EMERALD, "§aRanks"));

        // Slot 2: Max+ (Corante Roxo)
        addItem(2, Item.of(Material.INK_SACK, 5, "§5Max§6+")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new MaxPlusShopMenu(getPlayer(), this).handle();
                }));

        // Slot 3: Medalhas (Girassol) - CORRIGIDO
        addItem(3, Item.of(Material.DOUBLE_PLANT, 0, "§aMedalhas")  // DOUBLE_PLANT: 0 = Girassol, 1 = Lilás, 2 = Roseira, 3 = Peônia
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new MedalhasShopMenu(getPlayer(), this).handle();
                }));

        // Slots 4-6: Vazios

        // Slot 7: Como pagar? (Livro)
        addItem(7, Item.of(Material.BOOK, "§aComo pagar?")
                .flags(ItemFlag.values())
                .lore(Arrays.asList(
                        "§7Ao escolher um produto",
                        "§7voce recebera um mapa",
                        "§7com o QR CODE do §aPix.",
                        "",
                        "§7Basta escanear e aguardar!"
                )));

        // Slot 8: Meus pedidos (Papel)
        addItem(8, Item.of(Material.PAPER, "§aMeus pedidos")
                .flags(ItemFlag.values())
                .lore("§eClique para ver!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new MeusPedidosMenu(getPlayer(), this).handle();
                }));

        // Fileira 2 (slots 9-17): Todos vidros cinzas, com verde no slot 10 (sob Ranks)
        for (int i = 9; i <= 17; i++) {
            if (i == 10) {
                addItem(i, Item.of(Material.STAINED_GLASS_PANE, 5, " ")); // Verde
            } else {
                addItem(i, Item.of(Material.STAINED_GLASS_PANE, 7, " ")); // Cinza
            }
        }

        // Fileira 3 (slots 18-26): Vazia

        // Fileira 4 (slots 27-35)
        // Slot 29: VIP (Corante Verde Limão)
        addItem(29, Item.of(Material.INK_SACK, 10, "§aVIP")
                .flags(ItemFlag.values())
                .lore(Arrays.asList(
                        "§8Permanente.",
                        "",
                        "§a✓ §7Voo no lobby;",
                        "§a✓ §7Selecionar mapas;",
                        "§a✓ §7Anuncio ao entrar;",
                        "§a✓ §7Cosmeticos;",
                        "§7E muito mais!",
                        "",
                        "§aPreço: §eR$ 29,90"
                ))
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new RankPurchaseMenu(getPlayer(), this, "VIP", 29.90, "§aVIP", new String[]{
                            "§8Permanente.",
                            "",
                            "§a✓ §7Voo no lobby;",
                            "§a✓ §7Selecionar mapas;",
                            "§a✓ §7Anuncio ao entrar;",
                            "§a✓ §7Cosmeticos;",
                            "§7E muito mais!",
                            "",
                            "§aPreço: §eR$ 29,90"
                    }).handle();
                }));

        // Slot 30: Max (Corante Rosa)
        addItem(31, Item.of(Material.INK_SACK, 9, "§dMax")
                .flags(ItemFlag.values())
                .lore(Arrays.asList(
                        "§8Permanente.",
                        "",
                        "§a✓ §7Voo no lobby;",
                        "§a✓ §7Selecionar mapas;",
                        "§a✓ §7Anuncio ao entrar;",
                        "§a✓ §7Cosmeticos;",
                        "§7E muito mais!",
                        "",
                        "§aPreço: §eR$ 49,90"
                ))
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new RankPurchaseMenu(getPlayer(), this, "Max", 49.90, "§dMax", new String[]{
                            "§8Permanente.",
                            "",
                            "§a✓ §7Voo no lobby;",
                            "§a✓ §7Selecionar mapas;",
                            "§a✓ §7Anuncio ao entrar;",
                            "§a✓ §7Cosmeticos;",
                            "§7E muito mais!",
                            "",
                            "§aPreço: §eR$ 49,90"
                    }).handle();
                }));

        // Slot 32: Max+ (Corante Roxo)
        addItem(33, Item.of(Material.INK_SACK, 5, "§5Max§5+")
                .flags(ItemFlag.values())
                .lore(Arrays.asList(
                        "§530 dias.",
                        "",
                        "§a✓ §7Voo no lobby;",
                        "§a✓ §7Selecionar mapas;",
                        "§a✓ §7Anuncio ao entrar;",
                        "§a✓ §7Cosmeticos;",
                        "§7E muito mais!",
                        "",
                        "§aPreço: §eR$ 39,90"
                ))
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new RankPurchaseMenu(getPlayer(), this, "Max+", 39.90, "§5Max§5+", new String[]{
                            "§530 dias.",
                            "",
                            "§a✓ §7Voo no lobby;",
                            "§a✓ §7Selecionar mapas;",
                            "§a✓ §7Anuncio ao entrar;",
                            "§a✓ §7Cosmeticos;",
                            "§7E muito mais!",
                            "",
                            "§aPreço: §eR$ 39,90"
                    }).handle();
                }));

        // Fileira 5 (slots 36-44)
        // Slot 38: Vantagens VIP
        addItem(38, Item.of(Material.PAPER, "§aVantagens do VIP")
                .flags(ItemFlag.values())
                .lore("§7Todas vantagens do VIP;", "", "§eClique para ver!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new RankBenefitsMenu(getPlayer(), this, "VIP").handle();
                }));

        // Slot 40: Vantagens Max
        addItem(40, Item.of(Material.PAPER, "§aVantagens do Max")
                .flags(ItemFlag.values())
                .lore("§7Todas vantagens do Max;", "", "§eClique para ver!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new RankBenefitsMenu(getPlayer(), this, "Max").handle();
                }));

        // Slot 42: Vantagens Max+
        addItem(42, Item.of(Material.PAPER, "§aVantagens do Max+")
                .flags(ItemFlag.values())
                .lore("§7Todas vantagens do Max+;", "", "§eClique para ver!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new RankBenefitsMenu(getPlayer(), this, "Max+").handle();
                }));

        display();
    }
}