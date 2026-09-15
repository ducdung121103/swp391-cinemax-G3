package com.cinema.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tiện ích chuyển đổi đối tượng Java <-> chuỗi JSON thông qua Google Gson.
 */
public final class JsonUtil {
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
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
