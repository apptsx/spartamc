package com.minecraft.core.bukkit.menu.account;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.manager.list.TagManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class MaxPlusColorMenu extends Menu {

    private final Account account;
    private final Menu previous;
    
    private final ChatColor[] colors = new ChatColor[]{
            ChatColor.GOLD, ChatColor.YELLOW, ChatColor.RED, ChatColor.DARK_RED,
            ChatColor.AQUA, ChatColor.BLUE, ChatColor.GREEN, ChatColor.DARK_GREEN,
            ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE, ChatColor.WHITE
    };
    
    private final String[] colorNames = new String[]{
            "§6Dourado (1 mês)", "§eAmarelo (2 meses)", "§cVermelho (3 meses)",
            "§4Vermelho Escuro (4 meses)", "§bCiano (5 meses)", "§9Azul (6 meses)",
            "§aVerde (8 meses)", "§2Verde Escuro (9 meses)", "§dRoxo Claro (10 meses)",
            "§5Roxo Escuro (11 meses)", "§fBranco (12 meses)"
    };
    
    private final Material[] materials = new Material[]{
            Material.GOLD_INGOT, Material.INK_SACK, Material.INK_SACK, Material.INK_SACK,
            Material.INK_SACK, Material.INK_SACK, Material.INK_SACK, Material.INK_SACK,
            Material.INK_SACK, Material.INK_SACK, Material.BONE
    };
    
    private final int[] data = new int[]{0, 11, 1, 1, 6, 4, 2, 2, 9, 5, 0};

    public MaxPlusColorMenu(Player player, Menu previous) {
        super(player, "Cor do Max+", 6);
        this.account = Core.getAccountController().of(player.getUniqueId());
        this.previous = previous;
    }

    @Override
    public void handle() {
        clear();
        
        boolean hasMaxPlusRank = account.hasRank(RankType.MAX_PLUS);
        
        if (!hasMaxPlusRank) {
            getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_BASS, 1.0f, 0.5f);
            getPlayer().sendMessage("§cVocê precisa ter o rank Max+ para alterar a cor!");
            if (previous != null) previous.handle();
            else getPlayer().closeInventory();
            return;
        }
        
        String playerName = account.getNickname();
        int months = getMaxPlusMonths();
        char currentColor = account.getMaxPlusColor();
        
        // Cores nos slots 10-22 (sequência correta)
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22};
        
        for (int i = 0; i < colors.length && i < slots.length; i++) {
            int slot = slots[i];
            ChatColor color = colors[i];
            String colorName = colorNames[i];
            boolean hasAccess = (i + 1) <= months;
            boolean isSelected = color.getChar() == currentColor;
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Exemplo: §5§lMAX§" + color.getChar() + "§l+ §5" + playerName);
            lore.add("");
            
            if (hasAccess) {
                if (isSelected) lore.add("§a§l✓ Selecionado");
                else lore.add("§eClique para selecionar");
            } else {
                lore.add("§cRequer " + (i + 1) + " mês" + ((i + 1) != 1 ? "es" : "") + " de Max+");
            }
            
            int finalI = i;
            Item colorItem;
            if (data[i] > 0 && materials[i] == Material.INK_SACK) {
                colorItem = Item.of(materials[i], data[i], colorName, lore.toArray(new String[0]));
            } else {
                colorItem = Item.of(materials[i], colorName, lore.toArray(new String[0]));
            }
            
            addItem(slot, colorItem.click(e -> {
                if (hasAccess) {
                    account.setMaxPlusColor(colors[finalI].getChar());
                    account.saveContext(account.getContext());
                    getPlayer().sendMessage("§aCor do + alterada para " + colorNames[finalI] + "§a!");
                    getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_PLING, 1.0f, 2.0f);
                    TagManager.updateTag(account);
                    handle();
                } else {
                    getPlayer().sendMessage("§cVocê ainda não tem direito a essa cor!");
                    getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_BASS, 1.0f, 0.5f);
                }
            }));
        }
        
        // Slot 49 (50): Mostrar o MAX atual do jogador
        int level = account.getMaxPlusLevel();
        char colorChar = account.getMaxPlusColor();
        String maxColor;
        if (level == 2) maxColor = "§b";
        else if (level == 3) maxColor = "§6";
        else maxColor = "§5";
        
        List<String> currentLore = Arrays.asList(
            "§7Tag atual:",
            "",
            maxColor + "§lMAX§" + colorChar + "§l+ " + maxColor + playerName,
            "",
            "§7Nível: " + level,
            "§7Cor do +: " + colorNames[getColorIndex(colorChar)]
        );
        
        addItem(49, Item.of(Material.GOLD_INGOT, "§6§lMAX+ Atual", currentLore.toArray(new String[0])));
        
        // Nível 1 - Slot 37
        List<String> nivel1Lore = Arrays.asList(
            "", "§7Inclusão no Max+", "", "§7Exemplo:", "§5§lMAX+ §5" + playerName, "",
            account.getMaxPlusLevel() == 1 ? "§a§l✓ Selecionado" : (hasMaxPlusRank && months >= 1 ? "§aClique para selecionar" : "§cRequer Max+ e 1 mês")
        );
        Item nivel1 = Item.of(Material.INK_SACK, 13, "§5Tag nível 1", nivel1Lore.toArray(new String[0]));
        nivel1.click(event -> {
            if (hasMaxPlusRank && months >= 1) {
                account.setMaxPlusLevel(1);
                account.saveContext(account.getContext());
                getPlayer().sendMessage("§aNível de tag alterado para 1!");
                getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_PLING, 1.0f, 2.0f);
                TagManager.updateTag(account);
                handle();
            } else {
                getPlayer().sendMessage("§cRequer Max+ e 1 mês!");
                getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_BASS, 1.0f, 0.5f);
            }
        });
        addItem(37, nivel1);
        
        addItem(38, Item.of(Material.STAINED_GLASS_PANE, 7, "§7", ""));
        addItem(39, Item.of(Material.STAINED_GLASS_PANE, 7, "§7", ""));
        
        // Nível 2 - Slot 40
        int monthsNeeded2 = 3;
        int progress2 = Math.min(100, (months * 100) / monthsNeeded2);
        List<String> nivel2Lore = Arrays.asList(
            "", "§7Meses necessário:", "§a" + months + "/" + monthsNeeded2 + " §7(" + progress2 + "%)",
            "", "§7Exemplo:", "§b§lMAX+ §b" + playerName, "",
            account.getMaxPlusLevel() == 2 ? "§a§l✓ Selecionado" : (hasMaxPlusRank && months >= monthsNeeded2 ? "§aClique para selecionar" : "§cRequer Max+ e " + monthsNeeded2 + " meses")
        );
        Item nivel2 = Item.of(Material.DIAMOND, "§bTag nível 2", nivel2Lore.toArray(new String[0]));
        nivel2.click(event -> {
            if (hasMaxPlusRank && months >= monthsNeeded2) {
                account.setMaxPlusLevel(2);
                account.saveContext(account.getContext());
                getPlayer().sendMessage("§aNível de tag alterado para 2!");
                getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_PLING, 1.0f, 2.0f);
                TagManager.updateTag(account);
                handle();
            } else {
                getPlayer().sendMessage("§cRequer Max+ e " + monthsNeeded2 + " meses!");
                getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_BASS, 1.0f, 0.5f);
            }
        });
        addItem(40, nivel2);
        
        addItem(41, Item.of(Material.STAINED_GLASS_PANE, 7, "§7", ""));
        addItem(42, Item.of(Material.STAINED_GLASS_PANE, 7, "§7", ""));
        
        // Nível 3 - Slot 43
        int monthsNeeded3 = 6;
        int progress3 = Math.min(100, (months * 100) / monthsNeeded3);
        List<String> nivel3Lore = Arrays.asList(
            "", "§7Meses necessário:", "§a" + months + "/" + monthsNeeded3 + " §7(" + progress3 + "%)",
            "", "§7Exemplo:", "§6§lMAX+ §6" + playerName, "",
            account.getMaxPlusLevel() == 3 ? "§a§l✓ Selecionado" : (hasMaxPlusRank && months >= monthsNeeded3 ? "§aClique para selecionar" : "§cRequer Max+ e " + monthsNeeded3 + " meses")
        );
        Item nivel3 = Item.of(Material.GOLD_INGOT, "§6Tag nível 3", nivel3Lore.toArray(new String[0]));
        nivel3.click(event -> {
            if (hasMaxPlusRank && months >= monthsNeeded3) {
                account.setMaxPlusLevel(3);
                account.saveContext(account.getContext());
                getPlayer().sendMessage("§aNível de tag alterado para 3!");
                getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_PLING, 1.0f, 2.0f);
                TagManager.updateTag(account);
                handle();
            } else {
                getPlayer().sendMessage("§cRequer Max+ e " + monthsNeeded3 + " meses!");
                getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_BASS, 1.0f, 0.5f);
            }
        });
        addItem(43, nivel3);
        
        display();
    }
    
    private int getMaxPlusMonths() {
        if (!account.hasRank(RankType.MAX_PLUS)) return 0;
        return 6;
    }
    
    private int getColorIndex(char colorChar) {
        for (int i = 0; i < colors.length; i++) {
            if (colors[i].getChar() == colorChar) return i;
        }
        return 0;
    }
}
