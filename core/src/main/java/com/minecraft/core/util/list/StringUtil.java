package com.minecraft.core.util.list;

import com.minecraft.core.Core;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtil {

    static String NUMERIC_CHARS = "0123456789";
    static String LETTERS_LOWER_CHARS = "abcdefghijklmnopqrstuvwxyz";
    static String LETTERS_UPPER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static String SYMBOLS_CHARS = "#$%*&_+=^?/";
    static String FULL_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            /* letters */ + "abcdefghijklmnopqrstuvwxyz"
            /* numeric */ + "0123456789"
            /* symbols */ + "#$%*&_+=^?/";
    static String EXCLUSIVE_CHARS = "abcdefghijklmnopqrstuvwxyz"
            /* numeric */ + "0123456789";

    public static String generateCode() {
        return generateCode(8);
    }

    public static String generateLetterCode(int length) {
        return Core.RANDOM.ints(length, 0, LETTERS_LOWER_CHARS.length())
                .mapToObj(index -> "" + LETTERS_LOWER_CHARS.charAt(index)).collect(Collectors.joining());
    }

    public static String generateExclusiveCode(int length) {
        return Core.RANDOM.ints(length, 0, EXCLUSIVE_CHARS.length())
                .mapToObj(index -> "" + EXCLUSIVE_CHARS.charAt(index)).collect(Collectors.joining());
    }

    public static String generateCode(int length) {
        return Core.RANDOM.ints(length, 0, FULL_CHARS.length())
                .mapToObj(index -> "" + FULL_CHARS.charAt(index)).collect(Collectors.joining());
    }

    public static String generateNumberCode(int length) {
        return Core.RANDOM.ints(length, 0, NUMERIC_CHARS.length())
                .mapToObj(index -> "" + NUMERIC_CHARS.charAt(index)).collect(Collectors.joining());
    }

    public static String generateSymbolsCode(int length) {
        return Core.RANDOM.ints(length, 0, SYMBOLS_CHARS.length())
                .mapToObj(index -> "" + SYMBOLS_CHARS.charAt(index)).collect(Collectors.joining());
    }

    public static List<String> formatForLore(String text) {
        return getLore(30, text);
    }

    public static List<String> getLore(int max, String text) {
        List<String> lore = new ArrayList<>();
        text = ChatColor.translateAlternateColorCodes('&', text);
        String[] split = text.split(" ");
        String color = "";
        text = "";
        for (int i = 0; i < split.length; i++) {
            if (ChatColor.stripColor(text).length() >= max || ChatColor.stripColor(text).endsWith(".")
                    || ChatColor.stripColor(text).endsWith("!")) {
                lore.add(text);
                if (text.endsWith(".") || text.endsWith("!"))
                    lore.add("");
                text = color;
            }
            String toAdd = split[i];
            if (toAdd.contains("§"))
                color = ChatColor.getLastColors(toAdd.toLowerCase());
            if (toAdd.contains("\n")) {
                toAdd = toAdd.substring(0, toAdd.indexOf("\n"));
                split[i] = split[i].substring(toAdd.length() + 1);
                lore.add(text + (text.isEmpty() ? "" : " ") + toAdd);
                text = color;
                i--;
            } else {
                text += (ChatColor.stripColor(text).isEmpty() ? "" : " ") + toAdd;
            }
        }
        lore.add(text);
        return lore;
    }

    public enum StringHelper {
        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),
        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),
        LEFT_PARENTHESIS('(', 4),
        RIGHT_PERENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),
        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 4),
        COLORCODE('§', 0);

        private final static int CENTER_MOTD_PX = 132;
        private final static int CENTER_CHAT_PX = 154;
        private final char character;
        private final int length;

        StringHelper(char character, int length) {
            this.character = character;
            this.length = length;
        }

        public static StringHelper getDefaultFontInfo(char c) {
            for (StringHelper dFI : values()) {
                if (dFI.getCharacter() == c) {
                    return dFI;
                }
            }
            return DEFAULT;
        }

        public static String makeCenteredMotd(String message) {
            int messagePxSize = 0;
            boolean previousCode = false;
            boolean isBold = false;
            char[] arrayOfChar;
            int i = (arrayOfChar = message.toCharArray()).length;
            for (int j = 0; j < i; j++) {
                char c = arrayOfChar[j];
                if (c == '§') {
                    previousCode = true;
                } else if (previousCode) {
                    previousCode = false;
                    if ((c == 'l') || (c == 'L')) {
                        isBold = true;
                    } else {
                        isBold = false;
                    }
                } else {
                    StringHelper dFI = getDefaultFontInfo(c);
                    messagePxSize += (isBold ? dFI.getBoldLength() : dFI.getLength());
                    messagePxSize++;
                }
            }
            int halvedMessageSize = messagePxSize / 2;
            int toCompensate = CENTER_MOTD_PX - halvedMessageSize;
            int spaceLength = SPACE.getLength() + 1;
            int compensated = 0;
            StringBuilder sb = new StringBuilder();
            while (compensated < toCompensate) {
                sb.append(" ");
                compensated += spaceLength;
            }
            return sb + message;
        }

        public static String makeCenteredMessage(String message) {
            int messagePxSize = 0;
            boolean previousCode = false;
            boolean isBold = false;
            char[] arrayOfChar;
            int i = (arrayOfChar = message.toCharArray()).length;
            for (int j = 0; j < i; j++) {
                char c = arrayOfChar[j];
                if (c == '§') {
                    previousCode = true;
                } else if (previousCode) {
                    previousCode = false;
                    if ((c == 'l') || (c == 'L')) {
                        isBold = true;
                    } else {
                        isBold = false;
                    }
                } else {
                    StringHelper dFI = getDefaultFontInfo(c);
                    messagePxSize += (isBold ? dFI.getBoldLength() : dFI.getLength());
                    messagePxSize++;
                }
            }
            int halvedMessageSize = messagePxSize / 2;
            int toCompensate = CENTER_CHAT_PX - halvedMessageSize;
            int spaceLength = SPACE.getLength() + 1;
            int compensated = 0;
            StringBuilder sb = new StringBuilder();
            while (compensated < toCompensate) {
                sb.append(" ");
                compensated += spaceLength;
            }
            return sb + message;
        }

        public char getCharacter() {
            return this.character;
        }

        public int getLength() {
            return this.length;
        }

        public int getBoldLength() {
            if (this == SPACE) {
                return this.getLength();
            }
            return this.length + 1;
        }
    }
}
