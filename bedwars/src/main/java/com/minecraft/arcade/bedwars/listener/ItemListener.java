package com.minecraft.arcade.bedwars.listener;

import com.minecraft.arcade.bedwars.BedWars;
import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.menu.shop.ShopMenu;
import com.minecraft.arcade.bedwars.structure.egg.EggBridgeManager;
import com.minecraft.arcade.bedwars.structure.egg.EggBridgeTask;
import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.room.map.area.Cuboid;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import com.minecraft.core.bukkit.manager.list.CooldownManager;
import com.minecraft.core.bukkit.manager.list.DropValidator;
import com.minecraft.core.member.list.bedwars.objects.enums.BedShop;
import com.minecraft.core.util.list.StringUtil;
import com.minecraft.core.util.list.TimeUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.minecraft.core.util.list.bukkit.BukkitUtil.consumeItem;
import static com.minecraft.core.util.list.bukkit.BukkitUtil.removeItemInHand;

public class ItemListener implements Listener {

    private final BedWars plugin;

    private final String SPAWNED_BY_KEY = "spawnedBy";
    private final String TEAM_KEY = "teamOwner";
    private final String SWAP_SNOWBALL_KEY = "swapSnowball";

    private final List<EntityDamageEvent.DamageCause> causes;

    private final Set<Material> BLOCKS_PERMITTED_TO_EXPLODE = Stream
            .of(Material.ENDER_STONE, Material.WOOD, Material.WOOL, Material.STAINED_CLAY)
            .collect(Collectors.toSet());

    public ItemListener() {
        this.plugin = BedWars.getPlugin(BedWars.class);

        this.causes = Arrays.asList(
                EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
                EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
        );
    }

    @EventHandler
    public void onUtilityItems(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Arena arena = user.getArena();

        Team team = user.getTeam();

        ItemStack hand = player.getItemInHand();

        if (arena.isPhase(RoomPhase.PLAYING) && hand != null && !hand.getType().equals(Material.AIR) && event.getAction().name().contains("RIGHT")) {

            switch (hand.getType()) {
                case COMPASS: {
                    player.performCommand("bussola");
                    break;
                }

                case BOOK: {
                    event.setCancelled(true);
                    if (user.getContext().isPurchasedPortableShop()) {
                        ShopMenu shopMenu = new ShopMenu(player, BedShop.FAVORITE);
                        shopMenu.handle();
                        shopMenu.display();
                    } else {
                        player.sendMessage("§cVocê precisa comprar a Loja Portátil na loja primeiro!");
                    }
                    break;
                }

                case MAGMA_CREAM: {
                    String SLINGSHOT_COOLDOWN = "Estilingue";
                    if (hasCooldown(player, SLINGSHOT_COOLDOWN)) return;

                    consumeItem(player);

                    Vector vector = player.getEyeLocation().getDirection().multiply(1.1F).setY(1.2F);

                    player.setFallDistance(-1.0F);
                    player.setVelocity(vector);

                    player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1.0f, 1.0f);

                    setCooldown(player, SLINGSHOT_COOLDOWN, 5);
                    break;
                }

                case FIREBALL: {
                    if (hasCooldown(player, "Fireball")) return;

                    consumeItem(player);

                    Fireball fireball = player.launchProjectile(Fireball.class);

                    fireball.getWorld().playSound(fireball.getLocation(), Sound.GHAST_FIREBALL, 1, 1);

                    fireball.setYield(0);
                    fireball.setFireTicks(0);
                    fireball.setIsIncendiary(false);

                    setCooldown(player, "Fireball", 1);
                    break;
                }

                case SNOW_BALL: {
                    String MOTH_COOLDOWN_KEY = "Traças";

                    if (hasCooldown(player, MOTH_COOLDOWN_KEY)) {
                        event.setCancelled(true);
                        return;
                    }

                    if (team == null) return;

                    // Consumir o item (remover 1 unidade)
                    consumeItem(player);

                    Snowball snowball = player.launchProjectile(Snowball.class);

                    snowball.setVelocity(snowball.getVelocity().multiply(1.5));

                    // Armazenar o TEAM ao invés da lista de players
                    snowball.setMetadata(TEAM_KEY, new FixedMetadataValue(plugin, team));

                    setCooldown(player, MOTH_COOLDOWN_KEY, 15);
                    break;
                }

                case CLAY_BALL: {
                    String SWAP_COOLDOWN_KEY = "Bola de Neve Swap";

                    if (hasCooldown(player, SWAP_COOLDOWN_KEY)) {
                        event.setCancelled(true);
                        return;
                    }

                    if (team == null) return;

                    // Consumir o item (remover 1 unidade)
                    consumeItem(player);

                    Snowball snowball = player.launchProjectile(Snowball.class);

                    snowball.setVelocity(snowball.getVelocity().multiply(1.5));
                    snowball.setMetadata(SWAP_SNOWBALL_KEY, new FixedMetadataValue(plugin, true));
                    snowball.setMetadata(TEAM_KEY, new FixedMetadataValue(plugin, team));

                    setCooldown(player, SWAP_COOLDOWN_KEY, 15);
                    break;
                }

                case MONSTER_EGG: {
                    String GOLEM_COOLDOWN_KEY = "Golem";

                    if (hasCooldown(player, GOLEM_COOLDOWN_KEY)) return;

                    if (team == null) return;

                    // Remover apenas 1 item (o ovo de monstro) ao invés de todos
                    if (hand != null && hand.getAmount() > 1) {
                        hand.setAmount(hand.getAmount() - 1);
                    } else {
                        player.setItemInHand(null);
                    }
                    player.updateInventory();

                    // Spawnar golem com CUSTOM reason para não ser cancelado
                    World world = player.getLocation().getWorld();
                    Location spawnLocation = player.getLocation().clone();
                    
                    // Usar spawnEntity com CUSTOM reason explicitamente
                    IronGolem golem = (IronGolem) world.spawnEntity(spawnLocation, EntityType.IRON_GOLEM);
                    golem.setCustomName("§cDefensor (" + TimeUtil.time(60 * 4) + ")");
                    golem.setCustomNameVisible(true);

                    // Armazenar o TEAM ao invés da lista de players
                    golem.setMetadata(TEAM_KEY, new FixedMetadataValue(plugin, team));

                    Core.getPlatform().runSync(() -> setEntityTarget(golem), 1L);

                    new BukkitRunnable() {
                        int time = 60 * 4;

                        @Override
                        public void run() {
                            time--;

                            if (time <= 0 && golem.isValid()) {
                                golem.remove();

                                cancel();
                                return;
                            }

                            golem.setCustomName("§cDefensor (" + TimeUtil.time(time) + ")");
                        }
                    }.runTaskTimer(plugin, 0, 20);

                    setCooldown(player, GOLEM_COOLDOWN_KEY, 120);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        if (projectile instanceof Egg) {
            EggBridgeManager.end((Egg) projectile);
        }

        if (projectile instanceof Snowball) {
            Snowball snowball = (Snowball) projectile;

            if (snowball.hasMetadata(SWAP_SNOWBALL_KEY)) {
                Entity hitEntity = event.getHitEntity();

                if (hitEntity instanceof Player) {
                    Player hitPlayer = (Player) hitEntity;
                    Player shooter = (Player) snowball.getShooter();

                    if (shooter != null && hitPlayer != null && !hitPlayer.equals(shooter)) {
                        User shooterUser = (User) User.of(shooter.getUniqueId());
                        User hitUser = (User) User.of(hitPlayer.getUniqueId());

                        if (shooterUser != null && hitUser != null &&
                            shooterUser.isPlayer() && hitUser.isPlayer() &&
                            shooterUser.inState(ArcadeState.ALIVE) && hitUser.inState(ArcadeState.ALIVE)) {

                            Team shooterTeam = shooterUser.getTeam();
                            Team hitTeam = hitUser.getTeam();

                            if (shooterTeam == null || hitTeam == null || !shooterTeam.equals(hitTeam)) {
                                Location shooterLoc = shooter.getLocation().clone();
                                Location hitLoc = hitPlayer.getLocation().clone();

                                shooter.teleport(hitLoc);
                                hitPlayer.teleport(shooterLoc);

                                shooter.playSound(shooter.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
                                hitPlayer.playSound(hitPlayer.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);

                                shooter.sendMessage("§aVocê trocou de lugar com §f" + hitPlayer.getName() + "§a!");
                                hitPlayer.sendMessage("§aVocê trocou de lugar com §f" + shooter.getName() + "§a!");
                            }
                        }
                    }
                }
                return;
            }

            if (snowball.hasMetadata(TEAM_KEY)) {
                Team team = (Team) snowball.getMetadata(TEAM_KEY).stream()
                        .findFirst().map(MetadataValue::value).orElse(null);

                if (team == null) return;

                // Vendo se a bola de neve ainda é válida, e spawnando traças
                Location location = snowball.getLocation();
                World world = location.getWorld();

                for (int i = 0; i < 3; i++) {
                    // Spawnar silverfish com CUSTOM reason para não ser cancelado
                    // Usar spawnEntity que usa CUSTOM reason por padrão
                    Silverfish silverFish = (Silverfish) world.spawnEntity(location, EntityType.SILVERFISH);
                    silverFish.setCustomName("§eTraça (" + TimeUtil.time(15) + ")");
                    silverFish.setCustomNameVisible(true);
                    silverFish.setTarget(null);

                    // Armazenar o TEAM ao invés da lista de players
                    silverFish.setMetadata(TEAM_KEY, new FixedMetadataValue(plugin, team));
                    silverFish.setMetadata("ball", new FixedMetadataValue(plugin, snowball));

                    // Definir alvo imediatamente após spawn
                    Core.getPlatform().runSync(() -> setEntityTarget(silverFish), 1L);
                }

                new BukkitRunnable() {
                    int time = 15;

                    @Override
                    public void run() {
                        time--;

                        List<Silverfish> entities = new ArrayList<>(snowball.getWorld().getEntitiesByClass(Silverfish.class))
                                .stream()
                                .filter(entity -> isSilverfishFromSnowball(entity, snowball))
                                .collect(Collectors.toList());

                        if (time <= 0) {
                            entities.forEach(Entity::remove);

                            cancel();
                            return;
                        }

                        entities.forEach(entity -> entity.setCustomName("§eTraça (" + TimeUtil.time(time) + ")"));
                    }
                }.runTaskTimer(plugin, 0, 20);
            }
        }

        if (projectile instanceof Fireball) {

            if (event.getHitEntity() instanceof Fireball)
                return;

            World world = projectile.getWorld();

            Location location = projectile.getLocation();

            int yield = 3;

            world.createExplosion(location.getX(), location.getY(), location.getZ(), yield, false, false);

            List<Block> blocks = new ArrayList<>(Cuboid.getBlocksFromCenter(projectile, yield));

            blocks.removeIf(block -> isOre(block.getType()));

            EntityExplodeEvent entityExplodeEvent = new EntityExplodeEvent(projectile, location, blocks, yield);

            Bukkit.getPluginManager().callEvent(entityExplodeEvent);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void entityTarget(EntityTargetLivingEntityEvent event) {
        Entity entity = event.getEntity();

        if (!entity.hasMetadata(TEAM_KEY)) return;

        Team entityTeam = (Team) entity.getMetadata(TEAM_KEY).stream()
                .findFirst().map(MetadataValue::value).orElse(null);

        if (entityTeam == null) return;

        // Se o alvo for um Player
        if (event.getTarget() instanceof Player) {
            Player target = (Player) event.getTarget();
            User targetUser = (User) User.of(target.getUniqueId());

            if (targetUser == null) return;

            Team targetTeam = targetUser.getTeam();

            // Cancelar se o alvo for do mesmo time
            if (targetTeam != null && targetTeam.equals(entityTeam)) {
                event.setCancelled(true);
            }
        }

        // Se o alvo for um Silverfish (Traça)
        if (event.getTarget() instanceof Silverfish) {
            Silverfish targetSilverfish = (Silverfish) event.getTarget();

            // Se a traça tem o mesmo time, cancelar
            if (targetSilverfish.hasMetadata(TEAM_KEY)) {
                Team targetTeam = (Team) targetSilverfish.getMetadata(TEAM_KEY).stream()
                        .findFirst().map(MetadataValue::value).orElse(null);

                if (targetTeam != null && targetTeam.equals(entityTeam)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void entityAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Entity entity = event.getEntity();

            if (entity.hasMetadata(TEAM_KEY)) {
                Team entityTeam = (Team) entity.getMetadata(TEAM_KEY).stream()
                        .findFirst().map(MetadataValue::value).orElse(null);

                if (entityTeam == null) return;

                Player damager = (Player) event.getDamager();
                User damagerUser = (User) User.of(damager.getUniqueId());

                if (damagerUser == null) return;

                Team damagerTeam = damagerUser.getTeam();

                // Cancelar se o jogador for do mesmo time da entidade
                if (damagerTeam != null && damagerTeam.equals(entityTeam)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        event.setDroppedExp(0);
    }

    @EventHandler
    public void onEntityFindTarget(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (Room arena : BedWars.getManager().getArcade().getArenas()) {
                World world = arena.getWorld();

                if (world != null)
                    for (Entity entity : world.getEntities()) {
                        if (!(entity instanceof Creature) || !entity.hasMetadata(TEAM_KEY)) continue;

                        Creature creature = (Creature) entity;

                        Team entityTeam = (Team) entity.getMetadata(TEAM_KEY).stream()
                                .findFirst().map(MetadataValue::value).orElse(null);

                        if (entityTeam == null) continue;

                        // Se já tem um alvo válido, não precisa procurar outro
                        if (creature.getTarget() instanceof Player) {
                            Player currentTarget = (Player) creature.getTarget();
                            User currentTargetUser = (User) User.of(currentTarget.getUniqueId());

                            if (currentTargetUser != null &&
                                    currentTargetUser.inState(ArcadeState.ALIVE) &&
                                    !currentTargetUser.isProtected()) {

                                Team currentTargetTeam = currentTargetUser.getTeam();

                                // Se o alvo atual é de time diferente, mantém ele
                                if (currentTargetTeam != null && !currentTargetTeam.equals(entityTeam)) {
                                    continue;
                                }
                            }
                        }

                        // Procurar novo alvo
                        setEntityTarget(creature);
                    }
            }
        }
    }

    private void setEntityTarget(Creature creature) {
        if (!creature.hasMetadata(TEAM_KEY)) return;

        Team entityTeam = (Team) creature.getMetadata(TEAM_KEY).stream()
                .findFirst().map(MetadataValue::value).orElse(null);

        if (entityTeam == null) return;

        // Pegando entidades próximas
        for (Entity nearbyEntity : creature.getNearbyEntities(10, 10, 10)) {
            if (!(nearbyEntity instanceof Player)) continue;

            Player target = (Player) nearbyEntity;

            User user = (User) User.of(target.getUniqueId());

            if (user == null || user.isProtected() || !user.inState(ArcadeState.ALIVE)) continue;

            Team targetTeam = user.getTeam();

            // Atacar apenas se for de time diferente
            if (targetTeam != null && !targetTeam.equals(entityTeam)) {
                creature.setTarget(target);
                break;
            }
        }
    }

    private boolean isSilverfishFromSnowball(Silverfish entity, Snowball snowball) {
        return entity.isValid() && entity.hasMetadata("ball") && entity.getMetadata("ball").stream()
                .findFirst()
                .map(MetadataValue::value)
                .orElse(null) == snowball;
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Arena arena = user.getArena();

        ItemStack item = event.getItem();

        switch (item.getType()) {
            case MILK_BUCKET: {
                event.setCancelled(true);

                user.getContext().setMilkTime(System.currentTimeMillis() + 30000L);

                player.sendMessage("§dLeite Mágico: §cVocê está imune de armadilhas por 30 segundos!");

                removeItemInHand(player);
                break;
            }

            case POTION: {
                int id = item.getDurability();

                event.setCancelled(true);

                switch (id) {
                    // Agilidade
                    case 2: {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 45, 1));

                        removeItemInHand(player);
                        break;
                    }

                    // Super Pulo V
                    case 11: {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 45, 4));

                        removeItemInHand(player);
                        break;
                    }

                    // Invisibilidade
                    case 14: {
                        /* Escondendo armaduras do jogador */
                        for (User arenaUser : arena.getUsers()) {
                            if (arenaUser == null) continue;

                            Player receiver = arenaUser.getAccount().player();

                            if (!arenaUser.isPlayer() || arenaUser.getTeam() != null && !arenaUser.getTeam().equals(user.getTeam())) {
                                ProtocolHandler.hideArmor(player, receiver);
                                ProtocolHandler.hidePlayerName(player, receiver);
                            }
                        }

                        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 30, 0));

                        Core.getPlatform().runSync(() -> arena.getPlayers().forEach(receiver -> {
                            ProtocolHandler.showArmor(player, receiver);
                            ProtocolHandler.showPlayerName(player, receiver);
                        }), 20 * 30);

                        removeItemInHand(player);
                        break;
                    }
                }

                break;
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onInvisibilityDamage(PlayerDamageTargetEvent event) {
        Player player = event.getTarget(), damager = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Arena arena = user.getArena();

        if (!event.isCancelled() && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);

            arena.getPlayers().forEach(receiver -> {
                ProtocolHandler.showArmor(player, receiver);
                ProtocolHandler.showPlayerName(player, receiver);
            });
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        ItemStack potion = event.getPotion().getItem();

        if (potion == null || potion.getType().equals(Material.AIR)) return;

        // Lentidão Arremessável
        if (potion.getDurability() == 16394) {
            event.setCancelled(true);

            event.getAffectedEntities().forEach(entity -> entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 5, 1)));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        User user = (User) User.of(event.getPlayer().getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Player player = event.getPlayer();

        Block block = event.getBlock();
        World world = block.getWorld();

        Location location = block.getLocation();

        Arena arena = user.getArena();

        if (!event.isCancelled() && arena.isPhase(RoomPhase.PLAYING)) {
            consumeItem(player);

            if (block.getType().equals(Material.TNT)) {
                Location adjustedLocation = location.clone().add(0.5, 0.5, 0.5);

                // Criar holograma de contagem
                arena.getAliveUsers().forEach(target -> {
                    // Holograma de TNT ativado
                    if (target.getMember().getMetadata().isTntTimerEnabled()) {
                        Player bukkitPlayer = target.getAccount().player();

                        // Renderizar holograma
                        if (bukkitPlayer != null) {
                            HologramClient hologram = BukkitCore.getManager().getHologram().spawnClient(
                                    bukkitPlayer, "tnt-" + StringUtil.generateNumberCode(5),
                                    adjustedLocation.clone().subtract(0, 1.5, 0)
                            );

                            final long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3);

                            hologram.setText(Collections.singletonList("§c" + TimeUtil.formatCooldown(endTime) + "..."));

                            hologram.spawnTo(bukkitPlayer);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    long remainingTime = endTime - System.currentTimeMillis();

                                    if (remainingTime <= 0) {
                                        cancel();
                                        BukkitCore.getManager().getHologram().removeClient(hologram);
                                        return;
                                    }

                                    hologram.setText(0, "§c" + TimeUtil.formatCooldown(endTime) + "...");
                                }
                            }.runTaskTimer(BedWars.getInstance(), 0, 1);
                        }
                    }
                });

                TNTPrimed tntPrimed = world.spawn(adjustedLocation, TNTPrimed.class);

                block.setType(Material.AIR);

                tntPrimed.setIsIncendiary(false);
                tntPrimed.setYield(0);
                tntPrimed.setFuseTicks(60);
                tntPrimed.setFireTicks(0);

                tntPrimed.setVelocity(new Vector(0, 0, 0));
                tntPrimed.teleport(adjustedLocation);

                world.playSound(adjustedLocation, Sound.FUSE, 1F, 0.8F);
            }

            if (arena.isType(Type.CASUAL) && block.getType().equals(Material.SLIME_BLOCK)) {
                if (!block.hasMetadata("jumper"))
                    block.setMetadata("jumper", new FixedMetadataValue(plugin, true));

                ParticleBuilder particle = new ParticleBuilder(ParticleEffect.VILLAGER_HAPPY, location.clone().add(0.5, 0.5, 0.5));

                particle.setSpeed(0.15f)
                        .setAmount(25)
                        .display();

                world.playSound(location, Sound.FALL_BIG, 2, 2f);

                Core.getPlatform().runSync(() -> block.setType(Material.AIR), 20 * 8);
            }
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        Block block = event.getBlock();

        if (block.hasMetadata("jumper"))
            event.setCancelled(true);
    }

    @EventHandler
    public void jumper(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Arena arena = user.getArena();

        Block down = event.getTo().getBlock().getRelative(BlockFace.DOWN);

        if (arena.isType(Type.CASUAL) && arena.isPhase(RoomPhase.PLAYING)) {

            if (down.getType().equals(Material.SLIME_BLOCK) && down.hasMetadata("jumper")) {
                Vector vector = player.getEyeLocation().getDirection().multiply(1.1F).setY(0.96F);

                player.setFallDistance(-1.0F);
                player.setVelocity(vector);

                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void entityDamageEvent(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player && causes.contains(event.getCause()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplodes(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        Location location = event.getLocation();

        World world = location.getWorld();

        Arena arena = (Arena) BukkitCore.getManager().getArcade().getArenas().stream()
                .filter(room -> room.getWorld().equals(world)).findFirst().orElse(null);

        if (entity instanceof TNTPrimed && event.blockList().isEmpty()) {
            int yield = 2;

            world.createExplosion(location.getX(), location.getY(), location.getZ(), yield, false, false);

            List<Block> blocks = new ArrayList<>(Cuboid.getBlocksFromCenter(entity, yield));

            blocks.removeIf(block -> isOre(block.getType()));

            EntityExplodeEvent entityExplodeEvent = new EntityExplodeEvent(entity, location, blocks, yield);

            Bukkit.getPluginManager().callEvent(entityExplodeEvent);
        }

        event.setCancelled(true);

        if (arena != null) {
            for (Player player : arena.getPlayers()) {
                if (player.getLocation().distance(location) <= 5) {
                    Vector direction = player.getLocation().toVector().subtract(location.toVector());

                    if (direction.length() > 0)
                        direction.normalize();
                    else
                        direction = new Vector(0, 0, 1);

                    double horizontal = entity instanceof TNTPrimed ? 2.5 : entity instanceof Fireball ? 3.2 : 0;

                    Vector horizontalVelocity = direction.multiply(horizontal);

                    double vertical = entity instanceof TNTPrimed ? 1.1 : entity instanceof Fireball ? 1.080 : 0;

                    horizontalVelocity.setY(vertical);

                    player.setVelocity(horizontalVelocity);
                }
            }

            Core.getPlatform().runSync(() -> {
                List<Block> blocks = event.blockList()
                        .stream()
                        .filter(block -> BLOCKS_PERMITTED_TO_EXPLODE.contains(block.getType()) && arena.isReversible(block))
                        .limit(Core.RANDOM.ints(5, 8).findFirst().orElse(5))
                        .collect(Collectors.toList());

                for (Block block : blocks) {
                    if (block.isEmpty()) continue;

                    block.breakNaturally();
                }
            }, 2L);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Egg) {
            Egg egg = (Egg) event.getEntity();

            if (egg.getShooter() instanceof Player) {
                Player shooter = (Player) egg.getShooter();

                User user = (User) User.of(shooter.getUniqueId());

                if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

                EggBridgeManager.save(new EggBridgeTask(egg, shooter, user.getArena(), user.getTeam().getColor()));
            }
        }
    }

    protected boolean hasCooldown(Player player, String name) {
        CooldownManager manager = BukkitCore.getManager().getCooldown();

        if (manager.hasCooldown(player, "item-" + name)) {
            Cooldown cooldown = manager.getCooldown(player, "item-" + name);

            if (cooldown == null) return false;

            player.sendMessage("§cAguarde " + new DecimalFormat("#.#").format(cooldown.getRemaining()) + " para usar " + name + " novamente.");
            return true;
        }

        return false;
    }

    protected void setCooldown(Player player, String name, long time) {
        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Cooldown cooldown = new Cooldown("item-" + name, time);

        cooldown.setShowBar(user.getMember().getMetadata().isShowBar());

        BukkitCore.getManager().getCooldown().addCooldown(player, cooldown);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent evt) {
        Player profile = evt.getPlayer();
        User user = (User) User.of(profile.getUniqueId());

        if (user.isSpectator()) {
            evt.setCancelled(true);
        } else {
            ItemStack item = evt.getItemDrop().getItemStack();
            if (!DropValidator.canDropItem(evt.getPlayer())) {
                evt.setCancelled(true);
                evt.getPlayer().sendMessage("§cVocê não pode executar esta ação no momento!");
                return;
            }

            if (item.getType().name().contains("_SWORD") || item.getType().name().contains("_HELMET") ||
                    item.getType().name().contains("_CHESTPLATE") || item.getType().name().contains("_LEGGINGS") ||
                    item.getType().name().contains("_BOOTS") || item.getType().name().contains("BOW") ||
                    item.getType().name().contains("_PICKAXE") || item.getType().name().contains("_AXE") ||
                    item.getType().name().contains("SHEARS") || item.getType().name().contains("COMPASS")) {
                evt.setCancelled(true);
            }
        }
    }

    private static boolean isOre(Material material) {
        return material == Material.IRON_INGOT || material == Material.GOLD_INGOT 
                || material == Material.EMERALD || material == Material.DIAMOND;
    }

}