package com.minecraft.arcade.bedwars.menu.ability;

import com.minecraft.core.member.list.bedwars.objects.ability.Ability;
import com.minecraft.core.member.list.bedwars.objects.ability.enums.AbilityType;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;

public class AbilityMenu extends Menu {

    private final User user;

    public AbilityMenu(Player player) {
        super(player, "Habilidades", 4);

        this.user = (User) User.of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        // Mostrar moedas do jogador
        addItem(4, buildCoinsDisplay());

        // Adicionar habilidades
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        AbilityType[] abilities = AbilityType.values();
        
        for (int i = 0; i < abilities.length && i < slots.length; i++) {
            addItem(slots[i], buildAbilityItem(abilities[i]));
        }

        addCloseButton();

        display();
    }

    protected Item buildCoinsDisplay() {
        Item icon = Item.of(Material.GOLD_INGOT)
                .flags(ItemFlag.values())
                .enchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>();
        lore.add("§7Moedas disponíveis: §e" + user.getCoins());
        lore.add("");
        lore.add("§7Ganhe moedas:");
        lore.add("§8• §7Kill: §e+10 moedas");
        lore.add("§8• §7Final Kill: §e+25 moedas");
        lore.add("§8• §7Quebrar cama: §e+50 moedas");
        lore.add("§8• §7Vitória: §e+100 moedas");

        icon.name("§6§lSuas Moedas")
                .lore(lore);

        return icon;
    }

    protected Item buildAbilityItem(AbilityType type) {
        Item icon = type.getIcon().clone()
                .flags(ItemFlag.values());

        String abilityName = type.name().toLowerCase();
        boolean hasAbilityInGame = user.getContext().hasAbility(type);
        boolean hasPurchasedPermanently = user.getMember() != null && user.getMember().hasPurchasedAbility(abilityName);
        boolean canPurchase = user.hasCoins(type.getCost());

        List<String> lore = new ArrayList<>(type.getDescription());
        lore.add("");
        lore.add("§7Custo: " + type.getCostFormatted());
        lore.add("");

        if (hasAbilityInGame) {
            lore.add("§a✓ Habilidade ativa nesta partida!");
            if (hasPurchasedPermanently) {
                lore.add("§7§o(Comprada permanentemente)");
            }
            icon.enchantment(Enchantment.DURABILITY, 1);
        } else if (hasPurchasedPermanently) {
            lore.add("§eVocê já comprou esta habilidade!");
            lore.add("§7Ela será ativada automaticamente");
            lore.add("§7na próxima partida.");
            lore.add("");
            lore.add("§eClique para ativar agora!");
        } else if (canPurchase) {
            lore.add("§eClique para comprar!");
            lore.add("§7§o(Compra permanente + ativa agora)");
        } else {
            int remaining = type.getCost() - user.getCoins();
            lore.add("§cFaltam §e" + remaining + " moedas§c.");
        }

        icon.name((hasAbilityInGame ? "§a" : hasPurchasedPermanently ? "§e" : canPurchase ? "§e" : "§c") + type.getName());
        icon.lore(lore);

        icon.click(event -> {
            if (hasAbilityInGame) {
                sound(MenuSound.ERROR);
                getPlayer().sendMessage("§cVocê já possui esta habilidade ativa nesta partida!");
                return;
            }

            if (hasPurchasedPermanently) {
                // Ativar habilidade já comprada
                sound(type.getPurchaseSound());
                user.getContext().addAbility(new Ability(type));
                getPlayer().sendMessage("§aVocê ativou a habilidade §e" + type.getName() + "§a!");
                applyAbilityEffect(type);
                handle();
                return;
            }

            if (!canPurchase) {
                sound(MenuSound.ERROR);
                int remaining = type.getCost() - user.getCoins();
                getPlayer().sendMessage("§cVocê não tem moedas suficientes! Faltam §e" + remaining + " moedas§c.");
                return;
            }

            sound(type.getPurchaseSound());

            user.removeCoins(type.getCost());
            
            // Salvar permanentemente no BedMember
            if (user.getMember() != null) {
                user.getMember().purchaseAbility(abilityName);
                user.getMember().selectAbility(abilityName);
            }
            
            // Ativar na partida atual
            user.getContext().addAbility(new Ability(type));

            getPlayer().sendMessage("§aVocê comprou permanentemente e ativou a habilidade §e" + type.getName() + "§a!");
            
            // Aplicar efeitos especiais da habilidade
            applyAbilityEffect(type);

            handle();
        });

        return icon;
    }

    private void applyAbilityEffect(AbilityType type) {
        switch (type) {
            case PERMANENT_SWORD:
                org.bukkit.inventory.ItemStack sword = getPlayer().getInventory().getItem(0);
                if (sword != null && sword.getType().name().contains("SWORD")) {
                    user.getContext().setPermanentSword(sword.clone());
                }
                break;
                
            default:
                break;
        }
    }
}

