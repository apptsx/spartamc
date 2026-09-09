package com.minecraft.arcade.duels.arcade.list.combat.fireball;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.map.area.Cuboid;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import com.minecraft.core.util.list.bukkit.ColorUtil;
import com.minecraft.core.util.list.serialization.Serialization;
import com.minecraft.arcade.duels.arcade.Arcade;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.arcade.objects.style.SidebarStyle;
import com.minecraft.arcade.duels.user.User;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.minecraft.core.util.list.bukkit.BukkitUtil.consumeItem;

@IgnoreEvent
public class Fireball extends Arcade {

    private final List<Material> DESTRUCTIBLE_BLOCKS = Arrays.asList(Material.ENDER_STONE, Material.WOOD, Material.BED_BLOCK);

    private final List<EntityDamageEvent.DamageCause> CAUSES = Arrays.asList(
            EntityDamageEvent.DamageCause.PROJECTILE, EntityDamageEvent.DamageCause.BLOCK_EXPLOSION, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION);

    private final Set<Material> BLOCKS_PERMITTED_TO_EXPLODE = Stream.of(Material.ENDER_STONE, Material.WOOD, Material.WOOL, Material.STAINED_CLAY).collect(Collectors.toSet());

    public Fireball(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.DUELS_FIREBALL);

        setStyle(SidebarStyle.BED);

        getBlocksAllowedToBreak().addAll(Arrays.asList(Material.WOOD, Material.ENDER_STONE));
    }

    @Override
    public void handleHotbar(Player player, Arena arena) {
        if (arena.isValid(player)) {
            User user = (User) User.of(player.getUniqueId());

            BukkitUtil.sendLeatherArmor(player, user.getTeam().getRgb());
            
            String base64 = user.getMember().getBase64(ArcadeCategory.DUELS_FIREBALL);

            if (base64 != null)
                Serialization.sendInventoryToPlayerFromBase64WithColor(player, base64, Material.WOOL, ColorUtil.getIdByColor(user.getTeam().getColor()));
        } else
            handleDefaultHotbar(player);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (isValid(player)) {
            User user = (User) User.of(player.getUniqueId());

            TeamPreset team = user.getTeam();

            Arena arena = user.getArena();

            Block block = event.getBlock();

            Location blockLoc = block.getLocation();

            boolean destruct = arena.getTeamList().stream().anyMatch(found -> {
                Location bedLocation = found.getBedLocation();

                return DESTRUCTIBLE_BLOCKS.contains(block.getType()) && bedLocation.distance(blockLoc) <= 8;
            });

            if (destruct) {
                event.setCancelled(false);

                if (block.getType() == Material.BED_BLOCK && !team.isYourBed(blockLoc)) {
                    TeamPreset found = arena.getTeamByBed(blockLoc);

                    if (found != null) {
                        block.setType(Material.AIR);

                        arena.send("",
                                "§f§lCAMA DESTRUÍDA §f" + Constant.ARROW_SYMBOL + "§7 A " + found.getColor() + "Cama do " + found.getName()
                                        + "§7 foi destruída por " + team.getColor() + player.getName() + "§7.",
                                "");

                        arena.sound(Sound.ENDERDRAGON_GROWL, 13, 1);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.hasItem() && event.getAction().name().contains("RIGHT") && isValid(player)) {
            ItemStack hand = player.getItemInHand();

            if (hand == null || hand.getType() == Material.AIR) return;

            // Fireball
            if (hand.getType() == Material.FIREBALL) {
                org.bukkit.entity.Fireball fireball = player.launchProjectile(org.bukkit.entity.Fireball.class);

                fireball.getWorld().playSound(fireball.getLocation(), Sound.GHAST_FIREBALL, 1, 1);

                fireball.setYield(0);
                fireball.setFireTicks(0);
                fireball.setIsIncendiary(false);

                consumeItem(player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onTNTPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (isValid(player)) {
            Block block = event.getBlockPlaced();

            User user = (User) User.of(player.getUniqueId());

            Arena arena = user.getArena();

            Location location = block.getLocation().clone();
            World world = block.getWorld();

            if (arena.isPhase(RoomPhase.PLAYING)) {
                consumeItem(player);

                if (block.getType() == Material.TNT) {
                    // Ajusta a localização para o centro do bloco
                    location = location.add(0.5, 0.5, 0.5);

                    TNTPrimed tntPrimed = world.spawn(location, TNTPrimed.class);

                    block.setType(Material.AIR);

                    tntPrimed.setIsIncendiary(false);
                    tntPrimed.setYield(0);
                    tntPrimed.setFuseTicks(40);
                    tntPrimed.setFireTicks(0);

                    tntPrimed.setVelocity(new Vector(0, 0, 0));
                    tntPrimed.teleport(location);

                    world.playSound(location, Sound.FUSE, 1F, 0.8F);
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && CAUSES.contains(event.getCause())) {
            Player player = (Player) event.getEntity();

            if (!isValid(player)) return;

            event.setDamage(0.1);

            double horizontal = 11.5;

            Vector direction = player.getVelocity().multiply(horizontal).setY(0.3);

            player.setVelocity(direction);
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        Location location = event.getLocation();

        World world = location.getWorld();

        if (!isArena(world)) return;

        Arena arena = (Arena) getRoom(world);

        if (event.getEntity() instanceof TNTPrimed && event.blockList().isEmpty()) {
            int yield = 3;

            world.createExplosion(location.getX(), location.getY(), location.getZ(), yield, false, false);

            List<Block> blocks = new ArrayList<>(Cuboid.getBlocksFromCenter(event.getEntity(), yield));

            EntityExplodeEvent entityExplodeEvent = new EntityExplodeEvent(event.getEntity(), location, blocks, yield);

            Bukkit.getPluginManager().callEvent(entityExplodeEvent);
        }

        event.setCancelled(true);

        if (arena != null) {

            Core.getPlatform().runSync(() -> {
                List<Block> blocks = event.blockList()
                        .stream()
                        .filter(block -> {
                            final boolean isDestructible = arena.getTeamList().stream().anyMatch(team -> {
                                Location bedLocation = team.getBedLocation();

                                return DESTRUCTIBLE_BLOCKS.contains(block.getType()) && bedLocation.distance(block.getLocation()) <= 8;
                            });

                            return isDestructible || BLOCKS_PERMITTED_TO_EXPLODE.contains(block.getType()) && arena.isReversible(block);
                        })
                        .limit(Core.RANDOM.ints(5, 9).findFirst().orElse(5))
                        .collect(Collectors.toList());

                blocks.forEach(Block::breakNaturally);
            }, 2L);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        if (projectile instanceof org.bukkit.entity.Fireball) {
            World world = projectile.getWorld();

            Location location = projectile.getLocation();

            int yield = 3;

            world.createExplosion(location.getX(), location.getY(), location.getZ(), yield, false, false);

            List<Block> blocks = new ArrayList<>(Cuboid.getBlocksFromCenter(projectile, yield));

            EntityExplodeEvent entityExplodeEvent = new EntityExplodeEvent(projectile, location, blocks, yield);

            Bukkit.getPluginManager().callEvent(entityExplodeEvent);
        }
    }
}
