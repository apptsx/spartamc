package com.minecraft.core.account.context.objects.medal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum Medal {

    NONE("Nenhuma", "", ChatColor.WHITE, null),

    // TOP — 4 cores exclusivas (&4 &b &3 &1)
    TOP1("Top 1°",  "§4§l✪", ChatColor.DARK_RED,  "medal.top1"),
    TOP2("Top 2°",  "§b§l✪", ChatColor.AQUA,       "medal.top2"),
    TOP3("Top 3°",  "§3§l✪", ChatColor.DARK_AQUA,  "medal.top3"),
    TOP4("Top 4°",  "§1§l✪", ChatColor.DARK_BLUE,  "medal.top4"),

    // ADMIN exclusiva
    ADMIN_STAR("Admin Star", "§4§l✠",  ChatColor.DARK_RED,  "medal.admin"),
    ADMIN_CROSS("Admin Cruz", "§4§l✟", ChatColor.DARK_RED,  "medal.admin.cross"),

    // BETA exclusiva
    BETA("Beta", "§3§l⚝", ChatColor.DARK_AQUA, "medal.beta"),

    // STAFF exclusivas
    STAFF_SHIELD("Staff Shield", "§9§l✜", ChatColor.BLUE, "medal.staff"),
    STAFF_CROSS("Staff Cruz",    "§9§l☩", ChatColor.BLUE, "medal.staff.cross"),
    STAFF_WISH("Staff Desejo",   "§9§l望", ChatColor.BLUE, "medal.staff.wish"),

    // VIP — diversificadas
    STAR("Estrela",      "§e§l✯",  ChatColor.YELLOW,       "medal.star"),
    SPARKLE("Brilho",    "§e§l✵",  ChatColor.YELLOW,       "medal.sparkle"),
    DIAMOND("Diamante",  "§b§l✦",  ChatColor.AQUA,         "medal.diamond"),
    CRYSTAL("Cristal",   "§b§l✧",  ChatColor.AQUA,         "medal.crystal"),
    SUN("Sol",           "§6§l❂",  ChatColor.GOLD,         "medal.sun"),
    FLOWER("Flor",       "§a§l❀",  ChatColor.GREEN,        "medal.flower"),
    ROSE("Rosa",         "§d§l✿",  ChatColor.LIGHT_PURPLE, "medal.rose"),
    BLOOM("Bloom",       "§a§l❁",  ChatColor.GREEN,        "medal.bloom"),
    CLOVER("Trevo",      "§a§l❃",  ChatColor.GREEN,        "medal.clover"),
    NOTE("Nota",         "§e§l♩",  ChatColor.YELLOW,       "medal.note"),
    MUSIC("Música",      "§e§l♪",  ChatColor.YELLOW,       "medal.music"),
    HARMONY("Harmonia",  "§e§l♫",  ChatColor.YELLOW,       "medal.harmony"),
    MELODY("Melodia",    "§6§l♬",  ChatColor.GOLD,         "medal.melody"),
    SNOWFLAKE("Neve",    "§b§l❆",  ChatColor.AQUA,         "medal.snowflake"),
    HEART("Coração",     "§c§l❥",  ChatColor.RED,          "medal.heart"),
    CHECK("Check",       "§a§l✓",  ChatColor.GREEN,        "medal.check"),
    ARROW("Seta",        "§6§l➤",  ChatColor.GOLD,         "medal.arrow"),
    YIN("Yin Yang",      "§f§l☯",  ChatColor.WHITE,        "medal.yin"),
    BIOHAZARD("Bio",     "§2§l☣",  ChatColor.DARK_GREEN,   "medal.biohazard"),
    CRESCENT("Lua",      "§e§l☪",  ChatColor.YELLOW,       "medal.crescent"),
    ASTERISK("Asterisco","§c§l✶",  ChatColor.RED,          "medal.asterisk"),
    BURST("Explosão",    "§6§l✷",  ChatColor.GOLD,         "medal.burst"),
    SPARK("Faísca",      "§e§l✸",  ChatColor.YELLOW,       "medal.spark"),
    FLARE("Flare",       "§c§l✹",  ChatColor.RED,          "medal.flare"),
    RING("Anel",         "§b§l✺",  ChatColor.AQUA,         "medal.ring"),
    GEAR("Engrenagem",   "§7§l✻",  ChatColor.GRAY,         "medal.gear"),
    WHEEL("Roda",        "§7§l✼",  ChatColor.GRAY,         "medal.wheel"),
    PETAL("Pétala",      "§d§l✽",  ChatColor.LIGHT_PURPLE, "medal.petal"),
    BLOSSOM("Blossom",   "§a§l✾",  ChatColor.GREEN,        "medal.blossom"),
    PENCIL("Lápis",      "§f§l✐",  ChatColor.WHITE,        "medal.pencil"),
    OHMS("Ohms",         "§e§l℧",  ChatColor.YELLOW,       "medal.ohms"),
    WISH("Desejo",       "§6§l望",  ChatColor.GOLD,         "medal.wish"),
    FELP("FELP",         "§f§lF", ChatColor.WHITE,       "medal.felp"),
    FLORENTA("Florenta",  "§c§l❁",  ChatColor.RED,          "medal.florenta"),
    ROSA_FLOWER("Flor",    "§d§l✿",  ChatColor.DARK_PURPLE,  "medal.rosa.flower"),
    STAR_MEDAL("Star",     "§d§l✦",  ChatColor.LIGHT_PURPLE, "medal.star.rank"),
    START_MEDAL("Start",   "§3§l✏",  ChatColor.DARK_AQUA,    "medal.start"),
    CLAN_CLASH_2("ClanClash2", "§6§l✟", ChatColor.GOLD,      "medal.clash2"),
    CLAN_CLASH("ClanClash", "§c§l☩", ChatColor.RED,          "medal.clash");

    private final String name, coloredSymbol;
    private final ChatColor color;
    private final String permission;

    public static Medal of(String name) {
        return Stream.of(values())
                .filter(medal -> medal.name().equalsIgnoreCase(name) || medal.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public String getSymbol() {
        return coloredSymbol;
    }

    public String getColoredName() {
        return color + name;
    }

    public String getColoredSymbol() {
        return coloredSymbol;
    }

    public String getPermission() {
        return permission != null ? permission : "medal." + name().toLowerCase();
    }
}
