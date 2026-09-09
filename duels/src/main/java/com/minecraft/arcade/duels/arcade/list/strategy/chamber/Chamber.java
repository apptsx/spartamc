package com.minecraft.arcade.duels.arcade.list.strategy.chamber;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.core.bukkit.event.type.player.PlayerCooldownEndEvent;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import com.minecraft.arcade.duels.arcade.Arcade;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.arcade.objects.style.SidebarStyle;
import com.minecraft.arcade.duels.user.User;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.PlayerInventory;

@IgnoreEvent
public class Chamber extends Arcade {

    private final String ARROW_COOLDOWN_KEY = "Flecha";

    public Chamber(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.DUELS_CHAMBER);

        setStyle(SidebarStyle.POINT);
        setMaxScore(3);
    }

    @Override
    public void handleHotbar(Player player, Arena arena) {
        if (arena.isValid(player)) {
            User user = (User) User.of(player.getUniqueId());

            PlayerInventory inv = player.getInventory();

            inv.clear();

            BukkitUtil.sendLeatherArmor(player, user.getTeam().getRgb());

            inv.setItem(0, new Item(Material.WOOD_SWORD).unbreakable());
            inv.setItem(1, new Item(Material.BOW).unbreakable());

            inv.setItem(2, new Item(Material.ARROW));
        } else
            handleDefaultHotbar(player);
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
    public void onArrowHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        projectile.remove();

        if (projectile instanceof Arrow && projectile.getShooter() instanceof Player) {
            Player shooter = (Player) projectile.getShooter();

            if (event.getHitEntity() instanceof Player) {
                Player receiver = (Player) event.getHitEntity();

                if (!(isValid(shooter) || isValid(receiver)) || receiver.equals(shooter))
                    return;

                User user = (User) User.of(shooter.getUniqueId()),
                        receiverUser = (User) User.of(receiver.getUniqueId());

                if (user.getTeam().equals(receiverUser.getTeam())) return;

                Arena arena = user.getArena();

                if (arena.equals(receiverUser.getArena()) && arena.isPhase(RoomPhase.PLAYING)) {
                    TeamPreset winner = user.getTeam();

                    winner.setScore(winner.getScore() + 1);

                    arena.send(receiverUser.getTeam().getColor() + receiver.getName() + "§e foi atingido por " +
                            winner.getColor() + shooter.getName() + "§e.");

                    if (winner.getScore() >= getMaxScore()) {
                        arena.setWinner(winner);

                        arena.setPhase(RoomPhase.ENDING);

                        receiverUser.setState(ArcadeState.DEAD);
                        arena.pullBack(receiver);
                    } else {
                        /* Realizar reinício da partida */
                        arena.setPhase(RoomPhase.RESTARTING);

                        shooter.playSound(shooter.getLocation(), Sound.SUCCESSFUL_HIT, 1.0f, 1.0f);
                    }
                }
            }
        }
    }
}
