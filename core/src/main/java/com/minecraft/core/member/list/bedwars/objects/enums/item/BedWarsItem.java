package com.minecraft.core.member.list.bedwars.objects.enums.item;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.member.list.bedwars.objects.enums.BedOre;
import com.minecraft.core.member.list.bedwars.objects.enums.BedShop;
import com.minecraft.core.member.list.bedwars.objects.enums.item.flag.BedItemFlag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Getter
@AllArgsConstructor
public enum BedWarsItem {

    /* Blocks Item List */
    BLOCKS_WOOL("Lã", BedShop.BLOCKS, BedOre.IRON, new Item(Material.WOOL, 16), 4,
            asList("§7Um bloco útil para fazer pontes",
                    "§7entre as ilhas. Ficará da",
                    "§7cor do seu time.")),
    BLOCKS_WOOD("Madeira", BedShop.BLOCKS, BedOre.GOLD, new Item(Material.WOOD, 16), 4, asList(
            "§7Útil para defender sua cama,",
            "§7resistente contra picaretas."
    )),
    BLOCK_CLAY("Argila", BedShop.BLOCKS, BedOre.IRON, new Item(Material.STAINED_CLAY, 16), 12, asList(
            "§7Bloco sólido o suficiente",
            "§7para proteger sua cama."
    )),
    BLOCKS_GLASS("Vidro à prova de explosões", BedShop.BLOCKS, BedOre.IRON, new Item(Material.STAINED_GLASS, 4), 12,
            singletonList(Type.CASUAL), asList(
            "§7Proteja-se contra qualquer",
            "§7explosão sofrida."
    )),
    BLOCKS_END("Blocos do Fim", BedShop.BLOCKS, BedOre.IRON, new Item(Material.ENDER_STONE, 12), 24, asList(
            "§7Um pouco mais duro que a Argila",
            "§7protege um pouco de explosões."
    )),
    BLOCKS_LADDER("Escadas", BedShop.BLOCKS, BedOre.IRON, new Item(Material.LADDER, 16), 4,
            singletonList("§7Evite levar danos desnecessários.")),
    BLOCKS_OBSIDIAN("Obsidiana", BedShop.BLOCKS, BedOre.EMERALD, new Item(Material.OBSIDIAN, 4), 4,
            singletonList(Type.CASUAL), singletonList("§7Obtenha uma proteção máxima na sua cama.")),

    /* Combat Item List */
    COMBAT_STONE_SWORD("Espada de Pedra", BedShop.COMBAT, BedOre.IRON, Item.of(Material.STONE_SWORD), 10),
    COMBAT_IRON_SWORD("Espada de Ferro", BedShop.COMBAT, BedOre.GOLD, Item.of(Material.IRON_SWORD), 7),
    COMBAT_DIAMOND_SWORD("Espada de Diamante", BedShop.COMBAT, BedOre.EMERALD, Item.of(Material.DIAMOND_SWORD), 4),
    COMBAT_STICK("Graveto", BedShop.COMBAT, BedOre.GOLD, Item.of(Material.STICK).enchantment(Enchantment.KNOCKBACK, 1), 5),

    /* Armor Item List */
    ARMOR_CHAINMAIL("Armadura de Malha", BedShop.ARMOR, BedOre.IRON, Item.of(Material.CHAINMAIL_BOOTS), 20,
            singletonList("§7Você não perde ao morrer.")),
    ARMOR_IRON("Armadura de Ferro", BedShop.ARMOR, BedOre.GOLD, Item.of(Material.IRON_BOOTS), 12,
            singletonList("§7Você não perde ao morrer.")),
    ARMOR_DIAMOND("Armadura de Diamante", BedShop.ARMOR, BedOre.EMERALD, Item.of(Material.DIAMOND_BOOTS), 6,
            singletonList("§7Você não perde ao morrer.")),

    /* Tools Item List */
    TOOLS_SHEARS("Tesoura", BedShop.TOOLS, BedOre.IRON, Item.of(Material.SHEARS),
            20, singletonList("§7Você não perde ao morrer.")),
    TOOLS_WOOD_PICKAXE("Picareta de Madeira", BedShop.TOOLS, BedOre.IRON, Item.of(Material.WOOD_PICKAXE).enchantment(Enchantment.DIG_SPEED, 1),
            10, singletonList("§7Ao morrer sua picareta sofrerá um downgrade.")),
    TOOLS_IRON_PICKAXE("Picareta de Ferro", BedShop.TOOLS, BedOre.IRON, Item.of(Material.IRON_PICKAXE).enchantment(Enchantment.DIG_SPEED, 2),
            20, singletonList("§7Ao morrer sua picareta sofrerá um downgrade.")),
    TOOLS_GOLD_PICKAXE("Picareta de Ouro", BedShop.TOOLS, BedOre.GOLD, Item.of(Material.GOLD_PICKAXE).enchantment(Enchantment.DIG_SPEED, 3),
            8, singletonList("§7Ao morrer sua picareta sofrerá um downgrade.")),
    TOOLS_DIAMOND_PICKAXE("Picareta de Diamante", BedShop.TOOLS, BedOre.GOLD, Item.of(Material.DIAMOND_PICKAXE).enchantment(Enchantment.DIG_SPEED, 3),
            16, singletonList("§7Ao morrer sua picareta sofrerá um downgrade.")),
    TOOLS_WOOD_AXE("Machado de Madeira", BedShop.TOOLS, BedOre.IRON, Item.of(Material.WOOD_AXE).enchantment(Enchantment.DIG_SPEED, 1),
            10, singletonList("§7Ao morrer seu machado sofrerá um downgrade.")),
    TOOLS_IRON_AXE("Machado de Ferro", BedShop.TOOLS, BedOre.IRON, Item.of(Material.IRON_AXE).enchantment(Enchantment.DIG_SPEED, 2),
            20, singletonList("§7Ao morrer seu machado sofrerá um downgrade.")),
    TOOLS_GOLD_AXE("Machado de Ouro", BedShop.TOOLS, BedOre.GOLD, Item.of(Material.GOLD_AXE).enchantment(Enchantment.DIG_SPEED, 3),
            8, singletonList("§7Ao morrer seu machado sofrerá um downgrade.")),
    TOOLS_DIAMOND_AXE("Machado de Diamante", BedShop.TOOLS, BedOre.GOLD, Item.of(Material.DIAMOND_AXE).enchantment(Enchantment.DIG_SPEED, 3),
            16, singletonList("§7Ao morrer seu machado sofrerá um downgrade.")),

    /* Artillery Item List */
    ARTILLERY_ARROW("Flechas", BedShop.ARTILLERY, BedOre.GOLD, new Item(Material.ARROW, 8), 2, singletonList(Type.CASUAL), new ArrayList<>()),
    ARTILLERY_BOW("Arco", BedShop.ARTILLERY, BedOre.GOLD, Item.of(Material.BOW), 12, singletonList(Type.CASUAL), new ArrayList<>()),
    ARTILLERY_BOW_STRENGTH("Arco (Força I)", BedShop.ARTILLERY, BedOre.GOLD, Item.of(Material.BOW)
            .enchantment(Enchantment.ARROW_DAMAGE, 1), 24, singletonList(Type.CASUAL), new ArrayList<>()), // adicionar força I
    ARTILLERY_BOW_STRENGTH_IMPACT("Arco (Força e Impacto I)", BedShop.ARTILLERY, BedOre.EMERALD, Item.of(Material.BOW)
            .enchantment(Enchantment.ARROW_DAMAGE, 1)
            .enchantment(Enchantment.ARROW_KNOCKBACK, 1), 6, singletonList(Type.CASUAL), new ArrayList<>()),

    /* Potions Item List */
    POTIONS_AGILITY("Poção Agilidade II", BedShop.POTION, BedOre.EMERALD, new Item(Material.POTION, 1, 2)
            .name("§ePoção Agilidade II (45 segundos)")
            .flags(ItemFlag.values())
            .lore(new ArrayList<>()), 1, singletonList("§745 segundos ágil.")),
    POTIONS_SUPER_JUMP("Poção Super Pulo V", BedShop.POTION, BedOre.EMERALD, new Item(Material.POTION, 1, 11)
            .name("§aPoção Super Pulo V (45 segundos)")
            .flags(ItemFlag.values())
            .lore(new ArrayList<>()), 2, singletonList("§745 segundos pulando alto.")),
    POTIONS_INVISIBILITY("Poção Invisibilidade", BedShop.POTION, BedOre.EMERALD, new Item(Material.POTION, 1, 14)
            .name("§bPoção Invisibilidade (30 segundos)")
            .flags(ItemFlag.values())
            .lore(new ArrayList<>()), 2, singletonList("§730 segundos invisível.")),
    POTIONS_SLOWNESS("Poção Lentidão Arremessável", BedShop.POTION, BedOre.EMERALD, new Item(Material.POTION, 1, 16394)
            .name("§6Poção Lentidão II (5 segundos)")
            .flags(ItemFlag.values())
            .lore(new ArrayList<>()), 1, singletonList("§75 segundos lento")),

    /* Util Item List */
    UTIL_GOLD_APPLE("Maçã Dourada", BedShop.UTIL, BedOre.GOLD, Item.of(Material.GOLDEN_APPLE), 3, new ArrayList<>()),
    UTIL_MOTH_BALL("Bolinha de traça", BedShop.UTIL, BedOre.IRON, Item.of(Material.SNOW_BALL, "§eBolinha de traça"), 40, asList(
            "§7Invoque 3 traças onde a Bola",
            "§7de Neve cair. Dura 15 segundos.")),
    UTIL_FIREBALL("Fireball", BedShop.UTIL, BedOre.IRON, Item.of(Material.FIREBALL, "§cBola de Fogo"), 40,
            singletonList(Type.CASUAL), new ArrayList<>()),
    UTIL_SWITCHER("Switcher", BedShop.UTIL, BedOre.EMERALD, Item.of(Material.SNOW_BALL, "§eSwitcher"), 3, asList(
            "§7Use 3 bolhas de neve.",
            "§7Ao atingir um jogador,",
            "§7troque de lugar com ele!")),
    UTIL_SLINGSHOT("Estilingue", BedShop.UTIL, BedOre.GOLD, Item.of(Material.MAGMA_CREAM, "§eEstilingue"), 7,
            singletonList(Type.CASUAL), new ArrayList<>()),
    UTIL_DEFENSE("Defesa dos Sonhos", BedShop.UTIL, BedOre.IRON, Item.of(Material.MONSTER_EGG, "§eDefesa dos Sonhos"), 90, asList(
            "§7Invoque um Golem de Ferro para",
            "§7proteger sua base. Dura 4 minutos.")),
    UTIL_TNT("TNT", BedShop.UTIL, BedOre.GOLD, Item.of(Material.TNT, "§cExplosivo"), 4,
            asList("§7Uma TNT instantânea, exploda",
                    "§7tudo pela frente.")),
    UTIL_PEARL("Ender Pearl", BedShop.UTIL, BedOre.EMERALD, Item.of(Material.ENDER_PEARL), 2, new ArrayList<>()),
    UTIL_WATER_BUCKET("Balde de Água", BedShop.UTIL, BedOre.GOLD, Item.of(Material.WATER_BUCKET), 3,
            singletonList("§7Proteja contra TNT's")),
    UTIL_MILK_BUCKET("Leitinho Mágico", BedShop.UTIL, BedOre.GOLD, Item.of(Material.MILK_BUCKET, "§dLeite Mágico"), 2,
            asList("§7Fique imune às armadilhas durante",
                    "§730 segundos.")),
    UTIL_JUMPER("Jumper", BedShop.UTIL, BedOre.EMERALD, Item.of(Material.SLIME_BLOCK, "§aJumper"), 2,
            singletonList(Type.CASUAL), asList(
            "§7Use os slimes para pular",
            "§7o mais alto possível.")),
    UTIL_SPONGE("Esponja", BedShop.UTIL, BedOre.GOLD, new Item(Material.SPONGE, 4), 1, asList(
            "§7Seque toda água onde o bloco",
            "§7for colocado.")),
    UTIL_EGG("Ovo de Pontes", BedShop.UTIL, BedOre.EMERALD, Item.of(Material.EGG, "§6Ovo de Pontes"), 1, asList(
            "§7Faça pontes automaticamente!",
            "§7irado né?")),
    UTIL_TRACKING("Rastreador", BedShop.UTIL, BedOre.EMERALD, Item.of(Material.COMPASS, "§cRastreador"), 2,
            List.of(Type.CASUAL),
            asList("§7Localize os times inimigos.", "§7Uso único."),
            singletonList(BedItemFlag.CAN_BUY_AFTER_BED_BREAK)),
    LOJA_PORTATIL("Loja portátil", BedShop.UTIL, BedOre.EMERALD, Item.of(Material.BOOK, "§aLoja portátil"), 4, asList(
            "§7Abra a loja em qualquer",
                    "§7lugar!")),
    
    /* Rotativos Item List */
    ROTATIVOS_BLINK("Blink", BedShop.ROTATIVOS, BedOre.GOLD, Item.of(Material.NETHER_STAR, "§dBlink"), 3, asList(
            "§7Crie uma folha 5 blocos acima",
            "§7de você!",
            "§7Cooldown: §e10 segundos")),
    ROTATIVOS_SNOWBALL_SWAP("Bola de Neve Swap", BedShop.ROTATIVOS, BedOre.EMERALD, Item.of(Material.CLAY_BALL, "§fBola de Neve §bSwap"), 5, asList(
            "§7Jogue uma bola de neve",
            "§7que troca de lugar com",
            "§7o jogador acertado!",
            "§7Cooldown: §e15 segundos"));

    private final String name;

    private final BedShop shop;
    private final BedOre ore;

    private final ItemStack stack;

    private final int price;

    private final List<Type> types;
    private final List<String> lore;

    private final List<BedItemFlag> flags;

    BedWarsItem(String name, BedShop shop, BedOre ore, ItemStack stack, int price) {
        this(name, shop, ore, stack, price, List.of(Type.CASUAL), new ArrayList<>(), new ArrayList<>());
    }

    BedWarsItem(String name, BedShop shop, BedOre ore, ItemStack stack, int price, List<String> lore) {
        this(name, shop, ore, stack, price, List.of(Type.CASUAL), lore, new ArrayList<>());
    }

    BedWarsItem(String name, BedShop shop, BedOre ore, ItemStack stack, int price, List<Type> types, List<String> lore) {
        this(name, shop, ore, stack, price, types, lore, new ArrayList<>());
    }

    public static List<BedWarsItem> list() {
        return new ArrayList<>(asList(values()));
    }

    public static List<BedWarsItem> list(BedShop shop) {
        return list().stream().filter(item -> item.getShop().equals(shop)).collect(Collectors.toList());
    }

    public static BedWarsItem of(String name) {
        return of(item -> item.name().equalsIgnoreCase(name) || item.getName().equalsIgnoreCase(name));
    }

    public static BedWarsItem of(Predicate<BedWarsItem> filter) {
        return list().stream().filter(filter).findFirst().orElse(null);
    }

    public boolean hasFlag(BedItemFlag flag) {
        return this.flags.contains(flag);
    }

    public boolean isAvailableForMode(Type type) {
        if (type.equals(Type.CUSTOM)) return true;

        return types.contains(type);
    }

    public boolean isType(BedShop type) {
        return this.shop == type;
    }

    public boolean isColored() {
        return isType(BedShop.BLOCKS) && (this == BLOCKS_WOOL || this == BLOCKS_GLASS || this == BLOCK_CLAY);
    }

    public boolean isToolPickaxe() {
        String name = stack.getType().name();

        return shop == BedShop.TOOLS && name.endsWith("_PICKAXE");
    }

    public boolean isToolAxe() {
        String name = stack.getType().name();

        return shop == BedShop.TOOLS && name.endsWith("_AXE");
    }

    public boolean isInferior(BedWarsItem item) {
        if (item == null) return false;

        return ordinal() <= item.ordinal();
    }
}
