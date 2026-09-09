package com.minecraft.arcade.pvp.listener;

import com.minecraft.arcade.pvp.arcade.Arcade;
import com.minecraft.arcade.pvp.arcade.arena.Arena;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.feast.Feast;
import com.minecraft.arcade.pvp.user.User;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.api.punishment.Punishment;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.arcade.ArcadeHolder;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageEvent;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.AsyncUpdateEvent;
import com.minecraft.core.member.list.pvp.PvPMember;
import com.minecraft.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ArcadeListener implements Listener {

    private final List<Material> ALLOWED_DROPS = Arrays.asList(
            Material.MUSHROOM_SOUP, Material.BOWL, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM
    );

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        User user = (User) User.of(event.getPlayer().getUniqueId());

        if (user == null) return;

        Account account = user.getAccount();
        
        // Verificar se o jogador está mutado
        Punishment mute = account.getActivePunishment(PunishmentCategory.MUTE);
        if (mute != null) {
            String timeMessage = mute.isTemporary() 
                ? " §7(Expira em: §f" + com.minecraft.core.util.list.TimeUtil.formatTime(mute.getExpiresAt(), com.minecraft.core.util.list.TimeUtil.TimeFormat.SHORT) + "§7)"
                : "";
            account.send("§cVocê está mutado! Motivo: §f" + mute.getCause() + timeMessage);
            event.setCancelled(true);
            return;
        }
        
        PvPMember member = user.getMember();

        Arena arena = user.getArena();
        Tag tag = account.getTag();

        String message = event.getMessage();

        arena.getAccounts().forEach(target -> target.send(tag.getByPrefix(target.getTagPrefix()) + account.getNickname() + ": "
                        + (account.hasRank(RankType.VIP) ? "§f" + Util.color(message) : "§7" + message)
        ));
    }

    @EventHandler
    public void onTimer(AsyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (ArcadeHolder holder : BukkitCore.getManager().getArcade().getArcades()) {
                if (holder == null) continue;

                Arcade arcade = (Arcade) holder;

                List<Arena> arenaList = arcade.getRooms().stream().map(room -> (Arena) room).toList();

                arenaList.forEach(arcade::timer);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDamage(PlayerDamageEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Arcade arcade = user.getArcade();

        event.setCancelled(user.isProtected());

        /* Evento não cancelado */
        if (!event.isCancelled() && event.isDead()) {
            event.setCancelled(true);

            User killer = user.inCombat() ? (User) User.of(user.getCombat().getTarget().getUniqueId()) : null;

            arcade.handleDeath(user, killer);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamageByPlayer(PlayerDamageTargetEvent event) {
        Player target = event.getTarget(), damager = event.getPlayer();

        User user = (User) User.of(target.getUniqueId()),
                killer = (User) User.of(damager.getUniqueId());

        if (user == null || killer == null) {
            event.setCancelled(true);
            return;
        }

        Arcade arcade = user.getArcade();

        if (user.equals(killer)) {
            event.setCancelled(true);
            return;
        }

        if (user.isProtected() || killer.isProtected()) {
            event.setCancelled(true);
            return;
        }

        if (!event.isCancelled()) {
            if (event.isDead()) {
                event.setCancelled(true);

                arcade.handleDeath(user, killer);
            } else {
                user.setCombat(damager);
                killer.setCombat(target);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || user.isProtected()) {
            event.setCancelled(true);
            return;
        }

        Item item = event.getItemDrop();

        ItemStack stack = item.getItemStack();

        if (stack != null && !(ALLOWED_DROPS.contains(stack.getType()) || Arrays.stream(Feast.getFeastStacks()).anyMatch(search -> search.isSimilar(stack))))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }
}
