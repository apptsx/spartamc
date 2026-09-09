package com.minecraft.core.util.list;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static String getCurrentDate() {
        return new SimpleDateFormat("dd/MM/yy").format(new Date());
    }

    public static String getDate(long millis) {
        return getDate(millis, DateFormat.BIG, true);
    }

    public static String getDate(long millis, DateFormat format, boolean withHoursAndMinutes) {
        String date = new SimpleDateFormat("dd/MM/yy" + (withHoursAndMinutes ? (format.equals(DateFormat.BIG) ? " - " : " ") + "HH:mm" : ""))
                .format(new Date(millis));

        if (withHoursAndMinutes) {
            date = date.replace("-", "às");
        }

        return date;
    }

    public static String getSimpleDate(long millis) {
        return getDate(millis, DateFormat.SMALL, true);
    }

    public static String formatDate(long time) {
        return formatDate(time, DateFormat.SMALL);
    }

    public static String formatDate(long time, DateFormat format) {
        boolean big = format.equals(DateFormat.BIG);

        long diff = System.currentTimeMillis() - time;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long years = days / 365;

        seconds %= 60; // Restante dos segundos após subtrair os minutos completos
        minutes %= 60; // Restante dos minutos após subtrair as horas completas
        hours %= 24; // Restante das horas após subtrair os dias completos
        days %= 365; // Restante dos dias após subtrair os anos completos

        StringBuilder result = new StringBuilder();

        if (years > 0)
            result.append(years).append(big ? "anos" : "a");

        if (days > 0)
            result.append(years > 0 ? " " : "").append(days).append(big ? "dias" : "d");

        if (hours > 0)
            result.append(days > 0 ? " " : "").append(hours).append(big ? "horas" : "h");

        if (minutes > 0)
            result.append(hours > 0 ? " " : "").append(minutes).append(big ? "minutos" : "m");

        if (seconds > 0)
            result.append((hours > 0 || minutes > 0) ? " " : "").append(seconds).append(big ? "segundos" : "s");

        return result.toString();
    }

    public enum DateFormat {
        BIG, SMALL
    }
}
