package com.minecraft.core.bukkit.listener;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.type.title.CustomTitle;
import com.minecraft.core.event.IgnoreEvent;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryAnvil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentHashMap;

@IgnoreEvent
public class CustomTitleListener implements Listener {

    private static final ConcurrentHashMap<Player, CustomTitleAnvil> openAnvils = new ConcurrentHashMap<>();
    private static final int MAX_TITLE_LENGTH = 32;

    public CustomTitleListener() {
    }

    public static void openAnvil(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        if (openAnvils.containsKey(player)) {
            openAnvils.get(player).close();
        }

        CustomTitleAnvil anvil = new CustomTitleAnvil(player, account);
        openAnvils.put(player, anvil);
        anvil.open();
    }

    @IgnoreEvent
    private static class CustomTitleAnvil implements Listener {

        private final Player player;
        private final Account account;
        private ContainerAnvil anvilContainer;
        private int containerId;
        private BukkitRunnable updateTask;
        private boolean taskCancelled = false;

        public CustomTitleAnvil(Player player, Account account) {
            this.player = player;
            this.account = account;
        }

        public void open() {
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
                    new ChatMessage("§8§lTÍTULO PERSONALIZADO"),
                    0));

            entityPlayer.activeContainer = anvilContainer;
            entityPlayer.activeContainer.windowId = containerId;
            entityPlayer.activeContainer.addSlotListener(entityPlayer);

            ItemStack paper = new ItemStack(org.bukkit.Material.PAPER);
            ItemMeta meta = paper.getItemMeta();

            String currentTitle = account.getCustomTitle();
            if (currentTitle != null && !currentTitle.isEmpty()) {
                meta.setDisplayName(currentTitle);
            } else {
                meta.setDisplayName("§7Digite aqui...");
            }

            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("");
            lore.add("§7Digite o título desejado");
            lore.add("§7(use & para cores)");
            lore.add("");
            lore.add("§eClique no resultado para salvar!");
            meta.setLore(lore);
            paper.setItemMeta(meta);

            anvilContainer.getBukkitView().getTopInventory().setItem(0, paper);

            Core.getJavaPlugin().getServer().getPluginManager().registerEvents(this, Core.getJavaPlugin());

            Core.getPlatform().runSync(() -> {
                try {
                    anvilContainer.a();
                    EntityPlayer ep = ((CraftPlayer) player).getHandle();
                    ep.updateInventory(anvilContainer);
                } catch (Exception ignored) {
                }
            });

            updateTask = new BukkitRunnable() {
                private String lastTitle = "";

                @Override
                public void run() {
                    if (taskCancelled || !player.isOnline() || player.getOpenInventory().getType() != org.bukkit.event.inventory.InventoryType.ANVIL) {
                        cancel();
                        return;
                    }

                    try {
                        Inventory anvilInv = player.getOpenInventory().getTopInventory();
                        if (anvilInv == null) {
                            cancel();
                            return;
                        }

                        String currentTitleText = "";
                        try {
                            EntityPlayer ep = ((CraftPlayer) player).getHandle();
                            if (ep.activeContainer == anvilContainer) {
                                java.lang.reflect.Field renameField = ContainerAnvil.class.getDeclaredField("renameText");
                                renameField.setAccessible(true);
                                String renameText = (String) renameField.get(anvilContainer);

                                if (renameText != null && !renameText.isEmpty()) {
                                    currentTitleText = ChatColor.translateAlternateColorCodes('&', renameText);
                                } else {
                                    ItemStack input = anvilInv.getItem(0);
                                    if (input != null && input.hasItemMeta() && input.getItemMeta().hasDisplayName()) {
                                        currentTitleText = input.getItemMeta().getDisplayName();
                                    }
                                }
                            } else {
                                ItemStack input = anvilInv.getItem(0);
                                if (input != null && input.hasItemMeta() && input.getItemMeta().hasDisplayName()) {
                                    currentTitleText = input.getItemMeta().getDisplayName();
                                }
                            }
                        } catch (Exception e) {
                            ItemStack input = anvilInv.getItem(0);
                            if (input != null && input.hasItemMeta() && input.getItemMeta().hasDisplayName()) {
                                currentTitleText = input.getItemMeta().getDisplayName();
                            }
                        }

                        if (!currentTitleText.equals(lastTitle)) {
                            lastTitle = currentTitleText;

                            String stripped = ChatColor.stripColor(currentTitleText);

                            if (!currentTitleText.isEmpty() && !stripped.equals("Digite aqui...")) {
                                ItemStack result = new ItemStack(org.bukkit.Material.PAPER);
                                ItemMeta resultMeta = result.getItemMeta();

                                if (stripped.length() > MAX_TITLE_LENGTH) {
                                    resultMeta.setDisplayName("§c§lMUITO LONGO!");
                                    resultMeta.setLore(java.util.Arrays.asList(
                                            "",
                                            "§cMáximo: " + MAX_TITLE_LENGTH + " caracteres",
                                            "§cAtual: " + stripped.length() + " caracteres"
                                    ));
                                } else {
                                    resultMeta.setDisplayName(currentTitleText);
                                    resultMeta.setLore(java.util.Arrays.asList(
                                            "",
                                            "§aClique para salvar!",
                                            "§7" + stripped.length() + "/" + MAX_TITLE_LENGTH + " caracteres"
                                    ));
                                }

                                result.setItemMeta(resultMeta);
                                anvilInv.setItem(2, result);

                                EntityPlayer ep = ((CraftPlayer) player).getHandle();
                                if (ep.activeContainer == anvilContainer) {
                                    anvilContainer.a();
                                    ep.updateInventory(anvilContainer);
                                } else {
                                    player.updateInventory();
                                }
                            } else {
                                anvilInv.setItem(2, null);
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
            updateTask.runTaskTimer(Core.getJavaPlugin(), 0L, 1L);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getWhoClicked().equals(player)) return;

            Inventory clickedInventory = event.getClickedInventory();
            if (clickedInventory == null) return;

            if (!(clickedInventory instanceof CraftInventoryAnvil)) return;

            CraftInventoryAnvil anvil = (CraftInventoryAnvil) clickedInventory;

            if (event.getSlot() == 2) {
                ItemStack result = anvil.getItem(2);

                if (result != null && result.getType() == org.bukkit.Material.PAPER) {
                    ItemMeta meta = result.getItemMeta();
                    if (meta != null && meta.hasDisplayName()) {
                        String rawTitle = meta.getDisplayName();
                        String title = ChatColor.translateAlternateColorCodes('&', rawTitle);
                        String stripped = ChatColor.stripColor(title);

                        if (stripped.equals("MUITO LONGO!") || stripped.isEmpty()) {
                            event.setCancelled(true);
                            player.sendMessage("§cTítulo inválido!");
                            return;
                        }

                        if (stripped.length() > MAX_TITLE_LENGTH) {
                            event.setCancelled(true);
                            player.sendMessage("§cO título é muito longo! Máximo de " + MAX_TITLE_LENGTH + " caracteres.");
                            return;
                        }

                        event.setCancelled(true);
                        anvil.setItem(2, null);
                        player.setItemOnCursor(null);
                        player.updateInventory();
                        player.closeInventory();

                        Core.getPlatform().runSync(() -> {
                            account.setCustomTitle(title);
                            player.sendMessage("§aTítulo salvo: " + title);
                            CustomTitle.showTitle(player);
                        });
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

            taskCancelled = true;
            if (updateTask != null) {
                updateTask.cancel();
            }

            if (event.getInventory() instanceof CraftInventoryAnvil) {
                CraftInventoryAnvil anvil = (CraftInventoryAnvil) event.getInventory();
                anvil.setItem(2, null);
            }

            player.setItemOnCursor(null);
            player.updateInventory();

            Core.getPlatform().runSync(() -> {
                for (int i = 0; i < 36; i++) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (item != null && item.getType() == org.bukkit.Material.PAPER) {
                        ItemMeta itemMeta = item.getItemMeta();
                        if (itemMeta != null && itemMeta.hasDisplayName()) {
                            player.getInventory().setItem(i, null);
                        }
                    }
                }
                player.updateInventory();
            }, 1L);

            openAnvils.remove(player);
            HandlerList.unregisterAll(this);
        }

        public void close() {
            taskCancelled = true;
            if (updateTask != null) {
                updateTask.cancel();
            }
            player.closeInventory();
            openAnvils.remove(player);
            HandlerList.unregisterAll(this);
        }
    }
}
