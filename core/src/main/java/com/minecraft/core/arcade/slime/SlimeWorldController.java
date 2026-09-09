package com.minecraft.core.arcade.slime;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.util.list.FileUtil;
import com.minecraft.core.util.list.Validator;
import com.minecraft.core.util.list.WorldUtil;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.plugin.SWMPlugin;
import lombok.SneakyThrows;
import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class SlimeWorldController {

    private static final Map<String, SlimeWorld> templateCache = new ConcurrentHashMap<>();
    private static final boolean USE_FALLBACK = true;

    public static void cloneWorldFromTemplate(SWMPlugin plugin, String templateWorld, String worldName) {
        cloneWorldFromTemplate(plugin, templateWorld, worldName, () -> {
        });
    }

    public static void cloneWorldFromTemplate(SWMPlugin plugin, String template, String name, SlimeAction afterGenerate) {
        SlimeLoader loader = plugin.getLoader(Constant.SLIME_FILE_LOADER);

        if (loader == null) {
            plugin.getLogger().info("Loader \"" + Constant.SLIME_FILE_LOADER + "\" não encontrado.");
            return;
        }

        if (template.equalsIgnoreCase(name)) {
            plugin.getLogger().info("Os mundos não podem ter os nomes iguais. (" + template + "/" + name + ")");
            return;
        }

        plugin.getLogger().info("Gerando mundo \"" + name + "\" de \"" + template + "\"...");

        generateWorld(plugin, loader, template, name, afterGenerate, 3);
    }

    private static void generateWorld(SWMPlugin plugin, SlimeLoader loader, String template, String name, SlimeAction afterGenerate, int attempts) {
        if (attempts <= 0) {
            if (USE_FALLBACK) {
                plugin.getLogger().info("Usando fallback - gerando mundo normal para \"" + name + "\"...");
                generateFallbackWorld(plugin, template, name, afterGenerate);
            } else {
                plugin.getLogger().info("Não foi possível gerar o mundo \"" + name + "\" após " + attempts + " tentativas.");
            }
            return;
        }

        try {
            Instant start = Instant.now();

            SlimeWorld templateWorld = templateCache.get(template);

            if (templateWorld == null) {
                SlimePropertyMap properties = new SlimePropertyMap();

                properties.setString(SlimeProperties.WORLD_TYPE, "flat");
                properties.setString(SlimeProperties.DIFFICULTY, "normal");

                properties.setBoolean(SlimeProperties.ALLOW_ANIMALS, false);
                properties.setBoolean(SlimeProperties.ALLOW_MONSTERS, false);

                templateWorld = plugin.loadWorld(loader, template, true, properties);

                if (templateWorld == null) {
                    plugin.getLogger().info("O template \"" + template + "\" não foi encontrado.");
                    if (USE_FALLBACK) {
                        generateFallbackWorld(plugin, template, name, afterGenerate);
                    }
                    return;
                }

                templateCache.put(template, templateWorld);
            }

            SlimeWorld slimeWorld = templateWorld.clone(name, loader);

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    plugin.generateWorld(slimeWorld);

                    World bukkitWorld = Bukkit.getWorld(slimeWorld.getName());

                    if (bukkitWorld != null) {
                        if (bukkitWorld.getBlockAt(0, 0, 0) != null) {
                            WorldUtil.setup(bukkitWorld);

                            afterGenerate.run();

                            plugin.getLogger().info("Mundo \"" + name + "\" carregado e gerado em "
                                    + Duration.between(start, Instant.now()).toMillis() + "ms.");
                        } else {
                            plugin.getLogger().info("Não foi possível executar a ação do mundo " + name);
                            generateWorld(plugin, loader, template, name, afterGenerate, attempts - 1);
                        }
                    } else {
                        plugin.getLogger().info("Não foi possível executar a ação do mundo " + name);
                        generateWorld(plugin, loader, template, name, afterGenerate, attempts - 1);
                    }

                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Não foi possível gerar o mundo \"" + name + "\" (1x)", e);
                    if (USE_FALLBACK) {
                        generateFallbackWorld(plugin, template, name, afterGenerate);
                    } else {
                        generateWorld(plugin, loader, template, name, afterGenerate, attempts - 1);
                    }
                }
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Não foi possível gerar o mundo \"" + name + "\" (2x)", e);
            if (USE_FALLBACK) {
                generateFallbackWorld(plugin, template, name, afterGenerate);
            } else {
                generateWorld(plugin, loader, template, name, afterGenerate, attempts - 1);
            }
        }
    }

    private static void generateFallbackWorld(SWMPlugin plugin, String template, String name, SlimeAction afterGenerate) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                WorldCreator creator = new WorldCreator(name);
                creator.environment(World.Environment.NORMAL);
                creator.type(WorldType.FLAT);
                creator.generatorSettings("2;0;1;");
                
                World world = creator.createWorld();
                
                if (world != null) {
                    WorldUtil.setup(world);
                    afterGenerate.run();
                    plugin.getLogger().info("Mundo fallback \"" + name + "\" gerado com sucesso!");
                } else {
                    plugin.getLogger().warning("Não foi possível criar o mundo fallback \"" + name + "\"");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Erro ao gerar mundo fallback \"" + name + "\"", e);
            }
        });
    }

    @SneakyThrows
    public static void removeWorldsFromLoader(String loaderName) {
        SWMPlugin plugin = SWMPlugin.getInstance();
        if (plugin == null) {
            Core.getLogger().warning("SWMPlugin instance is null");
            return;
        }
        
        SlimeLoader loader = plugin.getLoader(loaderName);

        if (loader == null) {
            Core.getLogger().info("Loader não encontrado!");
            return;
        }

        try {
            loader.listWorlds().forEach(world -> {
                try {
                    loader.deleteWorld(world);
                } catch (Exception e) {
                    Core.getLogger().log(Level.WARNING, "Não foi possível deletar o mundo \"" + world + "\".", e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public static void removeWorldsFromFileLoader() {
        SWMPlugin plugin = SWMPlugin.getInstance();
        if (plugin == null) {
            Core.getLogger().warning("SWMPlugin instance is null");
            return;
        }
        
        SlimeLoader loader = plugin.getLoader(Constant.SLIME_FILE_LOADER);

        if (loader == null) {
            Core.getLogger().info("Loader não encontrado!");
            return;
        }

        try {
            for (String worldName : loader.listWorlds()) {
                if (!Validator.hasNumberInString(worldName)) continue;

                World world = Bukkit.getWorld(worldName);

                if (world != null) {
                    Bukkit.unloadWorld(world, false);
                    FileUtil.delete(world.getWorldFolder());
                }

                loader.deleteWorld(worldName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unloadWorld(World world) {
        unloadWorld(world, () -> {
        });
    }

    public static void unloadWorld(World world, SlimeAction afterUnloadWorld) {
        SWMPlugin plugin = SWMPlugin.getInstance();
        if (plugin == null) {
            Core.getLogger().warning("SWMPlugin instance is null");
            return;
        }
        
        SlimeLoader loader = plugin.getLoader(Constant.SLIME_FILE_LOADER);

        if (loader == null) {
            Core.getLogger().log(Level.WARNING, "Não foi possível encontrar o loader \"Hikari\".");
            return;
        }

        if (world == null) return;

        String worldName = world.getName();
        File file = world.getWorldFolder();

        try {
            Bukkit.unloadWorld(worldName, false);
            FileUtil.delete(file);

            loader.deleteWorld(worldName);

        } catch (Exception e) {
            Core.getLogger().log(Level.WARNING, "Ocorreu um erro ao encerrar o mundo \"" + (worldName) + "\"...", e);
        }

        if (Bukkit.getWorld(worldName) == null) {
            Core.getLogger().info("[SlimeWorld] Mundo \"" + worldName + "\" deletado! Executando ação...");

            afterUnloadWorld.run();
        } else {
            Core.getLogger().info("[SlimeWorld] Não foi possível deletar o mundo \"" + worldName + "\".");
        }
    }

    public static void loadSlimeWorld(SWMPlugin plugin, String worldName, SlimeAction afterLoad) {
        loadSlimeWorld(plugin, worldName, afterLoad, 3);
    }

    private static void loadSlimeWorld(SWMPlugin plugin, String worldName, SlimeAction afterLoad, int attempts) {
        if (attempts <= 0) {
            if (USE_FALLBACK) {
                plugin.getLogger().info("Usando fallback para carregar mundo \"" + worldName + "\"...");
                loadFallbackWorld(plugin, worldName, afterLoad);
            } else {
                plugin.getLogger().warning("Não foi possível carregar o mundo \"" + worldName + "\" após " + attempts + " tentativas.");
            }
            return;
        }

        try {
            SlimeLoader loader = plugin.getLoader(Constant.SLIME_FILE_LOADER);

            if (loader == null) {
                plugin.getLogger().warning("Loader \"" + Constant.SLIME_FILE_LOADER + "\" não encontrado.");
                if (USE_FALLBACK) {
                    loadFallbackWorld(plugin, worldName, afterLoad);
                }
                return;
            }

            Instant start = Instant.now();

            SlimePropertyMap properties = new SlimePropertyMap();
            properties.setString(SlimeProperties.DIFFICULTY, "normal");
            properties.setBoolean(SlimeProperties.ALLOW_ANIMALS, false);
            properties.setBoolean(SlimeProperties.ALLOW_MONSTERS, false);

            SlimeWorld slimeWorld = plugin.loadWorld(loader, worldName, true, properties);

            if (slimeWorld == null) {
                plugin.getLogger().warning("Não foi possível carregar o mundo Slime \"" + worldName + "\".");
                if (USE_FALLBACK) {
                    loadFallbackWorld(plugin, worldName, afterLoad);
                } else {
                    loadSlimeWorld(plugin, worldName, afterLoad, attempts - 1);
                }
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    plugin.generateWorld(slimeWorld);

                    World bukkitWorld = Bukkit.getWorld(slimeWorld.getName());

                    if (bukkitWorld != null) {
                        WorldUtil.setup(bukkitWorld);
                        
                        afterLoad.run();

                        plugin.getLogger().info("Mundo Slime \"" + worldName + "\" carregado em "
                                + Duration.between(start, Instant.now()).toMillis() + "ms.");
                    } else {
                        plugin.getLogger().warning("Mundo \"" + worldName + "\" não foi gerado corretamente.");
                        loadSlimeWorld(plugin, worldName, afterLoad, attempts - 1);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Erro ao gerar mundo \"" + worldName + "\"", e);
                    loadSlimeWorld(plugin, worldName, afterLoad, attempts - 1);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao carregar mundo Slime \"" + worldName + "\"", e);
            if (USE_FALLBACK) {
                loadFallbackWorld(plugin, worldName, afterLoad);
            } else {
                loadSlimeWorld(plugin, worldName, afterLoad, attempts - 1);
            }
        }
    }

    private static void loadFallbackWorld(SWMPlugin plugin, String worldName, SlimeAction afterLoad) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                WorldCreator creator = new WorldCreator(worldName);
                creator.environment(World.Environment.NORMAL);
                creator.type(WorldType.FLAT);
                creator.generatorSettings("2;0;1;");
                
                World world = creator.createWorld();
                
                if (world != null) {
                    WorldUtil.setup(world);
                    afterLoad.run();
                    plugin.getLogger().info("Mundo fallback \"" + worldName + "\" carregado com sucesso!");
                } else {
                    plugin.getLogger().warning("Não foi possível criar o mundo fallback \"" + worldName + "\"");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Erro ao gerar mundo fallback \"" + worldName + "\"", e);
            }
        });
    }

    public interface SlimeAction {
        void run();
    }
}
