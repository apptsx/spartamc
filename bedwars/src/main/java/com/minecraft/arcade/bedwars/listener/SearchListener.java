package com.minecraft.arcade.bedwars.listener;

import com.minecraft.arcade.bedwars.arcade.Arcade;
import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.route.RouteContext;
import com.minecraft.core.api.reconnect.Reconnect;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.backend.data.list.server.RouteData;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.member.list.bedwars.BedMember;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

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

            // BYPASS PARA STAFF: Se não tem rota mas é staff, permitir entrada para configurar mapas
            if (route == null || !route.isValidArcade()) {
                // Tentar carregar conta do jogador (pode não estar carregada ainda no AsyncPlayerPreLogin)
                Account account = Core.getAccountData().of(id, false);

                // Se não encontrou a conta, tentar carregar do controller
                if (account == null) {
                    account = Core.getAccountController().of(id);
                }

                // Verificar se é staff (TRIAL ou superior)
                if (account != null && account.isStaffer()) {
                    // Staff pode entrar sem rota para configurar mapas
                    routeData.delete(id);

                    // Carregar dados do BedMember
                    BedMember member = Core.getBedWarsData().of(id, true);
                    if (member == null) {
                        member = Core.getBedWarsData().save(new BedMember(id, event.getName()));
                    }
                    
                    if (member != null) {
                        Core.getMemberController().save(member);
                        Core.getBedWarsData().cancelExpiration(id);
                        
                        // Criar usuário SEM arena para entrar no mundo normal (bypass para configurar mapas)
                        User user = new User(member, null, Join.VANISH);
                        Core.getLogger().info("[BYPASS] Staff " + account.getNickname() + " entrou sem rota para configurar mapas.");
                        return; // Permitir entrada
                    }
                    
                    // Se chegou aqui, não conseguiu criar bypass mas é staff
                    Core.getLogger().warning("[BYPASS] Não foi possível criar bypass para staff " + account.getNickname());
                }

                // Se não é staff ou não conseguiu bypass, negar entrada
                routeData.delete(id);
                Core.message("Rota: " + route,
                        "Válida: " + (route != null ? route.isValidArcade() : "NÃO ENCONTRADO"));
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

            Arena arena = (Arena) arcade.getRoom(arcadeRoute);

            // Se não encontrou a arena pela rota, tentar buscar pelo findBestArena (lazy loading)
            if (arena == null) {
                Core.getLogger().info("[BedWars] Arena não encontrada pela rota, tentando findBestArena para " + event.getName());
                arena = arcade.findBestArena(id, arcadeRoute);
            }

            // BYPASS PARA STAFF: Se não há arena disponível mas é staff, permitir entrada para configurar
            if (arena == null) {
                Account account = Core.getAccountData().of(id, false);
                if (account == null) {
                    account = Core.getAccountController().of(id);
                }
                
                if (account != null && account.isStaffer()) {
                    // Staff pode entrar sem arena para configurar mapas
                    routeData.delete(id);
                    
                    BedMember member = Core.getBedWarsData().of(id, true);
                    if (member == null) {
                        member = Core.getBedWarsData().save(new BedMember(id, event.getName()));
                    }
                    
                    if (member != null) {
                        Core.getMemberController().save(member);
                        Core.getBedWarsData().cancelExpiration(id);
                        
                        // Criar usuário SEM arena para entrar no mundo normal (bypass para configurar mapas)
                        User user = new User(member, null, Join.VANISH);
                        Core.getLogger().info("[BYPASS] Staff " + account.getNickname() + " entrou sem arena disponível para configurar mapas.");
                        return; // Permitir entrada
                    }
                }
                
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.NO_ROOM_AVAILABLE_MESSAGE);
                return;
            }

            // Garantir que a conta do jogador esteja carregada antes de continuar
            Account account = Core.getAccountData().of(id, false);
            if (account == null) {
                account = Core.getAccountController().of(id);
            }
            
            // Se ainda não encontrou a conta, tentar criar/carregar
            if (account == null) {
                // Tentar carregar novamente com o nome do evento
                Core.getLogger().warning("[BedWars] Conta não encontrada para " + event.getName() + " (" + id + "), tentando carregar...");
                // Aguardar um pouco para dar tempo de carregar
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                account = Core.getAccountController().of(id);
            }
            
            // Se ainda não encontrou, negar entrada
            if (account == null) {
                Core.getLogger().severe("[BedWars] Não foi possível carregar conta para " + event.getName() + " (" + id + ")");
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE + " (Ex0002)");
                return;
            }
            
            // Carregando dados da conta Arcade
            BedMember member = Core.getBedWarsData().of(id, true);

            if (member == null) {
                member = Core.getBedWarsData().save(new BedMember(id, event.getName()));

                if (member == null) {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE + " (Ex0003)");
                    return;
                }
            }

            Core.getMemberController().save(member);
            Core.getBedWarsData().cancelExpiration(id);

            // Criando usuário
            User user = new User(member, arena, arcadeRoute.getJoin());

            if (user.getAccount().hasReconnect()) {
                Reconnect reconnect = Core.getReconnectData().of(id);

                if (reconnect != null)
                    user.getContext().setReconnect(reconnect);
            }
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        User user = (User) User.of(event.getPlayer().getUniqueId());

        if (user == null)
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE + " (Ex0003)");
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user != null) {
            // Se o usuário não tem arena (bypass para staff), teleportar para o mundo padrão
            if (user.getArena() == null) {
                // Tentar encontrar um mundo padrão ou usar o primeiro mundo disponível
                org.bukkit.World defaultWorld = org.bukkit.Bukkit.getWorld("world");
                if (defaultWorld == null && !org.bukkit.Bukkit.getWorlds().isEmpty()) {
                    defaultWorld = org.bukkit.Bukkit.getWorlds().get(0);
                }
                
                if (defaultWorld != null) {
                    org.bukkit.Location spawn = defaultWorld.getSpawnLocation();
                    if (spawn == null) {
                        spawn = new org.bukkit.Location(defaultWorld, 0.5, 100, 0.5);
                    }
                    player.teleport(spawn);
                    player.sendMessage("§aVocê entrou no modo de configuração.");
                    player.sendMessage("§7Use §e/mapworld §7para carregar e teleportar para mapas.");
                }
                
                // Não chamar handle() para não entrar em nenhuma arena
                return;
            }
            
            user.handle();
        }
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

        if (user != null && user.getArena() != null)
            user.getArena().quit(player);

        Core.getBedWarsData().startExpiration(player.getUniqueId());
    }
}
