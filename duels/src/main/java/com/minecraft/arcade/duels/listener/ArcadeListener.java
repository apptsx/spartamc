package com.minecraft.arcade.duels.listener;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.arcade.ArcadeHolder;
import com.minecraft.core.arcade.feature.ArcadeFeature;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.room.map.rollback.RollbackBlock;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.event.type.player.PlayerArenaWarpEvent;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import com.minecraft.arcade.duels.Duels;
import com.minecraft.arcade.duels.arcade.Arcade;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ArcadeListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Tag tag = user.getTag();

        Arena arena = user.getArena();

        if (!arena.isPhase(RoomPhase.PLAYING)) {
            player.sendMessage("§cVocê não pode enviar mensagens.");
            return;
        }

        arena.send(tag.getColor() + arena.getRandomizedName(user.getAccount().getNickname()) + "§r§f: " + event.getMessage());
    }

    @EventHandler
    public void onTimer(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (ArcadeHolder holder : Duels.getManager().getArcade().getArcades()) {
                if (holder.getRooms().isEmpty()) continue;

                Arcade arcade = (Arcade) holder;

                List<Room> rooms = new ArrayList<>(arcade.getRooms());

                for (Room room : rooms) {
                    if (!(room instanceof Arena)) continue;

                    Arena arena = (Arena) room;

                    arena.timer();

                    if (arena.isPhase(RoomPhase.PLAYING)) {
                        for (Player player : arena.getPlayers()) {
                            User user = (User) User.of(player.getUniqueId());

                            if (user != null)
                                arcade.updateTeamStyle(arena, user.getSidebar());
                        }
                    }

                    arcade.checkAndGenerateArenas();

                    arcade.timer(arena);
                }
            }
        }

        /* Atualizando barras de Progresso */
        if (event.isType(UpdateType.TICK)) {
            BukkitCore.getManager().getCooldown().getCooldownMap().forEach((id, list) -> {
                Player player = Bukkit.getPlayer(id);

                if (player == null) return;

                User user = (User) User.of(player.getUniqueId());

                if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

                // Atualizando barra de progressão
                Arcade arcade = user.getArcade();

                if (arcade.hasFeature(ArcadeFeature.LEVEL_BAR_XP))
                    list.stream().findFirst().ifPresent(cooldown -> BukkitUtil.updateLevelBar(player, cooldown.getEndTime(), (int) cooldown.getDuration()));
            });
        }
    }

    @EventHandler
    public void onPartyWarp(PlayerArenaWarpEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user != null && user.isPlayer()) {
            Arena current = user.getArena(), arena = (Arena) event.getArena();

            System.out.println(player.getName() + " -> Saindo de " + current.getIdentifier() + " para " + arena.getIdentifier());

            Core.getPlatform().runSync(() -> {
                current.quit(player);

                user.setArena(arena);

                arena.join(player);
            });
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Arena arena = user.getArena();

        ItemStack stack = event.getItemDrop().getItemStack();

        if (stack == null || stack.getType() == Material.AIR) return;

        if (!arena.isPhase(RoomPhase.PLAYING)) {
            event.setCancelled(true);
            return;
        }

        if (arena.getArcade().hasFeature(ArcadeFeature.DROPS_ALL)) {
            event.setCancelled(false);
            return;
        }

        event.setCancelled(stack != null && stack.getType().name().contains("SWORD"));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Bloquear interação com armadura
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();

            Inventory clicked = event.getClickedInventory();

            if (clicked == null) return;

            User user = (User) User.of(player.getUniqueId());

            if (user == null || !user.isPlayer()) return;

            Arena arena = user.getArena();

            if (arena.isPhase(RoomPhase.PLAYING) && !arena.getArcade().hasFeature(ArcadeFeature.CAN_MOVE_ARMOR)
                    && clicked.getType().equals(InventoryType.PLAYER) && event.getSlot() >= 36 && event.getSlot() <= 39)
                event.setCancelled(true);

        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Arena arena = user.getArena();
        Arcade arcade = arena.getArcade();

        if (arena.isPhase(RoomPhase.STARTING) && arcade.hasFeature(ArcadeFeature.WITHOUT_MOVING_STARTING)) {
            if (event.getTo().getY() > event.getFrom().getY())
                player.teleport(user.getTeam().getBase());

            player.setWalkSpeed(0f);
            return;
        }

        Location to = event.getTo(), spawn = arena.getLocation("spawn");

        if (to.getY() <= (spawn.getY() - 20)) {
            if (!user.isProtected()) {
                User killer = user.inCombat() ? (User) User.of(user.getCombat().getTarget().getUniqueId()) : null;

                arcade.handleDeath(user, killer, Arcade.DeathCause.VOID);
            } else
                player.teleport(spawn);
        }
    }

    @EventHandler
    public void onBuild(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) {
            event.setCancelled(true);
            return;
        }

        Arena arena = user.getArena();
        Arcade arcade = arena.getArcade();

        if (!arena.isPhase(RoomPhase.PLAYING)) {
            event.setCancelled(true);
            return;
        }

        if (!arcade.hasFeature(ArcadeFeature.BUILD)) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();

        if (block.getType() == Material.SKULL) {
            event.setCancelled(true);
            return;
        }

        Location spawn = arena.getLocation("spawn"), blockLocation = block.getLocation();

        if (blockLocation.getBlockY() >= (spawn.getY() + arena.getMap().getBuildLimit())) {
            event.setCancelled(true);

            player.sendMessage("§cVocê está no limite de altura.");
            return;
        }

        event.setCancelled(false);

        arena.addRollBack(block, RollbackBlock.RollbackType.PLACE_BLOCK);
    }

    @EventHandler
    public void onBuildBucket(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) {
            event.setCancelled(true);
            return;
        }

        Arena arena = user.getArena();
        Arcade arcade = arena.getArcade();

        if (!arena.isPhase(RoomPhase.PLAYING) || !arcade.hasFeature(ArcadeFeature.BUILD)) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlockClicked();

        Location spawn = arena.getLocation("spawn"), blockLocation = block.getLocation();

        if (blockLocation.getBlockY() >= (spawn.getY() + arena.getMap().getBuildLimit())) {
            event.setCancelled(true);

            player.sendMessage("§cVocê está no limite de altura.");
            return;
        }

        event.setCancelled(false);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) {
            event.setCancelled(true);
            return;
        }

        Arena arena = user.getArena();
        Arcade arcade = arena.getArcade();

        if (!arena.isPhase(RoomPhase.PLAYING) || !arcade.hasFeature(ArcadeFeature.BUILD)) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();

        if (!arena.isReversible(block) && !(arcade.getBlocksAllowedToBreak().contains(block.getType()) || block.getType().name().contains("BED"))) {
            event.setCancelled(true);

            player.sendMessage("§cVocê não pode quebrar este bloco.");
            return;
        }

        event.setCancelled(false);

        arena.addRollBack(block, RollbackBlock.RollbackType.REMOVE_BLOCK);
    }
}
