package com.minecraft.arcade.duels.listener;

import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.core.bukkit.api.block.BlockApi;
import com.minecraft.core.util.list.bukkit.ColorUtil;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PearlFightListener implements Listener {

    private final Map<Location, BlockInfo> placedBlocks = new HashMap<>();
    private final Map<Location, BukkitTask> breakingTasks = new HashMap<>();

    private static class BlockInfo {
        final UUID owner;
        final Material blockType;
        final short blockData; // Data value da lã (cor)
        boolean breakingStarted = false;

        BlockInfo(UUID owner, Material blockType, short blockData) {
            this.owner = owner;
            this.blockType = blockType;
            this.blockData = blockData;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        if (!user.getArcade().isCategory(ArcadeCategory.DUELS_PEARL_FIGHT)) return;

        Arena arena = user.getArena();

        if (!arena.isPhase(RoomPhase.PLAYING)) return;

        Block block = event.getBlock();
        Location location = block.getLocation();

        placedBlocks.remove(location);
        
        BukkitTask task = breakingTasks.remove(location);
        if (task != null) {
            task.cancel();
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        if (!user.getArcade().isCategory(ArcadeCategory.DUELS_PEARL_FIGHT)) return;

        Arena arena = user.getArena();

        if (!arena.isPhase(RoomPhase.PLAYING)) return;

        Block block = event.getBlock();
        Location location = block.getLocation();

        // Obtém a cor da lã do time do jogador
        TeamPreset team = user.getTeam();
        short woolData = 0; // Default branco
        if (team != null && block.getType() == Material.WOOL) {
            woolData = (short) ColorUtil.getIdByColor(team.getColor());
        } else if (block.getType() == Material.WOOL) {
            // Se for lã, pega o data value atual do bloco (deprecated, mas necessário para 1.8)
            @SuppressWarnings("deprecation")
            byte data = block.getData();
            woolData = data;
        }

        BlockInfo info = new BlockInfo(player.getUniqueId(), block.getType(), woolData);
        placedBlocks.put(location, info);

        JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("Duels");
        if (plugin == null) return;

        // Agenda a animação de quebra após 2 segundos (40 ticks)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Verifica se o bloco ainda existe e está no mapa
            BlockInfo blockInfo = placedBlocks.get(location);
            if (blockInfo == null || blockInfo.breakingStarted) {
                return; // Bloco foi removido ou já está quebrando
            }

            Block currentBlock = location.getBlock();
            // Verifica se o bloco ainda existe e é do mesmo tipo
            // Para lã, também verificamos o data value (cor)
            boolean blockMatches = currentBlock.getType() == blockInfo.blockType;
            if (blockInfo.blockType == Material.WOOL && blockMatches) {
                @SuppressWarnings("deprecation")
                byte data = currentBlock.getData();
                blockMatches = data == blockInfo.blockData;
            }
            
            if (!blockMatches || currentBlock.getType() == Material.AIR) {
                placedBlocks.remove(location);
                return; // Bloco não existe mais ou foi alterado
            }

            // Marca que a quebra começou
            blockInfo.breakingStarted = true;

            // Duração da animação de quebra: 3 segundos
            int breakDuration = 3;

            // Inicia a animação de quebra usando BlockApi
            BlockApi.handleBreakEffect(location, breakDuration);

            // Cria uma referência final para usar no runnable
            final Material blockType = blockInfo.blockType;
            final short blockData = blockInfo.blockData;
            final UUID ownerId = blockInfo.owner;

            // Agenda a devolução do bloco após a animação terminar
            BukkitTask breakTask = new BukkitRunnable() {
                private int tickCount = 0;
                private final int totalTicks = breakDuration * 20; // 3 segundos = 60 ticks

                @Override
                public void run() {
                    tickCount++;

                    Block currentBlock = location.getBlock();

                    // Verifica se o bloco ainda existe e é do mesmo tipo/cor
                    boolean blockMatches = currentBlock.getType() == blockType;
                    if (blockType == Material.WOOL && blockMatches) {
                        @SuppressWarnings("deprecation")
                        byte data = currentBlock.getData();
                        blockMatches = data == blockData;
                    }
                    
                    if (currentBlock.getType() == Material.AIR || !blockMatches) {
                        // Bloco já foi quebrado ou alterado, cancela a tarefa
                        breakingTasks.remove(location);
                        placedBlocks.remove(location);
                        this.cancel();
                        return;
                    }

                    // Toca som a cada 6 ticks (aproximadamente 3 vezes por segundo)
                    if (tickCount % 6 == 0) {
                        // Toca som de quebra de bloco para todos os jogadores próximos
                        location.getWorld().playSound(location, Sound.DIG_WOOL, 0.8f, 1.2f);
                    }

                    // Quando a animação terminar, devolve o bloco ao jogador
                    if (tickCount >= totalTicks) {
                        // Verifica se o bloco ainda existe (a BlockApi pode já ter quebrado)
                        boolean blockStillExists = currentBlock.getType() != Material.AIR && currentBlock.getType() == blockType;
                        if (blockType == Material.WOOL && blockStillExists) {
                            @SuppressWarnings("deprecation")
                            byte data = currentBlock.getData();
                            blockStillExists = data == blockData;
                        }
                        
                        if (blockStillExists) {
                            // Quebra o bloco (a BlockApi já quebra, mas garantimos aqui)
                            currentBlock.setType(Material.AIR);
                        }

                        // Devolve o bloco ao jogador
                        Player owner = Bukkit.getPlayer(ownerId);
                        if (owner != null && owner.isOnline()) {
                            User ownerUser = (User) User.of(ownerId);
                            if (ownerUser != null && ownerUser.getArena() != null && ownerUser.getArena().isPhase(RoomPhase.PLAYING)) {
                                // Adiciona o bloco ao inventário do jogador com a cor correta
                                ItemStack blockItem;
                                if (blockType == Material.WOOL) {
                                    // Para lã, mantém a cor original
                                    blockItem = new ItemStack(blockType, 1, blockData);
                                } else {
                                    blockItem = new ItemStack(blockType, 1);
                                }

                                // Toca som de item pego
                                owner.playSound(location, Sound.ITEM_PICKUP, 0.5f, 1.5f);

                                // Tenta adicionar ao inventário, se não couber, dropa no chão
                                HashMap<Integer, ItemStack> leftover = owner.getInventory().addItem(blockItem);
                                if (!leftover.isEmpty()) {
                                    // Dropa os itens que não couberam
                                    leftover.values().forEach(item -> {
                                        location.getWorld().dropItemNaturally(location, item);
                                    });
                                }
                            }
                        }

                        // Remove do mapa de tarefas
                        breakingTasks.remove(location);

                        // Remove do mapa de blocos colocados
                        placedBlocks.remove(location);

                        // Cancela a tarefa
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);

            // Armazena a tarefa
            breakingTasks.put(location, breakTask);
        }, 40L); // 2 segundos = 40 ticks
    }
}

