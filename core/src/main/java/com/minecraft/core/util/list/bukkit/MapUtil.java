package com.minecraft.core.util.list.bukkit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.room.map.area.Cuboid;
import com.minecraft.core.arcade.room.map.location.SignedLocation;
import com.minecraft.core.arcade.room.map.location.SyntheticLocation;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MapCanvas;
import org.bukkit.util.Vector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;

public class MapUtil {

    @SuppressWarnings("deprecation")
    @SneakyThrows
    public static void buildMapWithImage(int slot, Player player, File imageFile) {
        // Carregar a imagem do arquivo
        BufferedImage image = ImageIO.read(imageFile);

        if (image == null) {
            Core.getLogger().warning("Não foi possível renderizar a imagem do mapa!");
            return;
        }

        // Criar um novo mapa
        MapView mapView = Bukkit.getServer().createMap(player.getWorld());

        // Limpar todos os renderizadores existentes
        mapView.getRenderers().forEach(mapView::removeRenderer);

        // Adicionar um renderizador personalizado para desenhar a imagem no mapa
        mapView.addRenderer(new MapRenderer() {
            @Override
            public void render(MapView view, MapCanvas canvas, Player player) {
                // Redimensionar a imagem para o tamanho do mapa (128x128)
                BufferedImage resizedImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                resizedImage.createGraphics().drawImage(image, 0, 0, 128, 128, null);

                // Desenhar a imagem no mapa
                canvas.drawImage(0, 0, resizedImage);
            }
        });

        // Enviar o mapa para a mão do jogador
        ItemStack mapItem = new ItemStack(Material.MAP);
        mapItem.setDurability(mapView.getId());

        player.getInventory().setItem(slot, mapItem);
    }

    public static boolean isNearPortal(Location location, int radius) {
        Set<Block> blocks = Cuboid.getBlocksFromCenter(location, radius);

        for (Block block : blocks) {
            if (block.getType().name().contains("PORTAL"))
                return true;
        }

        return false;
    }

    public static Vector rotateAroundAxisZ(Vector v, double angle) {
        double x, y, cos, sin;

        cos = Math.cos(angle);
        sin = Math.sin(angle);

        x = v.getX() * cos - v.getY() * sin;
        y = v.getX() * sin + v.getY() * cos;

        return v.setX(x).setY(y);
    }

    public static SignedLocation getSignedLocation(JsonElement jsonElement) {
        JsonObject locationObject = jsonElement.getAsJsonObject();

        String locationName = locationObject.get("name").getAsString();

        double posX = locationObject.get("x").getAsDouble();
        double posY = locationObject.get("y").getAsDouble();
        double posZ = locationObject.get("z").getAsDouble();

        float yaw = locationObject.has("yaw") ? locationObject.get("yaw").getAsFloat() : 0;
        float pitch = locationObject.has("pitch") ? locationObject.get("pitch").getAsFloat() : 0;

        SyntheticLocation synthetic = new SyntheticLocation(posX, posY, posZ, yaw, pitch);

        boolean axisX = locationObject.has("axisX") && locationObject.get("axisX").getAsBoolean();

        synthetic.setAxisX(axisX);

        return new SignedLocation(locationName, synthetic);
    }
}
