package com.minecraft.arcade.duels.arcade.list.strategy.thebridge;

import com.google.common.collect.ImmutableSet;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.event.type.player.PlayerCooldownEndEvent;
import com.minecraft.core.bukkit.manager.list.HologramManager;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.member.context.stats.ArcadeStats;
import com.minecraft.core.member.list.duels.DuelMember;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.DateUtil;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import com.minecraft.core.util.list.bukkit.ColorUtil;
import com.minecraft.core.util.list.bukkit.MapUtil;
import com.minecraft.core.util.list.serialization.Serialization;
import com.minecraft.arcade.duels.arcade.Arcade;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.arcade.objects.style.SidebarStyle;
import com.minecraft.arcade.duels.user.User;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@IgnoreEvent
public class TheBridge extends Arcade {

    private final HologramManager hologramManager;

    private final String ARROW_COOLDOWN_KEY = "Flecha",
            PORTAL_IDENTIFIER = "portal:%s"; // portal:red

    private final Set<Material> prohibitedDrops = ImmutableSet.of(
            Material.GOLDEN_APPLE, Material.ARROW, Material.BOW, Material.DIAMOND_PICKAXE,
            Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS
    );

    public TheBridge(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.DUELS_THE_BRIDGE);

        this.hologramManager = BukkitCore.getManager().getHologram();

        setStyle(SidebarStyle.POINT);
        setMaxScore(5);

        getBlocksAllowedToBreak().add(Material.STAINED_CLAY);
    }

    @Override
    public void handleSidebar(User user) {
        Sidebar sidebar = user.getSidebar();

        Arena arena = user.getArena();

        DuelMember member = user.getMember();

        sidebar.clear();
        sidebar.addRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + user.getArena().getIdentifier());
        sidebar.blankRow();

        sidebar.addRow("mode", "Modo: §a" + arena.getModeName());
        sidebar.addRow("map", "Mapa: §a" + arena.getMap().getName());
        sidebar.blankRow();

        if (arena.isPhase(RoomPhase.PLAYING)) {
            handleTeamStyle(arena, sidebar);

            if (user.isPlayer()) {
                ArcadeStats stats = member.getStats(getCategory());

                sidebar.addRow("kills", "Kills: §a" + Util.formatNumber(user.getKills()));
                sidebar.addRow("points", "Pontos: §a" + Util.formatNumber(user.getTeam().getScore()));
                sidebar.blankRow();
                sidebar.addRow("winstreak", "Winstreak: §a" + Util.formatNumber(stats.getWinStreak()));
            } else {
                sidebar.blankRow();
                sidebar.addRow("state", user.isVanish() ? "§cMODO VANISH" : "§eMODO ESPECTADOR");
            }
        } else {
            arena.createTimer(sidebar);

            sidebar.blankRow();
            sidebar.addRow("players", "Jogadores: §a" + arena.getMatchUsers().size() + "/" + arena.getMaxPlayers());
        }

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(user.getAccount());
    }

    @Override
    public void handleHotbar(Player player, Arena arena) {
        if (arena.isValid(player)) {
            User user = (User) User.of(player.getUniqueId());

            BukkitUtil.sendLeatherArmor(player, user.getTeam().getRgb());

            String base64 = user.getMember().getBase64(ArcadeCategory.DUELS_THE_BRIDGE);

            if (base64 != null)
                Serialization.sendInventoryToPlayerFromBase64WithColor(player, base64, Material.STAINED_CLAY, ColorUtil.getIdByColor(user.getTeam().getColor()));
        } else
            handleDefaultHotbar(player);
    }

    @Override
    public void handleDeath(User user, User killer, DeathCause cause) {
        super.handleDeath(user, killer, cause);

        if (killer != null) {
            killer.getSidebar().updateRow("kills", "Kills: §a" + Util.formatNumber(killer.getKills()));
        }
    }

    @Override
    public void start(Player player) {
        User user = (User) User.of(player.getUniqueId());

        if (user != null && user.isPlayer()) {
            Arena arena = user.getArena();

            TeamPreset team = user.getTeam();

            /* Renderizar hologramas dos portais */

            String teamIdentifier = String.format(PORTAL_IDENTIFIER, team.getName().toLowerCase());

            if (team.hasPortal() && hologramManager.notExistsClient(player, teamIdentifier)) {
                HologramClient yourPortal = hologramManager.spawnClient(player, teamIdentifier, team.getPortalLocation().clone().add(0, 1.8, 0));

                yourPortal.setText(Arrays.asList(
                        team.getColor() + "Sua fortaleza",
                        "§7Defenda a todo custo!"
                ));
            }

            List<TeamPreset> opponents = arena.getTeamList().stream().filter(found -> !found.equals(team) && found.hasPortal()).collect(Collectors.toList());

            opponents.forEach(opponent -> {
                String opponentIdentifier = String.format(PORTAL_IDENTIFIER, opponent.getName().toLowerCase());

                if (hologramManager.notExistsClient(player, opponentIdentifier)) {
                    HologramClient opponentPortal = hologramManager.spawnClient(player, opponentIdentifier, opponent.getPortalLocation().clone().add(0, 1.8, 0));

                    opponentPortal.setText(Arrays.asList(
                            opponent.getColor() + "Ponto de Controle",
                            "§7Pule para marcar!"));
                }
            });
        }
    }

    @EventHandler
    public void onArrowCooldownEnd(PlayerCooldownEndEvent event) {
        Player player = event.getPlayer();

        Cooldown cooldown = event.getCooldown();

        if (isValid(player) && cooldown.getName().equalsIgnoreCase(ARROW_COOLDOWN_KEY)) {
            player.getInventory().addItem(Item.of(Material.ARROW));

            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onArrowLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();

        if (projectile instanceof Arrow && projectile.getShooter() instanceof Player) {
            Player shooter = (Player) projectile.getShooter();

            if (isValid(shooter)) {
                Arena arena = (Arena) getRoom(shooter.getWorld());

                if (!arena.isPhase(RoomPhase.PLAYING)) {
                    event.setCancelled(true);
                    return;
                }

                shooter.getInventory().remove(Material.ARROW);

                BukkitCore.getManager().getCooldown().addCooldown(shooter.getUniqueId(), ARROW_COOLDOWN_KEY, 3);
            }
        }
    }

    @EventHandler
    public void onAppleConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        ItemStack item = event.getItem();

        if (isValid(player) && item != null && item.getType() == Material.GOLDEN_APPLE)
            player.setHealth(player.getMaxHealth());
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        ItemStack item = event.getItemDrop().getItemStack();

        if (isValid(player) && item != null && prohibitedDrops.contains(item.getType()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onStainedBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (isValid(player)) {
            Arena arena = (Arena) getRoom(player.getWorld());

            Block block = event.getBlock();

            if (arena.isPhase(RoomPhase.PLAYING) && block.getType().equals(Material.STAINED_CLAY))
                event.setCancelled(false);
        }
    }

    @EventHandler
    public void onBlockPortal(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (isValid(player)) {
            Arena arena = (Arena) getRoom(player.getWorld());

            Block block = event.getBlockPlaced();

            if (arena.isPhase(RoomPhase.PLAYING) && MapUtil.isNearPortal(block.getLocation(), 2))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) {
            Player player = event.getPlayer();

            if (!isValid(player)) return;

            User user = (User) User.of(player.getUniqueId());

            TeamPreset team = user.getTeam();

            Arena arena = user.getArena();

            event.setCancelled(true);

            if (arena.isPhase(RoomPhase.PLAYING)) {
                if (team.hasPortal() && team.getPortalLocation().distance(player.getLocation()) <= 5)
                    return;

                player.playSound(player.getLocation(), Sound.FALL_BIG, 1.0f, 1.0f);

                arena.getTeamList().stream().filter(loser -> !loser.equals(team))
                        .findFirst()
                        .ifPresent(loser -> handlePoint(user, arena, DeathCause.PLAYER, team, loser));
            }
        }
    }
}