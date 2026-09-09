package com.minecraft.arcade.pvp.listener;

import com.minecraft.arcade.pvp.arcade.Arcade;
import com.minecraft.arcade.pvp.arcade.arena.Arena;
import com.minecraft.arcade.pvp.user.User;
import com.minecraft.arcade.pvp.user.factory.UserFactory;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.route.RouteContext;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.backend.data.list.server.RouteData;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.member.list.pvp.PvPMember;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.UUID;

public class SearchListener implements Listener {

    private final RouteData routeData;

    public SearchListener() {
        this.routeData = Core.getRouteData();
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.ALLOWED)) {

            UUID id = event.getUniqueId();

            // Buscando dados da rota
            RouteContext route = routeData.read(id);

            if (route == null || !route.isValidArcade()) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.INVALID_ROUTE_MESSAGE);
                return;
            }

            routeData.delete(id);

            ArcadeRouteContext arcadeRoute = route.getArcade();

            Arcade arcade = (Arcade) BukkitCore.getManager().getArcade().read(arcadeRoute.getArcade());

            if (arcade == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.ARCADE_NOT_FOUND_MESSAGE);
                return;
            }

            Arena arena = arcade.findBestArena(id, arcadeRoute);

            if (arena == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.NO_ROOM_AVAILABLE_MESSAGE);
                return;
            }

            // Carregando dados da conta Arcade
            PvPMember member = Core.getPvpData().of(id, true);

            if (member == null) {
                member = Core.getPvpData().save(new PvPMember(id, event.getName()));

                if (member == null) {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE);
                    return;
                }
            }

            Core.getMemberController().save(member);
            Core.getPvpData().cancelExpiration(member.getId());

            // Instanciando usuário customizado
            UserFactory.create(arcade.getCategory(), member, arena, arcadeRoute.getJoin());
        }
    }

    @EventHandler
    public void login(PlayerLoginEvent event) {
        User user = (User) User.of(event.getPlayer().getUniqueId());

        if (user == null || user.getArena() == null)
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE + " (Invalid user/arena)");
    }

    @EventHandler
    public void spawn(PlayerSpawnLocationEvent event) {
        User user = (User) User.of(event.getPlayer().getUniqueId());

        event.setSpawnLocation(user.getArena().getLocation("spawn"));
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user != null)
            user.handle();
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        exit(event.getPlayer());
    }

    @EventHandler
    public void kick(PlayerKickEvent event) {
        exit(event.getPlayer());
    }

    protected void exit(Player player) {
        User user = (User) User.of(player.getUniqueId());

        if (user != null)
            user.getArena().quit(player);

        Core.getPvpData().startExpiration(user.getMember().getId());
        Core.getMemberController().remove(player.getUniqueId(), PvPMember.class);
    }
}
