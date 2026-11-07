package com.cinema.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Tiện ích chuyển đổi đối tượng Java <-> chuỗi JSON thông qua Google Gson.
 * Tương thích Java 17+ cho java.time (LocalDateTime, LocalDate).
 */
public final class JsonUtil {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src != null ? src.format(ISO_FORMATTER) : ""))
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src != null ? src.toString() : ""))
            .serializeNulls()
            .create();

    private JsonUtil() {}

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}


