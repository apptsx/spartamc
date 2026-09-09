package com.minecraft.core.util;

import com.google.common.base.Strings;
import net.md_5.bungee.api.ChatColor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static String formatNumber(double value) {
        return new DecimalFormat("#,###.#", new DecimalFormatSymbols(Locale.US)).format(value);
    }

    public static float clampYaw(float yaw) {
        while (yaw < -180.0F) {
            yaw += 360.0F;
        }
        while (yaw >= 180.0F) {
            yaw -= 360.0F;
        }

        return yaw;
    }

    public static String formatNumberWithLetter(double value) {
        if (value >= 1000000) {
            return new DecimalFormat("#,###.#m", new DecimalFormatSymbols(Locale.US)).format(value / 1000000);
        } else if (value >= 1000) {
            return new DecimalFormat("#,###.#k", new DecimalFormatSymbols(Locale.US)).format(value / 1000);
        } else {
            return new DecimalFormat("#,###.#", new DecimalFormatSymbols(Locale.US)).format(value);
        }
    }

    public static String formatNumberWithComma(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator(',');

        DecimalFormat decimalFormat = new DecimalFormat("#.0", symbols);

        return decimalFormat.format(value);
    }

    public static String formatDouble(double number) {
        DecimalFormat def = new DecimalFormat("#,###.###", new DecimalFormatSymbols(Locale.US));
        return def.format(number);
    }

    public static String formatInstant(Instant instant) {
        return Duration.between(instant, Instant.now()).toMillis() + "ms";
    }

    public static boolean isNumber(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int sum(List<Integer> integers) {
        int count = 0;

        if (integers.isEmpty()) {
            return 0;
        }

        for (int anInt : integers) {
            count += anInt;
        }

        return count;
    }

    public static int percentage(int current, int max) {
        if (max <= 0) return 0;

        return percentage(current, max, 100);
    }

    public static int percentage(int current, int max, int divisor) {
        return Math.min(current * divisor / max, divisor);
    }

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static boolean hasColor(String message) {
        String pattern = "([&§][0-9A-Fa-f])";

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(message);

        return matcher.find();
    }

    public static String extractColor(String message) {
        String pattern = "([&§][0-9A-Fa-f])";

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(message);

        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            result.append(matcher.group());
        }

        return result.toString();
    }

    public static String createProgressBar(ChatColor color, ChatColor otherColor, String symbol, int current, int max, int totalBars) {
        return createProgressBar(color, otherColor, symbol, current, max, totalBars, true);
    }

    public static String createProgressBar(ChatColor color, ChatColor otherColor, String symbol, int current, int max, int totalBars, boolean mColor) {
        float percent = (float) current / max;

        percent = Math.min(percent, 1.0f);  // Garante que percent não passe de 1
        int progressBars = (int) (totalBars * percent);

        String progressBar = "";
        if (mColor) {
            progressBar += "§m";
        }
        progressBar += color.toString();

        progressBar += Strings.repeat(symbol, progressBars);

        if (otherColor != null) {
            progressBar += otherColor.toString();
            progressBar += Strings.repeat(symbol, totalBars - progressBars);
        }

        return progressBar;
    }

    public static String createProgressBar(ChatColor color, ChatColor otherColor, String symbol, int current, int max, int totalBars, boolean mColor, int maxTeamLength) {
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);

        String progressBarString = Strings.repeat(color + (mColor ? "§m" : "") + symbol, progressBars)
                + Strings.repeat(otherColor + (mColor ? "§m" : "") + symbol, totalBars - progressBars);

        // Limita o comprimento do progressBarString
        if (progressBarString.length() > maxTeamLength) {
            progressBarString = progressBarString.substring(0, maxTeamLength);
        }

        return progressBarString;
    }
}
