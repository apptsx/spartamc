package com.minecraft.arcade.pvp.arcade;

import com.grinderwolf.swm.plugin.SWMPlugin;
import com.minecraft.arcade.pvp.PvP;
import com.minecraft.arcade.pvp.arcade.arena.Arena;
import com.minecraft.arcade.pvp.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.ArcadeHolder;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.arcade.slime.SlimeWorldController;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.api.npc.type.server.NpcServer;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@IgnoreEvent
public abstract class Arcade extends ArcadeHolder {

    private final File mapsDirectory;

    public Arcade(String mapsDirectory, Integer minRooms, Integer maxRooms, ArcadeCategory category) {
        super(mapsDirectory, minRooms, maxRooms, category);

        this.mapsDirectory = new File(PvP.getInstance().getDataFolder().getAbsolutePath() + "/maps");
    }

    public abstract void handleSidebar(User user);

    @Override
    public boolean load() {
        Core.getLogger().info("Iniciando " + getName() + "...");

        if (!loadMaps(mapsDirectory)) return false;

        List<Map> availableMaps = getMaps();

        if (availableMaps.isEmpty()) {
            Core.getLogger().warning("Nenhum mapa disponível para " + getName() + ".");
            return false;
        }

        int numRooms = getMinRooms();
        int mapIndex = 0;

        for (int i = 0; i < numRooms; i++) {
            Map map = availableMaps.get(mapIndex);

            if (setupRoom(map, getCategory().getSlots().get(0), Type.CASUAL) == null) return false;

            mapIndex = (mapIndex + 1) % availableMaps.size();
        }

        Core.getLogger().info("Modo de jogo " + getName() + " iniciado com sucesso.");
        return true;
    }

    @Override
    public void unload() {
        Core.getLogger().info("Encerrando jogo " + getName() + "...");

        getRooms().clear();

        Core.getLogger().info("O jogo " + getName() + " foi desligado com sucesso!");
    }

    @Override
    public Arena setupRoom(Map map, Slot slot, Type type) {
        return setupRoom(map, slot);
    }

    @Override
    public Arena setupRoom(Map map, Slot slot) {
        Instant now = Instant.now();

        int id = (getId().getAndIncrement() + 1);

        Arena arena = new Arena(id, this, map, slot);

        Core.getLogger().info("[" + getName() + "/" + id + "] Criando arena...");

        String templateName = getName(), // Arena, FPS, MLG
                worldName = templateName + "-" + id;

        SlimeWorldController.cloneWorldFromTemplate(SWMPlugin.getInstance(), templateName, worldName, () -> {
            loadEntities(arena);

            arena.handleBorder();

            getRooms().add(arena);

            Core.getLogger().info("[" + getName() + "/" + id + "] Arena criada com sucesso. (Tempo médio: " + Util.formatInstant(now) + ")");
        });

        return arena;
    }

    @Override
    public Arena findBestArena(UUID sender, ArcadeRouteContext route) {
        return (Arena) getRooms().stream().findFirst().orElse(null);
    }

    public void handleDeath(User user, User killer) {

    }

    public void timer(Arena arena) {

    }

    public void join(User user) {
        handleSidebar(user);
        buildHotbar(user.getAccount().player());
    }

    public void loadEntities(Arena arena) {

    }

    public boolean isValid(Player player) {
        User user = (User) User.of(player.getUniqueId());

        return user != null && user.getArcade().isCategory(getCategory()) && user.getJoin().equals(Join.PLAYER);
    }

    public void buildHotbar(Player player) {
        player.getInventory().clear();
    }

    public void handleLauncher(Player player, Vector direction) {
        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        user.setLauncher(true);

        player.setVelocity(direction);
        player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1.0f, 1.0f);
    }

    public void buildNpcHub(Arena arena) {
        Location location = arena.getLocation("npc_hub");

        NpcServer hub = BukkitCore.getManager().getNpc().spawnServer(location,
                "ewogICJ0aW1lc3RhbXAiIDogMTYwNzk2NzUxMzk2OCwKICAicHJvZmlsZUlkIiA6ICI5ZDEzZjcyMTcxM2E0N2U0OTAwZTMyZGVkNjBjNDY3MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUYWxvZGFvIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzExMzA4NmQzZGE5YjM2MmM2YjVkMzUwMzBjYmZhODVkYzVhZTZhMzIzZmFmZjg0NmIwM2UwMjcyODM3ZThjOTIiCiAgICB9CiAgfQp9",
                "iLNUhkwBQoPF/SXKMc1H0xApIBt6zJLh9doS59OXHoyoeBu83eUYqLM8yYjx2mn2b1HwSKWP3Zzrf0VqBkaHAm52plnpaUWHD3028W7VHzCLRgePDJlv4fhHfBl3o3zQPDkplIWiLoeeK1ipxqdcEnJj3dawe0+W2iHXDQ1xXDJu83VzpzB1tuVcvcw8bmP5LmWh2DuF0SniGgAfuRFL+8OcYMUU68MzMuSgZJj/aN1Ihmfav5GZusTpDqJFxarqRwcFeQtng7Kpfd3WxGAP6nUmeqTOjRQ2MOP0OLRmVr3WfkfN9YvIepLTSv3QjgRU4rAnC88Hhu8gdLBH+9cf2LH6WcpNKTuW/e33T94ji6PMXiZbqHMNS7XQWDuTmaxBkc2Zgecv4rNc1/6jjb7vqjVH48jELlnMTJtOpAFaPxXmzAAReo4iTLD3Q6KzdRNY/4xzp9id5VqDfvkKbySZ2I5KrAlaAs0zU2sKf+lGWeh7CSrILmbgUZmh7t8oxwTiHxsxtCR2hktkKsK7KAZM2PhAOH23n2Ltf4Fnmp6m7rWdsYJKUx3aSOFUNTeV/qbIzkGfSBrrqBjEKNIOKAFuYBh+3Ozu2QuPD66ZaAWFx86mmiO43eDuPcpl+AmwAB00SmbW05XzADoSZqt1fzIuXQphPid/+f0GKO4Fuj6uq50=");

        hub.setAction((player, action) -> player.performCommand("hub"));
        hub.setHand(Item.of(Material.ACACIA_DOOR_ITEM));

        hub.setContact(true);

        hub.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer("arena_hub", location);

        hologram.setText(Arrays.asList(
                "§6§lVOLTAR AO LOBBY",
                "§eClique para voltar"
        ));
    }
}
