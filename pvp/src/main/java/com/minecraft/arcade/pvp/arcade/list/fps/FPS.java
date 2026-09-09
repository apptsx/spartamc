package com.minecraft.arcade.pvp.arcade.list.fps;

import com.minecraft.arcade.pvp.arcade.Arcade;
import com.minecraft.arcade.pvp.arcade.arena.Arena;
import com.minecraft.arcade.pvp.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageEvent;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.member.list.pvp.PvPMember;
import com.minecraft.core.member.list.pvp.stats.list.ArenaStats;
import com.minecraft.core.util.list.DateUtil;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.concurrent.CompletableFuture;

import static com.minecraft.core.util.Util.formatNumber;

@IgnoreEvent
public class FPS extends Arcade {

    public FPS(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.PVP_FPS);
    }

    @Override
    public void join(User user) {
        super.join(user);

        if (user.isJoin(Join.PLAYER)) {
            CompletableFuture.runAsync(() -> replaceAllSigns(user.getArena()));
        }
    }

    @EventHandler
    public void onCancelProtection(PlayerDamageEvent event) {
        Player player = event.getPlayer();

        if (isValid(player)) {
            User user = (User) User.of(player.getUniqueId());

            if (user.isProtected() && event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                event.setCancelled(true);

                user.setProtected(false);

                buildHotbar(player);

                player.sendMessage("§cVocê perdeu a sua proteção do spawn.");
            }
        }
    }

    @Override
    public void buildHotbar(Player player) {
        super.buildHotbar(player);

        PlayerInventory menu = player.getInventory();

        User user = (User) User.of(player.getUniqueId());

        if (user.isProtected())
            menu.setItem(8, Item.of(Material.ACACIA_DOOR_ITEM, "§cVoltar ao Lobby")
                    .interact(event -> event.getPlayer().performCommand("hub")));
        else {

            BukkitUtil.sendRecraft(player);
            BukkitUtil.sendArmor(player, BukkitUtil.ArmorType.IRON);

            menu.setItem(0, Item.of(Material.DIAMOND_SWORD)
                    .enchantment(Enchantment.DAMAGE_ALL, 1)
                    .unbreakable());
        }
    }

    @Override
    public void handleSidebar(User user) {
        ArenaStats stats = user.getMember().getFpsStats();

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle("FPS");

        sidebar.addRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + user.getArena().getIdentifier());
        sidebar.blankRow();

        sidebar.addRow("kills", "Kills: §7" + formatNumber(stats.getKills()));
        sidebar.addRow("deaths", "Deaths: §7" + formatNumber(stats.getDeaths()));
        sidebar.blankRow();

        sidebar.addRow("streak", "Killstreak: §a" + formatNumber(stats.getKillStreak()));

        sidebar.blankRow();
        sidebar.addRow("coins", "Coins: §6" + formatNumber(user.getMember().getCoins()));

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(user.getAccount());
    }

    @Override
    public void loadEntities(Arena arena) {
        buildNpcHub(arena);
    }

    @Override
    public void handleDeath(User user, User killer) {
        Account account = user.getAccount();

        PvPMember member = user.getMember();
        ArenaStats stats = member.getFpsStats();

        Arena arena = user.getArena();

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

        /* Drop Itens do Inventário */
        for (ItemStack content : player.getInventory().getContents()) {
            if (content != null && BukkitUtil.isRecraft(content.getType()))
                player.getWorld().dropItemNaturally(player.getLocation(), content);
        }

        user.setCombat(null);
        user.getArena().spawn(player);

        if (hasKiller) {
            account = killer.getAccount();
            member = killer.getMember();

            stats = member.getFpsStats();

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

            BukkitUtil.sendArmor(account.player(), BukkitUtil.ArmorType.IRON);

            killer.setCombat(null);

            handleSidebar(killer);
        }
    }

    private void replaceAllSigns(Arena arena) {
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
                Core.getLogger().info("[FPS] Replaced " + count + " signs in world " + world.getName());
            }
        });
    }
}
