package com.minecraft.core.bukkit.manager.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.tag.Tag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ChefeTagAnimation {

    private static final Map<Player, AnimationData> animations = new HashMap<>();
    private static int taskId = -1;

    private static class AnimationData {
        int step = 0;
        long lastUpdate = 0;
        boolean goingGreen = true; // true = §2, false = §a
    }

    public static void start(Player player) {
        if (animations.containsKey(player)) return;
        
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;
        if (!account.getTag().equals(Tag.CHEFE)) return;
        
        animations.put(player, new AnimationData());
        
        if (taskId == -1) {
            startTask();
        }
    }

    public static void stop(Player player) {
        animations.remove(player);
    }

    private static void startTask() {
        if (taskId != -1) return; // Já está rodando
        
        org.bukkit.plugin.Plugin corePlugin = org.bukkit.Bukkit.getPluginManager().getPlugin("Core");
        if (corePlugin == null) return;
        
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
            corePlugin,
            () -> {
                long now = System.currentTimeMillis();
                
                for (Map.Entry<Player, AnimationData> entry : animations.entrySet()) {
                    Player player = entry.getKey();
                    AnimationData data = entry.getValue();
                    
                    // Atualizar a cada 200ms (4 ticks)
                    if (now - data.lastUpdate < 200) continue;
                    data.lastUpdate = now;
                    
                    Account account = Core.getAccountController().of(player.getUniqueId());
                    if (account == null || !player.isOnline()) {
                        animations.remove(player);
                        continue;
                    }
                    
                    String baseTag = account.getTag().getColoredName();
                    String animatedTag;
                    
                    if (data.step < 10) {
                        // Fase 1: Piscar entre §2 e §a
                        if (data.goingGreen) {
                            animatedTag = "§2§o";
                        } else {
                            animatedTag = "§a§o";
                        }
                        data.goingGreen = !data.goingGreen;
                        
                    } else if (data.step < 20) {
                        // Fase 2: Degradê (simplificado - apenas alterar cor)
                        String[] degradeColors = {"§a§o", "§2§o"};
                        int colorIndex = (data.step - 10) % degradeColors.length;
                        animatedTag = degradeColors[colorIndex];
                        
                    } else {
                        // Reiniciar ciclo
                        data.step = 0;
                        animatedTag = "§a§o";
                    }
                    
                    data.step++;
                    
                    // Atualizar o prefixo na scoreboard (TAB e display name)
                    org.bukkit.scoreboard.Team team = null;
                    for (org.bukkit.scoreboard.Team t : player.getScoreboard().getTeams()) {
                        if (t.getName().startsWith("tag:") && t.hasEntry(player.getName())) {
                            team = t;
                            break;
                        }
                    }
                    
                    if (team != null) {
                        // Manter sufixo original
                        String suffix = team.getSuffix();
                        team.setPrefix(animatedTag);
                        team.setSuffix(suffix);
                        
                        // Atualizar display name (nome acima da cabeça)
                        player.setDisplayName(animatedTag + player.getName() + (suffix != null ? suffix : ""));
                        player.setPlayerListName(animatedTag + player.getName() + (suffix != null ? suffix : ""));
                    }
                }
                
                if (animations.isEmpty()) {
                    Bukkit.getScheduler().cancelTask(taskId);
                    taskId = -1;
                }
            },
            0L, 4L // A cada 4 ticks (200ms)
        );
    }
}
