package com.minecraft.core.bukkit.api.block;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.minecraft.core.Core;
import com.minecraft.core.bukkit.BukkitCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class BlockApi {

    private static final Map<BlockPosition, BlockBreakTask> breakingBlocks = new ConcurrentHashMap<>();

    static {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<BlockPosition, BlockBreakTask>> iterator = breakingBlocks.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<BlockPosition, BlockBreakTask> entry = iterator.next();
                    BlockBreakTask task = entry.getValue();

                    if (task.update()) {
                        iterator.remove();
                    }
                }
            }
        }.runTaskTimer(BukkitCore.getInstance(), 0, 1);
    }

    public static void handleBreakEffect(Location blockLocation, int duration) {
        BlockPosition blockPos = new BlockPosition(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ());
        int entityId = blockPos.hashCode();

        breakingBlocks.put(blockPos, new BlockBreakTask(blockLocation, entityId, duration));
    }

    private static class BlockBreakTask {

        private final Location blockLocation;

        private final int entityId, totalStages = 10;

        private final long tickInterval;

        private int currentStage = 0;
        private long lastUpdateTime;

        public BlockBreakTask(Location blockLocation, int entityId, int durationInSeconds) {
            this.blockLocation = blockLocation;
            this.entityId = entityId;
            this.tickInterval = (durationInSeconds * 20L) / totalStages;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public boolean update() {
            long now = System.currentTimeMillis();
            if (now - lastUpdateTime >= tickInterval * 50) {
                if (currentStage >= totalStages) {
                    sendBreakPacket(-1);
                    blockLocation.getBlock().setType(Material.AIR);
                    return true; // Concluído
                }

                sendBreakPacket(currentStage);
                currentStage++;
                lastUpdateTime = now;
            }
            return false;
        }

        private void sendBreakPacket(int stage) {
            try {
                PacketContainer packet = BukkitCore.getManager().getProtocol().createPacket(com.comphenix.protocol.PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
                packet.getIntegers().write(0, entityId); // ID único
                packet.getBlockPositionModifier().write(0, new BlockPosition(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ()));
                packet.getIntegers().write(1, stage); // Estágio de quebra

                Bukkit.getOnlinePlayers().forEach(player -> {
                    try {
                        BukkitCore.getManager().getProtocol().sendServerPacket(player, packet);
                    } catch (Exception e) {
                        Core.getLogger().log(Level.WARNING, "Não foi possível enviar o pacote de quebra de bloco...", e);
                    }
                });
            } catch (Exception e) {
                Core.getLogger().log(Level.WARNING, "Erro ao criar pacote de quebra de bloco.", e);
            }
        }
    }
}
