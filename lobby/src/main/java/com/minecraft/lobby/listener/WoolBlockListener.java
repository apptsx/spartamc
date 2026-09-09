package com.minecraft.lobby.listener;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.backend.data.list.api.WoolsPlacedData;
import com.minecraft.lobby.Lobby;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class WoolBlockListener implements Listener {

    private static final String WOOL_METADATA = "sparta_colored_wool";
    private static final int COLOR_CHANGE_INTERVAL = 10;
    private static final int TOTAL_CYCLES = 6;

    private static final byte[] WOOL_COLORS = {14, 1, 4, 5, 3, 11, 10, 9};

    private static final Map<UUID, Integer> playerColorIndex = new HashMap<>();
    private static final Map<Location, Byte> blockColors = new HashMap<>();
    private static final Map<Location, Integer> blockCycles = new HashMap<>();
    private static final Map<Location, BukkitTask> blockTasks = new HashMap<>();
    private static final Map<UUID, Long> sessionWoolCount = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        Material handType = player.getItemInHand().getType();
        if (handType != Material.WOOL) return;

        Account account = Core.getAccountController().of(player.getUniqueId());
        // VIP e superiores (incluindo STAR/BOOSTER) podem usar
        if (account == null || (!account.hasRank(RankType.VIP) && !account.hasRank(RankType.BOOSTER))) {
            event.setCancelled(true);
            player.sendMessage("§cVocê não possui permissão para usar isso!\n§cAdquira o rank §a§lVIP§c em nossa loja: §e" + com.minecraft.core.Constant.SERVER_STORE + "!");
            return;
        }

        event.setCancelled(false);

        UUID uuid = player.getUniqueId();
        int colorIndex = playerColorIndex.getOrDefault(uuid, 0);
        byte woolColor = WOOL_COLORS[colorIndex];

        Block block = event.getBlock();
        block.setTypeIdAndData(35, woolColor, false);
        block.setMetadata(WOOL_METADATA, new FixedMetadataValue(Lobby.getInstance(), uuid.toString()));
        
        blockColors.put(block.getLocation(), woolColor);
        blockCycles.put(block.getLocation(), 0);
        
        // Contador temporário de wools placement durante a sessão
        sessionWoolCount.merge(uuid, 1L, Long::sum);
        
        startColorCycleForBlock(block.getLocation());
    }

    private void startColorCycleForBlock(Location loc) {
        final int[] colorIndex = {0};
        
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Lobby.getInstance(), new Runnable() {
            @Override
            public void run() {
                Block block = loc.getBlock();
                if (block == null || !block.hasMetadata(WOOL_METADATA)) {
                    cancelTask(loc);
                    return;
                }
                
                Integer cycles = blockCycles.getOrDefault(loc, 0);
                
                if (cycles >= TOTAL_CYCLES) {
                    block.setType(Material.AIR);
                    cancelTask(loc);
                    blockColors.remove(loc);
                    blockCycles.remove(loc);
                    return;
                }
                
                colorIndex[0] = (colorIndex[0] + 1) % WOOL_COLORS.length;
                byte currentColor = WOOL_COLORS[colorIndex[0]];
                
                block.setData(currentColor);
                blockColors.put(loc, currentColor);
                blockCycles.put(loc, cycles + 1);
                
                loc.getWorld().playSound(loc, Sound.DIG_WOOL, 0.1f, 1.0f);
            }
        }, COLOR_CHANGE_INTERVAL, COLOR_CHANGE_INTERVAL);
        
        blockTasks.put(loc, task);
    }

    private void cancelTask(Location loc) {
        BukkitTask task = blockTasks.get(loc);
        if (task != null) {
            task.cancel();
            blockTasks.remove(loc);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.hasMetadata(WOOL_METADATA)) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            Account account = Core.getAccountController().of(player.getUniqueId());
            if (account == null || !account.hasRank(RankType.VIP)) {
                Integer cycles = blockCycles.getOrDefault(block.getLocation(), 0);
                if (cycles < TOTAL_CYCLES) {
                    byte newColor = (byte) (block.getData() + 1);
                    block.setData(newColor);
                    blockCycles.put(block.getLocation(), cycles + 1);
                    block.getWorld().playSound(block.getLocation(), Sound.DIG_WOOL, 0.3f, 1.0f);
                }
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Block block = event.getBlock();
        if (block.hasMetadata(WOOL_METADATA)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            Material handType = player.getItemInHand().getType();

            if (handType == Material.WOOL) {
                Account account = Core.getAccountController().of(player.getUniqueId());
                if (account == null || !account.hasRank(RankType.VIP)) {
                    event.setCancelled(true);
                    player.sendMessage("§cVocê não possui permissão para usar isso!\n§cAdquira o rank §a§lVIP§c em nossa loja: §e" + com.minecraft.core.Constant.SERVER_STORE + "!");
                }
            }
        }
    }

    public static void startColorCycle(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerColorIndex.containsKey(uuid)) {
            playerColorIndex.put(uuid, 0);
        }

        Bukkit.getScheduler().runTaskTimer(Lobby.getInstance(), () -> {
            if (!player.isOnline()) return;

            // Não alterar o slot 3 se o jogador estiver no parkour
            if (com.minecraft.lobby.parkour.ParkourManager.isInParkour(player)) {
                return;
            }

            int currentIndex = playerColorIndex.getOrDefault(uuid, 0);
            currentIndex = (currentIndex + 1) % WOOL_COLORS.length;
            playerColorIndex.put(uuid, currentIndex);

            byte woolColor = WOOL_COLORS[currentIndex];
            player.getInventory().setItem(3, new ItemStack(Material.WOOL, 64, (short) (woolColor & 0xFF)));
        }, 10L, 10L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        
        // Salvar contagem de lãs da sessão antes de remover
        Long woolsPlaced = sessionWoolCount.get(uuid);
        if (woolsPlaced != null && woolsPlaced > 0) {
            WoolsPlacedData.getInstance().addWools(uuid, woolsPlaced);
            sessionWoolCount.remove(uuid);
        }
        
        // Remover todas as lãs colocadas pelo jogador
        for (Location loc : new ArrayList<>(blockColors.keySet())) {
            Block block = loc.getBlock();
            if (block != null && block.hasMetadata(WOOL_METADATA)) {
                String owner = block.getMetadata(WOOL_METADATA).get(0).asString();
                if (owner.equals(uuid.toString())) {
                    cancelTask(loc);
                    block.setType(Material.AIR);
                    blockColors.remove(loc);
                    blockCycles.remove(loc);
                }
            }
        }
    }

    public static void removeAllBlocks() {
        for (Location loc : new ArrayList<>(blockColors.keySet())) {
            Block block = loc.getBlock();
            if (block != null && block.hasMetadata(WOOL_METADATA)) {
                block.setType(Material.AIR);
            }
        }
        blockColors.clear();
        blockCycles.clear();
        for (BukkitTask task : blockTasks.values()) {
            task.cancel();
        }
        blockTasks.clear();
        sessionWoolCount.clear();
    }
}