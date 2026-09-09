package com.minecraft.core.util.list.serialization;

import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Serialization {

    /**
     * Converts the player inventory to a Base64 encoded string.
     *
     * @param playerInventory to turn into an array of strings.
     * @return string with serialized Inventory
     */
    @SneakyThrows
    public static String playerInventoryToBase64(PlayerInventory playerInventory) {
        // This contains contents, armor and offhand (contents are indexes 0 - 35, armor 36 - 39, offhand - 40)
        return itemStackArrayToBase64(playerInventory.getContents());
    }

    /**
     * A method to serialize an {@link ItemStack} array to Base64 String.
     *
     * @param items to turn into a Base64 String.
     * @return Base64 string of the items.
     */
    @SneakyThrows
    public static String itemStackArrayToBase64(List<ItemStack> items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.size());

            for (ItemStack item : items) {
                if (item != null) {
                    dataOutput.writeObject(item.serializeAsBytes());
                } else {
                    dataOutput.writeObject(null);
                }
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    /**
     * A method to serialize an {@link ItemStack} array to Base64 String.
     *
     * @param items to turn into a Base64 String.
     * @return Base64 string of the items.
     */
    @SneakyThrows
    public static String itemStackArrayToBase64(ItemStack... items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                if (item != null) {
                    dataOutput.writeObject(item.serializeAsBytes());
                } else {
                    dataOutput.writeObject(null);
                }
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    /**
     * Gets an array of ItemStacks from Base64 string.
     *
     * @param base64 Base64 string to convert to ItemStack array.
     * @return ItemStack array created from the Base64 string.
     */
    @SneakyThrows
    public static List<ItemStack> itemStackArrayFromBase64(String base64) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack[] items = new ItemStack[dataInput.readInt()];

            for (int Index = 0; Index < items.length; Index++) {
                byte[] stack = (byte[]) dataInput.readObject();

                if (stack != null) {
                    items[Index] = ItemStack.deserializeBytes(stack);
                } else {
                    items[Index] = null;
                }
            }

            dataInput.close();

            return Arrays.asList(items);
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class list.", e);
        }
    }

    public static List<ItemStack> itemStackFromData(String data) {
        List<ItemStack> items = new ArrayList<>();

        String[] base64 = data.split(" @ ");

        String hotBarData = base64[0], internalData = base64[1];

        List<ItemStack> hotbar = itemStackArrayFromBase64(hotBarData), internal = itemStackArrayFromBase64(internalData);

        items.addAll(hotbar);
        items.addAll(internal);

        return items;
    }

    public static void sendInventoryToPlayerFromBase64WithColor(Player player, String base64, Material material, int colorId) {
        PlayerInventory inv = player.getInventory();

        inv.clear();

        String[] data = base64.split(" @ ");

        if (data.length < 1) {
            player.sendMessage("§cNão foi possível renderizar o seu inventário!");
            return;
        }

        String internal = data[0], hotBar = data[1];

        List<ItemStack> internalContents = itemStackArrayFromBase64(internal);
        List<ItemStack> hotBarContents = itemStackArrayFromBase64(hotBar);

        int slot = 9;
        for (ItemStack content : internalContents) {
            if (content == null) continue;

            if (slot > 35) break;

            if (content.getType().equals(material))
                content.setDurability((short) colorId);

            inv.setItem(slot, content);
            slot++;
        }

        slot = 0;
        for (ItemStack content : hotBarContents) {
            if (content == null) continue;

            if (slot > 9) break;

            if (content.getType().equals(material))
                content.setDurability((short) colorId);

            inv.setItem(slot, content);
            slot++;
        }
    }

    public static String serializeInventoryOfPlayer(Player player) {
        List<ItemStack> items = Arrays.asList(player.getInventory().getContents());

        List<ItemStack> hotBar = items.subList(0, 9);

        String hotbarSerialized = Serialization.itemStackArrayToBase64(hotBar);

        List<ItemStack> internalItems = items.subList(9, 36); // Ajustado o índice inicial para 9

        String internalSerialized = Serialization.itemStackArrayToBase64(internalItems);

        return internalSerialized + " @ " + hotbarSerialized;
    }

    public static void sendInventoryToPlayerFromBase64(Player player, String base64) {
        PlayerInventory inv = player.getInventory();

        inv.clear();

        String[] data = base64.split(" @ ");

        if (data.length < 1) {
            player.sendMessage("§cNão foi possível renderizar o seu inventário!");
            return;
        }

        String internal = data[0], hotBar = data[1];

        List<ItemStack> internalContents = itemStackArrayFromBase64(internal);
        List<ItemStack> hotBarContents = itemStackArrayFromBase64(hotBar);

        int slot = 9;
        for (ItemStack content : internalContents) {
            if (slot > 35) break;

            inv.setItem(slot, content);
            slot++;
        }

        slot = 0;
        for (ItemStack content : hotBarContents) {
            if (slot > 9) break;

            inv.setItem(slot, content);
            slot++;
        }
    }
}