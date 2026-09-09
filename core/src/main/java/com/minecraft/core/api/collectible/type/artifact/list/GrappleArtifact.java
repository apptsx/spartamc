package com.minecraft.core.api.collectible.type.artifact.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftSnowball;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import net.minecraft.server.v1_8_R3.EntityFishingHook;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GrappleArtifact extends ArtifactCollectible {

    private final Map<UUID, GrappleHook> activeHooks = new HashMap<>();

    public GrappleArtifact() {
        super("Grappler", CollectibleRarity.RARE, Collections.singletonList(RankType.ADMIN), System.currentTimeMillis());

        setIcon(Item.of(Material.LEASH));
        setLore(Arrays.asList(
                "§7Use para lançar um gancho",
                "§7que prende em blocos ou entidades!",
                "",
                "§eClique esquerdo: Lançar gancho",
                "§eClique direito: Puxar"
        ));

        Bukkit.getPluginManager().registerEvents(this, Core.getJavaPlugin());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (activeHooks.containsKey(event.getPlayer().getUniqueId())) {
            activeHooks.get(event.getPlayer().getUniqueId()).remove();
            activeHooks.remove(event.getPlayer().getUniqueId());
        }
    }

    @Override
    public void handle(Player host) {
    }

    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (hasCooldown(player)) return;

        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            handleLaunch(player);
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            handlePull(player);
        }
    }

    private void handleLaunch(Player player) {
        if (activeHooks.containsKey(player.getUniqueId())) {
            activeHooks.get(player.getUniqueId()).remove();
        }

        launchHook(player);
        setCooldown(player, java.util.concurrent.TimeUnit.SECONDS.toMillis(8));
    }

    private void handlePull(Player player) {
        if (!activeHooks.containsKey(player.getUniqueId())) {
            return;
        }

        GrappleHook hook = activeHooks.get(player.getUniqueId());
        if (hook == null || hook.isDead()) {
            activeHooks.remove(player.getUniqueId());
            return;
        }

        if (!hook.isHooked() && !hook.hasBlockHit()) {
            return;
        }

        pullOnce(player, hook);
    }

    private void pullOnce(Player player, GrappleHook hook) {
        Location hookLocation = hook.getBukkitEntity().getLocation();

        double d = hookLocation.distance(player.getLocation());
        double t = d;

        double v_x = (1.0D + 0.07D * t) * (hookLocation.getX() - player.getLocation().getX()) / t;
        double v_y = (1.0D + 0.03D * t) * (hookLocation.getY() - player.getLocation().getY()) / t;
        double v_z = (1.0D + 0.07D * t) * (hookLocation.getZ() - player.getLocation().getZ()) / t;

        Vector v = player.getVelocity();
        v.setX(v_x);
        v.setY(v_y);
        v.setZ(v_z);
        player.setVelocity(v);

        player.playSound(player.getLocation(), Sound.STEP_GRAVEL, 10.0f, 10.0f);

        hook.remove();
        activeHooks.remove(player.getUniqueId());
    }

    private void launchHook(Player player) {
        Location eyeLocation = player.getEyeLocation();
        Location direction = eyeLocation.add(
                player.getLocation().getDirection().getX() * 2,
                player.getLocation().getDirection().getY() * 2,
                player.getLocation().getDirection().getZ() * 2
        );

        GrappleHook hook = new GrappleHook(player.getWorld(), ((CraftPlayer) player).getHandle());
        hook.spawn(direction);
        hook.move(
                player.getLocation().getDirection().getX() * 2.5D,
                player.getLocation().getDirection().getY() * 2.5D,
                player.getLocation().getDirection().getZ() * 2.5D
        );

        activeHooks.put(player.getUniqueId(), hook);
        player.playSound(player.getLocation(), Sound.CREEPER_HISS, 1.0f, 1.0f);

        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                ticks++;
                GrappleHook h = activeHooks.get(player.getUniqueId());

                if (h == null || h.isDead()) {
                    if (activeHooks.containsKey(player.getUniqueId())) {
                        activeHooks.remove(player.getUniqueId());
                    }
                    cancel();
                    return;
                }

                if (h.isHooked() || h.hasBlockHit()) {
                    cancel();
                    return;
                }

                if (ticks > 40) {
                    if (activeHooks.containsKey(player.getUniqueId())) {
                        activeHooks.get(player.getUniqueId()).remove();
                        activeHooks.remove(player.getUniqueId());
                    }
                    cancel();
                }
            }
        }.runTaskTimer(Core.getJavaPlugin(), 0L, 1L);
    }

    public class GrappleHook extends EntityFishingHook {

        private final Player owner;
        private org.bukkit.entity.Snowball snowball;
        private net.minecraft.server.v1_8_R3.EntitySnowball controller;
        private Entity hooked;
        private boolean isHooked;
        private boolean lastControllerDead;
        private boolean blockHit;

        public GrappleHook(org.bukkit.World world, EntityHuman entityhuman) {
            super(((CraftWorld) world).getHandle(), entityhuman);
            this.owner = (Player) entityhuman.getBukkitEntity();
        }

        protected void c() {}

        public void t_() {
            if (this.controller != null) {
                this.lastControllerDead = this.controller.dead;

                for (Entity entity : this.controller.world.getWorld().getEntities()) {
                    if (!(entity instanceof LivingEntity)) continue;
                    if (entity instanceof Firework) continue;
                    if (entity.getEntityId() == getBukkitEntity().getEntityId()) continue;
                    if (entity.getEntityId() == this.owner.getEntityId()) continue;
                    if (this.controller == null || entity.getEntityId() == this.controller.getBukkitEntity().getEntityId()) continue;

                    double dist = entity.getLocation().distance(this.controller.getBukkitEntity().getLocation());
                    double eyeDist = entity instanceof Player ? ((Player) entity).getEyeLocation().distance(this.controller.getBukkitEntity().getLocation()) : dist;

                    if (dist < 2.0D || eyeDist < 2.0D) {
                        this.controller.die();
                        this.hooked = entity;
                        this.isHooked = true;
                        this.locX = entity.getLocation().getX();
                        this.locY = entity.getLocation().getY();
                        this.locZ = entity.getLocation().getZ();
                        this.motX = 0.0D;
                        this.motY = 0.04D;
                        this.motZ = 0.0D;
                        return;
                    }
                }

                Location snowballLoc = this.controller.getBukkitEntity().getLocation();
                Block block = snowballLoc.getBlock();
                if (block.getType() != Material.AIR && block.getType() != Material.WATER && block.getType() != Material.STATIONARY_WATER) {
                    this.controller.die();
                    this.blockHit = true;
                    this.locX = snowballLoc.getX();
                    this.locY = snowballLoc.getY();
                    this.locZ = snowballLoc.getZ();
                    this.motX = 0.0D;
                    this.motY = 0.0D;
                    this.motZ = 0.0D;
                    this.lastControllerDead = true;
                }
            }

            try {
                if (this.hooked != null && !this.hooked.isDead()) {
                    this.locX = this.hooked.getLocation().getX();
                    this.locY = this.hooked.getLocation().getY();
                    this.locZ = this.hooked.getLocation().getZ();
                    this.motX = 0.0D;
                    this.motY = 0.04D;
                    this.motZ = 0.0D;
                    this.isHooked = true;
                }
            } catch (Exception e) {
                if (this.controller != null && this.controller.dead) {
                    this.isHooked = true;
                }
            }
        }

        public boolean hasBlockHit() {
            return this.blockHit;
        }

        public void die() {}

        public void remove() {
            super.die();
            activeHooks.remove(owner.getUniqueId());
        }

        public void spawn(Location location) {
            this.snowball = (Snowball) this.owner.launchProjectile(Snowball.class);
            this.snowball.setVelocity(this.snowball.getVelocity().multiply(2.0D));
            this.controller = ((CraftSnowball) this.snowball).getHandle();

            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[]{this.controller.getId()});

            for (Player p : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
            }

            ((CraftWorld) location.getWorld()).getHandle().addEntity(this);
        }

        public void move(double x, double y, double z) {
            this.motX = x;
            this.motY = y;
            this.motZ = z;
        }

        public boolean isHooked() {
            return this.isHooked;
        }

        public boolean isDead() {
            return this.dead;
        }
    }
}