package com.minecraft.lobby.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.lobby.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LobbyProcessCommand implements CommandInheritor {

    @Command(name = "lobbyprocess", aliases = {"lprocess", "lp"}, rank = RankType.ADMIN, onlyPlayer = false)
    public void processLobby(BukkitCommandContext context) {
        String[] args = context.getArgs();
        CommandSender sender = context.getSender();

        World world = null;
        
        // Se o mundo for especificado, usar ele; senão usar o mundo padrão
        if (args.length > 0 && !args[0].isEmpty()) {
            world = Bukkit.getWorld(args[0]);
            if (world == null) {
                sender.send("§cMundo não encontrado: §e" + args[0]);
                sender.send("§7Mundos disponíveis:");
                for (World w : Bukkit.getWorlds()) {
                    sender.send("§7- §e" + w.getName());
                }
                return;
            }
        } else {
            // Se for jogador, usar o mundo dele; senão usar o mundo padrão
            world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
                if (world == null) {
                    sender.send("§cNenhum mundo disponível!");
                    return;
            }
        }

        try {
            if (processWorld(sender, world)) {
                sender.send("§aProcessamento concluído! Use §e/lobby reload §apara recarregar as configurações.");
            }
        } catch (Exception e) {
            sender.send("§cErro ao processar lobby: " + e.getMessage());
            Core.getLogger().warning("Erro ao processar lobby: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean processWorld(CommandSender sender, World world) {
        sender.send("§aEscaneando placas no mundo §e" + world.getName() + "§a...");

        List<MapLocation> foundLocations = scanWorld(world);

        if (foundLocations.isEmpty()) {
            sender.send("§cNenhuma placa válida encontrada no mundo §e" + world.getName());
            sender.send("§7Coloque placas com o nome da localização na primeira linha.");
            sender.send("§7Opcional: yaw na segunda linha, pitch na terceira linha.");
            return false;
        }

        sender.send("§aEncontradas §e" + foundLocations.size() + " §aplacas válidas");
        sender.send("§7Placas serão removidas automaticamente após o processamento.");

        // Obter arquivo config.json do lobby
        File configFile = new File(Lobby.getInstance().getDataFolder(), "config.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject config;
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                config = Core.PARSER.parse(reader).getAsJsonObject();
            } catch (Exception e) {
                sender.send("§cErro ao ler config.json: " + e.getMessage());
                config = new JsonObject();
                config.addProperty("server", "HUB");
            }
        } else {
            config = new JsonObject();
            config.addProperty("server", "HUB");
        }

        // Garantir que o campo "server" existe
        if (!config.has("server")) {
            config.addProperty("server", "HUB");
        }

        // Mesclar locations por nome (substitui existentes com o mesmo nome, mantém demais)
        Map<String, JsonObject> nameToLoc = new LinkedHashMap<>();
        if (config.has("locations") && config.get("locations").isJsonArray()) {
            for (JsonElement el : config.getAsJsonArray("locations")) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();
                String n = obj.has("name") ? obj.get("name").getAsString() : null;
                if (n != null) nameToLoc.put(n, obj);
            }
        }

        // Adicionar/atualizar localizações encontradas
        for (MapLocation mapLoc : foundLocations) {
            Location l = mapLoc.location;
            JsonObject loc = new JsonObject();
            loc.addProperty("name", mapLoc.name);
            loc.addProperty("world", l.getWorld().getName());
            loc.addProperty("x", l.getX());
            loc.addProperty("y", l.getY());
            loc.addProperty("z", l.getZ());
            if (l.getYaw() != 0 || l.getPitch() != 0) {
                loc.addProperty("yaw", l.getYaw());
                loc.addProperty("pitch", l.getPitch());
            }
            nameToLoc.put(mapLoc.name, loc);
        }

        // Atualizar array de locations
        JsonArray locations = new JsonArray();
        for (JsonObject obj : nameToLoc.values()) {
            locations.add(obj);
        }
        config.add("locations", locations);

        // Salvar arquivo
        try {
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(gson.toJson(config));
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar config.json: " + e.getMessage(), e);
        }

        sender.send("§a═══════════════════════════════════════");
        sender.send("§aLobby processado com sucesso!");
        sender.send("§a═══════════════════════════════════════");
        sender.send("§7Mundo: §e" + world.getName());
        sender.send("§7Arquivo: §e" + configFile.getAbsolutePath());
        sender.send("§7Localizações encontradas: §e" + foundLocations.size());
        sender.send("§7Total de localizações no config: §e" + nameToLoc.size());
        sender.send("§7Placas removidas automaticamente do mundo.");
        sender.send("§a═══════════════════════════════════════");

        return true;
    }

    private List<MapLocation> scanWorld(World world) {
        List<MapLocation> foundLocations = new ArrayList<>();

        int minY = 0;
        int maxY = 100;

        Location center = world.getSpawnLocation();
        int radius = 200;

        for (int x = center.getBlockX() - radius; x <= center.getBlockX() + radius; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = center.getBlockZ() - radius; z <= center.getBlockZ() + radius; z++) {
                    Block block = world.getBlockAt(x, y, z);

                    if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
                        try {
                            Sign sign = (Sign) block.getState();
                            String firstLine = sign.getLine(0).trim();

                            // Ignorar placas vazias
                            if (firstLine.isEmpty()) {
                                continue;
                            }

                            // Usar a primeira linha diretamente como nome (sem colchetes)
                            String locationName = firstLine;

                            Core.getLogger().info("[LobbyProcess] Placa encontrada em " + block.getLocation().getBlockX() + ", " +
                                    block.getLocation().getBlockY() + ", " + block.getLocation().getBlockZ() +
                                    " com nome: '" + locationName + "'");

                            // Base na posição da placa (centralizada no bloco)
                            Location loc = block.getLocation().clone();
                            loc.setX(loc.getBlockX() + 0.5);
                            loc.setZ(loc.getBlockZ() + 0.5);

                            // Direção do texto da placa
                            BlockFace face = getSignDirection(block);

                            // Ler yaw e pitch das linhas 2 e 3 (opcional)
                            float yaw = 0;
                            float pitch = 0;

                            try {
                                String yawStr = sign.getLine(1).trim();
                                if (!yawStr.isEmpty() && yawStr.matches("-?\\d+(\\.\\d+)?")) {
                                    yaw = Float.parseFloat(yawStr);
                                }
                                
                                String pitchStr = sign.getLine(2).trim();
                                if (!pitchStr.isEmpty() && pitchStr.matches("-?\\d+(\\.\\d+)?")) {
                                    pitch = Float.parseFloat(pitchStr);
                                }
                            } catch (Exception e) {
                                // ignore
                            }

                            // Se yaw não foi especificado, usar a direção da placa
                            if (yaw == 0) {
                                if (face != null) {
                                    yaw = getYawFromBlockFace(face);
                                }
                            }

                            loc.setYaw(yaw);
                            loc.setPitch(pitch);

                            foundLocations.add(new MapLocation(locationName, loc));
                            Core.getLogger().info("[LobbyProcess] Localização adicionada: " + locationName + 
                                    " (X: " + loc.getX() + ", Y: " + loc.getY() + ", Z: " + loc.getZ() +
                                    ", Yaw: " + yaw + ", Pitch: " + pitch + ")");

                            // Remover imediatamente a placa do mundo
                            try {
                                block.setType(Material.AIR);
                            } catch (Exception ignore) {
                            }
                        } catch (Exception e) {
                            Core.getLogger().warning("[LobbyProcess] Erro ao processar placa: " + e.getMessage());
                        }
                    }
                }
            }
        }
        Core.getLogger().info("[LobbyProcess] Lidas " + foundLocations.size() + " localizações do mundo " + world.getName());

        return foundLocations;
    }

    private static BlockFace getSignDirection(Block block) {
        try {
            Material type = block.getType();
            if (type == Material.WALL_SIGN) {
                org.bukkit.material.Sign signMaterial = (org.bukkit.material.Sign) block.getState().getData();
                BlockFace facing = signMaterial.getFacing();
                return getOppositeFace(facing);
            } else if (type == Material.SIGN_POST) {
                byte data = block.getData();
                int rotation = data & 0xF;
                switch (rotation) {
                    case 0:
                        return BlockFace.SOUTH;
                    case 1:
                        return BlockFace.SOUTH_SOUTH_WEST;
                    case 2:
                        return BlockFace.SOUTH_WEST;
                    case 3:
                        return BlockFace.WEST_SOUTH_WEST;
                    case 4:
                        return BlockFace.WEST;
                    case 5:
                        return BlockFace.WEST_NORTH_WEST;
                    case 6:
                        return BlockFace.NORTH_WEST;
                    case 7:
                        return BlockFace.NORTH_NORTH_WEST;
                    case 8:
                        return BlockFace.NORTH;
                    case 9:
                        return BlockFace.NORTH_NORTH_EAST;
                    case 10:
                        return BlockFace.NORTH_EAST;
                    case 11:
                        return BlockFace.EAST_NORTH_EAST;
                    case 12:
                        return BlockFace.EAST;
                    case 13:
                        return BlockFace.EAST_SOUTH_EAST;
                    case 14:
                        return BlockFace.SOUTH_EAST;
                    case 15:
                        return BlockFace.SOUTH_SOUTH_EAST;
                    default:
                        return BlockFace.SOUTH;
                }
            }
        } catch (Exception e) {
        }
        return BlockFace.NORTH;
    }

    private static BlockFace getOppositeFace(BlockFace face) {
        switch (face) {
            case NORTH:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.NORTH;
            case EAST:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.EAST;
            case NORTH_EAST:
                return BlockFace.SOUTH_WEST;
            case NORTH_WEST:
                return BlockFace.SOUTH_EAST;
            case SOUTH_EAST:
                return BlockFace.NORTH_WEST;
            case SOUTH_WEST:
                return BlockFace.NORTH_EAST;
            default:
                return BlockFace.SOUTH;
        }
    }

    private static float getYawFromBlockFace(BlockFace face) {
        switch (face) {
            case NORTH:
                return 180.0f;
            case SOUTH:
                return 0.0f;
            case EAST:
                return -90.0f;
            case WEST:
                return 90.0f;
            case NORTH_EAST:
                return -135.0f;
            case NORTH_WEST:
                return 135.0f;
            case SOUTH_EAST:
                return -45.0f;
            case SOUTH_WEST:
                return 45.0f;
            // Direções intermediárias
            case NORTH_NORTH_EAST:
                return -157.5f;
            case NORTH_NORTH_WEST:
                return 157.5f;
            case SOUTH_SOUTH_EAST:
                return -22.5f;
            case SOUTH_SOUTH_WEST:
                return 22.5f;
            case EAST_NORTH_EAST:
                return -112.5f;
            case EAST_SOUTH_EAST:
                return -67.5f;
            case WEST_NORTH_WEST:
                return 112.5f;
            case WEST_SOUTH_WEST:
                return 67.5f;
            default:
                return 0.0f;
        }
    }

    private static class MapLocation {
        final String name;
        final Location location;

        MapLocation(String name, Location location) {
            this.name = name;
            this.location = location;
        }
    }
}

