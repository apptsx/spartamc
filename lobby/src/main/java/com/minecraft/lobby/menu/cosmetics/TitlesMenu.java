package com.minecraft.lobby.menu.cosmetics;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.type.title.TitleCategory;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.lobby.menu.cosmetics.CustomTitleAnvilMenu;
import com.minecraft.lobby.menu.cosmetics.SearchAnvilMenu;
import com.minecraft.lobby.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;

@IgnoreEvent
public class TitlesMenu implements Listener {

    private final Player player;
    private final User user;
    private final Inventory inventory;

    public TitlesMenu(Player player, User user) {
        this.player = player;
        this.user = user;
        this.inventory = Bukkit.createInventory(null, 54, "Títulos - Categorias");
        
        setupCategories();
        player.openInventory(inventory);
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("Lobby"));
    }

    private void setupCategories() {
        Account account = user.getAccount();
        boolean isStaff = account.hasRank(com.minecraft.core.account.context.objects.rank.type.RankType.HELPER);
        boolean isPartnerOrHigher = account.hasRank(com.minecraft.core.account.context.objects.rank.type.RankType.PARTNER);
        
        // Linha 1 (slots 11-15): Categorias principais
        // Hunger Games - Slot 11
        inventory.setItem(11, ItemFactory
                .createItemStack(Material.MUSHROOM_SOUP, "§aHunger Games", Arrays.asList(
                        "§7Títulos relacionados ao",
                        "§7modo Hunger Games.",
                        "",
                        "§eClique para abrir!"
                )));

        // BedWars - Slot 12
        inventory.setItem(12, ItemFactory
                .createItemStack(Material.BED, "§aBedWars", Arrays.asList(
                        "§7Títulos relacionados ao",
                        "§7modo BedWars.",
                        "",
                        "§eClique para abrir!"
                )));

        // SkyWars - Slot 13
        inventory.setItem(13, ItemFactory
                .createItemStack(Material.FEATHER, "§aSkyWars", Arrays.asList(
                        "§7Títulos relacionados ao",
                        "§7modo SkyWars.",
                        "",
                        "§eClique para abrir!"
                )));

        // The Bridge - Slot 14
        inventory.setItem(14, ItemFactory
                .createItemStack(Material.DIAMOND_HOE, "§aThe Bridge", Arrays.asList(
                        "§7Títulos relacionados ao",
                        "§7modo The Bridge.",
                        "",
                        "§eClique para abrir!"
                )));

        // Duels - Slot 15
        inventory.setItem(15, ItemFactory
                .createItemStack(Material.IRON_SWORD, "§aDuels", Arrays.asList(
                        "§7Títulos relacionados ao",
                        "§7modo Duels.",
                        "",
                        "§eClique para abrir!"
                )));

        // Linha 2 (slots 20-22): Categorias secundárias
        // Geral - Slot 21
        inventory.setItem(21, ItemFactory
                .createItemStack(Material.NAME_TAG, "§aGeral", Arrays.asList(
                        "§7Títulos gerais",
                        "§7como Ausente, Beta, Cargo, etc.",
                        "",
                        "§eClique para abrir!"
                )));
        
        // Especial - Slot 22
        inventory.setItem(22, ItemFactory
                .createItemStack(Material.NETHER_STAR, "§aEspecial", Arrays.asList(
                        "§7Títulos especiais e únicos.",
                        "",
                        "§eClique para abrir!"
                )));
        
        // PvP - Slot 23
        inventory.setItem(23, ItemFactory
                .createItemStack(Material.DIAMOND_SWORD, "§aPvP", Arrays.asList(
                        "§7Títulos relacionados ao",
                        "§7modo PvP.",
                        "",
                        "§eClique para abrir!"
                )));

        // Staff (apenas para staff) - Slot 31
        if (isStaff) {
            inventory.setItem(31, ItemFactory
                    .createItemStack(Material.REDSTONE_COMPARATOR, "§aStaff", Arrays.asList(
                            "§7Títulos exclusivos da staff.",
                            "",
                            "§eClique para abrir!"
                    )));
        }

        // Buscar títulos - Slot 52
        inventory.setItem(52, ItemFactory
                .createItemStack(Material.HOPPER, "§aBuscar títulos", Arrays.asList(
                        "§7Busque um título específico",
                        "",
                        "§eClique para buscar!"
                )));

        // Título customizável - Slot 53 - apenas para PARTNER ou superior
        if (isPartnerOrHigher) {
            inventory.setItem(53, ItemFactory
                    .createItemStack(Material.NAME_TAG, "§aTítulo Customizável", Arrays.asList(
                            "§7Crie seu próprio título",
                            "§7customizável!",
                            "",
                            "§eClique para criar!"
                    )));
        }

        // Remover Título - Slot 48
        inventory.setItem(48, ItemFactory
                .createItemStack(Material.BARRIER, "§cRemover Título", Arrays.asList(
                        "§7Clique para remover",
                        "§7seu título atual."
                )));

        // Voltar - Slot 49
        inventory.setItem(49, ItemFactory
                .createItemStack(Material.ARROW, "§aVoltar", Arrays.asList("§7Voltar ao menu de cosméticos")));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
        
        if (!event.getWhoClicked().equals(player)) return;
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(inventory)) return;

        switch (event.getSlot()) {
            case 11:
                new TitleCategoryMenu(player, user, TitleCategory.HUNGER_GAMES);
                break;
            case 12:
                new TitleCategoryMenu(player, user, TitleCategory.BEDWARS);
                break;
            case 13:
                new TitleCategoryMenu(player, user, TitleCategory.SKYWARS);
                break;
            case 14:
                new TitleCategoryMenu(player, user, TitleCategory.THE_BRIDGE);
                break;
            case 15:
                new TitleCategoryMenu(player, user, TitleCategory.DUELS);
                break;
            case 20:
                new TitleCategoryMenu(player, user, TitleCategory.GERAL);
                break;
            case 21:
                new TitleCategoryMenu(player, user, TitleCategory.SPECIAL);
                break;
            case 22:
                new TitleCategoryMenu(player, user, TitleCategory.PVP);
                break;
            case 31:
                if (user.getAccount().hasRank(com.minecraft.core.account.context.objects.rank.type.RankType.HELPER)) {
                    new TitleCategoryMenu(player, user, TitleCategory.STAFF);
                }
                break;
            case 52: // Buscar títulos
                openSearchAnvil();
                break;
            case 53: // Título customizável
                if (user.getAccount().hasRank(com.minecraft.core.account.context.objects.rank.type.RankType.PARTNER)) {
                    player.setLevel(30);
                    new CustomTitleAnvilMenu(player, user).open();
                }
                break;
            case 48:
                Account account = user.getAccount();
                // Remove todos os títulos ativos (qualquer formato)
                account.getActiveCollectibles().removeIf(id ->
                        id.startsWith("TITLE:") ||
                        id.startsWith("CUSTOM_TITLE:") ||
                        id.toLowerCase().startsWith("title:"));
                account.setCustomTitle("");
                // Define título vazio (NoneTitle)
                account.getActiveCollectibles().add("title:comum:nenhum");
                // Salva o contexto
                account.saveContext(account.getContext());
                // Remove visualmente
                com.minecraft.core.api.collectible.type.title.CustomTitle.removeTitle(player);
                com.minecraft.core.api.collectible.type.title.TitleCollectible.removeTitle(player);
                // Limpa o cache para evitar reaplicação
                Core.getAccountData().removeCache(account);
                player.sendMessage("§aTítulo removido com sucesso!");
                player.closeInventory();
                break;
            case 49:
                HandlerList.unregisterAll(this);
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(
                        Bukkit.getPluginManager().getPlugin("Lobby"),
                        new Runnable() {
                            @Override
                            public void run() {
                                new com.minecraft.core.bukkit.menu.server.collectible.CollectibleMenu(player);
                            }
                        },
                        2L
                );
                break;
        }
    }

    private void openSearchAnvil() {
        player.closeInventory();
        player.setLevel(30);
        new SearchAnvilMenu(player, user).open();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }
}
