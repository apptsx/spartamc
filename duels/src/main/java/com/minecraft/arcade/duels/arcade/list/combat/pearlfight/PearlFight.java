package com.minecraft.arcade.duels.arcade.list.combat.pearlfight;

import com.minecraft.core.Core;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.util.list.bukkit.ColorUtil;
import com.minecraft.arcade.duels.arcade.Arcade;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.arcade.objects.style.SidebarStyle;
import com.minecraft.arcade.duels.user.User;
import com.minecraft.arcade.duels.user.factory.list.PearlFightUser;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

@IgnoreEvent
public class PearlFight extends Arcade {

    public PearlFight(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.DUELS_PEARL_FIGHT);

        setStyle(SidebarStyle.LATENCY);
    }

    @Override
    public void handleHotbar(Player player, Arena arena) {
        if (arena.isValid(player)) {
            player.getInventory().clear();

            User user = (User) User.of(player.getUniqueId());
            if (user == null) {
                handleDefaultHotbar(player);
                return;
            }

            TeamPreset team = user.getTeam();
            if (team == null) {
                handleDefaultHotbar(player);
                return;
            }

            // Atualiza a saúde do jogador baseado nas vidas
            if (user instanceof PearlFightUser) {
                updatePlayerHealth(player, (PearlFightUser) user);
            }

            // Stick com knockback 1
            ItemStack stick = new ItemStack(Material.STICK);
            stick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
            player.getInventory().setItem(0, stick);

            player.getInventory().setItem(1, new ItemStack(Material.ENDER_PEARL, 5));

            int woolColor = ColorUtil.getIdByColor(team.getColor());
            ItemStack wool = new ItemStack(Material.WOOL, 16, (short) woolColor);
            player.getInventory().setItem(2, wool);

            ItemStack tesoura = new ItemStack(Material.SHEARS);
            tesoura.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
            player.getInventory().setItem(3, tesoura);
        } else {
            handleDefaultHotbar(player);
        }
    }

    private void updatePlayerHealth(Player player, PearlFightUser user) {
        double maxHealth = user.getLives() * 2.0;
        if (maxHealth < 2.0) maxHealth = 2.0;
        
        player.setMaxHealth(maxHealth);
        player.setHealth(maxHealth);
    }

    @Override
    public void handleTeamStyle(Arena arena, Sidebar sidebar) {
        List<TeamPreset> teamList = arena.getTeamList();

        for (TeamPreset team : teamList) {
            if (team == null || team.getPlayers().isEmpty()) continue;

            if (arena.isSlot(Slot.SOLO)) {
                Player player = team.getPlayers().stream().findFirst().orElse(null);

                if (player == null) continue;

                PearlFightUser user = (PearlFightUser) PearlFightUser.of(player.getUniqueId());

                if (user != null) {
                    String hearts = "❤".repeat(user.getLives());
                    sidebar.addRow(team.getCodeId(), team.getColor() + player.getName() + ": §4" + hearts);
                }
            }
        }

        sidebar.blankRow();
    }

    @Override
    public void updateTeamStyle(Arena arena, Sidebar sidebar) {
        List<TeamPreset> teamList = arena.getTeamList();

        for (TeamPreset team : teamList) {
            if (team == null || team.getPlayers().isEmpty()) continue;

            if (arena.isSlot(Slot.SOLO)) {
                Player player = team.getPlayers().stream().findFirst().orElse(null);

                if (player == null) continue;

                PearlFightUser user = (PearlFightUser) PearlFightUser.of(player.getUniqueId());

                if (user != null) {
                    String hearts = "❤".repeat(user.getLives());
                    sidebar.updateRow(team.getCodeId(), team.getColor() + player.getName() + ": §4" + hearts);
                }
            }
        }
    }

    @Override
    public void handleDeath(User user, User killer, DeathCause cause) {
        Player player = user.getAccount().player();

        Arena arena = user.getArena();

        PearlFightUser pearlUser;
        if (user instanceof PearlFightUser) {
            pearlUser = (PearlFightUser) user;
        } else {
            User foundUser = (User) User.of(player.getUniqueId());
            if (foundUser instanceof PearlFightUser) {
                pearlUser = (PearlFightUser) foundUser;
            } else {
                Core.getLogger().warning("[PearlFight] User não é PearlFightUser para " + player.getName() + ". Usando lógica padrão.");
                super.handleDeath(user, killer, cause);
                return;
            }
        }

        TeamPreset team = pearlUser.getTeam();

        if (killer != null) {
            killer.setKills(killer.getKills() + 1);
            killer.setCombat(null);
        }

        pearlUser.removeLife();

        sendDeathMessage(arena, pearlUser, killer, cause);

        arena.getMatchUsers().forEach(matchUser -> updateTeamStyle(arena, matchUser.getSidebar()));

        BukkitCore.getManager().getCooldown().resetCooldown(player);

        // Atualiza a saúde máxima baseado nas vidas restantes
        updatePlayerHealth(player, pearlUser);

        if (pearlUser.isDead()) {
            TeamPreset winner = (TeamPreset) arena.getTeams().stream()
                    .filter(foundTeam -> !foundTeam.equals(team))
                    .findFirst()
                    .orElse(null);

            if (winner != null) {
                arena.setWinner(winner);
                arena.setPhase(RoomPhase.ENDING);
                pearlUser.setState(ArcadeState.DEAD);
            }
        } else {
            final Location spawnLocation = team.getBase();
            
            if (spawnLocation != null && spawnLocation.getWorld() != null) {
                player.teleport(spawnLocation);
            }

            player.setFireTicks(0);
            player.setFallDistance(0);
            player.getActivePotionEffects().forEach(effect -> 
                player.removePotionEffect(effect.getType())
            );
            
            player.setAllowFlight(true);
            player.setFlying(true);
            player.setCanPickupItems(false);
            player.setGameMode(GameMode.SPECTATOR);
            player.getInventory().clear();
            
            JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("Duels");
            if (plugin != null) {
                final int[] countdown = {3};
                
                ProtocolHandler.sendBar(player, "§eRenascendo em §c" + countdown[0] + "§e...");
                
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!player.isOnline() || !arena.isPhase(RoomPhase.PLAYING) || pearlUser.isDead()) {
                            this.cancel();
                            return;
                        }
                        
                        countdown[0]--;
                        
                        if (countdown[0] > 0) {
                            ProtocolHandler.sendBar(player, "§eRenascendo em §c" + countdown[0] + "§e...");
                        } else {
                            ProtocolHandler.sendBar(player, "§aRenascido!");
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                if (player.isOnline() && arena.isPhase(RoomPhase.PLAYING) && !pearlUser.isDead()) {
                                    player.teleport(spawnLocation);
                                    player.setGameMode(GameMode.SURVIVAL);
                                    player.setAllowFlight(false);
                                    player.setFlying(false);
                                    player.setCanPickupItems(true);
                                    player.setFoodLevel(20);
                                    player.setFireTicks(0);
                                    player.setFallDistance(0);
                                    
                                    handleHotbar(player, arena);
                                    
                                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                        if (player.isOnline()) {
                                            ProtocolHandler.sendBar(player, "");
                                        }
                                    }, 5L);
                                }
                            }, 5L);
                            
                            this.cancel();
                        }
                    }
                }.runTaskTimer(plugin, 20L, 20L);
            }
        }
        pearlUser.setCombat(null);
    }
}

