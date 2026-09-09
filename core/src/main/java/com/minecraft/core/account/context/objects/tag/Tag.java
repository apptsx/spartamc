package com.minecraft.core.account.context.objects.tag;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.tag.prefix.TagPrefix;
import com.minecraft.core.account.context.objects.tag.role.TagRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Getter
@AllArgsConstructor
public enum Tag {

    MEMBER("Membro", "Z", "§7", emptyList(), RankType.MEMBER, TagRole.NONE, ChatColor.GRAY),

    /* Custom Role Tags */
    RED("Vermelho", "§c§lV §r§c", "A", TagRole.COLORED, ChatColor.RED),
    BLUE("Azul", "§9§lA §r§9", "B", TagRole.COLORED, ChatColor.BLUE),
    PINK("Rosa", "§d§lR §r§d", "C", TagRole.COLORED, ChatColor.LIGHT_PURPLE),
    ORANGE("Laranja", "§6§lL §r§6", "D", TagRole.COLORED, ChatColor.GOLD),
    YELLOW("Amarelo", "§e§lA §r§e", "E", TagRole.COLORED, ChatColor.YELLOW),
    GREEN("Verde", "§a§lV §r§a", "F", TagRole.COLORED, ChatColor.GREEN),
    AQUA("Ciano", "§b§lC §r§b", "G", TagRole.COLORED, ChatColor.AQUA),
    GRAY("Cinza", "§8§lC §r§8", "H", TagRole.COLORED, ChatColor.DARK_GRAY),
    MASKED("Camuflada", "X", "§7§k", TagRole.COLORED, ChatColor.MAGIC),
    SPECTATOR("Espectador", "X", "§7", emptyList(), RankType.MEMBER, TagRole.COLORED, ChatColor.GRAY),

    /* Vip Role Tags */
    BOOSTER("Star", "R", "§d§lSTAR §d", emptyList(), RankType.BOOSTER, TagRole.VIP, ChatColor.LIGHT_PURPLE),
    VIP("VIP", "Q", "§a§lVIP §r§a", emptyList(), RankType.VIP, TagRole.VIP, ChatColor.GREEN),
    MAX("Max", "N", "§d§lMAX §d", emptyList(), RankType.MAX, TagRole.VIP, ChatColor.LIGHT_PURPLE),
    MAX_PLUS("Max+", "M", "§5§lMAX§6§l+ §5", emptyList(), RankType.MAX_PLUS, TagRole.VIP, ChatColor.DARK_PURPLE),
    BETA("Beta", "L", "§1§lBETA §r§1", emptyList(), RankType.BETA, TagRole.VIP, ChatColor.DARK_BLUE),

    /* Tags */
    TAG_CARNAVAL("Carnaval", "§6§lCARNAVAL §6", TagRole.SPECIAL, ChatColor.GOLD),
    TAG_FEAR("Fear", "§4§lFEAR §4", TagRole.SPECIAL, ChatColor.DARK_RED),
    TAG_FERIAS("Férias", "§a§lFERIAS §a", TagRole.SPECIAL, ChatColor.GREEN),
    TAG_NATAL("Natal", "§c§lNATAL §c", TagRole.SPECIAL, ChatColor.RED),
    TAG_2026("2026", "§b§l2026 §b", TagRole.SPECIAL, ChatColor.AQUA),
    TAG_CHAMPION("Champion", "§6§lCHAMPION §6", TagRole.SPECIAL, ChatColor.GOLD),

    /* Special Role Tags */
    PARTNER("Partner", "K", "§b§lPARTNER §r§b", singletonList("partner"), RankType.PARTNER, TagRole.SPECIAL, ChatColor.AQUA),
    YT("YT", "Y", "§b§lYT §b", singletonList("yt"), RankType.PARTNER, TagRole.SPECIAL, ChatColor.AQUA),
    STREAMER("Streamer", "S", "§b§lSTREAMER §b", singletonList("streamer"), RankType.PARTNER, TagRole.SPECIAL, ChatColor.AQUA),
    PARTNER_PLUS("Partner+", "J", "§3§lPARTNER+§r§3", singletonList("partnerplus"), RankType.PARTNER_PLUS, TagRole.SPECIAL, ChatColor.DARK_AQUA),
    BUILDER("Builder", "H", "§2§lBUILDER §r§2", emptyList(), RankType.BUILDER, TagRole.SPECIAL, ChatColor.DARK_GREEN),
    STUDIO("Studio", "G", "§2§lSTUDIO §r§2", emptyList(), RankType.STUDIO, TagRole.SPECIAL, ChatColor.DARK_GREEN),

    /* Staff Role Tags */
    HELPER("Helper", "F", "§9§lHELPER §r§9", emptyList(), RankType.HELPER, TagRole.STAFF, ChatColor.BLUE),
    TRIAL("Trial", "E", "§5§lTRIAL §r§5", emptyList(), RankType.TRIAL, TagRole.STAFF, ChatColor.DARK_PURPLE),
    MOD("Moderador", "D", "§5§lMOD §r§5", emptyList(), RankType.MOD, TagRole.STAFF, ChatColor.DARK_PURPLE),
    MODPLUS("Moderador+", "C", "§5§lMOD+ §r§5", emptyList(), RankType.MODPLUS, TagRole.STAFF, ChatColor.DARK_PURPLE),
    ADMIN("Admin", "B", "§4§lADMIN §r§4", emptyList(), RankType.ADMIN, TagRole.STAFF, ChatColor.DARK_RED),
    CHEFE("Love", "A", "§d§lLOVE §d", emptyList(), RankType.CHEFE, TagRole.SPECIAL, ChatColor.LIGHT_PURPLE);

    private final String name, order, prefix;

    private final List<String> surnames;

    private final RankType source;
    private final TagRole role;

    private final ChatColor color;

    private final List<UUID> onlyThese;

    /* Custom Tag Constructor */
    Tag(String name, String order, String prefix, List<String> surnames, RankType source, TagRole role, ChatColor color) {
        this(name, order, prefix, surnames, source, role, color, new ArrayList<>());
    }

    Tag(String name, String prefix, TagRole role, ChatColor color) {
        this(name, prefix, "Y", role, color);
    }

    Tag(String name, String prefix, String order, TagRole role, ChatColor color) {
        this(name, order, prefix, emptyList(), RankType.MEMBER, role, color, new ArrayList<>());
    }

    public static Tag of(String name) {
        return Arrays.stream(values())
                .filter(tag -> tag.name().equalsIgnoreCase(name) || tag.getName().equalsIgnoreCase(name) || tag.getSurnames().stream().anyMatch(surName -> surName.equalsIgnoreCase(name)))
                .findFirst()
                .orElse(null);
    }

    public static Tag of(RankType source) {
        return Arrays.stream(values())
                .filter(tag -> tag.getSource().equals(source))
                .findFirst()
                .orElse(null);
    }

    public String getByPrefix(TagPrefix prefix) {
        if (this == SPECTATOR || this == MEMBER) return getPrefix();

        String prefixRegex = getPrefix().split(" ")[0];

        String result = prefixRegex;

        switch (prefix) {
            case DEFAULT: {
                result = getPrefix();
                break;
            }
            case COLOR: {
                result = getColor().toString();
                break;
            }
            case BRACKETS: {
                String name = isRole(TagRole.COLORED)
                        ? ChatColor.stripColor(getPrefix()).split(" ")[0]
                        : getName().toUpperCase();

                result = getColor() + "[" + name + "] ";
                break;
            }
            case BRACKETS_NORMAL_CASE: {
                String name = isRole(TagRole.COLORED)
                        ? ChatColor.stripColor(getPrefix()).split(" ")[0]
                        : getName();

                result = getColor() + "[" + name + "] ";
                break;
            }
            case DEFAULT_WHITE: {
                result = prefixRegex + " §r";
                break;
            }
            case DEFAULT_GRAY: {
                result = prefixRegex + " §r§7";
                break;
            }
        }

        return result;
    }

    public boolean isOnlyThese(UUID id) {
        return !onlyThese.isEmpty() && onlyThese.contains(id);
    }

    public boolean isRole(TagRole role) {
        return this.role.equals(role);
    }

    public String getColoredName() {
        return color + name;
    }

    public String getPermission() {
        return "tag." + name.toLowerCase();
    }
}
