package com.minecraft.arcade.pvp.arcade.list.arena;

import com.minecraft.arcade.pvp.arcade.Arcade;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.controller.KitController;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.feast.Feast;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.menu.DailyKitMenu;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.menu.ShopMenu;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.leaderboard.ArenaKillsLeaderboard;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.leaderboard.ArenaDeathsLeaderboard;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.leaderboard.ArenaKillstreakLeaderboard;
import com.minecraft.arcade.pvp.user.User;
import com.minecraft.arcade.pvp.user.factory.list.ArenaUser;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.permission.Permission;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageEvent;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.member.list.pvp.PvPMember;
import com.minecraft.core.member.list.pvp.stats.list.ArenaStats;
import com.minecraft.core.util.list.DateUtil;
import com.minecraft.core.util.list.TimeUtil;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static com.minecraft.core.util.Util.formatNumber;

@IgnoreEvent
public class Arena extends Arcade {

    private final String FEAST_PROPERTY = "arcade-feast",
            FEAST_HOLOGRAM_KEY = "arcade-feast-hologram";

    public static final String DAILY_KIT_COOLDOWN = "arcade-pvp-daily-kit";

    public Arena(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.PVP_ARENA);
    }

    @Override
    public void join(User user) {
        super.join(user);

        if (user.isJoin(Join.PLAYER)) {
            CompletableFuture.runAsync(() -> {
                new ArenaKillsLeaderboard(user.getAccount().player(), user.getArena()).handle();
                replaceAllSigns(user.getArena());
                removeSpecificSigns(user.getArena());
            });
        }
    }

    public Feast getFeast(com.minecraft.arcade.pvp.arcade.arena.Arena arena) {
        return (Feast) arena.getProperty(FEAST_PROPERTY);
    }

    public void updateFeast(com.minecraft.arcade.pvp.arcade.arena.Arena arena, Feast feast) {
        arena.writeProperty(FEAST_PROPERTY, feast);
    }

    public HologramServer getFeastHologram(com.minecraft.arcade.pvp.arcade.arena.Arena arena) {
        return (HologramServer) arena.getProperty(FEAST_HOLOGRAM_KEY);
    }

    @Override
    public void timer(com.minecraft.arcade.pvp.arcade.arena.Arena arena) {
        if (!arena.hasProperty(FEAST_PROPERTY)) {
            Feast feast = new Feast(arena, arena.getLocation("feast_spawn"));

            HologramServer feastHologram = BukkitCore.getManager().getHologram().spawnServer(FEAST_HOLOGRAM_KEY, feast.getLocation().clone().add(0, 1, 0));

            feastHologram.setText(Arrays.asList(
                    "§6§lFEAST",
                    "§eSurgirá em §b" + TimeUtil.formatTime(feast.getTime())
            ));

            arena.writeProperty(FEAST_PROPERTY, feast);
            arena.writeProperty(FEAST_HOLOGRAM_KEY, feastHologram);
        } else {

            Feast feast = getFeast(arena);

            if (feast.getTime() > 0)
                feast.setTime(feast.getTime() - 1);

            int time = feast.getTime();

            /* Atualizar Holograma */
            HologramServer hologram = getFeastHologram(arena);

            if (hologram != null) {
                String remainingTime = TimeUtil.formatTime(feast.getTime());

                hologram.setText(1, (!feast.isSpawned() ? "§eSurgirá em" : "§eSerá removido em") + " §b" + remainingTime);
            }

            /* Executar operações do feast */
            if (time > 0 && time <= 60 && (time % 15 == 0 || time <= 5)) {
                arena.send("§cO feast será " + (feast.isSpawned() ? "removido" : "spawnado") + " em " + TimeUtil.formatTime(time, TimeUtil.TimeFormat.NORMAL) + ".");
            }

            if (time <= 0) {
                if (feast.isSpawned())
                    feast.destruct();
                else
                    feast.spawn();
            }

            arena.writeProperty(FEAST_PROPERTY, feast);
        }
    }

    @Override
    public void loadEntities(com.minecraft.arcade.pvp.arcade.arena.Arena arena) {
        buildNpcHub(arena);
    }

    @Override
    public void handleDeath(User user, User killer) {
        Account account = user.getAccount();

        PvPMember member = user.getMember();
        ArenaStats stats = member.getArenaStats();

        com.minecraft.arcade.pvp.arcade.arena.Arena arena = user.getArena();

        Player player = account.player();

        boolean hasKiller = killer != null;

        int xp = Core.RANDOM.ints(7, 15).findFirst().orElse(6);

        if (stats.getKillStreak() >= 5 && hasKiller) {
            arena.send(account.getColoredName() + "§e perdeu seu streak de §6" + stats.getKillStreak() + "§e para " + killer.getAccount().getColoredName() + "§e!");

            player.getWorld().strikeLightningEffect(player.getLocation());
        }

        stats.setDeaths();

        account.send("§cVocê morreu" + (hasKiller ? " para " + killer.getAccount().getNickname() : "") + ".",
                "§4-" + xp + " XP.");

        member.updateStats(member.getPvPStats());

        if (player.getFireTicks() > 0)
            Core.getPlatform().runSync(() -> player.setFireTicks(0), 5L);

        for (ItemStack content : player.getInventory().getContents()) {
            if (content != null && BukkitUtil.isRecraft(content.getType()))
                player.getWorld().dropItemNaturally(player.getLocation(), content);
        }

        user.setCombat(null);
        user.getArena().spawn(player);

        if (hasKiller) {
            account = killer.getAccount();
            member = killer.getMember();

            stats = member.getArenaStats();

            xp = Core.RANDOM.ints(15, 30).findFirst().orElse(10);

            int coins = Core.RANDOM.ints(40, 100).findFirst().orElse(40);

            stats.setKills();

            if (stats.getKillStreak() % 5 == 0)
                arena.send(account.getColoredName() + "§e alcançou um streak de §6" + stats.getKillStreak() + "§e!");

            member.addCoins(coins);

            member.updateStats(member.getPvPStats());

            account.send("§aVocê abateu " + player.getName() + ".",
                    "§b+" + xp + " XP",
                    "§6+" + coins + " Coins");

            killer.setCombat(null);

            handleSidebar(killer);
        }
    }

    @Override
    public void handleSidebar(User userObj) {
        ArenaUser user = (ArenaUser) userObj;

        ArenaStats stats = user.getStats();

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle("ARENA");

        sidebar.addRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + user.getArena().getIdentifier());
        sidebar.blankRow();

        sidebar.addRow("kills", "Kills: §7" + formatNumber(stats.getKills()));
        sidebar.addRow("deaths", "Deaths: §7" + formatNumber(stats.getDeaths()));
        sidebar.blankRow();

        if (user.hasPrimary() && !user.getPrimary().isEmpty())
            sidebar.addRow("primary", "Kit 1: §a" + user.getPrimary().getName());

        if (user.hasSecondary() && !user.getSecondary().isEmpty())
            sidebar.addRow("secondary", "Kit 2: §a" + user.getSecondary().getName());

        sidebar.addRow("streak", "Killstreak: §a" + formatNumber(stats.getKillStreak()));

        sidebar.blankRow();
        sidebar.addRow("coins", "Coins: §6" + formatNumber(user.getMember().getCoins()));

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(user.getAccount());
    }

    @Override
    public void buildHotbar(Player player) {
        super.buildHotbar(player);

        ArenaUser user = (ArenaUser) ArenaUser.of(player.getUniqueId());

        PlayerInventory menu = player.getInventory();

        if (user.isProtected()) {
            for (int i = 0; i < 2; i++) {
                int id = i + 1;

                menu.setItem(i, Item.of(Material.CHEST, "§aSelecionar Kit " + id)
                        .interact(event -> event.getPlayer().performCommand("kit" + (id > 1 ? "2" : ""))));
            }

            menu.setItem(4, Item.of(Material.STORAGE_MINECART, "§aKit Diário")
                    .flags(ItemFlag.values())
                    .enchantment(Enchantment.DURABILITY, 1)
                    .interact(event -> {
                        Player target = event.getPlayer();

                        Account account = Core.getAccountController().of(target.getUniqueId());

                        if (account.hasCooldown(DAILY_KIT_COOLDOWN)) {
                            Permission permission = account.getPermissions().stream()
                                    .filter(searched -> searched.getKey().toLowerCase().startsWith("arcade.pvp.kit") && !searched.isPermanent())
                                    .findFirst()
                                    .orElse(null);

                            if (permission != null) {
                                String[] parts = permission.getKey().split("\\.");

                                String keyName = parts[parts.length - 1];
                                String kitName = keyName.substring(0, 1).toUpperCase() + keyName.substring(1).toLowerCase();

                                account.send("§aO seu kit diário é o " + kitName + ".");
                                account.sound(Sound.NOTE_BASS);
                            } else
                                account.send("§cAguarde " + account.getFormattedCooldown(DAILY_KIT_COOLDOWN) + " para resgatar outro kit.");

                            return;
                        }

                        if (KitController.getKitsUserDontHave(target).isEmpty()) {
                            account.send("§cVocê já possui todos os kits!");
                            return;
                        }

                        new DailyKitMenu(target).handle();
                    }));

            menu.setItem(5, Item.of(Material.EMERALD, "§aLoja de Kits")
                    .interact(event -> new ShopMenu(event.getPlayer()).handle()));

            menu.setItem(8, Item.of(Material.ACACIA_DOOR_ITEM, "§cVoltar ao Lobby")
                    .interact(event -> event.getPlayer().performCommand("hub")));
        } else {

            BukkitUtil.sendRecraft(player, 20);

            int slot = 1;

            if (user.hasPrimary()) {
                Kit primary = user.getPrimary();

                for (ItemStack item : primary.getSpecialItems()) {
                    menu.setItem(slot, item);
                    slot++;
                }
            }

            if (user.hasSecondary()) {
                Kit secondary = user.getSecondary();

                for (ItemStack item : secondary.getSpecialItems()) {
                    menu.setItem(slot, item);
                    slot++;
                }
            }

            Item sword = Item.of(Material.STONE_SWORD, "§eEspada").unbreakable();

            if (user.isNotUsingKit())
                sword.name("§cEspada Afiada")
                        .enchantment(Enchantment.DAMAGE_ALL, 1);

            menu.setItem(0, sword);
            menu.setItem(8, Item.of(Material.COMPASS, "§aBússola"));
        }
    }

    @EventHandler
    public void onCancelProtection(PlayerDamageEvent event) {
        Player player = event.getPlayer();

        if (isValid(player)) {
            User user = (User) User.of(player.getUniqueId());

            com.minecraft.arcade.pvp.arcade.arena.Arena arena = user.getArena();

            Location spawn = arena.getLocation("spawn");

            if (user.isProtected() && player.getLocation().distance(spawn) > 20 && event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                event.setCancelled(true);

                user.setProtected(false);

                buildHotbar(player);

                player.sendMessage("§cVocê perdeu a sua proteção do spawn.");
            }
        }
    }

    @EventHandler
    public void onJumper(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (isValid(player)) {
            Block down = event.getTo().getBlock().getRelative(BlockFace.DOWN);

            Vector velocity = player.getLocation().getDirection();

            /* Bloco de Slime */
            if (down.getType().equals(Material.SLIME_BLOCK))
                handleLauncher(player, velocity.multiply(4).setY(1.5));

            if (down.getType().equals(Material.SPONGE))
                handleLauncher(player, velocity.setY(7));
        }
    }

    @EventHandler
    public void fallByLauncher(PlayerDamageEvent event) {
        User user = (User) User.of(event.getPlayer().getUniqueId());

        if (user != null && user.isLauncher() && event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            event.setCancelled(true);

            user.setLauncher(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCompass(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isValid(player)) {
            if (event.hasItem() && event.getAction().name().contains("RIGHT")) {
                ItemStack item = event.getItem();

                if (item.getType() == Material.COMPASS) {
                    boolean searched = false;

                    for (Entity entity : player.getNearbyEntities(150, 200, 150)) {
                        if (!(entity instanceof Player) || !isValid((Player) entity)) break;

                        User target = (User) User.of(entity.getUniqueId());

                        if (!target.isProtected()) {
                            player.setCompassTarget(entity.getLocation());
                            player.sendMessage("§aBussola apontando para " + entity.getName() + ".");

                            searched = true;
                            break;
                        }
                    }

                    if (!searched)
                        player.sendMessage("§cNenhum jogador foi encontrado.");

                    return;
                }
            }

            if (event.hasBlock()) {
                Block clicked = event.getClickedBlock();

                if (clicked.hasMetadata("feast"))
                    event.setCancelled(false);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        Player enchanter = event.getEnchanter();

        if (isValid(enchanter) && event.getInventory() instanceof EnchantingInventory) {
            EnchantingInventory inventory = (EnchantingInventory) event.getInventory();

            int[] expLevelCosts = event.getExpLevelCostsOffered();

            Arrays.fill(expLevelCosts, 1);
        }
    }

    @EventHandler
    public void enchantOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player && event.getInventory() instanceof EnchantingInventory) {
            Player player = (Player) event.getPlayer();

            EnchantingInventory inventory = (EnchantingInventory) event.getInventory();

            if (isValid(player))
                inventory.setItem(1, new Item(Material.INK_SACK, 3, 4));
        }
    }

    @EventHandler
    public void enchantClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player && event.getInventory() instanceof EnchantingInventory) {
            Player player = (Player) event.getWhoClicked();

            if (isValid(player) && event.getSlot() == 1)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void enchantClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player && event.getInventory() instanceof EnchantingInventory) {
            Player player = (Player) event.getPlayer();

            EnchantingInventory inventory = (EnchantingInventory) event.getInventory();

            if (isValid(player))
                inventory.removeItem(new Item(Material.INK_SACK, 3, 4));
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (isValid(player)) {
            ItemStack stack = event.getItemDrop().getItemStack();

            if (stack == null || stack.getType() == Material.AIR) return;

            ArenaUser user = (ArenaUser) ArenaUser.of(player.getUniqueId());

            if (user == null || user.isProtected()) {
                event.setCancelled(true);
                return;
            }

            Kit primary = user.getPrimary(), secondary = user.getSecondary();

            if (primary != null && primary.withSpecial(stack) || secondary != null && secondary.withSpecial(stack))
                event.setCancelled(true);
        }
    }

    private void replaceAllSigns(com.minecraft.arcade.pvp.arcade.arena.Arena arena) {
        org.bukkit.World world = arena.getWorld();
        if (world == null) return;

        Bukkit.getScheduler().runTask(BukkitCore.getInstance(), () -> {
            int count = 0;
            for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                if (chunk == null || !chunk.isLoaded()) continue;

                for (org.bukkit.block.BlockState state : chunk.getTileEntities()) {
                    if (state instanceof Sign) {
                        Sign sign = (Sign) state;
                        boolean changed = false;

                        for (int i = 0; i < 4; i++) {
                            String line = sign.getLine(i);
                            if (line != null && line.contains("LANDS")) {
                                sign.setLine(i, line.replace("LANDS", com.minecraft.core.Constant.SERVER_NAME.toUpperCase()));
                                changed = true;
                            }
                        }

                        if (changed) {
                            sign.update();
                            count++;
                        }
                    }
                }
            }
            if (count > 0) {
                Core.getLogger().info("[Arena] Replaced " + count + " signs in world " + world.getName());
            }
        });
    }

    private void removeSpecificSigns(com.minecraft.arcade.pvp.arcade.arena.Arena arena) {
        org.bukkit.World world = arena.getWorld();
        if (world == null) return;

        Bukkit.getScheduler().runTask(BukkitCore.getInstance(), () -> {
            int count = 0;
            for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                if (chunk == null || !chunk.isLoaded()) continue;

                for (org.bukkit.block.BlockState state : chunk.getTileEntities()) {
                    if (state instanceof Sign) {
                        Sign sign = (Sign) state;
                        
                        for (int i = 0; i < 4; i++) {
                            String line = sign.getLine(i);
                            if (line != null && line.toLowerCase().contains("lb")) {
                                sign.getBlock().setType(Material.AIR);
                                count++;
                                break;
                            }
                        }
                    }
                }
            }
            if (count > 0) {
                Core.getLogger().info("[Arena] Removed " + count + " 'lb' signs in world " + world.getName());
            }
        });
    }
}
