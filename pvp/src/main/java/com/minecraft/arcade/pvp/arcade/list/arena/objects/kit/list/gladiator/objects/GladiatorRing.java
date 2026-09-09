package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.list.gladiator.objects;

import com.minecraft.arcade.pvp.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.category.ArcadeCategory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

@Getter
@Setter
@RequiredArgsConstructor
public class GladiatorRing {

    @Getter
    private final static Set<GladiatorRing> rings = new HashSet<>();

    private final Player player, target;

    private final Location back, spawn;

    private final int radius = 8, height = 10, defaultHeight = 100;

    private final List<Block> rollBackBlocks = new ArrayList<>();

    private Location playerSpawn, targetSpawn;

    private boolean spawned = false;

    private int time = 0;

    public static GladiatorRing of(Player player) {
        return rings.stream()
                .filter(ring -> ring.getPlayer().getUniqueId().equals(player.getUniqueId()) || ring.getTarget().getUniqueId().equals(player.getUniqueId()))
                .findFirst()
                .orElse(null);
    }

    public static boolean inRing(Player player) {
        return of(player) != null;
    }

    public static GladiatorRing of(Player player, Player target) {
        return rings.stream()
                .filter(ring -> ring.getPlayer().getUniqueId().equals(player.getUniqueId()) && ring.getTarget().getUniqueId().equals(target.getUniqueId())
                        || ring.getTarget().getUniqueId().equals(player.getUniqueId()) && ring.getPlayer().getUniqueId().equals(target.getUniqueId()))
                .findFirst()
                .orElse(null);
    }

    public static boolean isSameRing(Player player, Player target) {
        return of(player, target) != null;
    }

    public static void remove(GladiatorRing ring) {
        ring.destruct();

        rings.remove(ring);
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(Arrays.asList(player, target));
    }

    public Player getAnotherPlayer(Player player) {
        return getPlayers().stream()
                .filter(arenaPlayer -> !arenaPlayer.equals(player) && arenaPlayer.getWorld().equals(player.getWorld()))
                .findFirst()
                .orElse(null);
    }

    public boolean isValid() {
        return getPlayers().stream().allMatch(player -> {
            User user = (User) User.of(player.getUniqueId());

            return user != null && !user.isProtected() && user.getArcade().isCategory(ArcadeCategory.PVP_ARENA);
        });
    }

    public void spawn() {
        Block mainBlock = getMainBlock();

        // Definindo vidro para a estrutura, incluindo o teto
        for (double x = -radius; x <= radius; x += 1.0D) {
            for (double z = -radius; z <= radius; z += 1.0D) {
                for (double y = 0.0D; y <= height; y += 1.0D) {
                    Location blockLocation = new Location(mainBlock.getWorld(), mainBlock.getX() + x, mainBlock.getY() + y, mainBlock.getZ() + z);

                    blockLocation.getBlock().setType(Material.GLASS);
                    rollBackBlocks.add(blockLocation.getBlock());
                }

                // Teto da arena
                Location roofLocation = new Location(mainBlock.getWorld(), mainBlock.getX() + x, mainBlock.getY() + height + 1, mainBlock.getZ() + z);
                roofLocation.getBlock().setType(Material.GLASS);
                rollBackBlocks.add(roofLocation.getBlock());
            }
        }

        // Definindo o interior da arena como ar
        for (double x = -radius + 1; x <= radius - 1; x += 1.0D) {
            for (double z = -radius + 1; z <= radius - 1; z += 1.0D) {
                for (double y = 1.0D; y < height; y += 1.0D) {
                    Location blockLocation = new Location(mainBlock.getWorld(), mainBlock.getX() + x, mainBlock.getY() + y, mainBlock.getZ() + z);

                    blockLocation.getBlock().setType(Material.AIR);
                    rollBackBlocks.remove(blockLocation.getBlock());
                }
            }
        }

        this.playerSpawn = new Location(mainBlock.getWorld(), mainBlock.getX() + 6.5D, mainBlock.getY() + 1.0D, mainBlock.getZ() + 6.5D, 135, 0);
        this.targetSpawn = new Location(mainBlock.getWorld(), mainBlock.getX() - 5.5D, mainBlock.getY() + 1.0D, mainBlock.getZ() - 5.5D, 315, 0);

        this.spawned = true;
    }

    private Block getMainBlock() {
        Location baseLocation = new Location(spawn.getWorld(), 500, 20, 500); // Localização base (X 500, Z 500, Y 20)

        Location clone = baseLocation.clone();
        clone.setY(clone.getY() + defaultHeight); // Definindo a altura

        boolean ok = true;
        int searchRadius = 20; // Distância para procurar um espaço livre

        while (ok) {
            ok = false;
            boolean stop = false;

            for (double x = -radius; x <= radius; x += 1.0D) {
                for (double z = -radius; z <= radius; z += 1.0D) {
                    for (double y = 0.0D; y <= height; y += 1.0D) {
                        Location blockLocation = new Location(clone.getWorld(), clone.getX() + x, clone.getY() + y, clone.getZ() + z);

                        // Verifica se há blocos no local
                        if (blockLocation.getBlock().getType() != Material.AIR) {
                            ok = true;
                            clone.setX(clone.getX() + searchRadius); // Move X ou Z para tentar outro local
                            stop = true;
                        }

                        if (stop) break;
                    }
                    if (stop) break;
                }
                if (stop) break;
            }
        }

        return clone.getBlock();
    }

    public void destruct() {
        /* Teleportar jogadores para o back */
        getPlayers().forEach(player -> {
            User user = (User) User.of(player.getUniqueId());

            // Remove all potion effects
            for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            if (user != null && !user.isProtected())
                player.teleport(back);
        });

        rollBackBlocks.forEach(block -> block.setType(Material.AIR));

        this.spawned = false;
        this.rollBackBlocks.clear();
    }

    public void teleport() {
        if (!isSpawned()) {
            Core.getLogger().warning("Não foi possível gerar o ringue de Gladiator.");
            return;
        }

        player.teleport(playerSpawn);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 5, 5));

        target.teleport(targetSpawn);
        target.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 5, 5));
    }
}
