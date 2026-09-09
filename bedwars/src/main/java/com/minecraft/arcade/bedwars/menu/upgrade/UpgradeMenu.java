package com.minecraft.arcade.bedwars.menu.upgrade;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.menu.ability.AbilityMenu;
import com.minecraft.arcade.bedwars.menu.upgrade.trap.TrapMenu;
import com.minecraft.arcade.bedwars.structure.generator.objects.level.GeneratorLevel;
import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.arcade.bedwars.structure.team.objects.forge.Forge;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.TeamUpgrade;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.armor.ArmorProtection;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.haste.Haste;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.sword.Sword;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.trap.Trap;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.trap.enums.TrapCategory;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.trap.enums.TrapType;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.Core;
import com.minecraft.core.member.list.bedwars.objects.enums.BedOre;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public class UpgradeMenu extends Menu {

    private final User user;

    private final Arena arena;

    private final boolean isTrioOrQuartet;

    public UpgradeMenu(Player player) {
        super(player, "Melhorias", 4);

        this.user = (User) User.of(player.getUniqueId());

        this.arena = user.getArena();

        this.isTrioOrQuartet = arena.getSlot().ordinal() >= Slot.TRIO.ordinal();
    }

    @Override
    public void handle() {
        clear();

        Team team = user.getTeam();

        addItem(10, buildForge(team));
        addItem(11, buildSword(team));
        addItem(12, buildHaste(team));
        addItem(13, buildArmor(team));
        addItem(14, buildAutoRegeneration(team));
        addItem(16, buildAbilitiesAccess()); // Novo: Acesso ao menu de habilidades

        // Adicionar armadilhas diretamente no menu
        addItem(28, buildTrapItem(team, TrapType.BLACKOUT));
        addItem(29, buildTrapItem(team, TrapType.FATIGUE));
        addItem(30, buildTrapItem(team, TrapType.CURSE));

        addCloseButton();

        display();
    }

    protected Item buildSword(Team team) {
        Item icon = Item.of(Material.DIAMOND_SWORD)
                .flags(ItemFlag.values());

        Sword sword = team.getSword();

        if (sword != null && !icon.hasEnchantments())
            icon.enchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Torne as suas espadas",
                "§7mais poderosas.",
                ""
        ));

        for (Sword swordLevel : Sword.values()) {
            if (isTrioOrQuartet && swordLevel.equals(Sword.THREE)) continue;

            boolean has = sword != null && sword.ordinal() >= swordLevel.ordinal();

            lore.add((has ? "§a✓" : "§c྾") + " §7Nível " + (swordLevel.ordinal() + 1) + ": §7" + swordLevel.getName()
                    + ", §b" + swordLevel.getAdjustedPrice(arena) + " diamantes");
        }

        lore.add("");
        lore.add(team.isHighestSword()
                ? "§cNível máximo."
                : team.canBuySword(getPlayer()) ? "§eClique para evoluir!"
                : "§cVocê não tem diamantes suficientes.");
        
        if (sword != null) {
            lore.add("");
            lore.add("§eShift + Clique para devolver!");
        }

        icon.name((team.canBuySword(getPlayer()) ? "§a" : "§c") + "Afiação")
                .lore(lore)
                .click(event -> {
                    // Sistema de devolução com Shift+Click
                    if (event.isShiftClick() && sword != null) {
                        sound(MenuSound.SUCCESS);
                        
                        int refund = sword.getAdjustedPrice(arena);
                        getPlayer().getInventory().addItem(new Item(Material.DIAMOND, refund));
                        
                        // Voltar para o nível anterior
                        Sword previous = sword.ordinal() > 0 ? Sword.values()[sword.ordinal() - 1] : null;
                        team.getUpgrade().setSword(previous);
                        
                        team.getPlayers().forEach(player -> {
                            player.sendMessage(team.getColor() + getPlayer().getName() 
                                    + "§e devolveu o upgrade de espada! §b+" + refund + " diamantes");
                            
                            Core.getPlatform().runSync(() -> {
                                // Remover encantamento antigo e aplicar o novo (ou remover se null)
                                ItemStack swordItem = player.getInventory().getItem(0);
                                if (swordItem != null && swordItem.getType().name().contains("SWORD")) {
                                    swordItem.removeEnchantment(Enchantment.DAMAGE_ALL);
                                    if (previous != null) {
                                        previous.applyEnchantment(player);
                                    }
                                }
                                player.updateInventory();
                            }, 1L);
                        });
                        
                        handle();
                        return;
                    }
                    if (isTrioOrQuartet && sword != null && sword.equals(Sword.TWO) || team.isHighestSword()) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cA sua espada está no nível máximo.");

                        return;
                    }

                    Sword next = team.getNextSword();

                    if (!team.canBuySword(getPlayer())) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem diamantes suficientes. " +
                                "Faltam " + user.getRemainingOre(BedOre.DIAMOND, next.getAdjustedPrice(arena)) + " diamantes.");

                        return;
                    }

                    sound(MenuSound.SUCCESS);

                    user.removeOre(BedOre.DIAMOND, next.getAdjustedPrice(arena));

                    team.getUpgrade().setSword(next);

                    // Aplica para comprador
                    user.getTeam().getSword().applyEnchantment(user.getPlayer());
                    user.getPlayer().sendMessage(team.getColor()
                            + getPlayer().getName() + "§e evoluiu as espadas para o nível §c" + next.getTag() + "§e!");

                    // Aplica para o time inteiro
                    team.getPlayers().forEach(player -> {
                        Core.getLogger().info("Jogadores do time: " + player);
                        player.sendMessage(team.getColor()
                                + getPlayer().getName() + "§e evoluiu as espadas para o nível §c" + next.getTag() + "§e!");

                        team.getSword().applyEnchantment(player);
                    });

                    handle();
                });

        return icon;
    }

    protected Item buildHaste(Team team) {
        Item icon = Item.of(Material.GOLD_PICKAXE)
                .flags(ItemFlag.values());

        Haste haste = team.getHaste();

        if (haste != null && !icon.hasEnchantments())
            icon.enchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Torne as suas picaretas",
                "§7mais ágeis.",
                ""
        ));

        for (Haste hasteLevel : Haste.values()) {
            boolean has = haste != null && haste.ordinal() >= hasteLevel.ordinal();

            lore.add((has ? "§a✓" : "§c྾") + " §7Nível " + (hasteLevel.ordinal() + 1) + ": §7" + hasteLevel.getName()
                    + ", §b" + hasteLevel.getAdjustedPrice(arena) + " diamantes");
        }

        lore.add("");
        lore.add(team.isHighestHaste()
                ? "§cNível máximo."
                : team.canBuyHaste(getPlayer()) ? "§eClique para evoluir!"
                : "§cVocê não tem diamantes suficientes.");
        
        if (haste != null) {
            lore.add("");
            lore.add("§eShift + Clique para devolver!");
        }

        icon.name((team.canBuyHaste(getPlayer()) ? "§a" : "§c") + "Agilidade")
                .lore(lore)
                .click(event -> {
                    // Sistema de devolução com Shift+Click
                    if (event.isShiftClick() && haste != null) {
                        sound(MenuSound.SUCCESS);
                        
                        int refund = haste.getAdjustedPrice(arena);
                        getPlayer().getInventory().addItem(new Item(Material.DIAMOND, refund));
                        
                        // Voltar para o nível anterior
                        Haste previous = haste.ordinal() > 0 ? Haste.values()[haste.ordinal() - 1] : null;
                        team.getUpgrade().setHaste(previous);
                        
                        team.getPlayers().forEach(player -> {
                            player.sendMessage(team.getColor() + getPlayer().getName() 
                                    + "§e devolveu o upgrade de agilidade! §b+" + refund + " diamantes");
                            
                            Core.getPlatform().runSync(() -> {
                                // Remover encantamento de todas as ferramentas
                                for (ItemStack item : player.getInventory().getContents()) {
                                    if (item != null && (item.getType().name().contains("PICKAXE") || item.getType().name().contains("AXE"))) {
                                        item.removeEnchantment(Enchantment.DIG_SPEED);
                                    }
                                }
                                
                                // Aplicar novo encantamento (se tiver)
                                if (previous != null) {
                                    previous.applyEnchantment(player);
                                }
                                
                                player.updateInventory();
                            }, 1L);
                        });
                        
                        handle();
                        return;
                    }

                    if (team.isHighestHaste()) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cA sua picareta está no nível máximo.");

                        return;
                    }

                    Haste next = team.getNextHaste();

                    if (!team.canBuyHaste(getPlayer())) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem diamantes suficientes. " +
                                "Faltam " + user.getRemainingOre(BedOre.DIAMOND, next.getAdjustedPrice(arena)) + " diamantes.");

                        return;
                    }

                    sound(MenuSound.SUCCESS);

                    user.removeOre(BedOre.DIAMOND, next.getAdjustedPrice(arena));




                    team.getUpgrade().setHaste(next);

                    team.getPlayers().forEach(player -> {
                        team.getHaste().applyEnchantment(player);
                        player.sendMessage(team.getColor()
                                + getPlayer().getName() + "§e evoluiu as picaretas para o nível §c" + next.getTag() + "§e!");
                    });

                    user.getTeam().getHaste().applyEnchantment(user.getPlayer());
                    user.getPlayer().sendMessage(team.getColor()
                            + getPlayer().getName() + "§e evoluiu as picaretas para o nível §c" + next.getTag() + "§e!");

                });

        return icon;
    }

    protected Item buildArmor(Team team) {
        Item icon = Item.of(Material.IRON_BOOTS)
                .flags(ItemFlag.values());

        ArmorProtection armor = team.getArmor();

        if (armor != null && !icon.hasEnchantments())
            icon.enchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Torne as suas armaduras",
                "§7mais resistentes.",
                ""
        ));

        for (ArmorProtection armorLevel : ArmorProtection.values()) {
            if (armorLevel.equals(ArmorProtection.NONE)) continue;

            boolean has = armor != null && armor.ordinal() >= armorLevel.ordinal();

            lore.add((has ? "§a✓" : "§c྾") + " §7Nível " + armorLevel.ordinal() + ": §7" + armorLevel.getName()
                    + ", §b" + armorLevel.getAdjustedPrice(arena) + " diamantes");
        }

        lore.add("");
        lore.add(team.isHighestArmor()
                ? "§cNível máximo."
                : team.canBuyArmor(getPlayer()) ? "§eClique para evoluir!"
                : "§cVocê não tem diamantes suficientes.");
        
        if (armor != null && !armor.equals(ArmorProtection.NONE)) {
            lore.add("");
            lore.add("§eShift + Clique para devolver!");
        }

        icon.name((team.canBuyArmor(getPlayer()) ? "§a" : "§c") + "Armaduras")
                .lore(lore)
                .click(event -> {
                    // Sistema de devolução com Shift+Click
                    if (event.isShiftClick() && armor != null && !armor.equals(ArmorProtection.NONE)) {
                        sound(MenuSound.SUCCESS);
                        
                        int refund = armor.getAdjustedPrice(arena);
                        getPlayer().getInventory().addItem(new Item(Material.DIAMOND, refund));
                        
                        // Voltar para o nível anterior
                        ArmorProtection previous = armor.ordinal() > 1 ? ArmorProtection.values()[armor.ordinal() - 1] : ArmorProtection.NONE;
                        team.getUpgrade().setArmor(previous);
                        
                        team.getPlayers().forEach(player -> {
                            player.sendMessage(team.getColor() + getPlayer().getName() 
                                    + "§e devolveu o upgrade de armadura! §b+" + refund + " diamantes");
                            Core.getPlatform().runSync(() -> {
                                // Remover encantamento de proteção de todas as peças de armadura
                                ItemStack[] armorContents = player.getInventory().getArmorContents();
                                for (int i = 0; i < armorContents.length; i++) {
                                    ItemStack item = armorContents[i];
                                    if (item != null && (item.getType().name().contains("LEGGINGS") || 
                                        item.getType().name().contains("BOOTS") ||
                                        item.getType().name().contains("CHESTPLATE") ||
                                        item.getType().name().contains("HELMET"))) {
                                        item.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
                                        armorContents[i] = item;
                                    }
                                }
                                player.getInventory().setArmorContents(armorContents);
                                
                                // Aplicar encantamento anterior (se não for NONE)
                                if (!previous.equals(ArmorProtection.NONE)) {
                                    previous.applyEnchantment(player);
                                }
                                
                                player.updateInventory();
                            }, 2L);
                        });
                        
                        handle();
                        return;
                    }

                    if (team.isHighestArmor()) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cA sua armadura está no nível máximo.");

                        return;
                    }

                    ArmorProtection next = team.getNextArmor();

                    if (!team.canBuyArmor(getPlayer())) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem diamantes suficientes. " +
                                "Faltam " + user.getRemainingOre(BedOre.DIAMOND, next.getAdjustedPrice(arena)) + " diamantes.");

                        return;
                    }

                    sound(MenuSound.SUCCESS);

                    user.removeOre(BedOre.DIAMOND, next.getAdjustedPrice(arena));




                    team.getUpgrade().setArmor(next);

                    team.getPlayers().forEach(player -> {
                        player.sendMessage(team.getColor()
                                + getPlayer().getName() + "§e evoluiu as armaduras para o nível §c" + next.getTag() + "§e!");

                        // Aplicar encantamento de forma síncrona após um pequeno delay
                        Core.getPlatform().runSync(() -> {
                            team.getArmor().applyEnchantment(player);
//                            next.applyEnchantment(player);
                        }, 2L);
                    });

                    user.getTeam().getArmor().applyEnchantment(user.getPlayer());
                    user.getPlayer().sendMessage(team.getColor()
                            + getPlayer().getName() + "§e evoluiu as armaduras para o nível §c" + next.getTag() + "§e!");

                    handle();
                });

        return icon;
    }

    protected Item buildAutoRegeneration(Team team) {
        Item icon = Item.of(Material.BEACON)
                .flags(ItemFlag.values());

        TeamUpgrade upgrade = team.getUpgrade();

        final int cost = 1;
        final boolean buy = BukkitUtil.getItemAmount(getPlayer(), Material.DIAMOND) >= cost;

        if (upgrade.isAutoRegeneration())
            icon.unsafeEnchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Invoque um campo de regeneração",
                "§7em volta da sua ilha.",
                ""
        ));

        if (upgrade.isAutoRegeneration())
            lore.add("§cCampo em ação.");
        else
            lore.addAll(Arrays.asList(
                    "§7Custo: §b" + cost + " diamante",
                    "",
                    buy ? "§eClique para adquirir!" : "§cVocê não tem diamantes suficientes."));

        icon.name((buy ? "§a" : "§c") + "Regeneração na ilha")
                .lore(lore)
                .click(event -> {
                    if (upgrade.isAutoRegeneration()) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cO campo de regeneração já foi ativado.");

                        return;
                    }

                    if (!buy) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem diamantes suficientes. " +
                                "Faltam " + user.getRemainingOre(BedOre.DIAMOND, cost) + " diamante.");

                        return;
                    }

                    sound(MenuSound.SUCCESS);

                    upgrade.setAutoRegeneration(true);
                    team.setUpgrade(upgrade);

                    user.removeOre(BedOre.DIAMOND, cost);

                    team.getPlayers().forEach(player -> player.sendMessage(team.getColor()
                            + getPlayer().getName() + "§e ativou o campo de regeneração."));

                    handle();
                });

        return icon;
    }

    protected Item buildForge(Team team) {
        Item icon = Item.of(Material.FURNACE)
                .flags(ItemFlag.values());

        Forge forge = team.getForge();

        if (forge != null && !forge.getLevel().equals(GeneratorLevel.NONE) && !icon.hasEnchantments())
            icon.enchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Melhore a produção de recursos",
                "§7da sua forja.",
                ""
        ));

        for (GeneratorLevel level : GeneratorLevel.values()) {
            if (level.equals(GeneratorLevel.NONE)) continue;

            boolean has = forge != null && forge.getLevel().ordinal() >= level.ordinal();

            lore.add((has ? "§a✓" : "§c྾") + " §7Nível " + level.ordinal() + ": " + level
                    + ", §b" + (arena.getSlot().ordinal() >= Slot.TRIO.ordinal() ? level.getCost() * 2 : level.getCost()) + " diamantes");
        }

        lore.add("");
        lore.add(forge != null && forge.isHighest()
                ? "§cNível máximo."
                : forge != null && forge.canPurchase(getPlayer()) ? "§eClique para evoluir!"
                : "§cVocê não tem diamantes suficientes.");

        icon.name((forge != null && forge.canPurchase(getPlayer()) ? "§a" : "§c") + "Forja")
                .lore(lore)
                .click(event -> {
                    if (forge == null) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cA forja não está disponível.");
                        return;
                    }

                    if (forge.isHighest()) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cA forja está no nível máximo.");
                        return;
                    }

                    if (!forge.canPurchase(getPlayer())) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem diamantes suficientes. " +
                                "Faltam " + user.getRemainingOre(BedOre.DIAMOND, forge.getNextCost()) + " diamantes.");
                        return;
                    }

                    sound(MenuSound.SUCCESS);

                    user.removeOre(BedOre.DIAMOND, forge.getNextCost());

                    forge.upgrade();

                    team.getPlayers().forEach(player -> player.sendMessage(team.getColor()
                            + getPlayer().getName() + "§e evoluiu a forja para o nível §c" + forge.getLevel().ordinal() + "§e!"));

                    handle();
                });

        return icon;
    }

    protected Item buildAbilitiesAccess() {
        Item icon = Item.of(Material.NETHER_STAR)
                .flags(ItemFlag.values())
                .enchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Compre habilidades especiais",
                "§7usando moedas ganhas na partida!",
                "",
                "§7Suas moedas: §e" + user.getCoins(),
                "",
                "§eClique para abrir!"
        ));

        icon.name("§6§lHabilidades")
                .lore(lore)
                .click(event -> {
                    sound(MenuSound.DONE);
                    new AbilityMenu(getPlayer()).handle();
                });

        return icon;
    }

    protected Item buildTrapItem(Team team, TrapType type) {
        Item icon = type.getIcon().clone()
                .flags(ItemFlag.values());

        List<String> description = new ArrayList<>(type.getDescription());

        boolean has = team.getTraps().stream().anyMatch(trap -> trap.getType().equals(type));
        boolean canPurchase = type.canPurchase(getPlayer());

        description.addAll(Arrays.asList(
                "",
                "§7Custo: §b" + type.getCost() + " diamante" + (type.getCost() > 1 ? "s" : ""),
                "",
                has ? "§cArmadilha adquirida." : canPurchase
                        ? "§eClique para comprar!"
                        : "§cVocê não tem diamantes suficientes."
        ));

        icon.name(((has || !canPurchase) ? "§c" : "§a") + type.getName());
        icon.lore(description);

        icon.click(event -> {
            int totalTraps = team.getTraps().size();

            if (totalTraps >= 3) {
                sound(MenuSound.ERROR);
                getPlayer().sendMessage("§cVocê já atingiu o limite de armadilhas.");
                return;
            }

            if (has) {
                sound(MenuSound.ERROR);
                getPlayer().sendMessage("§cVocê já adquiriu a armadilha " + type.getName() + ".");
                return;
            }

            if (!canPurchase) {
                sound(MenuSound.ERROR);
                getPlayer().sendMessage("§cVocê não tem diamantes suficientes. " +
                        "Faltam " + user.getRemainingOre(type.getOre(), type.getCost()) + " diamantes.");
                return;
            }

            sound(MenuSound.DONE);

            user.removeOre(type.getOre(), type.getCost());

            team.getTraps().add(new Trap(type, TrapCategory.of(totalTraps)));

            team.getPlayers().forEach(player -> player.sendMessage(team.getColor()
                    + getPlayer().getName() + "§e adquiriu §6Armadilha " + type.getName()));

            handle();
        });

        return icon;
    }
}
