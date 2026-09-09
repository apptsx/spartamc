package com.minecraft.core.util.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scroller {

    private final String text;
    private final List<String> frames;

    private int index;
    private boolean bool;

    public Scroller(String text, String c1, String c2, String c3) {
        this(text, c1, c2, c3, 12);
    }

    public Scroller(String text, String c1, String c2, String c3, int p) {
        this.text = text;
        this.frames = new ArrayList<>();

        createFrames(c1, c2, c3, p);
    }

    private void createFrames(String c1, String c2, String c3, int p) {
        if (text != null && !text.isEmpty()) {
            // Onda 1
            for (int i = 0; i < text.length(); i++)
                if (text.charAt(i) != ' ')
                    frames.add(c1 + text.substring(0, i) + c2 + text.charAt(i) + c3 + text.substring(i + 1));

            for (int i = 0; i < p; i++)
                frames.add(c1 + text);

            for (int i = 0; i < text.length(); i++)
                if (text.charAt(i) != ' ')
                    frames.add(c3 + text.substring(0, i) + c2 + text.charAt(i) + c1 + text.substring(i + 1));

            // Piscadinha
            String c3Blink = c3 + text;
            String whiteBlink = "§f§l" + text;

            for (int i = 0; i < 7; i++)
                frames.add(c3Blink);

            frames.addAll(Arrays.asList(
                    whiteBlink,
                    c3Blink,
                    whiteBlink,
                    c3Blink,
                    whiteBlink,
                    c3Blink
            ));
        }
    }

    public String next() {
        if (frames.isEmpty())
            return "";

        if (bool) {
            index--;
            if (index <= 0)
                bool = false;
        } else {
            index++;
            if (index >= frames.size()) {
                bool = true;
                return next();
            }
        }

        return frames.get(index);
    }
}