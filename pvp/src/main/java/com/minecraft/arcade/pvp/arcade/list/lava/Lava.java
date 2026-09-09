package com.minecraft.arcade.pvp.arcade.list.lava;

import com.minecraft.arcade.pvp.arcade.Arcade;
import com.minecraft.arcade.pvp.arcade.arena.Arena;
import com.minecraft.arcade.pvp.arcade.list.lava.leaderboard.LavaLevelLeaderboard;
import com.minecraft.arcade.pvp.user.User;
import com.minecraft.arcade.pvp.user.factory.list.LavaUser;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.api.npc.type.server.NpcServer;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.member.list.pvp.stats.list.LavaStats;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.DateUtil;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@IgnoreEvent
public class Lava extends Arcade {

    private final Set<Material> ALLOWED_DROPS = Stream.of(
            Material.BOWL, Material.MUSHROOM_SOUP, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM
    ).collect(Collectors.toSet());

    private final Set<UUID> inLava = ConcurrentHashMap.newKeySet();

    public Lava(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.PVP_LAVA);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        User user = (User) User.of(player.getUniqueId());

        if (user == null || user.getArcade() == null) return;
        if (!user.getArcade().getCategory().equals(ArcadeCategory.PVP_LAVA)) return;

        boolean isInLava = isPlayerInLava(player);

        if (isInLava && !inLava.contains(player.getUniqueId())) {
            inLava.add(player.getUniqueId());
            player.setFireTicks(Integer.MAX_VALUE);
            startLavaDamage(player);
        } else if (!isInLava && inLava.contains(player.getUniqueId())) {
            inLava.remove(player.getUniqueId());
            player.setFireTicks(0);
        }
    }

    private boolean isPlayerInLava(Player player) {
        Location loc = player.getLocation();
        Block blockFeet = loc.getBlock();
        Block blockHead = loc.clone().add(0, 1, 0).getBlock();
        Block blockBelow = loc.clone().subtract(0, 1, 0).getBlock();

        return (blockFeet.getType().name().contains("LAVA") && blockFeet.isLiquid()) ||
                (blockHead.getType().name().contains("LAVA") && blockHead.isLiquid()) ||
                (blockBelow.getType().name().contains("LAVA") && blockBelow.isLiquid());
    }

    private void startLavaDamage(Player player) {
        Bukkit.getScheduler().runTaskTimer(BukkitCore.getInstance(), () -> {
            if (!inLava.contains(player.getUniqueId())) {
                return;
            }

            if (!player.isOnline() || player.isDead()) {
                inLava.remove(player.getUniqueId());
                return;
            }

            if (!isPlayerInLava(player)) {
                inLava.remove(player.getUniqueId());
                player.setFireTicks(0);
                return;
            }

            User user = (User) User.of(player.getUniqueId());
            if (user == null) {
                inLava.remove(player.getUniqueId());
                return;
            }

            double currentHealth = player.getHealth();
            double damage = 4.0;

            if (currentHealth <= damage) {
                inLava.remove(player.getUniqueId());
                player.setFireTicks(0);
                player.setHealth(player.getMaxHealth());
                player.setNoDamageTicks(40);
                handleDeath(user, null);
            } else {
                player.damage(damage);
            }
        }, 0L, 10L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        ItemStack stack = event.getItemDrop().getItemStack();

        if (isValid(player) && stack != null)
            event.setCancelled(!ALLOWED_DROPS.contains(stack.getType()));
    }

    @Override
    public void join(User user) {
        super.join(user);

        CompletableFuture.runAsync(() -> new LavaLevelLeaderboard(user.getAccount().player(), user.getArena()).handle());
        replaceAllSigns(user.getArena());
    }

    @Override
    public void buildHotbar(Player player) {
        super.buildHotbar(player);

        BukkitUtil.sendRecraft(player);
    }

    @Override
    public void handleSidebar(User user) {
        LavaStats stats = user.getMember().getLavaStats();

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle("LAVA");

        sidebar.addRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + user.getArena().getIdentifier());
        sidebar.blankRow();

        sidebar.addRow("easy", "Dif. Fácil: §a" + Util.formatNumber(stats.getLevel(LavaStats.LavaLevel.EASY)));
        sidebar.addRow("medium", "Dif. Médio: §e" + Util.formatNumber(stats.getLevel(LavaStats.LavaLevel.MEDIUM)));
        sidebar.addRow("hard", "Dif. Difícil: §c" + Util.formatNumber(stats.getLevel(LavaStats.LavaLevel.HARD)));
        sidebar.addRow("extreme", "Dif. Extremo: §4" + Util.formatNumber(stats.getLevel(LavaStats.LavaLevel.EXTREME)));

        sidebar.blankRow();
        sidebar.addRow("coins", "Coins: §6" + Util.formatNumber(user.getMember().getCoins()));

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(user.getAccount());
    }

    @Override
    public void handleDeath(User user, User killer) {
        Player player = user.getAccount().player();
        Arena arena = user.getArena();

        inLava.remove(player.getUniqueId());

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        for (org.bukkit.entity.Item drop : player.getWorld().getEntitiesByClass(org.bukkit.entity.Item.class)) {
            if (drop != null && drop.getLocation().distance(player.getLocation()) < 5) {
                drop.remove();
            }
        }

        player.setFireTicks(0);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(5f);
        player.setExhaustion(0f);
        player.setFallDistance(0);
        player.setNoDamageTicks(40);

        if (player.isDead()) {
            player.spigot().respawn();
        }

        arena.spawn(player);

        BukkitUtil.sendRecraft(player);

        player.sendMessage("§cVocê morreu.");
    }

    @Override
    public void loadEntities(Arena arena) {
        List<Location> list = arena.getLocations("difficulty_easy", "difficulty_medium", "difficulty_hard", "difficulty_extreme");

        buildNpcHub(arena);

        handleDifficultyNpc(LavaStats.LavaLevel.EASY, list.get(0),
                "ewogICJ0aW1lc3RhbXAiIDogMTY3NzY0NTE5ODkxMywKICAicHJvZmlsZUlkIiA6ICI0NDAzZGM1NDc1YmM0YjE1YTU0OGNmZGE2YjBlYjdkOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJHYXl0b3duIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk1ZjAzZjUyYTc4ZjU0ZmI3Nzg5YmFkMWM5MzNmZjAwZTk2ZmQ5ZDBlZDY0MTdkN2U3NjkwZWVlNzAxMjIzNjYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                "Ya83JOegQ3uZhzEzcIo+7b12E8z/hlAaIB7axczPJOnpudOKLaSKgl3z5+/5XXGzn3fXCSw+DExQbLy+DizmM/mexQ+wL60i+rfVphvQ1421Apc0jcK15umYm65mlwhScKQuuceKtrs8Y+qq9UcmVjzIa8E93qEM6lOcSrdvDoWU3Pjrod7+R2EAggFS7S1LsdvhRxYru8N0Xh7tACD/TZ2kxyAluKKxnNsRLBFkxV9mdAvG7vwt2teDOoGrwYZ5DNDX+taC7wco+lUQpfwElYf/vrtRV9/axK9pc2hRbVuD7iL3ahsR343YUZysDcMTMuViBQxEVPXBYMgLODHblRrKBN+3sG2TFcoggQdYwcKwpEPKGheB4AmcX2EHXawmHyFxXJUxLSjNmvtbCKODx89Zjk70a3IhHQSOI/fARg8ggco3KdVVmo22XWXVn1MWeMdj6q+P0xR82VCzOipppbny7Hfahhq7hoCU6wjE4OZfjrq9CLA9WwJqbGaD5tz1N6Q1uFZbjJ3H4lIUne5A3CpK4vlJLg5X/ttpBz2K7pxByyKKZ1cshV6aQPEyrQuxRqV1JDrbcBdZV1og91oqa54r82VS3d2zk86YxJrRNaEFlc/Scz9lbE+y2rE3jCLjsdJtZj9tEaZUh0tXibUifNAwU8wyfFk9Ku1fhGhUoCc=");

        handleDifficultyNpc(LavaStats.LavaLevel.MEDIUM, list.get(1),
                "ewogICJ0aW1lc3RhbXAiIDogMTcyOTM2OTQ4NzUzMiwKICAicHJvZmlsZUlkIiA6ICI3OGFjMTRjOWQzZGU0Y2Q5YThiMzNhYzZlOGE4YzQyMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbmdlbGFQbGF5c3oiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDQxMTllYTI2NTRhYjkyZjM1MTQ3ODQ0NzE4YTc4ODkwMmExOTJlYTkyNDNmNmU5YmVkNmY2ZTY3YjQwMjhmZiIKICAgIH0KICB9Cn0=",
                "jaAT2fj+hVF+zv3zILe6ROWPTSD8pc3Orlu1q7ynD2rORm2cAaFKm9q3K3Ujn1r+jGn1K/QfEQX+uKRTDNn7Ua5f7beacBi6hfHLB11jj666V4N6n/y9mbuk6QggtGnYq3DH3Ysrf6G2gm3+iRr/rzmFhAqKvl7P8d5wHONmF49VVhGAXdrEZoFsX9hGxdYV7svaQ9fPDbRMckYWNvh3p8IEbXfbK3AxOFpa+1QhDsAiz3wW3PXY/B/NKRv8jcZyKNXYZ4LDS/hlQMUlSoIbK4BqgehJ4SimzdMvv18Wpod3MogmG29e3hRj+HKTzr20shqh99mpaau2Y+hsS1pOmJcoIIUyoBK0dHVp0nTdDga9aGWZDRD0HdB434+88UizHx7J/9M/KO+6PAGLlULvFUufvfA1kO+f0AN6lfGpEdw1fFPc7Q3NADZdDkxFhtUfnnxjmRyKEc2m9N4Kct/v5dJ5jVc2211xFJ6wvDxSRRqOzApFwl9OqFzkwsOlAsUvBxzyFkZ2I9sL3Z4hUzOnJNe6rp8XC9Xp0XcX+rM5iufjR33PSl91W4Ug+r+NLSllThfOEkRtaQ5I03k5M/TcIQ0OPjQLCmgw3Hsp/dEtKD18siC1MdppUpU+TH0lhJQtO4TG6BrFhrGwG/5B/gUcbVmT0p9XpN5sEX37g057OJw=");

        handleDifficultyNpc(LavaStats.LavaLevel.HARD, list.get(2),
                "ewogICJ0aW1lc3RhbXAiIDogMTcyOTM2OTQ2MzQ4NCwKICAicHJvZmlsZUlkIiA6ICJlZDUzZGQ4MTRmOWQ0YTNjYjRlYjY1MWRjYmE3N2U2NiIsCiAgInByb2ZpbGVOYW1lIiA6ICI0MTQxNDE0MWgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzFlZDc3YWRkYmYwNmU4MmQwZjhkMzg0ZjRhZTIwMGZiZDM0MTZlNDQ0Y2ViZTc3MjJmOWRkNjIxMmFmYjg2OCIKICAgIH0KICB9Cn0=",
                "GnQPF51s2feRTa8kdL6pvu5wwpfOhc22AdPBUFR30aTQFX83Vcyk3/9xoPR7oAwoMUrOzXvpJf9wxtTrWEe0Ux+4wBjkmQ5v5badEtt1EKjpDKTeRuse1ZS084SndFeLcUVDVdSWA38C3SR6yF/7VGRHdIsgHmdpJIEcLkAf6Ycur0SIZPiL8BlFDZbCr1ODWRB9f83MHI7Yjy/a1abvmDAD7bMNXiTwN3P6t+eMP+oAhQDN0w08A4my8/OZvlbA52acdBzoq/x0HAY91izOSdhSivcHlR0RxdxAPVx4h6YdoT7XHuRC3SPo+bPERRfM0AlvLPTxMvDie1n9EK+0lfHosa3wfwdk1dOuYRZESf6EeeX9wgZtsHivKJqS+0l+kUT7ubTUr30vhYWyH7h53/BVPQ2YeHJtjaorM20HlfHooI9A/3kka59QuZdB0IIZ1EABUm/AzDxzxPfPONh/nm7nDO3s2+eYyOE/7yxzW5vDj1IMizwgVOQEoUyLViQLyzUBijWb469VzRFFIK4MIPw8spBEEufNCQBaYL6iDCNtScXDsTie7TqZsu2walaIikK1WH+lIUaPNL6nbE8T/U9WOtRR+hMZgcL4bR+2pupyAz39YwrkNCRoUcDjz/08tlo4YeUFc0/MMirMW+3yXFn+aXYiC138qNBqPh4ds2c=");

        handleDifficultyNpc(LavaStats.LavaLevel.EXTREME, list.get(3),
                "ewogICJ0aW1lc3RhbXAiIDogMTcyOTM2OTQyNjYxNiwKICAicHJvZmlsZUlkIiA6ICJmYmFkNTg0ZmEyYTA0MjZhODZmMzgyMGFhZGEwOWVkZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJTb2xpZFRvYXN0ZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kMDNhZTdhOWU0YTdmZDhjMzk2NGQxNjUyMmNlYzUzNTQ0OWU4NWUxZGE1MjFhNDcwZTczYTNhYWY4NGZmYjFlIgogICAgfQogIH0KfQ==",
                "HQt9vg/yN18DwH404a52MBCXgD39ZhR1I5oj5h673a7Tebg1g8l6zagqUR6o2WgaT9wSLrgmKIwMT2EpZiWx/tMlB2RiXGrkUEPpFDE56oc4MjsM4/ocK1emQ7h6c1BpgDVZfXAZWT7vmFwP5WD3RixU/rpf7s/bNJpYAiskcFL5/yUqHNcFrSSXuvbJ51UzvOzMqnC/uHoag3d3nh3wrbjKQsC6wz5Y1SZHQdcN6nRDPidzygShBFcdQ0NQ7Ow8nVYnYoUQqSu3K9hRT+LQhP879xmiAJZDc13riTzR0PjkXjQ2SNYyn8EPs3ukS+Wmd19gEOdQIPSNAPx9s65mJ22F4f6xIkJ6XvWK7AOGarr8iHxFnWutlNpGmyCABsd8nBWxaLBwyQD44pnMqJt+JAwEGm+gY5+Xm1QlOzOt21q2JxUBxYtW0dSNJPCtuOjsfb0xg+zQlnSdMks5b/8X3/UHrt6AwMwyz52cHmEvVud/v7zgdRSu/KH2btj+w4eHl5WJoPgUeLyUjOTcopBPhiki4YeBABIE0WWav5Do1sy50HmyJfDCCudUa7a5GMcOdlrC5Qr4F1TJgsOdwhHMIAWOkjLyZbQqbXpT1mUmsuwNXyBkvIpm7Y0vS1zCDksg/jGdO2bEYHiuN6MzGzohao8f8+WMbdIzG6/0XhIqLQ=");

    }

    protected void handleDifficultyNpc(LavaStats.LavaLevel level, Location location, String value, String signature) {
        NpcServer server = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        server.setAction((player, a) -> {
            User userObj = (User) User.of(player.getUniqueId());

            if (userObj != null && userObj.getArcade().isCategory(ArcadeCategory.PVP_LAVA)) {
                LavaUser user = (LavaUser) userObj;

                user.conclude(level);

                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
            }
        });

        server.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(level.name().toLowerCase(), location);

        hologram.setText(Arrays.asList(
                level.getColor() + "§lNÍVEL " + level.getName().toUpperCase(),
                "§eClique para concluir"
        ));
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
                Core.getLogger().info("[Lava] Replaced " + count + " signs in world " + world.getName());
            }
        });
    }
}