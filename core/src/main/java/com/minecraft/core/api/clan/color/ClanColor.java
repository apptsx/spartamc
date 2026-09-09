package com.minecraft.core.api.clan.color;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum ClanColor {

    PRETO("Preto", "§0", "0", null, "preto", "black"),
    ROXO_ESCURO("Roxo Escuro", "§1", "1", null, "roxo escuro", "dark_blue"),
    VERDE_ESCURO("Verde Escuro", "§2", "2", null, "verde escuro", "dark_green"),
    CIANO("Ciano", "§3", "3", null, "ciano", "dark_aqua"),
    VERMELHO_ESCURO("Vermelho Escuro", "§4", "4", null, "vermelho escuro", "dark_red"),
    ROXO("Roxo", "§5", "5", null, "roxo", "purple"),
    LARANJA("Laranja", "§6", "6", null, "laranja", "gold"),
    CINZA("Cinza", "§7", "7", null, "cinza", "gray"),
    CINZA_ESCURO("Preto Médio", "§8", "8", null, "cinza escuro", "dark_gray"),
    AZUL("Azul", "§9", "9", null, "azul", "blue"),
    VERDE("Verde", "§a", "a", null, "verde", "green"),
    CIANO_CLARO("Aqua", "§b", "b", null, "ciano claro", "aqua"),
    VERMELHO("Vermelho", "§c", "c", null, null, "vermelho", "red"),
    ROSA("Rosa", "§d", "d", null, null, "rosa", "light_purple", "pink"),
    AMARELO("Amarelo", "§e", "e", null, null, "amarelo", "yellow"),
    BRANCO("Branco", "§f", "f", null, null, "branco", "white"),

    BRASIL("Brasil", "§e", "brazil", "§e", "§a", "brasil", "brazil"),
    CRUZEIRO("Cruzeiro", "§9", "cruzeiro", "§9", "§f", "cruzeiro");

    private final String name;
    private final String code;
    private final String id;
    private final String gradientStart;
    private final String gradientEnd;
    private final String[] aliases;

    ClanColor(String name, String code, String id, String gradientStart, String gradientEnd, String... aliases) {
        this.name = name;
        this.code = code;
        this.id = id;
        this.gradientStart = gradientStart;
        this.gradientEnd = gradientEnd;
        this.aliases = aliases;
    }

    public boolean hasGradient() {
        return gradientStart != null && gradientEnd != null && !gradientStart.isEmpty() && !gradientEnd.isEmpty();
    }

    public boolean hasMixedColorTag() {
        return hasGradient();
    }

    public String getMixedColorTag() {
        return gradientStart != null ? gradientStart : "";
    }

    public static String getDefaultColor() {
        return "§7";
    }

    public static String parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String normalized = input.trim().toLowerCase().replace("_", " ");

        if (normalized.startsWith("§") && normalized.length() == 2) {
            char code = normalized.charAt(1);
            for (ClanColor color : values()) {
                if (color.getCode().equals("§" + code)) {
                    return color.getCode();
                }
            }
        }

        if (normalized.startsWith("&") && normalized.length() == 2) {
            char code = normalized.charAt(1);
            for (ClanColor color : values()) {
                if (color.getCode().equals("§" + code)) {
                    return color.getCode();
                }
            }
        }

        for (ClanColor color : values()) {
            if (color.getId().equalsIgnoreCase(normalized)) {
                return color.getCode();
            }
        }

        for (ClanColor color : values()) {
            if (color.getName().equalsIgnoreCase(normalized)) {
                return color.getCode();
            }
        }

        for (ClanColor color : values()) {
            for (String alias : color.getAliases()) {
                if (alias.equalsIgnoreCase(normalized)) {
                    return color.getCode();
                }
            }
        }

        return null;
    }

    public static String getName(String colorCode) {
        if (colorCode == null) {
            return "Desconhecida";
        }

        for (ClanColor color : values()) {
            if (color.getCode().equals(colorCode)) {
                return color.getName();
            }
        }

        return "Desconhecida";
    }

    public static boolean isValid(String colorCode) {
        if (colorCode == null) {
            return false;
        }

        for (ClanColor color : values()) {
            if (color.getCode().equals(colorCode)) {
                return true;
            }
        }

        return false;
    }

    public static String getFormattedList() {
        StringBuilder builder = new StringBuilder("(");
        ClanColor[] colors = values();
        
        for (int i = 0; i < colors.length; i++) {
            ClanColor color = colors[i];
            builder.append(color.getCode()).append(color.getName());
            
            if (i < colors.length - 1) {
                builder.append("§c, ");
            }
        }
        
        builder.append("§c)");
        return builder.toString();
    }

    public static ClanColor getByCode(String colorCode) {
        if (colorCode == null) {
            return null;
        }
        
        for (ClanColor color : values()) {
            if (color.getCode().equals(colorCode)) {
                return color;
            }
        }
        
        return null;
    }

//    public static ClanColor getByMixedColorTag(String mixedTag) {
//        if (mixedTag == null || mixedTag.trim().isEmpty()) {
//            return null;
//        }
//
//        String normalized = mixedTag.replace('&', '§');
//
//        for (ClanColor color : values()) {
//            if (color.hasMixedColorTag() && color.getMixedColorTag().equals(normalized)) {
//                return color;
//            }
//        }
//
//        return null;
//    }
//
//    public static List<ClanColor> getColorsWithMixedTags() {
//        return Arrays.stream(values())
//                .filter(ClanColor::hasMixedColorTag)
//                .collect(Collectors.toList());
//    }
}