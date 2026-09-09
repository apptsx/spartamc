package com.minecraft.core.bukkit.api.option;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Minecraft server options.
 */
public class ServerOptions {
    public static boolean DAMAGE_ENABLED = false;

    public static boolean DROP_ITEM_ENABLED = false;

    public static boolean DEFAULT_CHAT = true;
    public static boolean CHAT_ENABLED = true;

    public static boolean DEFAULT_TAB_LIST = true;
    public static boolean CHANGE_SKIN_AND_TAG_IN_FAKE = true;

    public static boolean SPAWN_CREATURES = false;
    public static boolean FOOD_ENABLED = false;

    public static boolean ALWAYS_DAY = true;
    public static boolean WEATHER_ENABLED = false;

    public static boolean BLOCK_PLACE = false;
    public static boolean TNT_PLACE = false;
    public static boolean DEFAULT_BLOCK_BREAK = false;
    public static boolean BLOCK_BREAK = false;
    public static boolean BLOCK_IGNITE = false;
    public static boolean BLOCK_EXPLODE = false;

    public static boolean ITEM_DAMAGE = true;
    public static boolean BUCKET_ENABLED = false;

    public static boolean AUTO_CLEAR_DROPS = true;

    public static boolean BLOCK_INTERACTION_ENABLED = false;
    public static List<Material> NON_INTERACTIVE_BLOCKS = Arrays.asList(
            Material.FURNACE, Material.ENDER_CHEST, Material.MINECART,
            Material.CHEST, Material.BED_BLOCK, Material.WORKBENCH,
            Material.BOAT, Material.HOPPER, Material.COMMAND, Material.ANVIL);

    public static boolean SPECTATOR_ENABLED = false;

    public static int BED_PROTECTION_RANGE = 5;

    public static boolean BLOCK_TEMPORARY = false;
    public static List<Material> TEMPORARY_BLOCKS = new ArrayList<>();
}