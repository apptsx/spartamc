package com.minecraft.lobby.listener;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.toggle.Toggle;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.event.type.account.AccountClanChangeEvent;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageEvent;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.AsyncUpdateEvent;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.architect.Architect;
import com.minecraft.lobby.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public class ServerListener implements Listener {

    private final Architect architect;

    public ServerListener() {
        this.architect = Lobby.getLoader().getArchitect();
    }

    @EventHandler
    public void onAsyncCounterUpdate(AsyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            ServerType realServer = architect.getServer().getRealServerOfLobby();

            if (realServer != null) {
                for (ArcadeCategory arcade : ArcadeCategory.list(category -> category.getServer().equals(realServer))) {
                    HologramServer hologram = BukkitCore.getManager().getHologram().getServer(arcade.getId());

                    if (hologram != null) {
                        int onlinePlayers = Core.getArcadeData().getOnlinePlayers(arcade);

                        hologram.setText(hologram.getRows().size() - 1, "§e" + Util.formatNumber(onlinePlayers) + " jogando.");
                    }
                }
            }

            // Atualizar players na scoreboard
            for (UserModel user : User.getList()) {
                if (user != null) {
                    Sidebar sidebar = user.getSidebar();

                    if (sidebar != null)
                        sidebar.updateRow("players", "Players: §a" + Util.formatNumber(Core.getServerData().getOnlinePlayers()));
                }
            }
        }
    }

    @EventHandler
    public void onVoid(PlayerDamageEvent event) {
        Player player = event.getPlayer();

        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            event.setCancelled(true);

            Location spawn = architect.getLocation("spawn");

            if (spawn != null)
                player.teleport(spawn);
        }
    }

    @EventHandler
    public void onAccountClan(AccountClanChangeEvent event) {
        Account account = event.getAccount();

        User user = (User) User.of(account.getId());

        if (user != null)
            user.getSidebar().updateRow("clan", "Clan: §7" + (account.hasClan() ? account.getClanName() : "Nenhum"));
    }

    @EventHandler
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        architect.chat(player, event.getMessage());
    }

    @EventHandler
    public void onSlimeJump(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Material down = player.getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType();

        if (down.equals(Material.SLIME_BLOCK)) {
            player.setVelocity(architect.getSlimeJump(player.getLocation().getDirection()));

            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 2.0f);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().equals(event.getView().getBottomInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVisibilityItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account == null) return;

        ItemStack hand = event.getItem();

        if (event.hasBlock() && event.getClickedBlock().getType().name().contains("BUTTON"))
            event.setCancelled(true);

        if (event.hasItem() && hand != null) {
            if (hand.getType().equals(Material.INK_SACK)) {
                Toggle toggle = account.getToggle();

                if (account.hasCooldown(Constant.PLAYERS_VISIBILITY_COOLDOWN_KEY)) {
                    account.send("§cAguarde " + account.getFormattedCooldown(Constant.PLAYERS_VISIBILITY_COOLDOWN_KEY)
                            + " para alterar a visibilidade dos jogadores novamente.");
                    return;
                }

                // Ciclo: ON (0) -> FRIENDS (3) -> RANK_ONLY (2) -> OFF (1) -> ON (0)
                 int currentMode = toggle.getVisibilityMode();
                 int newVisibility;
                 
                 if (currentMode == 0) newVisibility = 3; // ON -> FRIENDS
                 else if (currentMode == 3) newVisibility = 2; // FRIENDS -> RANK_ONLY
                 else if (currentMode == 2) newVisibility = 1; // RANK_ONLY -> OFF
                 else newVisibility = 0; // OFF -> ON
                 
                 toggle.setVisibilityMode(newVisibility);
                 account.setToggle(toggle);

                 String visibilityName;
                 int dyeColor;

                 switch (newVisibility) {
                     case 0: // ON
                         visibilityName = "§aON";
                         dyeColor = 10; // Green
                         break;
                     case 1: // OFF
                         visibilityName = "§cOFF";
                         dyeColor = 8; // Gray
                         break;
                     case 2: // RANK_ONLY
                         visibilityName = "§dRANK ONLY";
                         dyeColor = 9; // Pink/Magenta
                         break;
                     case 3: // FRIENDS
                         visibilityName = "§9FRIENDS";
                         dyeColor = 12; // Blue
                         break;
                     default:
                         visibilityName = "§aON";
                         dyeColor = 10;
                 }

                 player.sendMessage("§aA visibilidade dos jogadores foi alterada para: " + visibilityName);
                 
                 player.setItemInHand(Item.of(Material.INK_SACK, dyeColor,
                         "§fPlayers: " + visibilityName));

                player.updateInventory();

                // Atualizar visibilidade baseado na nova configuração
                updatePlayerVisibility(player, toggle.getVisibilityMode());

                player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 2.0f);

                account.setCooldown(Constant.PLAYERS_VISIBILITY_COOLDOWN_KEY, TimeUnit.SECONDS.toMillis(5));
            }
        }
    }

    private void updatePlayerVisibility(Player player, int visibilityMode) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        
        Bukkit.getOnlinePlayers().forEach(online -> {
            if (online.equals(player)) return;
            
            switch (visibilityMode) {
                case 0: // ON - mostrar todos
                    player.showPlayer(online);
                    break;
                case 1: // OFF - esconder todos
                    player.hidePlayer(online);
                    break;
                case 2: // RANK_ONLY - mostrar apenas com rank
                    Account onlineAccount = Core.getAccountController().of(online.getUniqueId());
                    if (onlineAccount != null && onlineAccount.getRankType().ordinal() > 0) {
                        player.showPlayer(online);
                    } else {
                        player.hidePlayer(online);
                    }
                    break;
                case 3: // FRIENDS - mostrar apenas amigos
                    // TODO: Implementar verificação de amizade
                    // Por enquanto, mostrar apenas se for amigo
                    if (account != null && account.getFriends().contains(online.getUniqueId())) {
                        player.showPlayer(online);
                    } else {
                        player.hidePlayer(online);
                    }
                    break;
            }
        });
    }
}
