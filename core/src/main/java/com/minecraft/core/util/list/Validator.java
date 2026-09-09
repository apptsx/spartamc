package com.minecraft.core.util.list;

import java.util.UUID;
import java.util.regex.Pattern;

public class Validator {

    private static final Pattern url = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$");
    private static final Pattern validUserPattern = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");

    public static boolean isUrl(String str) {
        return url.matcher(str).matches();
    }

    public static boolean isNickname(String nickname) {
        return !nickname.isEmpty() && validUserPattern.matcher(nickname).matches();
    }

    public static boolean hasNumberInString(String str) {
        return str.chars().anyMatch(Character::isDigit);
    }

    public static Pattern caseInsensitive(String content) {
        return Pattern.compile("^" + content + "$", Pattern.CASE_INSENSITIVE);
    }

    public static boolean isValidUUID(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return false;
        }
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
