package com.minecraft.lobby.listener;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.type.trail.TrailCollectible;
import com.minecraft.core.api.collectible.type.title.CustomTitle;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.reconnect.Reconnect;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.user.User;
import com.minecraft.lobby.util.VanishParticlesTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class LoginListener implements Listener {

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!event.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.ALLOWED)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE);
            return;
        }

        new User(event.getUniqueId());
    }

    @EventHandler
    public void spawn(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        Account account = Core.getAccountController().of(player.getUniqueId());
        
        Location spawn = Lobby.getLoader().getArchitect().getLocation("spawn");
        
        if (spawn != null) {
            if (account != null && account.isVIP()) {
                Location highSpawn = spawn.clone();
                highSpawn.add(0, 5, 0);
                event.setSpawnLocation(highSpawn);
            } else {
                event.setSpawnLocation(spawn);
            }
        } else {
            event.setSpawnLocation(player.getWorld().getSpawnLocation());
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) {
            player.kickPlayer(Constant.ACCOUNT_LOAD_FAILED_MESSAGE);
            return;
        }

        user.handle();
        
        Account account = user.getAccount();
        
        Bukkit.getScheduler().runTaskLater(Lobby.getInstance(), () -> {
            if (player.isOnline()) {
                CollectibleOperator.enable(player);
            }
        }, 4L);
        
        if (account.isVIP()) {
            player.setAllowFlight(true);
            
            Location currentLoc = player.getLocation();
            Location highLoc = currentLoc.clone().add(0, 3.5, 0);
            player.teleport(highLoc);
            
            player.setFlying(true);
            player.setFlySpeed(0.6f);
        }
        
        // Reaplicar título personalizado se existir e estiver na lista de ativos
        if (account.hasCustomTitle() && account.getActiveCollectibles().stream().anyMatch(id -> id.startsWith("CUSTOM_TITLE:"))) {
            Bukkit.getScheduler().runTaskLater(Lobby.getInstance(), () -> {
                if (player.isOnline()) {
                    Account acc = Core.getAccountController().of(player.getUniqueId());
                    if (acc != null && acc.getCustomTitle() != null && !acc.getCustomTitle().isEmpty()) {
                        CustomTitle.showTitle(player);
                    }
                }
            }, 5L);
        }
        // Reaplicar título normal apenas se o jogador tiver um ativo
        Bukkit.getScheduler().runTaskLater(Lobby.getInstance(), () -> {
            if (player.isOnline()) {
                Account acc = Core.getAccountController().of(player.getUniqueId());
                if (acc != null && acc.getActiveCollectibles().stream().anyMatch(id -> id.startsWith("TITLE:"))) {
                    TitleCollectible.reapplyTitle(player);
                }
            }
        }, 5L);

        // Restaurar rastro se estava ativo
        Bukkit.getScheduler().runTaskLater(Lobby.getInstance(), () -> {
            if (player.isOnline() && TrailCollectible.wasActiveOnLogout(player.getUniqueId())) {
                TrailCollectible.restoreTrailWithSavedColor(player);
            }
        }, 10L);
        
        Reconnect reconnect = Core.getReconnectData().of(player.getUniqueId());

        if (reconnect != null) {
            ArcadeRouteContext route = reconnect.getRoute();

            player.sendMessage(new String[]{
                    "",
                    "§c§lVocê desconectou de uma partida de " + route.getArcade().getFullName(),
                    "§c§lem andamento. Use /reconectar para voltar a jogar.",
                    ""
            });

            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 2.0f);
        }

account.title(Constant.SERVER_TITLE, "§eBoas-vindas!");
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0f, 2.0f);
        
        // Atualizar leaderboard de wools quando jogador entra
        com.minecraft.lobby.architect.Architect architect = Lobby.getLoader().getArchitect();
        if (architect instanceof com.minecraft.lobby.architect.list.main.MainArchitect) {
            ((com.minecraft.lobby.architect.list.main.MainArchitect) architect).updateWoolsLeaderboard();
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        TitleCollectible.removeTitle(event.getPlayer());
        CustomTitle.removeTitle(event.getPlayer());
        TrailCollectible.cleanupAll();
        VanishParticlesTask.stop(event.getPlayer());
        exit(event.getPlayer());
    }

    @EventHandler
    public void kick(PlayerKickEvent event) {
        TitleCollectible.removeTitle(event.getPlayer());
        CustomTitle.removeTitle(event.getPlayer());
        TrailCollectible.cleanupAll();
        VanishParticlesTask.stop(event.getPlayer());
        exit(event.getPlayer());
    }

    protected void exit(Player player) {
        Lobby.getLoader().getArchitect().quit(player);
    }
}
