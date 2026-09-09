package com.minecraft.lobby.menu.shop.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;

public class ImageCreator {

    public static BufferedImage generateQR(String data) throws Exception {
        BitMatrix matrix = new MultiFormatWriter()
                .encode(data, BarcodeFormat.QR_CODE, 128, 128);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    @SuppressWarnings("deprecation")
    public static ItemStack generateMap(BufferedImage image, Player player) {
        String mapMaterial = Material.getMaterial("FILLED_MAP") != null ? "FILLED_MAP" : "MAP";
        ItemStack itemStack = new ItemStack(Material.getMaterial(mapMaterial));

        MapView mapView = Bukkit.createMap(player.getWorld());
        mapView.setScale(MapView.Scale.CLOSEST);
        mapView.getRenderers().clear();
        mapView.addRenderer(new MapRenderer() {
            @Override
            public void render(MapView view, MapCanvas canvas, Player p) {
                canvas.drawImage(0, 0, image);
            }
        });

        if (Material.getMaterial("FILLED_MAP") != null) {
            MapMeta meta = (MapMeta) itemStack.getItemMeta();
            try {
                MapMeta.class.getMethod("setMapView", MapView.class).invoke(meta, mapView);
            } catch (Exception e) {
                itemStack.setDurability((short) mapView.getId());
            }
            itemStack.setItemMeta(meta);
        } else {
            itemStack.setDurability((short) mapView.getId());
        }

        return itemStack;
    }
}
