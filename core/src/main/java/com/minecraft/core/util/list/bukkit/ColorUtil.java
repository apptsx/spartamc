package com.minecraft.core.util.list.bukkit;

import net.md_5.bungee.api.ChatColor;

public class ColorUtil {

    public static int getIdByColor(ChatColor color) {
        if (color == null) return 0; // Fallback para branco
        
        // Mapeamento de ChatColor para data value da lã (wool)
        // Data values: 0=branco, 1=laranja, 2=magenta, 3=azul claro, 4=amarelo, 5=verde, 
        // 6=rosa, 7=cinza, 8=cinza claro, 9=ciano, 10=roxo, 11=azul, 12=marrom, 
        // 13=verde escuro, 14=vermelho, 15=preto
        if (color.equals(ChatColor.RED)) {
            return 14; // Vermelho
        } else if (color.equals(ChatColor.BLUE)) {
            return 11; // Azul
        } else if (color.equals(ChatColor.GREEN)) {
            return 5; // Verde
        } else if (color.equals(ChatColor.YELLOW)) {
            return 4; // Amarelo
        } else if (color.equals(ChatColor.LIGHT_PURPLE)) {
            return 6; // Rosa/Magenta
        } else if (color.equals(ChatColor.GOLD)) {
            return 1; // Laranja
        } else if (color.equals(ChatColor.AQUA)) {
            return 9; // Ciano
        } else if (color.equals(ChatColor.DARK_GRAY)) {
            return 7; // Cinza
        } else if (color.equals(ChatColor.WHITE)) {
            return 0; // Branco
        } else if (color.equals(ChatColor.DARK_PURPLE)) {
            return 10; // Roxo
        }
        
        // Fallback para branco se a cor não for encontrada
        return 0;
    }

    public static String getColorName(ChatColor color) {
        if (color == null) return "Desconhecida";
        
        if (color.equals(ChatColor.RED)) {
            return "Vermelho";
        } else if (color.equals(ChatColor.BLUE)) {
            return "Azul";
        } else if (color.equals(ChatColor.GREEN)) {
            return "Verde";
        } else if (color.equals(ChatColor.YELLOW)) {
            return "Amarelo";
        } else if (color.equals(ChatColor.LIGHT_PURPLE)) {
            return "Rosa";
        } else if (color.equals(ChatColor.WHITE)) {
            return "Branco";
        } else if (color.equals(ChatColor.GOLD)) {
            return "Laranja";
        } else if (color.equals(ChatColor.DARK_PURPLE)) {
            return "Roxo";
        } else if (color.equals(ChatColor.AQUA)) {
            return "Ciano";
        } else if (color.equals(ChatColor.DARK_GRAY)) {
            return "Cinza";
        }
        
        return "Desconhecida";
    }
}
