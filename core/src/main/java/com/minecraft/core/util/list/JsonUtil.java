package com.minecraft.core.util.list;

import com.minecraft.core.Core;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    public static JsonObject jsonTree(Object src) {
        return Core.GSON.toJsonTree(src).getAsJsonObject();
    }

    public static String elementToString(JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                return primitive.getAsString();
            }
        }
        return Core.GSON.toJson(element);
    }

    public static <T> T mapToObject(Map<String, String> map, Class<T> tClass) {
        JsonObject object = new JsonObject();

        map.forEach((key, value) -> {
            try {
                object.add(key, Core.PARSER.parse(value));
            } catch (Exception e) {
                object.addProperty(key, value);
            }
        });

        try {
            return Core.GSON.fromJson(object, tClass);
        } catch (Exception e) {
            if (e.getCause() instanceof ClassNotFoundException || e.getCause() instanceof NoClassDefFoundError || e.getCause() instanceof ClassNotFoundException) {
                
                Core.getLogger().severe("[JsonUtil] Erro ao deserializar " + tClass.getSimpleName() + ": Classe não encontrada no classpath");
                Core.getLogger().severe("[JsonUtil] Erro: " + e.getMessage());
                if (e.getCause() != null) {
                    Core.getLogger().severe("[JsonUtil] Causa: " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
                }
                return null;
            }
            throw new RuntimeException("Erro ao deserializar " + tClass.getSimpleName(), e);
        }
    }

    public static Map<String, String> objectToMap(Object src) {
        Map<String, String> map = new HashMap<>();

        try {
            JsonObject object = (JsonObject) Core.GSON.toJsonTree(src);
            object.entrySet().forEach(entry -> map.put(entry.getKey(), Core.GSON.toJson(entry.getValue())));
        } catch (Exception ignored) {
        }

        return map;
    }
}
