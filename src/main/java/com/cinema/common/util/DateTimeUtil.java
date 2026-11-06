package com.cinema.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Tiá»‡n Ã­ch Ä‘á»‹nh dáº¡ng vÃ  phÃ¢n tÃ­ch chuá»—i thá»i gian chuáº©n Viá»‡t Nam vÃ  ISO.
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
public final class DateTimeUtil {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DB_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtil() {}

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    public static String formatTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(TIME_FORMATTER);
    }

    public static LocalDateTime parseDbDateTime(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        return LocalDateTime.parse(text, DB_DATETIME_FORMATTER);
    }
}

