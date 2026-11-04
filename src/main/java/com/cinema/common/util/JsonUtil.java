package com.cinema.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tiá»‡n Ã­ch chuyá»ƒn Ä‘á»•i Ä‘á»‘i tÆ°á»£ng Java <-> chuá»—i JSON thÃ´ng qua Google Gson.
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
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

