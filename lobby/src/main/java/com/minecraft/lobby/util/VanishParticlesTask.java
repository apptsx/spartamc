package com.minecraft.lobby.util;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.BukkitCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class VanishParticlesTask {

    private static final Map<Player, BukkitRunnable> activeTasks = new HashMap<>();

    public static boolean canSeeVanish(Player viewer, Player target) {
        Account viewerAccount = Core.getAccountController().of(viewer.getUniqueId());
        Account targetAccount = Core.getAccountController().of(target.getUniqueId());
        
        if (viewerAccount == null || targetAccount == null) {
            return false;
        }
        
        RankType viewerRank = viewerAccount.getRankType();
        RankType targetRank = targetAccount.getRankType();
        
        return viewerRank.ordinal() >= targetRank.ordinal();
    }

    public static void start(Player player) {
        if (activeTasks.containsKey(player)) {
            return;
        }

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    activeTasks.remove(player);
                    return;
                }

                Location loc = player.getLocation().add(0, 2.5, 0);
                
                player.getWorld().playEffect(loc, org.bukkit.Effect.HAPPY_VILLAGER, 1);

                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (canSeeVanish(target, player)) {
                        target.playEffect(loc, org.bukkit.Effect.HAPPY_VILLAGER, 1);
                    }
                }
            }
        };

        activeTasks.put(player, runnable);
        runnable.runTaskTimer(BukkitCore.getInstance(), 0, 4L);
    }

    public static void stop(Player player) {
        BukkitRunnable runnable = activeTasks.remove(player);
        if (runnable != null) {
            runnable.cancel();
        }
    }

    public static boolean isRunning(Player player) {
        return activeTasks.containsKey(player);
    }
}