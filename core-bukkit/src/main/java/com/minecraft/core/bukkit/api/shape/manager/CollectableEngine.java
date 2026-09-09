package com.minecraft.core.bukkit.api.shape.manager;

import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.shape.ImageShapeBuilder;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CollectableEngine {

    @Getter
    private static final CollectableEngine singleton = new CollectableEngine();

    private Map<Player, Location> playerLastPosition = new HashMap<>();
    private Map<Player, Float> playerLastYaw = new HashMap<>();
    private Map<Player, String> playerActiveCosmetic = new HashMap<>();

    private BukkitRunnable runnable;

    public BufferedImage loadImageFromDirectory(String directoryPath, String imageName) {
        File folder = new File(directoryPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Diretório não encontrado: " + directoryPath);
            return null;
        }

        File imageFile = new File(folder, imageName + ".png");
        if (!imageFile.exists()) {
            System.out.println("Imagem não encontrada: " + imageName + ".png");
            return null;
        }

        BufferedImage image = null;
        try {
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
            System.out.println("Erro ao carregar a imagem: " + e.getMessage());
            e.printStackTrace();
        }

        return image;
    }

    public BufferedImage loadImageFromResources(String resourceSubdir, String imageName) {
        BufferedImage image = null;
        String resourcePath = (resourceSubdir == null || resourceSubdir.isEmpty())
                ? (imageName + ".png")
                : (resourceSubdir + "/" + imageName + ".png");

        try (java.io.InputStream in = BukkitCore.getInstance().getResource(resourcePath)) {
            if (in == null) {
                System.out.println("Recurso não encontrado: " + resourcePath);
                return null;
            }
            image = ImageIO.read(in);
        } catch (IOException e) {
            System.out.println("Erro ao carregar a imagem de recursos: " + e.getMessage());
            e.printStackTrace();
        }

        return image;
    }

    public void renderImageAsParticle(Player player, String directoryPath, String imageName, float ratio) {
        BufferedImage image = loadImageFromDirectory(directoryPath, imageName);

        if (image == null) {
            System.out.println("Imagem não carregada. Abortando renderização.");
            return;
        }

        ImageShapeBuilder particles = new ImageShapeBuilder(image, 1);
        particles.setAnchor((image.getWidth() / 2), (image.getWidth() / 2));
        particles.setDisplayRatio(ratio);

        Vector dist = player.getEyeLocation().getDirection().multiply(-0.5);
        Location location = player.getEyeLocation().add(dist).subtract(0, -1, 0);
        float currentYaw = player.getLocation().getYaw();
        Location lastLocation = playerLastPosition.get(player);

        Float lastYaw = playerLastYaw.get(player);

        if (lastLocation == null || !hasPlayerMoved(lastLocation, player.getLocation()) && !hasPlayerRotated(lastYaw, currentYaw)) {
            Map<Location, Color> particleMap = particles.getParticles(location, 0, currentYaw);

            for (Map.Entry<Location, Color> entry : particleMap.entrySet()) {
                Location spot = entry.getKey();
                Color color = entry.getValue();

                if (color == Color.BLACK) {
                    color = Color.fromRGB(10, 10, 10);
                }

                PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                        EnumParticle.REDSTONE,
                        true,
                        (float) spot.getX(),
                        (float) spot.getY(),
                        (float) spot.getZ(),
                        (float) color.getRed() / 255,
                        (float) color.getGreen() / 255,
                        (float) color.getBlue() / 255,
                        1.0F,
                        0
                );

                sendPacketToAllPlayers(player, packet);
            }
        }

        playerLastPosition.put(player, player.getLocation());
        playerLastYaw.put(player, currentYaw);
    }

    // Renderiza imagem de recursos (classpath) como partículas
    public void renderResourceImageAsParticle(Player player, String resourceSubdir, String imageName, float ratio) {
        BufferedImage image = loadImageFromResources(resourceSubdir, imageName);

        if (image == null) {
            System.out.println("Imagem de recurso não carregada. Abortando renderização.");
            return;
        }

        ImageShapeBuilder particles = new ImageShapeBuilder(image, 1);
        particles.setAnchor((image.getWidth() / 2), (image.getWidth() / 2));
        particles.setDisplayRatio(ratio);

        Vector dist = player.getEyeLocation().getDirection().multiply(-0.5);
        Location location = player.getEyeLocation().add(dist).subtract(0, -1, 0);
        float currentYaw = player.getLocation().getYaw();
        Location lastLocation = playerLastPosition.get(player);

        Float lastYaw = playerLastYaw.get(player);

        if (lastLocation == null || !hasPlayerMoved(lastLocation, player.getLocation()) && !hasPlayerRotated(lastYaw, currentYaw)) {
            Map<Location, Color> particleMap = particles.getParticles(location, 0, currentYaw);

            for (Map.Entry<Location, Color> entry : particleMap.entrySet()) {
                Location spot = entry.getKey();
                Color color = entry.getValue();

                if (color == Color.BLACK) {
                    color = Color.fromRGB(10, 10, 10);
                }

                PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                        EnumParticle.REDSTONE,
                        true,
                        (float) spot.getX(),
                        (float) spot.getY(),
                        (float) spot.getZ(),
                        (float) color.getRed() / 255,
                        (float) color.getGreen() / 255,
                        (float) color.getBlue() / 255,
                        1.0F,
                        0
                );

                sendPacketToAllPlayers(player, packet);
            }
        }

        playerLastPosition.put(player, player.getLocation());
        playerLastYaw.put(player, currentYaw);
    }

    public void startRunnable(Player player, String imageName, float ratio) {
        String resourceSubdir = "capes";
        if (runnable != null) {
            runnable.cancel();
        }

        runnable = new BukkitRunnable() {
            long lastRenderTime = 0;

            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastRenderTime >= 500) {
                    renderResourceImageAsParticle(player, resourceSubdir, imageName, ratio);
                    lastRenderTime = currentTime;
                }
            }
        };

        runnable.runTaskTimer(BukkitCore.getInstance(), 0, 1L);
        playerActiveCosmetic.put(player, imageName);
    }


    public void stopRunnable(Player player) {
        if (runnable != null) {
            runnable.cancel();
            runnable = null;
        }
        playerActiveCosmetic.remove(player);
    }

    public boolean isRunning(Player player) {
        return playerActiveCosmetic.containsKey(player);
    }

    public void sendPacketToAllPlayers(Player player, Packet<?> packet) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.canSee(player)) {
                ((CraftPlayer) target).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }

    private boolean hasPlayerRotated(Float oldYaw, Float newYaw) {
        final float threshold = 5.0F;
        return Math.abs(oldYaw - newYaw) > threshold;
    }

    private boolean hasPlayerMoved(Location oldLocation, Location newLocation) {
        final double threshold = 0.1;
        return oldLocation.getWorld().equals(newLocation.getWorld()) &&
                (Math.abs(oldLocation.getX() - newLocation.getX()) > threshold ||
                        Math.abs(oldLocation.getY() - newLocation.getY()) > threshold ||
                        Math.abs(oldLocation.getZ() - newLocation.getZ()) > threshold);
    }
}