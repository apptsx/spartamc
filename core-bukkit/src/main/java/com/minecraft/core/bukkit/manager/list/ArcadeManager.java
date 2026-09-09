package com.minecraft.core.bukkit.manager.list;

import com.minecraft.core.Core;
import com.minecraft.core.arcade.ArcadeHolder;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.slime.SlimeWorldController;
import com.minecraft.core.util.list.loader.ClassLoader;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;

@Getter
public class ArcadeManager {

    private final Set<ArcadeHolder> arcades = new HashSet<>();

    private List<Class<?>> classList = new ArrayList<>();

    public void handle(JavaPlugin plugin, String source) {
        this.classList = ClassLoader.getClassesForPackage(plugin, source);

        /* Executando limpeza de lixos */
        SlimeWorldController.removeWorldsFromFileLoader();
        Core.getArcadeData().delete(Core.getServerType());

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("modes");

        if (section == null) {
            Core.getLogger().log(Level.SEVERE, "A seção 'modes' não foi encontrada no config.yml do plugin " + plugin.getName() + "!");
            return;
        }

        for (String name : section.getKeys(false)) {
            try {
                String directory = section.getString(name + ".directory");
                int minRooms = section.getInt(name + ".min"), maxRooms = section.getInt(name + ".max");

                ArcadeHolder arcade = (ArcadeHolder) read(name)
                        .getConstructor(String.class, Integer.class, Integer.class)
                        .newInstance(directory, minRooms, maxRooms);

                arcade.load();

                Core.getArcadeData().save(arcade);

                Bukkit.getPluginManager().registerEvents(arcade, plugin);

                arcades.add(arcade);
            } catch (Exception e) {
                Core.getLogger().log(Level.WARNING, "Não foi possível iniciar o jogo " + name, e);
            }
        }
    }

    public void unload() {
        SlimeWorldController.removeWorldsFromFileLoader();

        arcades.forEach(ArcadeHolder::unload);

        Core.getArcadeData().delete(Core.getServerType());

        arcades.clear();
    }

    public Class<?> read(String name) {
        return classList.stream().filter(arcade -> arcade.getSimpleName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public ArcadeHolder read(ArcadeCategory category) {
        return arcades.stream().filter(game -> game.isCategory(category)).findFirst().orElse(null);
    }

    public ArcadeHolder getGame(String name) {
        return arcades.stream().filter(game -> game.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Room getRandomArena(ArcadeCategory category) {
        ArcadeHolder game = read(category);

        if (game == null || game.getRooms().isEmpty()) return null;

        return game.getRooms().stream().findFirst().orElse(null);
    }

    public List<Room> getArenas() {
        List<Room> list = new ArrayList<>();

        arcades.forEach(game -> list.addAll(game.getRooms()));

        return list;
    }

    public List<Room> getArenas(ArcadeCategory category) {
        ArcadeHolder game = read(category);

        if (game == null || game.getRooms().isEmpty()) return Collections.emptyList();

        return new ArrayList<>(game.getRooms());
    }
}