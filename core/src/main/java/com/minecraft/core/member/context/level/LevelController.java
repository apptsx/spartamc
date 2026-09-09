package com.minecraft.core.member.context.level;

import java.util.HashMap;
import java.util.Map;

public class LevelController {

    private static final Map<Integer, String> levelMap = new HashMap<>();

    static {
        // Níveis 1-9: §7✩
        // Níveis 10-19: §a✫
        for (int i = 1; i < 20; i++) levelMap.put(i, i < 10 ? "§7✩" : "§a✫");

        // Níveis 20-29: §b✭
        for (int i = 20; i < 30; i++) levelMap.put(i, "§b✭");

        // Níveis 30-39: §3✮
        for (int i = 30; i < 40; i++) levelMap.put(i, "§3✮");

        // Níveis 40-49: §9✯
        for (int i = 40; i < 50; i++) levelMap.put(i, "§9✯");

        // Níveis 50-59: §5✰
        for (int i = 50; i < 60; i++) levelMap.put(i, "§5✰");

        // Níveis 60-69: §d✱
        for (int i = 60; i < 70; i++) levelMap.put(i, "§d✱");

        // Níveis 70-79: §c✲
        for (int i = 70; i < 80; i++) levelMap.put(i, "§c✲");

        // Níveis 80-89: §6✳
        for (int i = 80; i < 90; i++) levelMap.put(i, "§6✳");

        // Níveis 90-99: §e✴
        for (int i = 90; i < 100; i++) levelMap.put(i, "§e✴");

        // Nível 100-199: §f✵
        for (int i = 100; i < 200; i++) levelMap.put(i, "§f✵");

        // Nível 200-299: §2✶
        for (int i = 200; i < 300; i++) levelMap.put(i, "§2✶");

        // Nível 300-399: §3✷
        for (int i = 300; i < 400; i++) levelMap.put(i, "§3✷");

        // Nível 400-499: §1✸
        for (int i = 400; i < 500; i++) levelMap.put(i, "§1✸");

        // Nível 500-599: §5✹
        for (int i = 500; i < 600; i++) levelMap.put(i, "§5✹");

        // Nível 600-699: §d✺
        for (int i = 600; i < 700; i++) levelMap.put(i, "§d✺");

        // Nível 700-799: §c✻
        for (int i = 700; i < 800; i++) levelMap.put(i, "§c✻");

        // Nível 800-899: §6✼
        for (int i = 800; i < 900; i++) levelMap.put(i, "§6✼");

        // Nível 900-999: §e✽
        for (int i = 900; i < 1000; i++) levelMap.put(i, "§e✽");

        // Nível 1000-1999: §4✾
        for (int i = 1000; i < 2000; i++) levelMap.put(i, "§4✾");

        // Nível 2000-2999: §4✿
        for (int i = 2000; i < 3000; i++) levelMap.put(i, "§4✿");

        // Nível 3000-3999: §4❀
        for (int i = 3000; i < 4000; i++) levelMap.put(i, "§4❀");

        // Nível 4000-4999: §4❁
        for (int i = 4000; i < 5000; i++) levelMap.put(i, "§4❁");

        // Nível 5000-5999: §4❂
        for (int i = 5000; i < 6000; i++) levelMap.put(i, "§4❂");

        // Nível 6000-6999: §4❃
        for (int i = 6000; i < 7000; i++) levelMap.put(i, "§4❃");

        // Nível 7000-7999: §4❇
        for (int i = 7000; i < 8000; i++) levelMap.put(i, "§4❇");

        // Nível 8000-8999: §4❈
        for (int i = 8000; i < 9000; i++) levelMap.put(i, "§4❈");

        // Nível 9000-9999: §4❉
        for (int i = 9000; i < 10000; i++) levelMap.put(i, "§4❉");

        // Nível 10000+: §4❊
        levelMap.put(10000, "§4❊");
    }

    public static String getDefaultLevelColor(int level) {
        if (level <= 0) return "§7[§7✩0§7]";

        String colorCode;
        String symbol;
        
        if (level >= 1000) {
            colorCode = "§4";
            symbol = "✾";
        } else if (level >= 100) {
            colorCode = getSymbolColor(level);
            symbol = "✵";
        } else if (level >= 10) {
            colorCode = getSymbolColor(level);
            symbol = "✫";
        } else {
            colorCode = "§7";
            symbol = "✩";
        }
        
        return colorCode + "[" + symbol + level + "]";
    }
    
    private static String getSymbolColor(int level) {
        if (level >= 1000) return "§4";
        if (level >= 100) {
            int key = (level / 100) * 100;
            switch (key) {
                case 100: return "§f";
                case 200: return "§2";
                case 300: return "§3";
                case 400: return "§1";
                case 500: return "§5";
                case 600: return "§d";
                case 700: return "§c";
                case 800: return "§6";
                case 900: return "§e";
                default: return "§f";
            }
        }
        // 10-99
        int key = (level / 10) * 10;
        switch (key) {
            case 10: return "§a";
            case 20: return "§b";
            case 30: return "§3";
            case 40: return "§9";
            case 50: return "§5";
            case 60: return "§d";
            case 70: return "§c";
            case 80: return "§6";
            case 90: return "§e";
            default: return "§a";
        }
    }
}