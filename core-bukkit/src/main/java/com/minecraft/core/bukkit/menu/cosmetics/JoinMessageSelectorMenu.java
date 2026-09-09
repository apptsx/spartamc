package com.minecraft.core.bukkit.menu.cosmetics;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.joinmessage.JoinMessageMetadata;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.api.joinmessage.JoinMessage;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

public class JoinMessageSelectorMenu extends Menu {

    private final Account account;

    public JoinMessageSelectorMenu(Player player, Menu previousMenu) {
        super(player, "Mensagens de Entrada", 6);
        this.account = Core.getAccountController().of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        JoinMessageMetadata metadata = account.getJoinMessageMetadata();
        List<JoinMessage> availableMessages = metadata.getAvailableMessages(account.getRank().getType());

        int[][] slotsByRow = {
            {10, 11, 12, 13, 14, 15, 16},
            {19, 20, 21, 22, 23, 24, 25},
            {28, 29, 30, 31, 32, 33, 34},
            {37, 38, 39, 40, 41, 42, 43}
        };

        int slotIndex = 0;
        for (JoinMessage message : availableMessages) {
            if (slotIndex >= slotsByRow.length * 7) break;

            int row = slotIndex / 7;
            int col = slotIndex % 7;
            int slot = slotsByRow[row][col];

            boolean isSelected = metadata.getSelectedMessage() != null 
                    && metadata.getSelectedMessage().equals(message.getId());

            List<String> lore = new ArrayList<>();
            lore.add("");
            
            Tag tag = account.getTag();
            String rankColor = tag.getColor().toString();
            String fullPreview = rankColor + account.getNickname() + "§6 " + message.getMessage()
                    .replace("{player}", account.getNickname())
                    .replace("{displayname}", getPlayer().getDisplayName());
            lore.add("§ePrévia:");
            lore.add(fullPreview);
            lore.add("");
            
            if (isSelected) {
                lore.add("§a✔ SELECIONADO");
                lore.add("");
                lore.add("§7Clique §cDIREITO §7para desselecionar");
            } else {
                lore.add("§7Clique §eESQUERDO §7para selecionar");
            }

            String rarityColor = message.getRarity().getColor();
            
            Item icon = message.getIcon();
            if (icon == null) {
                icon = Item.of(Material.PAPER, "§6" + message.getName()).lore(lore);
            } else {
                icon.name(rarityColor + message.getName()).lore(lore);
            }

            final boolean selected = isSelected;
            icon.click(event -> {
                ClickType click = event.getClick();
                
                if (click == ClickType.RIGHT) {
                    if (selected) {
                        metadata.setSelectedMessage(null);
                        account.send("§cVocê desselecionou a mensagem de entrada.");
                    } else {
                        metadata.setSelectedMessage(message);
                        account.send("§aVocê selecionou a mensagem: §6" + message.getName());
                    }
                    sound(MenuSound.PAGINATED);
                    account.saveContext(account.getContext());
                    handle();
                    return;
                }
                
                if (click == ClickType.LEFT) {
                    if (selected) {
                        return;
                    }
                    metadata.setSelectedMessage(message);
                    account.send("§aVocê selecionou a mensagem: §6" + message.getName());
                    sound(MenuSound.PAGINATED);
                    account.saveContext(account.getContext());
                    handle();
                }
            });

            addItem(slot, icon);
            slotIndex++;
        }

        addCloseButton();
        display();
    }
}
