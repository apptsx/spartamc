package com.minecraft.core.bukkit.api.permission.injector;

import com.minecraft.core.bukkit.api.permission.injector.loaders.LoaderNetUtil;
import com.minecraft.core.bukkit.api.permission.injector.loaders.LoaderNormal;
import lombok.val;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;

public class RegExpMatcher implements PermissionMatcher {

    private final Pattern rangeExpression = Pattern.compile("(\\d+)-(\\d+)");

    private Object patternCache;

    public RegExpMatcher() {
        val cacheBuilder = this.getClassGuava("com.google.common.cache.CacheBuilder");
        val cacheLoader = this.getClassGuava("com.google.common.cache.CacheLoader");
        try {
            val obj = cacheBuilder.getMethod("newBuilder").invoke(null);
            val maximumSize = obj.getClass().getMethod("maximumSize", Long.TYPE);
            val obj2 = maximumSize.invoke(obj, 500);
            val loader = this.hasNetUtil() ? new LoaderNetUtil() : new LoaderNormal();
            val build = obj2.getClass().getMethod("build", cacheLoader);

            this.patternCache = build.invoke(obj2, loader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isMatches(String expression, String permission) {
        try {
            val get = this.patternCache.getClass().getMethod("get", Object.class);
            get.setAccessible(true);
            val obj = get.invoke(this.patternCache, expression);
            return ((Pattern) obj).matcher(permission).matches();
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Class<?> getClassGuava(String str) {
        Class<?> guavaClass = null;
        try {
            if (this.hasNetUtil()) {
                str = "net.minecraft.util." + str;
            }
            guavaClass = Class.forName(str);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return guavaClass;
    }

    private boolean hasNetUtil() {
        try {
            Class.forName("net.minecraft.util.com.google.common.cache.LoadingCache");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
