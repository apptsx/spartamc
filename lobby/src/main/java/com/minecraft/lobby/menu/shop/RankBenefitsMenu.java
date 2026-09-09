package com.minecraft.lobby.menu.shop;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Arrays;
import java.util.List;

public class RankBenefitsMenu extends Menu {

    private final String rankName;

    public RankBenefitsMenu(Player player, Menu last, String rankName) {
        super(player, "Beneficios: " + rankName, last, 3);
        this.rankName = rankName;
    }

    @Override
    public void handle() {
        clear();

        // Botão voltar
        addItem(26, Item.of(Material.ARROW, "§cVoltar")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    getLast().handle();
                }));

        // Livro escrito com beneficios
        addItem(13, createBookWithBenefits());

        display();
    }

    private Item createBookWithBenefits() {
        Item bookItem = Item.of(Material.WRITTEN_BOOK, "§aBeneficios do " + rankName);
        
        BookMeta bookMeta = (BookMeta) bookItem.getItemMeta();
        
        // Definir título e autor
        bookMeta.setTitle("§aBeneficios " + rankName);
        bookMeta.setAuthor("§6" + com.minecraft.core.Constant.SERVER_NAME);
        
        // Adicionar páginas
        List<String> pages = getBookPages();
        bookMeta.setPages(pages);
        
        bookItem.setItemMeta(bookMeta);
        
        return bookItem;
    }

    private List<String> getBookPages() {
        switch (rankName) {
            case "VIP":
                return Arrays.asList(
                    "§6§lBENEFICIOS VIP\n\n§8Permanente.\n\n§a✓ §7Voo no lobby;\n§a✓ §7Selecionar mapas;\n§a✓ §7Anuncio ao entrar;\n§a✓ §7Cosmeticos;\n§7E muito mais!",
                    
                    "§6§lINFORMACOES\n\n§7Ao comprar o rank VIP,\nvoce tem acesso vitalicio\na todos os beneficios\nlistados na pagina\nanterior.\n\n§a§lLoja: §ehttps://" + com.minecraft.core.Constant.SERVER_STORE
                );
                
            case "Max":
                return Arrays.asList(
                    "§6§lBENEFICIOS MAX\n\n§8Permanente.\n\n§a✓ §7Todos os beneficios VIP;\n§a✓ §7Acesso a kits exclusivos;\n§a✓ §7Tag §dMax;\n§a✓ §7Prioridade em entrar;\n§7E muito mais!",
                    
                    "§6§lDIFERENCIAIS\n\n§7Além dos beneficios VIP,\no rank Max oferece\nvantagens exclusivas\nem eventos e\nrecompensas diarias.\n\n§a§lLoja: §ehttps://" + com.minecraft.core.Constant.SERVER_STORE
                );
                
            case "Max+":
                return Arrays.asList(
                    "§6§lBENEFICIOS MAX+\n\n§530 dias.\n\n§a✓ §7Todos os beneficios Max;\n§a✓ §7Acesso a clans;\n§a✓ §7Tag §5Max§5+;\n§a✓ §7Descontos na loja;\n§7E muito mais!",
                    
                    "§6§lPROMOCAO\n\n§7O rank Max+ é temporario\nmas pode ser renovado\ncom descontos especiais!\n\n§7Acompanhe nossas\npromocoes no discord.\n\n§a§lLoja: §ehttps://" + com.minecraft.core.Constant.SERVER_STORE,
                    
                    "§6§lVANTAGENS EXTRAS\n\n§7• Suporte prioritario\n• Acesso a betas\n• Eventos exclusivos\n• Tag personalizada\n• Emotes especiais"
                );
                
            default:
                return Arrays.asList("§cBeneficios nao encontrados.");
        }
    }
}