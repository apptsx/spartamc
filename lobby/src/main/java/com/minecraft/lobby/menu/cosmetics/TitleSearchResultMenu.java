package com.minecraft.lobby.menu.cosmetics;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.type.title.CustomTitle;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.lobby.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@IgnoreEvent
public class TitleSearchResultMenu implements Listener {

    private final Player player;
    private final User user;
    private final String searchTerm;
    private final Inventory inventory;

    public TitleSearchResultMenu(Player player, User user, String searchTerm) {
        this.player = player;
        this.user = user;
        this.searchTerm = searchTerm;
        this.inventory = Bukkit.createInventory(null, 54, "Resultado: " + searchTerm);

        setupItems();
        player.openInventory(inventory);
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("Lobby"));
    }

    private static final int[] SEARCH_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30, 37, 38, 39};
    
    private void setupItems() {
        Account account = user.getAccount();
        List<Collectible> allTitles = account.getCollectibles(CollectibleCategory.TITLE);

        List<TitleCollectible> foundTitles = new ArrayList<>();
        Core.getCollectibleController()
                .list(CollectibleCategory.TITLE)
                .stream()
                .filter(c -> c instanceof TitleCollectible)
                .map(c -> (TitleCollectible) c)
                .filter(t -> allTitles.stream().anyMatch(at -> at.getName().equals(t.getName())))
                .filter(t -> ChatColor.stripColor(t.getTitle()).toLowerCase().contains(searchTerm.toLowerCase()))
                .forEach(foundTitles::add);

        // Limpar slots
        for (int slot : SEARCH_SLOTS) {
            inventory.setItem(slot, null);
        }

        if (foundTitles.isEmpty()) {
            inventory.setItem(22, ItemFactory.createItemStack(Material.STAINED_GLASS_PANE, "§cTítulo não encontrado!",
                    Arrays.asList("§7Nenhum título encontrado para: §f" + searchTerm,
                            "",
                            "§7Verifique a ortografia e tente novamente.")));
        } else {
            int index = 0;
            for (TitleCollectible title : foundTitles) {
                if (index >= SEARCH_SLOTS.length) break;
                ItemStack item = new ItemStack(Material.NAME_TAG);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(title.getTitle());
                meta.setLore(Arrays.asList("§7Clique para aplicar este título!",
                        "",
                        "§7Título: §f" + ChatColor.stripColor(title.getTitle())));
                item.setItemMeta(meta);
                inventory.setItem(SEARCH_SLOTS[index++], item);
            }
        }

        inventory.setItem(49, ItemFactory.createItemStack(Material.ARROW, "§aVoltar",
                Arrays.asList("§7Voltar para busca")));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);

        if (!event.getWhoClicked().equals(player)) return;
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(inventory)) return;

        int slot = event.getSlot();

        if (slot == 49) {
            player.closeInventory();
            new TitlesMenu(player, user);
            return;
        }

        if (isSearchSlot(slot)) {
            ItemStack clicked = inventory.getItem(slot);
            if (clicked == null || !clicked.hasItemMeta()) return;

            String rawTitle = clicked.getItemMeta().getDisplayName();
            String titleName = ChatColor.stripColor(rawTitle);

            Account account = user.getAccount();
            TitleCollectible found = Core.getCollectibleController()
                    .list(CollectibleCategory.TITLE)
                    .stream()
                    .filter(c -> c instanceof TitleCollectible)
                    .map(c -> (TitleCollectible) c)
                    .filter(t -> t.getTitle().equals(rawTitle) || ChatColor.stripColor(t.getTitle()).equals(titleName))
                    .findFirst()
                    .orElse(null);

            if (found != null) {
                player.closeInventory();
                TitleCollectible.removeTitle(player);
                CustomTitle.removeTitle(player);
                account.activateCollectible(found);
                found.showTitle(player);
                player.sendMessage("§aTítulo aplicado com sucesso: " + found.getTitle());
            } else {
                player.sendMessage("§cErro ao aplicar título!");
            }
        }
    }

    private boolean isSearchSlot(int slot) {
        for (int s : SEARCH_SLOTS) {
            if (s == slot) return true;
        }
        return false;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }
}
