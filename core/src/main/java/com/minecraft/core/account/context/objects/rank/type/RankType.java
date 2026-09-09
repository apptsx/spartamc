package com.minecraft.core.account.context.objects.rank.type;

import com.minecraft.core.account.context.objects.medal.Medal;
import com.minecraft.core.account.context.objects.rank.type.role.RankRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Getter
@AllArgsConstructor
public enum RankType {

    MEMBER("Membro", RankRole.NONE, ChatColor.GRAY, emptyList()),

    /* VIP Ranks */
    BOOSTER("Star", RankRole.VIP, ChatColor.LIGHT_PURPLE, emptyList()),
    VIP("VIP", RankRole.VIP, ChatColor.GREEN, emptyList()),
    SPARTA("Spark", RankRole.VIP, ChatColor.GOLD, emptyList()),
    SHINE("Shine", RankRole.VIP, ChatColor.YELLOW, emptyList()),
    MAX("Max", RankRole.VIP, ChatColor.LIGHT_PURPLE, emptyList()),
    START("Start", RankRole.VIP, ChatColor.DARK_AQUA, emptyList()),
    MAX_PLUS("Max+", RankRole.VIP, ChatColor.DARK_PURPLE, emptyList()),
    BETA("Beta", RankRole.VIP, ChatColor.DARK_BLUE, emptyList()),

    /* Special Ranks */
    PARTNER("Partner", RankRole.SPECIAL, ChatColor.AQUA, emptyList()),
    PARTNER_PLUS("Partner+", RankRole.SPECIAL, ChatColor.AQUA, emptyList()),
    GRAMMY("Grammy", RankRole.SPECIAL, ChatColor.GOLD, emptyList()),
    BUILDER("Builder", RankRole.SPECIAL, ChatColor.DARK_GREEN, emptyList()),
    STUDIO("Studio", RankRole.SPECIAL, ChatColor.DARK_GREEN, emptyList()),

    /* Staff Ranks */
    HELPER("Helper", RankRole.STAFF, ChatColor.BLUE, emptyList()),
    TRIAL("Trial", RankRole.STAFF, ChatColor.DARK_PURPLE, emptyList()),
    MOD("Moderador", RankRole.STAFF, ChatColor.DARK_PURPLE, emptyList()),
    MODPLUS("Moderador+", RankRole.STAFF, ChatColor.DARK_PURPLE, emptyList()),
    ADMIN("Admin", RankRole.STAFF, ChatColor.DARK_RED, singletonList("*")),
    CHEFE("Love", RankRole.STAFF, ChatColor.LIGHT_PURPLE, singletonList("*"));

    private final String name;

    private final RankRole role;
    private final ChatColor color;

    private final List<String> permissions;

    public static RankType of(String name) {
        return Arrays.stream(values())
                .filter(rank -> rank.name().equalsIgnoreCase(name) || rank.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public String getColoredName() {
        return color + name;
    }

    public String getBoldColorName() {
        return color + "§l" + name;
    }

    public RankType bellow() {
        return this == MEMBER ? MEMBER : values()[ordinal() - 1];
    }

    public boolean isHigherOrEqual(RankType other) {
        return this.ordinal() >= other.ordinal();
    }

    public boolean isLowerOrEqual(RankType other) {
        return this.ordinal() <= other.ordinal();
    }

    public boolean isHigher(RankType other) {
        return this.ordinal() > other.ordinal();
    }

    public boolean isLower(RankType other) {
        return this.ordinal() < other.ordinal();
    }

    public List<Medal> getDefaultMedals() {
        // Medalhas base para ranks VIP (Booster até Max+)
        List<Medal> vipMedals = Arrays.asList(
                Medal.YIN,        // YingYang
                Medal.SNOWFLAKE,  // Neve
                Medal.WISH,       // Desejo
                Medal.BIOHAZARD,  // Bio
                Medal.SPARKLE     // Brilho
        );
        
        // Beta recebe todas as medalhas inferiores + Beta
        List<Medal> betaMedals = new java.util.ArrayList<>(vipMedals);
        betaMedals.add(Medal.BETA);
        
        // Partner recebe todas + Cristal
        List<Medal> partnerMedals = new java.util.ArrayList<>(betaMedals);
        partnerMedals.add(Medal.CRYSTAL);
        
        // Partner+ recebe todas + Diamante
        List<Medal> partnerPlusMedals = new java.util.ArrayList<>(partnerMedals);
        partnerPlusMedals.add(Medal.DIAMOND);
        
        // Helper ou superior recebe todas + Staff Desejo, Flare, Faisca, Petala
        List<Medal> staffMedals = new java.util.ArrayList<>(partnerPlusMedals);
        staffMedals.add(Medal.STAFF_WISH);  // Staff Desejo
        staffMedals.add(Medal.FLARE);       // Flare
        staffMedals.add(Medal.SPARK);       // Faisca
        staffMedals.add(Medal.PETAL);       // Petala
        staffMedals.add(Medal.STAFF_SHIELD);
        staffMedals.add(Medal.STAFF_CROSS);
        
        return switch (this) {
            case BOOSTER, VIP, SPARTA, SHINE, MAX, START, MAX_PLUS -> vipMedals;
            case BETA -> betaMedals;
            case PARTNER -> partnerMedals;
            case PARTNER_PLUS -> partnerPlusMedals;
            case HELPER, TRIAL, MOD, MODPLUS -> staffMedals;
            case ADMIN, CHEFE -> Arrays.asList(Medal.values());
            case GRAMMY, BUILDER, STUDIO -> partnerPlusMedals;
            default -> emptyList();
        };
    }
}