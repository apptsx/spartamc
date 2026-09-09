package com.minecraft.core.util.list;

import java.text.DecimalFormat;

public class TimeUtil {

    public static long convertTime(long days, long hours, long minutes, long seconds) {
        long day = 86400 * days;
        long hour = 3600 * hours;
        long minute = 60 * minutes;

        long time = minute + hour + day + seconds;

        return System.currentTimeMillis() + time * 1000L;
    }

    public static long getTime(String timeFormat) {
        if (timeFormat.equals("-1")
                || timeFormat.equals("-1L") || timeFormat.equalsIgnoreCase("never") || timeFormat.equalsIgnoreCase("n"))
            return -1L;

        String[] times = timeFormat.split(",");

        int day = 0, hour = 0, minute = 0, second = 0;

        for (String time : times) {
            time = time.toLowerCase();

            if (time.contains("d")) {
                day = Integer.parseInt(time.replace("d", ""));
            }

            if (time.contains("h")) {
                hour = Integer.parseInt(time.replace("h", ""));
            }

            if (time.contains("m")) {
                minute = Integer.parseInt(time.replace("m", ""));
            }

            if (time.contains("s")) {
                second = Integer.parseInt(time.replace("s", ""));
            }
        }

        return convertTime(day, hour, minute, second);
    }

    public static boolean isValidTime(String timeFormat) {
        try {
            getTime(timeFormat);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String formatTimeInDays(long time) {
        long now = System.currentTimeMillis();
        long diff = time - now;

        if (diff < 0) {
            return "Tempo expirado";
        }

        int seconds = (int) (diff / 1000L);

        if (seconds >= 86400) {
            int days = seconds / 86400;

            return days + " dia" + (days > 1 ? "s" : "");
        } else if (seconds >= 3600) {
            int hours = seconds / 3600;

            return hours + " hora" + (hours > 1 ? "s" : "");
        } else if (seconds >= 60) {
            int minutes = seconds / 60;

            return minutes + " minuto" + (minutes > 1 ? "s" : "");
        } else {
            return seconds + " segundo" + (seconds > 1 ? "s" : "");
        }
    }

    public static int longToSeconds(long time) {
        long now = System.currentTimeMillis(), diff = time - now;

        return (int) (diff / 1000L);
    }

    public static String formatTime(long time) {
        return formatTime(time, TimeFormat.NORMAL);
    }

    public static String formatTime(long time, TimeFormat format) {
        String message = "";

        if (time <= -1L) return "Eterno";

        long now = System.currentTimeMillis(), diff = time - now;

        int seconds = (int) (diff / 1000L);

        boolean hasDay = seconds >= 86400L, hasHour = seconds >= 3600, hasMinute = seconds >= 60, hasSeconds = seconds >= 1;

        boolean isNormalFormat = format.equals(TimeFormat.NORMAL);

        if (hasDay) {
            int day = seconds / 86400;

            seconds %= 86400;

            message = day + (isNormalFormat ? (" dia" + (day > 1 ? "s" : "")) : "d");
        }

        if (hasHour) {
            int hour = seconds / 3600;

            if (hour != 0) {
                seconds %= 3600;

                message = message + (hasDay ? ", " : "") + hour + (isNormalFormat ? (" hora" + (hour > 1 ? "s" : "")) : "h");
            }
        }

        if (hasMinute) {
            int min = seconds / 60;

            if (min != 0) {
                seconds %= 60;

                message = message + (hasHour ? ", " : "") + min + (isNormalFormat ? (" minuto" + (min > 1 ? "s" : "")) : "m");
            }
        }

        if (hasSeconds && seconds != 0) {
            message = message + (hasMinute ? ", " : "") + seconds + (isNormalFormat ? (" segundo" + (seconds > 1 ? "s" : "")) : "s");
        }

        return message;
    }

    public static String time(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    public static String formatCooldown(long time) {
        long now = System.currentTimeMillis();
        long diff = time - now;

        if (diff < 0)
            return "0s";

        long seconds = diff / 1000;
        long millis = diff % 1000; // Milissegundos restantes após calcular os segundos
        long minutes = seconds / 60;
        seconds %= 60;

        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        StringBuilder builder = new StringBuilder();

        if (minutes > 0)
            builder.append(minutes).append("m ");

        if (seconds > 0 || millis > 0) {
            builder.append(seconds);

            if (millis > 0) {
                String text = decimalFormat.format((double) millis / 1000);

                if (text.length() > 2)
                    builder.append(",").append(text.substring(2)); // Removendo o zero à esquerda
            }

            builder.append("s");
        }

        return builder.toString();
    }

    public static String formatTime(int seconds) {
        return formatTime(seconds, TimeFormat.SHORT);
    }

    public static String formatTime(int seconds, TimeFormat format) {
        StringBuilder result = new StringBuilder();

        int minutes = seconds / 60;
        boolean isPresentMinute = minutes > 0, isNormal = format.equals(TimeFormat.NORMAL);

        if (isPresentMinute)
            result.append(minutes).append(isNormal ? " " : "")
                    .append("m")
                    .append(isNormal ? "inuto" + (minutes > 1 ? "s" : "") : "");

        seconds = seconds % 60;

        if (seconds > 0)
            result.append(isPresentMinute ? ", " : "").append(seconds).append(isNormal ? " " : "")
                    .append("s")
                    .append(isNormal ? "egundo" + (seconds > 1 ? "s" : "") : "");

        return result.toString();
    }

    public enum TimeFormat {NORMAL, SHORT}
}
