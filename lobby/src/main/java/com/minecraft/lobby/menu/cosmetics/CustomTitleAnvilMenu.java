package com.minecraft.lobby.menu.cosmetics;

import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.type.title.CustomTitle;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.lobby.user.User;
import java.util.UUID;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryAnvil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;

@IgnoreEvent
public class CustomTitleAnvilMenu implements Listener {

    private final Player player;
    private final User user;
    private ContainerAnvil anvilContainer;
    private int containerId;
    private BukkitRunnable updateTask;
    private boolean taskCancelled = false;
    private boolean processed = false;
    private final String paperId;

    private static String stripCodeColors(String input) {
        return input.replaceAll("(&|§)[0-9a-fk-orA-FK-OR]", "");
    }

    private static boolean isValidTitle(String input) {
        if (input == null || input.isEmpty()) return false;
        String stripped = stripCodeColors(input);
        if (stripped.length() < 2 || stripped.length() > 32) return false;
        return stripped.matches("[\\w\\sÀ-ÿ\\p{P}\\p{S}]+");
    }

    public CustomTitleAnvilMenu(Player player, User user) {
        this.player = player;
        this.user = user;
        this.paperId = UUID.randomUUID().toString().substring(0, 8);
    }

    public void open() {
        if (!user.getAccount().hasRank(RankType.PARTNER)) {
            player.sendMessage("§cVocê precisa ser Partner ou superior para criar um título customizável!");
            return;
        }

        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        anvilContainer = new ContainerAnvil(entityPlayer.inventory, entityPlayer.world,
                new BlockPosition(0, 0, 0), entityPlayer) {
            @Override
            public boolean a(EntityHuman entityhuman) {
                return true;
            }
        };

        containerId = entityPlayer.nextContainerCounter();

        entityPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(
                containerId,
                "minecraft:anvil",
                new ChatMessage("Título Customizável"),
                0));

        entityPlayer.activeContainer = anvilContainer;
        entityPlayer.activeContainer.windowId = containerId;
        entityPlayer.activeContainer.addSlotListener(entityPlayer);

        ItemStack paper = new ItemStack(org.bukkit.Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName("§eDigite seu título...");
        paper.setItemMeta(meta);

        anvilContainer.getBukkitView().getTopInventory().setItem(0, paper);

        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("Lobby"));

        updateTask = new BukkitRunnable() {
            private String lastTitle = "";

            @Override
            public void run() {
                if (taskCancelled || !player.isOnline() || player.getOpenInventory().getType() != org.bukkit.event.inventory.InventoryType.ANVIL) {
                    cancel();
                    return;
                }

                try {
                    String currentTitle = "";
                    try {
                        Field renameField = ContainerAnvil.class.getDeclaredField("renameText");
                        renameField.setAccessible(true);
                        String renameText = (String) renameField.get(anvilContainer);

                        if (renameText != null && !renameText.isEmpty()) {
                            currentTitle = renameText;
                        } else {
                            ItemStack input = anvilContainer.getBukkitView().getTopInventory().getItem(0);
                            if (input != null && input.hasItemMeta() && input.getItemMeta().hasDisplayName()) {
                                currentTitle = input.getItemMeta().getDisplayName();
                            }
                        }
                    } catch (Exception e) {
                        ItemStack input = anvilContainer.getBukkitView().getTopInventory().getItem(0);
                        if (input != null && input.hasItemMeta() && input.getItemMeta().hasDisplayName()) {
                            currentTitle = input.getItemMeta().getDisplayName();
                        }
                    }

                    if (!org.bukkit.ChatColor.stripColor(currentTitle).equals(lastTitle)) {
                        lastTitle = org.bukkit.ChatColor.stripColor(currentTitle);

                        if (!lastTitle.isEmpty() && !lastTitle.equals("§eDigite seu título...")) {
                            if (isValidTitle(lastTitle)) {
                                ItemStack result = new ItemStack(org.bukkit.Material.PAPER);
                                ItemMeta resultMeta = result.getItemMeta();
                                resultMeta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', lastTitle));
                                result.setItemMeta(resultMeta);

                                anvilContainer.getBukkitView().getTopInventory().setItem(2, result);
                            } else {
                                anvilContainer.getBukkitView().getTopInventory().setItem(2, null);
                            }

                            EntityPlayer ep = ((CraftPlayer) player).getHandle();
                            if (ep.activeContainer == anvilContainer) {
                                anvilContainer.a();
                                ep.updateInventory(anvilContainer);
                            } else {
                                player.updateInventory();
                            }
                        } else {
                            anvilContainer.getBukkitView().getTopInventory().setItem(2, null);
                            EntityPlayer ep = ((CraftPlayer) player).getHandle();
                            if (ep.activeContainer == anvilContainer) {
                                anvilContainer.a();
                                ep.updateInventory(anvilContainer);
                            } else {
                                player.updateInventory();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        updateTask.runTaskTimer(Bukkit.getPluginManager().getPlugin("Lobby"), 0L, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player)) return;

        if (!(event.getClickedInventory() instanceof CraftInventoryAnvil)) return;

        CraftInventoryAnvil anvil = (CraftInventoryAnvil) event.getClickedInventory();

        if (event.getSlot() == 2) {
            ItemStack result = anvil.getItem(2);

            if (result != null && result.getType() == org.bukkit.Material.PAPER) {
                ItemMeta meta = result.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String customTitle = org.bukkit.ChatColor.stripColor(meta.getDisplayName());

                    if (isValidTitle(customTitle)) {
                        event.setCancelled(true);
                        processed = true;
                        
                        // Limpar TODOS os slots do anvil imediatamente
                        anvil.setItem(0, null);
                        anvil.setItem(1, null);
                        anvil.setItem(2, null);
                        player.setItemOnCursor(null);
                        
                        // Forçar atualização antes de fechar
                        EntityPlayer ep = ((CraftPlayer) player).getHandle();
                        if (ep.activeContainer == anvilContainer) {
                            anvilContainer.a();
                            ep.updateInventory(anvilContainer);
                        }
                        player.updateInventory();
                        
                        player.closeInventory();

                        TitleCollectible.removeTitle(player);
                        CustomTitle.removeTitle(player);

                        Account account = user.getAccount();
                         // Permitir cores usando &
                         String coloredTitle = org.bukkit.ChatColor.translateAlternateColorCodes('&', customTitle);
                         account.setCustomTitle(coloredTitle);
                         account.getActiveCollectibles().removeIf(id -> id.startsWith("TITLE:"));
                         account.getActiveCollectibles().add("CUSTOM_TITLE:" + coloredTitle);
                         account.saveContext(account.getContext());

                        CustomTitle.showTitle(player);
                        player.sendMessage("§aTítulo customizável criado: §f" + customTitle);
                        return;
                    } else {
                        player.sendMessage("§cTítulo inválido! Use letras, números, cores (&), pontuação e símbolos (2-32 caracteres visíveis).");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getWhoClicked().equals(player)) return;

        if (event.getInventory() instanceof CraftInventoryAnvil) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getPlayer().equals(player)) return;
        if (processed) return;

        taskCancelled = true;
        if (updateTask != null) {
            updateTask.cancel();
        }

        // Limpar TODOS os slots da anvil imediatamente
        if (event.getInventory() instanceof CraftInventoryAnvil) {
            CraftInventoryAnvil anvil = (CraftInventoryAnvil) event.getInventory();
            anvil.setItem(0, null);
            anvil.setItem(1, null);
            anvil.setItem(2, null);
        }

        player.setItemOnCursor(null);

        // Limpar apenas papéis com o ID deste menu
        for (int i = 0; i < 36; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == org.bukkit.Material.PAPER && item.hasItemMeta()) {
                String name = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());
                if (name.contains(paperId)) {
                    player.getInventory().setItem(i, null);
                }
            }
        }
        player.updateInventory();

        // Verificar novamente após um pequeno delay
        Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("Lobby"),
                new Runnable() {
                    @Override
                    public void run() {
                        player.setItemOnCursor(null);
                        for (int i = 0; i < 36; i++) {
                            ItemStack item = player.getInventory().getItem(i);
                            if (item != null && item.getType() == org.bukkit.Material.PAPER && item.hasItemMeta()) {
                                String name = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());
                                if (name.contains(paperId)) {
                                    player.getInventory().setItem(i, null);
                                }
                            }
                        }
                        player.updateInventory();
                    }
                },
                1L
        );

        org.bukkit.event.HandlerList.unregisterAll(this);

        Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("Lobby"),
                new Runnable() {
                    @Override
                    public void run() {
                        new TitlesMenu(player, user);
                    }
                },
                2L
        );
    }
}
