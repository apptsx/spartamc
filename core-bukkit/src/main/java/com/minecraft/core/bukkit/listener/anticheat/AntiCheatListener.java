package com.minecraft.core.bukkit.listener.anticheat;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitCore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiCheatListener implements Listener {

    private static final double MAX_REACH = 4.5;
    private static final double MAX_REACH_VERTICAL = 2.0;
    private static final double BASE_SPEED_LIMIT = 0.65;
    private static final double ICE_SPEED_LIMIT = 1.3;
    private static final double MAX_FALL_DISTANCE = 0.5;
    private static final long MIN_ATTACK_DELAY_MS = 200;
    private static final long MAX_SWING_DELAY_MS = 400;

    private final Map<UUID, PlayerData> playerData = new HashMap<>();

    public AntiCheatListener() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : BukkitCore.getInstance().getServer().getOnlinePlayers()) {
                    AntiCheatAPI.reduceAllViolations(player);
                }
            }
        }.runTaskTimerAsynchronously(BukkitCore.getInstance(), 0L, 20L * 30);
    }

    private static class PlayerData {
        boolean wasOnGround = true;
        double lastDeltaY = 0.0;
        int spiderTicks = 0;
        long lastMoveTime = System.currentTimeMillis();
        long lastAttackTime = 0;
        long lastDamageTime = 0;
    }

    private PlayerData getData(Player player) {
        return playerData.computeIfAbsent(player.getUniqueId(), k -> new PlayerData());
    }

    private boolean isStaff(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        return account != null && account.isStaffer();
    }

    private boolean isInLiquid(Player player) {
        Location loc = player.getLocation();
        Material m = loc.getBlock().getType();
        Material below = loc.getBlock().getRelative(BlockFace.DOWN).getType();
        return m == Material.WATER || below == Material.WATER
                || m == Material.LAVA || below == Material.LAVA
                || m == Material.STATIONARY_WATER || below == Material.STATIONARY_WATER
                || m == Material.STATIONARY_LAVA || below == Material.STATIONARY_LAVA;
    }

    private boolean isClimbable(Block block) {
        Material t = block.getType();
        return t == Material.LADDER || t == Material.VINE;
    }

    private boolean isIce(Block block) {
        Material t = block.getType();
        return t == Material.ICE || t == Material.PACKED_ICE;
    }

    private boolean isPartialHeight(Block block) {
        if (block == null) return false;
        Material t = block.getType();
        if (t == Material.SOUL_SAND) return true;
        if (t == Material.SNOW || t == Material.SNOW_BLOCK) return true;
        if (t == Material.CARPET || t.name().contains("STEP") || t.name().contains("SLAB")) return true;
        return !t.isOccluding();
    }

    private boolean isNearSolidBlock(Player player) {
        Location loc = player.getLocation();
        Block b = loc.getBlock();
        return b.getRelative(BlockFace.NORTH).getType().isSolid()
                || b.getRelative(BlockFace.SOUTH).getType().isSolid()
                || b.getRelative(BlockFace.EAST).getType().isSolid()
                || b.getRelative(BlockFace.WEST).getType().isSolid();
    }

    private int getPotionAmplifier(Player player, PotionEffectType type) {
        Collection<org.bukkit.potion.PotionEffect> effects = player.getActivePotionEffects();
        for (org.bukkit.potion.PotionEffect effect : effects) {
            if (effect.getType().equals(type))
                return effect.getAmplifier();
        }
        return -1;
    }

    private int getPing(Player player) {
        try {
            Object ep = player.getClass().getMethod("getHandle").invoke(player);
            return (int) ep.getClass().getField("ping").get(ep);
        } catch (Exception e) {
            return 0;
        }
    }

    private void checkSpeed(Player player, Location from, Location to) {
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isInsideVehicle()) return;
        if (isInLiquid(player)) return;

        PlayerData data = getData(player);

        double deltaX = to.getX() - from.getX();
        double deltaZ = to.getZ() - from.getZ();
        double horizontal = Math.hypot(deltaX, deltaZ);

        double limit = BASE_SPEED_LIMIT;

        Block below = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        if (isIce(below)) limit = ICE_SPEED_LIMIT;

        int speedAmp = getPotionAmplifier(player, PotionEffectType.SPEED);
        if (speedAmp >= 0) {
            limit += (speedAmp + 1) * 0.15;
        }

        long now = System.currentTimeMillis();
        long diff = now - data.lastMoveTime;
        data.lastMoveTime = now;
        if (diff < 5) diff = 5;
        double ticks = Math.min(20.0, Math.max(5.0, (getPing(player) + 100) / 50.0));
        double tickFactor = Math.max(1.0, diff / 50.0);
        tickFactor = Math.min(tickFactor, ticks);

        limit *= tickFactor;

        if (horizontal > limit) {
            AntiCheatAPI.addViolation(player, AntiCheatAPI.HackType.SPEED);
        } else {
            AntiCheatAPI.reduceViolation(player, AntiCheatAPI.HackType.SPEED);
        }
    }

    private void checkFlight(Player player, Location from, Location to, PlayerData data) {
        if (player.getAllowFlight() || player.isFlying()) return;
        if (isInLiquid(player)) return;
        if (player.isInsideVehicle()) return;
        if (player.hasPotionEffect(PotionEffectType.JUMP)) return;

        if (isClimbable(player.getLocation().getBlock())
                || isClimbable(player.getLocation().getBlock().getRelative(BlockFace.DOWN))) return;

        double deltaY = to.getY() - from.getY();
        boolean wasOnGround = data.wasOnGround;
        data.wasOnGround = player.isOnGround();

        if (!player.isOnGround() && !wasOnGround && deltaY > 0) {
            double expected = (data.lastDeltaY - 0.08) * 0.98;
            int ping = getPing(player);
            double margin = ping > 300 ? 0.05 : 0.0;

            if (expected > 0 && deltaY > (expected + 0.1 + margin) && player.getFallDistance() < MAX_FALL_DISTANCE) {
                AntiCheatAPI.addViolation(player, AntiCheatAPI.HackType.FLY);
            }
        } else if (player.isOnGround()) {
            AntiCheatAPI.reduceViolation(player, AntiCheatAPI.HackType.FLY);
        }

        data.lastDeltaY = deltaY;
    }

    private void checkSpider(Player player, Location from, Location to, PlayerData data) {
        if (player.getAllowFlight() || player.isFlying()) return;
        if (isInLiquid(player)) return;
        if (player.hasPotionEffect(PotionEffectType.JUMP)) return;

        double deltaY = to.getY() - from.getY();
        if (deltaY > 0 && !player.isOnGround() && isNearSolidBlock(player)) {
            Block b = player.getLocation().getBlock();
            if (!isClimbable(b) && !isClimbable(b.getRelative(BlockFace.DOWN))) {
                data.spiderTicks++;
                if (data.spiderTicks > 10) {
                    AntiCheatAPI.addViolation(player, AntiCheatAPI.HackType.SCAFFOLD);
                    data.spiderTicks = 5;
                }
            } else {
                data.spiderTicks = 0;
            }
        } else {
            data.spiderTicks = 0;
        }
    }

    private void checkPhase(Player player, Location from, Location to) {
        if (player.getAllowFlight() || player.isFlying()) return;
        if (isInLiquid(player)) return;

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) return;

        Block toFeet = to.getBlock();
        Block fromFeet = from.getBlock();
        Block below = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

        if (isPartialHeight(below)) return;
        if (isPartialHeight(fromFeet) || isPartialHeight(toFeet)) return;
        if (isClimbable(toFeet) || isClimbable(toFeet.getRelative(BlockFace.DOWN))) return;

        if (toFeet.getType().isOccluding() && !fromFeet.getType().isOccluding()) {
            Block eye = player.getEyeLocation().getBlock();
            if (eye.getType().isOccluding()) {
                AntiCheatAPI.addViolation(player, AntiCheatAPI.HackType.PACKET);
                return;
            }
        }

        Block fromEye = fromFeet.getRelative(BlockFace.UP);
        Block toEye = toFeet.getRelative(BlockFace.UP);
        if (toEye.getType().isOccluding() && !fromEye.getType().isOccluding()) {
            AntiCheatAPI.addViolation(player, AntiCheatAPI.HackType.PACKET);
        }
    }

    private void checkNoFall(Player player, Location from, Location to, PlayerData data) {
        if (player.getAllowFlight() || player.isFlying()) return;
        if (isInLiquid(player)) return;
        if (player.isInsideVehicle()) return;
        if (player.hasPotionEffect(PotionEffectType.JUMP)) return;

        Block feet = to.getBlock();
        if (isClimbable(feet) || isClimbable(feet.getRelative(BlockFace.DOWN))) return;

        double deltaY = to.getY() - from.getY();

        if (player.isOnGround() && deltaY > 0.001) {
            Block below = feet.getRelative(BlockFace.DOWN);
            if (!isPartialHeight(below)) {
                AntiCheatAPI.addViolation(player, AntiCheatAPI.HackType.FLY);
            }
            return;
        }

        if (player.isOnGround() && player.getFallDistance() > 0.5f) {
            Block below = feet.getRelative(BlockFace.DOWN);
            boolean hasSupport = below.getType().isSolid()
                    || isPartialHeight(below)
                    || below.getType().isOccluding();
            if (!hasSupport) {
                AntiCheatAPI.addViolation(player, AntiCheatAPI.HackType.FLY);
            }
        }
    }

    private void checkReach(Player attacker, Entity target, double distance) {
        if (target instanceof Player) {
            Player p = (Player) target;
            double verticalDiff = Math.abs(attacker.getEyeLocation().getY() - p.getEyeLocation().getY());
            double horizontal = Math.hypot(
                    attacker.getLocation().getX() - p.getLocation().getX(),
                    attacker.getLocation().getZ() - p.getLocation().getZ()
            );
            if (horizontal > MAX_REACH || verticalDiff > MAX_REACH_VERTICAL) {
                AntiCheatAPI.addViolation(attacker, AntiCheatAPI.HackType.REACH);
            } else {
                AntiCheatAPI.reduceViolation(attacker, AntiCheatAPI.HackType.REACH);
            }
        } else if (distance > MAX_REACH) {
            AntiCheatAPI.addViolation(attacker, AntiCheatAPI.HackType.REACH);
        } else {
            AntiCheatAPI.reduceViolation(attacker, AntiCheatAPI.HackType.REACH);
        }
    }

    private void checkAttackSpeed(Player attacker, PlayerData data) {
        long now = System.currentTimeMillis();
        long sinceLast = now - data.lastAttackTime;

        if (data.lastAttackTime > 0 && sinceLast < MIN_ATTACK_DELAY_MS) {
            AntiCheatAPI.addViolation(attacker, AntiCheatAPI.HackType.TIMER);
        } else if (sinceLast > MIN_ATTACK_DELAY_MS * 2) {
            AntiCheatAPI.reduceViolation(attacker, AntiCheatAPI.HackType.TIMER);
        }
        data.lastAttackTime = now;
    }

    private void checkAttackSequence(Player player, PlayerData data) {
        if (data.lastDamageTime > 0) {
            long since = System.currentTimeMillis() - data.lastDamageTime;
            if (since > MAX_SWING_DELAY_MS) {
                AntiCheatAPI.addViolation(player, AntiCheatAPI.HackType.AIM);
            }
            data.lastDamageTime = 0;
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (isStaff(player)) return;
        if (player.getGameMode().toString().contains("CREATIVE")
                || player.getGameMode().toString().contains("SPECTATOR")) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        PlayerData data = getData(player);

        checkSpider(player, from, to, data);
        checkFlight(player, from, to, data);
        checkNoFall(player, from, to, data);
        checkSpeed(player, from, to);
        checkPhase(player, from, to);
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (isStaff(player)) return;
        if (player.getAllowFlight()) return;

        if (!player.isOnGround()) {
            AntiCheatAPI.addViolation(player, AntiCheatAPI.HackType.FLY);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            if (isStaff(attacker)) return;

            double distance = attacker.getLocation().distance(event.getEntity().getLocation());
            checkReach(attacker, event.getEntity(), distance);

            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                PlayerData data = getData(attacker);
                checkAttackSpeed(attacker, data);
                data.lastDamageTime = System.currentTimeMillis();
            }
        }

        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            PlayerData victimData = getData(victim);
            victimData.lastDamageTime = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        Player player = event.getPlayer();
        if (isStaff(player)) return;

        PlayerData data = getData(player);
        if (data.lastDamageTime > 0) {
            long since = System.currentTimeMillis() - data.lastDamageTime;
            if (since > MAX_SWING_DELAY_MS) {
                AntiCheatAPI.addViolation(player, AntiCheatAPI.HackType.AIM);
            }
            data.lastDamageTime = 0;
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerData.remove(player.getUniqueId());
        AntiCheatAPI.removePlayer(player.getUniqueId());
    }
}
