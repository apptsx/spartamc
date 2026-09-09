package com.minecraft.core.member.list.eggwars.objects.enums.item;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.member.list.eggwars.objects.enums.EggOre;
import com.minecraft.core.member.list.eggwars.objects.enums.EggShop;
import com.minecraft.core.member.list.eggwars.objects.enums.item.flag.EggItemFlag;
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
public enum EggWarsItem {

    /* Blocks Item List */
    BLOCKS_WOOL("Lã", EggShop.BLOCKS, EggOre.IRON, new Item(Material.WOOL, 16), 4,
            asList("§7Um bloco útil para fazer pontes",
                    "§7entre as ilhas. Ficará da",
                    "§7cor do seu time.")),
    BLOCKS_WOOD("Madeira", EggShop.BLOCKS, EggOre.GOLD, new Item(Material.WOOD, 16), 4, asList(
            "§7Útil para defender seu ovo,",
            "§7resistente contra picaretas."
    )),
    BLOCK_CLAY("Argila", EggShop.BLOCKS, EggOre.IRON, new Item(Material.STAINED_CLAY, 16), 12, asList(
            "§7Bloco sólido o suficiente",
            "§7para proteger seu ovo."
    )),
    BLOCKS_GLASS("Vidro à prova de explosões", EggShop.BLOCKS, EggOre.IRON, new Item(Material.STAINED_GLASS, 4), 12,
            singletonList(Type.CASUAL), asList(
            "§7Proteja-se contra qualquer",
            "§7explosão sofrida."
    )),
    BLOCKS_END("Blocos do Fim", EggShop.BLOCKS, EggOre.IRON, new Item(Material.ENDER_STONE, 12), 24, asList(
            "§7Um pouco mais duro que a Argila",
            "§7protege um pouco de explosões."
    )),
    BLOCKS_LADDER("Escadas", EggShop.BLOCKS, EggOre.IRON, new Item(Material.LADDER, 16), 4,
            singletonList("§7Evite levar danos desnecessários.")),
    BLOCKS_OBSIDIAN("Obsidiana", EggShop.BLOCKS, EggOre.DIAMOND, new Item(Material.OBSIDIAN, 4), 4,
            singletonList(Type.CASUAL), singletonList("§7Obtenha uma proteção máxima no seu ovo.")),

    /* Combat Item List */
    COMBAT_STONE_SWORD("Espada de Pedra", EggShop.COMBAT, EggOre.IRON, Item.of(Material.STONE_SWORD), 10),
    COMBAT_IRON_SWORD("Espada de Ferro", EggShop.COMBAT, EggOre.GOLD, Item.of(Material.IRON_SWORD), 7),
    COMBAT_DIAMOND_SWORD("Espada de Diamante", EggShop.COMBAT, EggOre.DIAMOND, Item.of(Material.DIAMOND_SWORD), 4),
    COMBAT_STICK("Graveto", EggShop.COMBAT, EggOre.GOLD, Item.of(Material.STICK).enchantment(Enchantment.KNOCKBACK, 1), 5),

    /* Armor Item List */
    ARMOR_CHAINMAIL("Armadura de Malha", EggShop.ARMOR, EggOre.IRON, Item.of(Material.CHAINMAIL_BOOTS), 20,
            singletonList("§7Você não perde ao morrer.")),
    ARMOR_IRON("Armadura de Ferro", EggShop.ARMOR, EggOre.GOLD, Item.of(Material.IRON_BOOTS), 12,
            singletonList("§7Você não perde ao morrer.")),
    ARMOR_DIAMOND("Armadura de Diamante", EggShop.ARMOR, EggOre.DIAMOND, Item.of(Material.DIAMOND_BOOTS), 6,
            singletonList("§7Você não perde ao morrer.")),

    /* Tools Item List */
    TOOLS_SHEARS("Tesoura", EggShop.TOOLS, EggOre.IRON, Item.of(Material.SHEARS),
            20, singletonList("§7Você não perde ao morrer.")),
    TOOLS_WOOD_PICKAXE("Picareta de Madeira", EggShop.TOOLS, EggOre.IRON, Item.of(Material.WOOD_PICKAXE).enchantment(Enchantment.DIG_SPEED, 1),
            10, singletonList("§7Ao morrer sua picareta sofrerá um downgrade.")),
    TOOLS_IRON_PICKAXE("Picareta de Ferro", EggShop.TOOLS, EggOre.IRON, Item.of(Material.IRON_PICKAXE).enchantment(Enchantment.DIG_SPEED, 2),
            20, singletonList("§7Ao morrer sua picareta sofrerá um downgrade.")),
    TOOLS_GOLD_PICKAXE("Picareta de Ouro", EggShop.TOOLS, EggOre.GOLD, Item.of(Material.GOLD_PICKAXE).enchantment(Enchantment.DIG_SPEED, 3),
            8, singletonList("§7Ao morrer sua picareta sofrerá um downgrade.")),
    TOOLS_DIAMOND_PICKAXE("Picareta de Diamante", EggShop.TOOLS, EggOre.GOLD, Item.of(Material.DIAMOND_PICKAXE).enchantment(Enchantment.DIG_SPEED, 3),
            16, singletonList("§7Ao morrer sua picareta sofrerá um downgrade.")),
    TOOLS_WOOD_AXE("Machado de Madeira", EggShop.TOOLS, EggOre.IRON, Item.of(Material.WOOD_AXE).enchantment(Enchantment.DIG_SPEED, 1),
            10, singletonList("§7Ao morrer seu machado sofrerá um downgrade.")),
    TOOLS_IRON_AXE("Machado de Ferro", EggShop.TOOLS, EggOre.IRON, Item.of(Material.IRON_AXE).enchantment(Enchantment.DIG_SPEED, 2),
            20, singletonList("§7Ao morrer seu machado sofrerá um downgrade.")),
    TOOLS_GOLD_AXE("Machado de Ouro", EggShop.TOOLS, EggOre.GOLD, Item.of(Material.GOLD_AXE).enchantment(Enchantment.DIG_SPEED, 3),
            8, singletonList("§7Ao morrer seu machado sofrerá um downgrade.")),
    TOOLS_DIAMOND_AXE("Machado de Diamante", EggShop.TOOLS, EggOre.GOLD, Item.of(Material.DIAMOND_AXE).enchantment(Enchantment.DIG_SPEED, 3),
            16, singletonList("§7Ao morrer seu machado sofrerá um downgrade.")),

    /* Artillery Item List */
    ARTILLERY_ARROW("Flechas", EggShop.ARTILLERY, EggOre.GOLD, new Item(Material.ARROW, 8), 2, singletonList(Type.CASUAL), new ArrayList<>()),
    ARTILLERY_BOW("Arco", EggShop.ARTILLERY, EggOre.GOLD, Item.of(Material.BOW), 12, singletonList(Type.CASUAL), new ArrayList<>()),
    ARTILLERY_BOW_STRENGTH("Arco (Força I)", EggShop.ARTILLERY, EggOre.GOLD, Item.of(Material.BOW)
            .enchantment(Enchantment.ARROW_DAMAGE, 1), 24, singletonList(Type.CASUAL), new ArrayList<>()),
    ARTILLERY_BOW_STRENGTH_IMPACT("Arco (Força e Impacto I)", EggShop.ARTILLERY, EggOre.DIAMOND, Item.of(Material.BOW)
            .enchantment(Enchantment.ARROW_DAMAGE, 1)
            .enchantment(Enchantment.ARROW_KNOCKBACK, 1), 6, singletonList(Type.CASUAL), new ArrayList<>()),

    /* Potions Item List */
    POTIONS_AGILITY("Poção Agilidade II", EggShop.POTION, EggOre.DIAMOND, new Item(Material.POTION, 1, 2)
            .name("§ePoção Agilidade II (45 segundos)")
            .flags(ItemFlag.values())
            .lore(new ArrayList<>()), 1, singletonList("§745 segundos ágil.")),
    POTIONS_SUPER_JUMP("Poção Super Pulo V", EggShop.POTION, EggOre.DIAMOND, new Item(Material.POTION, 1, 11)
            .name("§aPoção Super Pulo V (45 segundos)")
            .flags(ItemFlag.values())
            .lore(new ArrayList<>()), 2, singletonList("§745 segundos pulando alto.")),
    POTIONS_INVISIBILITY("Poção Invisibilidade", EggShop.POTION, EggOre.DIAMOND, new Item(Material.POTION, 1, 14)
            .name("§bPoção Invisibilidade (30 segundos)")
            .flags(ItemFlag.values())
            .lore(new ArrayList<>()), 2, singletonList("§730 segundos invisível.")),
    POTIONS_SLOWNESS("Poção Lentidão Arremessável", EggShop.POTION, EggOre.DIAMOND, new Item(Material.POTION, 1, 16394)
            .name("§6Poção Lentidão II (5 segundos)")
            .flags(ItemFlag.values())
            .lore(new ArrayList<>()), 1, singletonList("§75 segundos lento")),

    /* Util Item List */
    UTIL_GOLD_APPLE("Maçã Dourada", EggShop.UTIL, EggOre.GOLD, Item.of(Material.GOLDEN_APPLE), 3, new ArrayList<>()),
    UTIL_MOTH_BALL("Bolinha de traça", EggShop.UTIL, EggOre.IRON, Item.of(Material.SNOW_BALL, "§eBolinha de traça"), 40, asList(
            "§7Invoque 3 traças onde a Bola",
            "§7de Neve cair. Dura 15 segundos.")),
    UTIL_FIREBALL("Fireball", EggShop.UTIL, EggOre.IRON, Item.of(Material.FIREBALL, "§cBola de Fogo"), 40,
            singletonList(Type.CASUAL), new ArrayList<>()),
    UTIL_SLINGSHOT("Estilingue", EggShop.UTIL, EggOre.GOLD, Item.of(Material.MAGMA_CREAM, "§eEstilingue"), 7,
            singletonList(Type.CASUAL), new ArrayList<>()),
    UTIL_DEFENSE("Defesa dos Sonhos", EggShop.UTIL, EggOre.IRON, Item.of(Material.MONSTER_EGG, "§eDefesa dos Sonhos"), 90, asList(
            "§7Invoque um Golem de Ferro para",
            "§7proteger sua base. Dura 4 minutos.")),
    UTIL_TNT("TNT", EggShop.UTIL, EggOre.GOLD, Item.of(Material.TNT, "§cExplosivo"), 4,
            asList("§7Uma TNT instantânea, exploda",
                    "§7tudo pela frente.")),
    UTIL_PEARL("Ender Pearl", EggShop.UTIL, EggOre.DIAMOND, Item.of(Material.ENDER_PEARL), 2, new ArrayList<>()),
    UTIL_WATER_BUCKET("Balde de Água", EggShop.UTIL, EggOre.GOLD, Item.of(Material.WATER_BUCKET), 3,
            singletonList("§7Proteja contra TNT's")),
    UTIL_MILK_BUCKET("Leitinho Mágico", EggShop.UTIL, EggOre.GOLD, Item.of(Material.MILK_BUCKET, "§dLeite Mágico"), 2,
            asList("§7Fique imune às armadilhas durante",
                    "§730 segundos.")),
    UTIL_JUMPER("Jumper", EggShop.UTIL, EggOre.DIAMOND, Item.of(Material.SLIME_BLOCK, "§aJumper"), 2,
            singletonList(Type.CASUAL), asList(
            "§7Use os slimes para pular",
            "§7o mais alto possível.")),
    UTIL_SPONGE("Esponja", EggShop.UTIL, EggOre.GOLD, new Item(Material.SPONGE, 4), 1, asList(
            "§7Seque toda água onde o bloco",
            "§7for colocado.")),
    UTIL_EGG("Ovo de Pontes", EggShop.UTIL, EggOre.DIAMOND, Item.of(Material.EGG, "§6Ovo de Pontes"), 1, asList(
            "§7Faça pontes automaticamente!",
            "§7irado né?")),
    UTIL_TRACKING("Rastreador", EggShop.UTIL, EggOre.DIAMOND, Item.of(Material.COMPASS, "§cRastreador"), 2,
            List.of(Type.CASUAL),
            asList("§7Localize os times inimigos.", "§7Uso único."),
            singletonList(EggItemFlag.CAN_BUY_AFTER_EGG_BREAK)),
    LOJA_PORTATIL("Loja portátil", EggShop.UTIL, EggOre.DIAMOND, Item.of(Material.BOOK, "§aLoja portátil"), 4, asList(
            "§7Abra a loja em qualquer",
                    "§7lugar!"));

    private final String name;

    private final EggShop shop;
    private final EggOre ore;

    private final ItemStack stack;

    private final int price;

    private final List<Type> types;
    private final List<String> lore;

    private final List<EggItemFlag> flags;

    EggWarsItem(String name, EggShop shop, EggOre ore, ItemStack stack, int price) {
        this(name, shop, ore, stack, price, List.of(Type.CASUAL), new ArrayList<>(), new ArrayList<>());
    }

    EggWarsItem(String name, EggShop shop, EggOre ore, ItemStack stack, int price, List<String> lore) {
        this(name, shop, ore, stack, price, List.of(Type.CASUAL), lore, new ArrayList<>());
    }

    EggWarsItem(String name, EggShop shop, EggOre ore, ItemStack stack, int price, List<Type> types, List<String> lore) {
        this(name, shop, ore, stack, price, types, lore, new ArrayList<>());
    }

    EggWarsItem(String name, EggShop shop, EggOre ore, ItemStack stack, int price, List<Type> types, List<String> lore, List<EggItemFlag> flags) {
        this.name = name;
        this.shop = shop;
        this.ore = ore;
        this.stack = stack;
        this.price = price;
        this.types = types;
        this.lore = lore;
        this.flags = flags;
    }

    public static List<EggWarsItem> list() {
        return new ArrayList<>(asList(values()));
    }

    public static List<EggWarsItem> list(EggShop shop) {
        return list().stream().filter(item -> item.getShop().equals(shop)).collect(Collectors.toList());
    }

    public static EggWarsItem of(String name) {
        return of(item -> item.name().equalsIgnoreCase(name) || item.getName().equalsIgnoreCase(name));
    }

    public static EggWarsItem of(Predicate<EggWarsItem> filter) {
        return list().stream().filter(filter).findFirst().orElse(null);
    }

    public boolean hasFlag(EggItemFlag flag) {
        return this.flags.contains(flag);
    }

    public boolean isAvailableForMode(Type type) {
        if (type.equals(Type.CUSTOM)) return true;

        return types.contains(type);
    }

    public boolean isType(EggShop type) {
        return this.shop == type;
    }

    public boolean isColored() {
        return isType(EggShop.BLOCKS) && (this == BLOCKS_WOOL || this == BLOCKS_GLASS || this == BLOCK_CLAY);
    }

    public boolean isToolPickaxe() {
        String name = stack.getType().name();

        return shop == EggShop.TOOLS && name.endsWith("_PICKAXE");
    }

    public boolean isToolAxe() {
        String name = stack.getType().name();

        return shop == EggShop.TOOLS && name.endsWith("_AXE");
    }

    public boolean isInferior(EggWarsItem item) {
        if (item == null) return false;

        return ordinal() <= item.ordinal();
    }
    
    public boolean isSword() {
        String name = stack.getType().name();
        return name.endsWith("_SWORD");
    }
}

