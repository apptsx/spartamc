package com.minecraft.core.arcade.category;

import com.minecraft.core.Core;
import com.minecraft.core.arcade.category.builder.ArcadeCategoryBuilder;
import com.minecraft.core.arcade.feature.ArcadeFeature;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.rule.ArcadeRule;
import com.minecraft.core.arcade.style.ArcadeStyle;
import com.minecraft.core.server.type.ServerType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.Arrays.asList;

@Getter
@RequiredArgsConstructor
public enum ArcadeCategory {

    NONE(ArcadeCategoryBuilder.builder().server(ServerType.BUNGEE).name("Vázio").build()),

    HUNGERGAMES(ArcadeCategoryBuilder.builder()
            .server(ServerType.HUNGERGAMES)
            .name("Solo")
            .iconId("MUSHROOM_SOUP")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(List.of(Type.CASUAL))
            .build()),
    BEDWARS_SOLO(ArcadeCategoryBuilder.builder()
            .server(ServerType.BEDWARS)
            .name("Solo")
            .iconId("BED")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(List.of(Type.CASUAL))
            .build()),

    PARTY_SOLO(ArcadeCategoryBuilder.builder()
            .server(ServerType.PARTY)
            .name("Solo")
            .iconId("BED")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(List.of(Type.CASUAL))
            .build()),
    BEDWARS_DUO(ArcadeCategoryBuilder.builder()
            .server(ServerType.BEDWARS)
            .name("Duplas")
            .iconId("BED")
            .slots(singletonList(Slot.DUO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(List.of(Type.CASUAL))
            .build()),
    BEDWARS_TRIO(ArcadeCategoryBuilder.builder()
            .server(ServerType.BEDWARS)
            .name("Trios")
            .iconId("BED")
            .slots(singletonList(Slot.TRIO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(List.of(Type.CASUAL))
            .build()),
    BEDWARS_QUARTET(ArcadeCategoryBuilder.builder()
            .server(ServerType.BEDWARS)
            .name("Quartetos")
            .iconId("BED")
            .slots(singletonList(Slot.QUARTET))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(List.of(Type.CASUAL))
            .build()),
    BEDWARS_VERSUS_SOLO(ArcadeCategoryBuilder.builder()
            .server(ServerType.BEDWARS)
            .name("1v1")
            .iconId("BED")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(singletonList(Type.CASUAL))
            .build()),
    BEDWARS_VERSUS_DUO(ArcadeCategoryBuilder.builder()
            .server(ServerType.BEDWARS)
            .name("2v2")
            .iconId("BED")
            .slots(singletonList(Slot.DUO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(singletonList(Type.CASUAL))
            .build()),
    BEDWARS_VERSUS_TRIO(ArcadeCategoryBuilder.builder()
            .server(ServerType.BEDWARS)
            .name("3v3")
            .iconId("BED")
            .slots(singletonList(Slot.TRIO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(singletonList(Type.CASUAL))
            .build()),
    BEDWARS_VERSUS_QUARTET(ArcadeCategoryBuilder.builder()
            .server(ServerType.BEDWARS)
            .name("4v4")
            .iconId("BED")
            .slots(singletonList(Slot.QUARTET))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(singletonList(Type.CASUAL))
            .build()),

    EGGWARS_SOLO(ArcadeCategoryBuilder.builder()
            .server(ServerType.EGGWARS)
            .name("Solo")
            .iconId("DRAGON_EGG")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(List.of(Type.CASUAL))
            .build()),
    EGGWARS_DUO(ArcadeCategoryBuilder.builder()
            .server(ServerType.EGGWARS)
            .name("Duplas")
            .iconId("DRAGON_EGG")
            .slots(singletonList(Slot.DUO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(List.of(Type.CASUAL))
            .build()),
    EGGWARS_TRIO(ArcadeCategoryBuilder.builder()
            .server(ServerType.EGGWARS)
            .name("Trios")
            .iconId("DRAGON_EGG")
            .slots(singletonList(Slot.TRIO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(List.of(Type.CASUAL))
            .build()),
    EGGWARS_QUARTET(ArcadeCategoryBuilder.builder()
            .server(ServerType.EGGWARS)
            .name("Quartetos")
            .iconId("DRAGON_EGG")
            .slots(singletonList(Slot.QUARTET))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(List.of(Type.CASUAL))
            .build()),
    EGGWARS_VERSUS_SOLO(ArcadeCategoryBuilder.builder()
            .server(ServerType.EGGWARS)
            .name("1v1")
            .iconId("DRAGON_EGG")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(singletonList(Type.CASUAL))
            .build()),
    EGGWARS_VERSUS_DUO(ArcadeCategoryBuilder.builder()
            .server(ServerType.EGGWARS)
            .name("2v2")
            .iconId("DRAGON_EGG")
            .slots(singletonList(Slot.DUO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(singletonList(Type.CASUAL))
            .build()),
    EGGWARS_VERSUS_TRIO(ArcadeCategoryBuilder.builder()
            .server(ServerType.EGGWARS)
            .name("3v3")
            .iconId("DRAGON_EGG")
            .slots(singletonList(Slot.TRIO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(singletonList(Type.CASUAL))
            .build()),
    EGGWARS_VERSUS_QUARTET(ArcadeCategoryBuilder.builder()
            .server(ServerType.EGGWARS)
            .name("4v4")
            .iconId("DRAGON_EGG")
            .slots(singletonList(Slot.QUARTET))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(singletonList(Type.CASUAL))
            .build()),

    // THE BRIDGE CATEGORY

    THE_BRIDGE_SOLO(ArcadeCategoryBuilder.builder()
            .server(ServerType.THE_BRIDGE)
            .style(ArcadeStyle.STRATEGY)
            .name("The Bridge")
            .iconId("STAINED_CLAY")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.EDITABLE_MENU, ArcadeFeature.BUILD, ArcadeFeature.LIQUID, ArcadeFeature.NOT_PULL_BACK, ArcadeFeature.CABINS, ArcadeFeature.NOT_TELEPORT_STARTING, ArcadeFeature.LEVEL_BAR_XP))
            .types(List.of(Type.CASUAL))
            .build()),

    // DUELS CATEGORY

    DUELS_SIMULATOR(ArcadeCategoryBuilder.builder()
            .server(ServerType.DUELS)
            .style(ArcadeStyle.COMBAT)
            .name("Simulator")
            .iconId("WEB")
            .slots(singletonList(Slot.SOLO))
            .lore(asList("§7O Simulator é um modo de combate",
                    "§7entre 2 jogadores, onde ambos",
                    "§7podem usar kits."))
            .features(asList(ArcadeFeature.CUSTOM_USER, ArcadeFeature.DAMAGE, ArcadeFeature.LIQUID, ArcadeFeature.BUILD, ArcadeFeature.EDITABLE_MENU, ArcadeFeature.DROPS_ALL))
            .build()),
    DUELS_SOUP(ArcadeCategoryBuilder.builder()
            .server(ServerType.DUELS)
            .style(ArcadeStyle.COMBAT)
            .name("Sopa")
            .iconId("MUSHROOM_SOUP")
            .slots(singletonList(Slot.SOLO))
            .features(singletonList(ArcadeFeature.DAMAGE))
            .build()),
    DUELS_GLADIATOR(ArcadeCategoryBuilder.builder()
            .server(ServerType.DUELS)
            .style(ArcadeStyle.COMBAT)
            .name("Gladiator")
            .iconId("IRON_FENCE")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.EDITABLE_MENU, ArcadeFeature.BUILD, ArcadeFeature.LIQUID, ArcadeFeature.DROPS_ALL))
            .build()),
    DUELS_SUMO(ArcadeCategoryBuilder.builder()
            .server(ServerType.DUELS)
            .style(ArcadeStyle.COMBAT)
            .name("Sumo")
            .iconId("APPLE")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.NOT_TAKE_LIFE, ArcadeFeature.WITHOUT_MOVING_STARTING, ArcadeFeature.NOT_TELEPORT_STARTING))
            .build()),
    DUELS_THE_BRIDGE(ArcadeCategoryBuilder.builder()
            .server(ServerType.DUELS)
            .style(ArcadeStyle.STRATEGY)
            .name("The Bridge")
            .iconId("STAINED_CLAY")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.EDITABLE_MENU, ArcadeFeature.BUILD, ArcadeFeature.LIQUID, ArcadeFeature.NOT_PULL_BACK, ArcadeFeature.CABINS, ArcadeFeature.NOT_TELEPORT_STARTING, ArcadeFeature.LEVEL_BAR_XP))
            .build()),
    DUELS_UHC(ArcadeCategoryBuilder.builder()
            .server(ServerType.DUELS)
            .style(ArcadeStyle.COMBAT)
            .name("UHC")
            .iconId("GOLDEN_APPLE")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.EDITABLE_MENU, ArcadeFeature.BUILD, ArcadeFeature.LIQUID))
            .build()),
    DUELS_BOXING(ArcadeCategoryBuilder.builder()
            .server(ServerType.DUELS)
            .style(ArcadeStyle.COMBAT)
            .name("Boxing")
            .iconId("DIAMOND_SWORD")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.NOT_TAKE_LIFE, ArcadeFeature.WITHOUT_MOVING_STARTING, ArcadeFeature.NOT_TELEPORT_STARTING))
            .build()),
    DUELS_CHAMBER(ArcadeCategoryBuilder.builder()
            .server(ServerType.DUELS)
            .style(ArcadeStyle.STRATEGY)
            .name("Chamber")
            .iconId("ARROW")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.LEVEL_BAR_XP, ArcadeFeature.WITHOUT_MOVING_STARTING, ArcadeFeature.NOT_TELEPORT_STARTING, ArcadeFeature.SCORE_DEATH))
            .build()),
    DUELS_FIREBALL(ArcadeCategoryBuilder.builder()
            .server(ServerType.DUELS)
            .style(ArcadeStyle.COMBAT)
            .name("Fireball")
            .iconId("FIREBALL")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.EDITABLE_MENU, ArcadeFeature.BUILD, ArcadeFeature.LIQUID))
            .build()),
    DUELS_NO_DEBUFF(ArcadeCategoryBuilder.builder()
            .server(ServerType.DUELS)
            .style(ArcadeStyle.COMBAT)
            .name("No Debuff")
            .iconId("POTION")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.EDITABLE_MENU, ArcadeFeature.LEVEL_BAR_XP))
            .build()),
    DUELS_PEARL_FIGHT(ArcadeCategoryBuilder.builder()
            .server(ServerType.DUELS)
            .style(ArcadeStyle.COMBAT)
            .name("Pearl Fight")
            .iconId("ENDER_PEARL")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.BUILD, ArcadeFeature.LIQUID, ArcadeFeature.FALL_DAMAGE))
            .build()),

    /* PvP Categories */
    PVP_ARENA(ArcadeCategoryBuilder.builder()
            .server(ServerType.PVP)
            .style(ArcadeStyle.COMBAT)
            .name("Arena")
            .iconId("STONE_SWORD")
            .slots(singletonList(Slot.SOLO))
            .build()),
    PVP_MLG(ArcadeCategoryBuilder.builder()
            .server(ServerType.PVP)
            .style(ArcadeStyle.FUN)
            .name("MLG")
            .iconId("WATER_BUCKET")
            .slots(singletonList(Slot.SOLO))
            .build()),
    PVP_FPS(ArcadeCategoryBuilder.builder()
            .server(ServerType.PVP)
            .style(ArcadeStyle.COMBAT)
            .name("FPS")
            .iconId("GLASS")
            .slots(singletonList(Slot.SOLO))
            .build()),
    PVP_LAVA(ArcadeCategoryBuilder.builder()
            .server(ServerType.PVP)
            .style(ArcadeStyle.STRATEGY)
            .name("Lava")
            .iconId("LAVA_BUCKET")
            .slots(singletonList(Slot.SOLO))
            .build()),

    /* Sky Wars Categories List */
    SKYWARS_SOLO(ArcadeCategoryBuilder.builder()
            .server(ServerType.SKYWARS)
            .name("Solo")
            .iconId("BOW")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(List.of(Type.CASUAL))
            .build()),
    SKYWARS_DUO(ArcadeCategoryBuilder.builder()
            .server(ServerType.SKYWARS)
            .name("Duplas")
            .iconId("BOW")
            .slots(singletonList(Slot.DUO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(List.of(Type.CASUAL))
            .build()),
    SKYWARS_TRIO(ArcadeCategoryBuilder.builder()
            .server(ServerType.SKYWARS)
            .name("Trios")
            .iconId("BOW")
            .slots(singletonList(Slot.TRIO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(List.of(Type.CASUAL))
            .build()),
    SKYWARS_QUARTET(ArcadeCategoryBuilder.builder()
            .server(ServerType.SKYWARS)
            .name("Quartetos")
            .iconId("BOW")
            .slots(singletonList(Slot.QUARTET))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(List.of(Type.CASUAL))
            .build()),
    SKYWARS_VERSUS_SOLO(ArcadeCategoryBuilder.builder()
            .server(ServerType.SKYWARS)
            .name("1v1")
            .iconId("BOW")
            .slots(singletonList(Slot.SOLO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(singletonList(Type.CASUAL))
            .build()),
    SKYWARS_VERSUS_DUO(ArcadeCategoryBuilder.builder()
            .server(ServerType.SKYWARS)
            .name("2v2")
            .iconId("BOW")
            .slots(singletonList(Slot.DUO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(singletonList(Type.CASUAL))
            .build()),
    SKYWARS_VERSUS_TRIO(ArcadeCategoryBuilder.builder()
            .server(ServerType.SKYWARS)
            .name("3v3")
            .iconId("BOW")
            .slots(singletonList(Slot.TRIO))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(singletonList(Type.CASUAL))
            .build()),
    SKYWARS_VERSUS_QUARTET(ArcadeCategoryBuilder.builder()
            .server(ServerType.SKYWARS)
            .name("4v4")
            .iconId("BOW")
            .slots(singletonList(Slot.QUARTET))
            .features(asList(ArcadeFeature.DAMAGE, ArcadeFeature.FALL_DAMAGE))
            .types(singletonList(Type.CASUAL))
            .build()),
    ;

    private final ArcadeCategoryBuilder builder;

    public static ArcadeCategory of(String name) {
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(name) || category.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static List<ArcadeCategory> list() {
        return Stream.of(values()).collect(Collectors.toList());
    }

    public static List<ArcadeCategory> list(Predicate<ArcadeCategory> filter) {
        return list().stream().filter(filter).collect(Collectors.toList());
    }

    public static List<ArcadeCategory> of(ServerType server) {
        return list(arcade -> arcade.getServer().equals(server)).stream().collect(Collectors.toList());
    }

    public boolean isSingleSlot() {
        return getSlots().size() == 1;
    }

    public boolean hasSlot(Slot slot) {
        return getSlots().contains(slot);
    }

    public boolean hasFeature(ArcadeFeature feature) {
        return getFeatures().contains(feature);
    }

    public int getPlayingNow() {
        return Core.getArcadeData().getOnlinePlayers(this);
    }

    public String getId() {
        return getServer().name().toLowerCase() + ":" + name().toLowerCase();
    }

    public ServerType getServer() {
        return builder.getServer();
    }

    public ArcadeRule getRule() {
        return builder.getRule();
    }

    public ArcadeStyle getStyle() {
        return builder.getStyle();
    }

    public String getName() {
        return builder.getName();
    }

    public String getFullName() {
        return getServer().getName() + " " + getName();
    }

    public String getIconId() {
        return builder.getIconId();
    }

    public List<Slot> getSlots() {
        return builder.getSlots();
    }

    public List<String> getLore() {
        return builder.getLore();
    }

    public List<ArcadeFeature> getFeatures() {
        return builder.getFeatures();
    }

    public List<Type> getTypes() {
        return builder.getTypes();
    }

}
