package com.minecraft.core.bukkit.menu.account.joinmessage;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.joinmessage.JoinMessage;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JoinMessageMenu extends Menu {

    private final Account account;

    public JoinMessageMenu(Player player, Menu last) {
        super(player, "Mensagens de Entrada", last, 6, 21);
        this.account = Core.getAccountController().of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        List<JoinMessage> availableMessages = account.getAvailableJoinMessages();
        JoinMessage selectedMessage = account.getSelectedJoinMessageEnum();
        availableMessages.sort(Comparator.comparingInt(msg -> msg.getRarity().ordinal()));

        buildPageItems(availableMessages, 10, (message, slot) -> {
            boolean hasAccess = account.hasAccessToJoinMessage(message);
            boolean isSelected = message.equals(selectedMessage);

            List<String> lore = new ArrayList<>();
            lore.add("§8Mensagens de Entrada");
            lore.add("");
            lore.add("§7Raridade: " + message.getRarity().getColoredName());
            lore.add("");
            lore.add("§7Prévia:");
            lore.add(account.getTag().getByPrefix(account.getTagPrefix()) + getPlayer().getName() + " §6" + message.getMessage());
            lore.add("");
            
            if (!hasAccess) {
                lore.add("§7Exclusivo de " + message.getRequiredRank().getName());
            } else if (isSelected) {
                lore.add("§eSelecionada.");
            } else {
                lore.add("§eClique para selecionar!");
            }

            Material material = hasAccess ? Material.BOOK : Material.INK_SACK;
            int data = hasAccess ? 0 : 8;
            
            String name = (hasAccess ? "§a" + message.getName() : "§c");
            Item item = Item.of(material, data, name, lore).flags(ItemFlag.values());
            if (isSelected) {
                item.enchantment(Enchantment.DURABILITY, 1);
            }

            addItem(slot, item.click(event -> {
                if (!hasAccess) {
                    sound(MenuSound.ERROR);
                    account.send("§cVocê não tem permissão.");
                    return;
                }

                if (isSelected) {
                    account.setSelectedJoinMessage(null);
                    sound(MenuSound.SUCCESS);
                    account.send("§cVocê deselecionou §e" + message.getName() + "§c.");
                } else {
                    account.setSelectedJoinMessage(message);
                    sound(MenuSound.SUCCESS);
                    account.send("§bVocê selecionou §e" + message.getName() + "§b!");
                }
                handle();
            }));
        });

        int randomSlot = getTotalSlots() - (getTotalPages() > 1 ? 5 : 6);
        
        addItem(randomSlot, Item.of(Material.HOPPER_MINECART, "§aSeleção aleatória",
                "§7Selecione esta opção para",
                "§7usar mensagens aleatórias.",
                "",
                "Estado: " + (selectedMessage == null ? "§aAtivada" : "§cDesativada"),
                "",
                selectedMessage == null ? "§eClique para ativar!" : "§eAtivado."
                ).click(event -> {
                    if (selectedMessage == null) {
                        sound(MenuSound.ERROR);
                        return;
                    }

                    account.setSelectedJoinMessage(null);
                    sound(MenuSound.SUCCESS);
                    account.send("§aModo aleatório ativado! Uma mensagem aleatória será usada.");
                    handle();
                }));

        if (isReturnable())
            addBackButton();

        display();
    }
}
