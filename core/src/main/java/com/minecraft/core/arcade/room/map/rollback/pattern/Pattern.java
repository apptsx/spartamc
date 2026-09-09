package com.minecraft.core.arcade.room.map.rollback.pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Pattern {

    private Material material;
    private byte data;
    private boolean glow;

    public static List<Pattern> parse(String arg) {
        List<Pattern> patterns = new ArrayList<>();
        String[] array = arg.split(",");
        for (String string : array) {

            Material material = null;
            byte data = 0;

            if (string.contains(":")) {

                String[] blockAndData = string.split(":");

                if (isInteger(blockAndData[0]))
                    material = Material.getMaterial(Integer.parseInt(blockAndData[0]));
                else
                    material = Material.getMaterial(blockAndData[0]);

                if (blockAndData.length > 1) {
                    try {
                        data = Byte.parseByte(blockAndData[1]);
                    } catch (NumberFormatException ignored) {
                        return null;
                    }
                }

            } else {
                if (isInteger(string))
                    material = Material.getMaterial(Integer.parseInt(string));
                else
                    material = Material.getMaterial(string.toUpperCase());

            }
            if (material == null)
                return null;
            if (!material.isBlock())
                continue;
            patterns.add(new Pattern(material, data, false));
        }
        return patterns;
    }

    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static Pattern of(Material material, int bt) {
        return new Pattern(material, (byte) bt, false);
    }

    public static Pattern of(Material material, int bt, boolean glow) {
        return new Pattern(material, (byte) bt, glow);
    }

    public static Pattern of(Material material) {
        return new Pattern(material, (byte) 0, false);
    }

    public static Pattern of(Material material, boolean glow) {
        return new Pattern(material, (byte) 0, glow);
    }

    public static Pattern of(int material, int bt) {
        return new Pattern(Material.getMaterial(material), (byte) bt, false);
    }

    @Override
    public String toString() {
        return "Pattern{" +
                "material=" + material +
                ", data=" + data +
                '}';
    }
}