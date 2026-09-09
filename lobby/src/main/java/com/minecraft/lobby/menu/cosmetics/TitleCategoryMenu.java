package com.minecraft.lobby.menu.cosmetics;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.collectible.type.title.TitleCategory;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.event.IgnoreEvent;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@IgnoreEvent
public class TitleCategoryMenu implements Listener {

    private final Player player;
    private final User user;
    private final TitleCategory category;
    private final Inventory inventory;
    private final Map<Integer, TitleCollectible> titleMap = new HashMap<>();
    private List<TitleCollectible> categoryTitles = new ArrayList<>();
    
    private int currentPage = 0;
    private static final int TITLES_PER_PAGE = 28; // 4 fileiras x 7 colunas
    private static final int[] SLOT_GRID = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public TitleCategoryMenu(Player player, User user, TitleCategory category) {
        this.player = player;
        this.user = user;
        this.category = category;
        this.inventory = Bukkit.createInventory(null, 54, "Títulos - " + category.getDisplayName());
        
        setupItems();
        player.openInventory(inventory);
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("Lobby"));
    }

    private void setupItems() {
        Account account = user.getAccount();
        
        // Limpar mapa de títulos da página anterior
        titleMap.clear();
        
        // Carregar títulos do jogador
        List<Collectible> allTitles = account.getCollectibles(CollectibleCategory.TITLE);
        
        // Filtrar títulos por categoria - apenas os que o jogador possui
        categoryTitles = Core.getCollectibleController()
                .list(CollectibleCategory.TITLE)
                .stream()
                .filter(c -> c instanceof TitleCollectible)
                .map(c -> (TitleCollectible) c)
                .filter(title -> title.getTitleCategory() == category)
                .filter(title -> allTitles.stream().anyMatch(t -> t.getName().equals(title.getName())))
                .collect(Collectors.toList());

        // Limpar slots do grid
        for (int slot : SLOT_GRID) {
            inventory.setItem(slot, null);
        }

        // Se não houver títulos, mostrar vidro vermelho no slot 22
        if (categoryTitles.isEmpty()) {
            inventory.setItem(31, Item.of(Material.STAINED_GLASS_PANE, 14, "§cVocê não possui títulos!"));
        } else {
            // Paginação
            int startIndex = currentPage * TITLES_PER_PAGE;
            int endIndex = Math.min(startIndex + TITLES_PER_PAGE, categoryTitles.size());
            
            int index = 0;
            for (int i = startIndex; i < endIndex && index < SLOT_GRID.length; i++) {
                TitleCollectible title = categoryTitles.get(i);

                boolean isActive = account.getActiveCollectibles().contains("TITLE:" + title.getName());

                List<String> lore = new ArrayList<>();
                lore.addAll(title.getLore());
                lore.add("");
                lore.add(isActive ? "§6Selecionado atualmente." : "§eClique para selecionar!");

                Material material = Material.NAME_TAG;

                inventory.setItem(SLOT_GRID[index], ItemFactory
                        .createItemStack(material, "§a" + title.getName(), lore));

                titleMap.put(SLOT_GRID[index], title);

                index++;
            }
        }
        
        setupNavigationButtons();
    }
    
    private void setupNavigationButtons() {
        // Remover título - slot 45
        inventory.setItem(45, Item.of(Material.STAINED_GLASS, 14, "§cRemover Título", "§7Clique para remover seu título atual"));
        
        // Voltar - slot 46
        inventory.setItem(46, ItemFactory
                .createItemStack(Material.ARROW, "§aVoltar", Arrays.asList("§7Voltar ao Menu de Cosméticos")));
        
        // Paginação - slots 52 (anterior) e 53 (próxima)
        int totalPages = (int) Math.ceil((double) categoryTitles.size() / TITLES_PER_PAGE);
        
        if (currentPage > 0) {
            inventory.setItem(52, ItemFactory
                    .createItemStack(Material.ARROW, "§aPágina Anterior", Arrays.asList("§7Clique para voltar")));
        }

        if (currentPage < totalPages - 1) {
            inventory.setItem(53, ItemFactory
                    .createItemStack(Material.ARROW, "§aPróxima Página", Arrays.asList("§7Clique para avançar")));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
        
        if (!event.getWhoClicked().equals(player)) return;
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(inventory)) return;
        
        if (event.getSlot() == 45) {
            Account account = user.getAccount();
            
            // Remove todos os títulos ativos (qualquer formato)
            account.getActiveCollectibles().removeIf(id ->
                    id.startsWith("TITLE:") ||
                    id.startsWith("CUSTOM_TITLE:") ||
                    id.toLowerCase().startsWith("title:"));
            
            // Remover título customizado visualmente
            com.minecraft.core.api.collectible.type.title.CustomTitle.removeTitle(player);
            account.setCustomTitle(null);
            
            // Definir NoneTitle vazio
            account.getActiveCollectibles().add("title:comum:nenhum");
            
            // Remover o ArmorStand do título
            com.minecraft.core.api.collectible.type.title.TitleCollectible.removeTitle(player);
            
            // Salvar no MySQL
            account.saveContext(account.getContext());
            
            // Remover do cache para forçar recarga na próxima login
            Core.getAccountData().removeCache(account);
            
            player.sendMessage("§aTítulo removido com sucesso!");
            player.closeInventory();
            return;
        }
        
        if (event.getSlot() == 46) {
            HandlerList.unregisterAll(this);
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(
                    Bukkit.getPluginManager().getPlugin("Lobby"),
                    new Runnable() {
                        @Override
                        public void run() {
                            new TitlesMenu(player, user);
                        }
                    },
                    2L
            );
            return;
        }
        
        // Paginação
        if (event.getSlot() == 52) {
            if (currentPage > 0) {
                currentPage--;
                setupItems();
            }
            return;
        }
        
        if (event.getSlot() == 53) {
            int maxPage = (int) Math.ceil((double) categoryTitles.size() / TITLES_PER_PAGE) - 1;
            if (currentPage < maxPage) {
                currentPage++;
                setupItems();
            }
            return;
        }
        
        // Clicou em um título
        TitleCollectible title = titleMap.get(event.getSlot());
        if (title != null) {
            Account account = user.getAccount();
            boolean owns = account.getCollectibles(CollectibleCategory.TITLE)
                    .stream()
                    .anyMatch(t -> t.getName().equals(title.getName()));
            if (owns) {
                String titleId = "TITLE:" + title.getName();
                if (account.getActiveCollectibles().contains(titleId)) {
                    account.getActiveCollectibles().remove(titleId);
                    TitleCollectible.removeTitle(player);
                    account.saveContext(account.getContext());
                    player.sendMessage("§cTítulo desequipado!");
                } else {
                    account.activateCollectible(title);
                    title.showTitle(player);
                    player.sendMessage("§aTítulo equipado: " + title.getTitle());
                }
            } else {
                player.sendMessage("§cVocê não possui este título!");
            }
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer().equals(player) && event.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }
}
