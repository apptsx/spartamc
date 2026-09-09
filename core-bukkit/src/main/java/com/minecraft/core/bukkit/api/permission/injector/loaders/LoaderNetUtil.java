package com.minecraft.core.bukkit.api.permission.injector.loaders;

import com.google.common.cache.CacheLoader;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LoaderNetUtil extends CacheLoader<String, Pattern> {
    public static final String RAW_REGEX_CHAR = "$";

    protected static Pattern createPattern(final String expression) {
        try {
            return Pattern.compile(prepareRegexp(expression), 2);
        } catch (PatternSyntaxException ex) {
            return Pattern.compile(Pattern.quote(expression), 2);
        }
    }

    public static String prepareRegexp(String expression) {
        if (expression.startsWith("-")) {
            expression = expression.substring(1);
        }
        if (expression.startsWith("#")) {
            expression = expression.substring(1);
        }
        final boolean rawRegexp = expression.startsWith("$");
        if (rawRegexp) {
            expression = expression.substring(1);
        }
        return rawRegexp ? expression : expression.replace(".", "\\.").replace("*", "(.*)");
    }

    public Pattern load(final String arg0) throws Exception {
        return createPattern(arg0);
    }
}
