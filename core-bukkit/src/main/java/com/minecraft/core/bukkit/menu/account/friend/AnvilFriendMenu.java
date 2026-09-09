package com.minecraft.core.bukkit.menu.account.friend;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.util.list.Validator;
import net.minecraft.server.v1_8_R3.*;
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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

@IgnoreEvent
public class AnvilFriendMenu implements Listener {

    private final Player player;
    private final Account account;
    private ContainerAnvil anvilContainer;
    private int containerId;
    private BukkitRunnable updateTask;
    private boolean taskCancelled = false;

    public AnvilFriendMenu(Player player) {
        this.player = player;
        this.account = Core.getAccountController().of(player.getUniqueId());
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
                new ChatMessage("Reparar & Renomear"),
                0));

        entityPlayer.activeContainer = anvilContainer;
        entityPlayer.activeContainer.windowId = containerId;
        entityPlayer.activeContainer.addSlotListener(entityPlayer);

        ItemStack paper = new ItemStack(org.bukkit.Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName("Insira o usuário");
        paper.setItemMeta(meta);
        
        anvilContainer.getBukkitView().getTopInventory().setItem(0, paper);

        BukkitCore.getInstance().getServer().getPluginManager().registerEvents(this, BukkitCore.getInstance());
        
        Core.getPlatform().runSync(() -> {
            try {
                anvilContainer.a();
                EntityPlayer ep = ((CraftPlayer) player).getHandle();
                ep.updateInventory(anvilContainer);
            } catch (Exception ignored) {
            }
        });
        
        updateTask = new BukkitRunnable() {
            private String lastFriendName = "";
            
            @Override
            public void run() {
                if (taskCancelled || !player.isOnline() || player.getOpenInventory().getType() != InventoryType.ANVIL) {
                    cancel();
                    return;
                }
                
                try {
                    Inventory anvilInv = player.getOpenInventory().getTopInventory();
                    if (anvilInv == null) {
                        cancel();
                        return;
                    }
                    
                    // Limpar qualquer papel que possa ter sido movido para o inventário do jogador
                    for (int i = 0; i < 36; i++) {
                        ItemStack item = player.getInventory().getItem(i);
                        if (item != null && item.getType() == org.bukkit.Material.PAPER) {
                            ItemMeta itemMeta = item.getItemMeta();
                            if (itemMeta != null && itemMeta.hasDisplayName()) {
                                player.getInventory().setItem(i, null);
                            }
                        }
                    }
                    
                    // Limpar cursor se tiver papel
                    if (player.getItemOnCursor() != null && player.getItemOnCursor().getType() == org.bukkit.Material.PAPER) {
                        player.setItemOnCursor(null);
                    }
                    
                    String currentFriendName = "";
                    try {
                        EntityPlayer ep = ((CraftPlayer) player).getHandle();
                        if (ep.activeContainer == anvilContainer) {
                            java.lang.reflect.Field renameField = ContainerAnvil.class.getDeclaredField("renameText");
                            renameField.setAccessible(true);
                            String renameText = (String) renameField.get(anvilContainer);
                            
                            if (renameText != null && !renameText.isEmpty()) {
                                currentFriendName = renameText;
                            } else {
                                ItemStack input = anvilInv.getItem(0);
                                if (input != null && input.hasItemMeta() && input.getItemMeta().hasDisplayName()) {
                                    currentFriendName = input.getItemMeta().getDisplayName();
                                }
                            }
                        } else {
                            ItemStack input = anvilInv.getItem(0);
                            if (input != null && input.hasItemMeta() && input.getItemMeta().hasDisplayName()) {
                                currentFriendName = input.getItemMeta().getDisplayName();
                            }
                        }
                    } catch (Exception e) {
                        ItemStack input = anvilInv.getItem(0);
                        if (input != null && input.hasItemMeta() && input.getItemMeta().hasDisplayName()) {
                            currentFriendName = input.getItemMeta().getDisplayName();
                        }
                    }
                    
                    currentFriendName = org.bukkit.ChatColor.stripColor(currentFriendName);
                    if (!currentFriendName.equals(lastFriendName)) {
                        lastFriendName = currentFriendName;
                        
                        if (!currentFriendName.isEmpty() && !currentFriendName.equals("Insira o usuário")) {
                            ItemStack result = new ItemStack(org.bukkit.Material.PAPER);
                            ItemMeta resultMeta = result.getItemMeta();
                            resultMeta.setDisplayName(currentFriendName);
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
        updateTask.runTaskTimer(BukkitCore.getInstance(), 0L, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player)) return;
        
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;
        
        if (!(clickedInventory instanceof CraftInventoryAnvil)) return;
        
        CraftInventoryAnvil anvil = (CraftInventoryAnvil) clickedInventory;
        
        // Slot 2 é o slot de resultado do anvil
        if (event.getSlot() == 2) {
            ItemStack result = anvil.getItem(2);
            
            if (result != null && result.getType() == org.bukkit.Material.PAPER) {
                ItemMeta meta = result.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String rawFriendName = meta.getDisplayName();
                    final String friendName = org.bukkit.ChatColor.stripColor(rawFriendName);
                    
                    // Cancelar o evento ANTES de qualquer coisa
                    event.setCancelled(true);
                    
                    // Remover o item do slot de resultado IMEDIATAMENTE
                    anvil.setItem(2, null);
                    
                    // Limpar o cursor e atualizar inventário de forma síncrona
                    player.setItemOnCursor(null);
                    player.updateInventory();
                    
                    // Fechar o anvil imediatamente para evitar que o item seja movido
                    player.closeInventory();
                    
                    // Processar o pedido de amizade e limpar itens em uma task
                    Core.getPlatform().runSync(() -> {
                        // Limpar cursor
                        player.setItemOnCursor(null);
                        
                        // Verificar e remover qualquer papel do inventário
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
                        
                        // Verificar novamente após um pequeno delay para garantir
                        Core.getPlatform().runSync(() -> {
                            player.setItemOnCursor(null);
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
                        }, 2L);
                        
                        // Validações
                        if (!Validator.isNickname(friendName)) {
                            player.sendMessage("§cO nome inserido é inválido.");
                            player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                            return;
                        }
                        
                        // Buscar conta do amigo de forma assíncrona
                        Core.getPlatform().runAsync(() -> {
                            try {
                                Account target = Core.getAccountData().of(friendName);
                                
                                if (target == null || !target.isOnline()) {
                                    Core.getPlatform().runSync(() -> {
                                        player.sendMessage("§cO jogador " + friendName + " não foi encontrado.");
                                        player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                                    });
                                    return;
                                }
                                
                                if (account.equals(target)) {
                                    Core.getPlatform().runSync(() -> {
                                        player.sendMessage("§cVocê não pode adicionar a si mesmo como amigo.");
                                        player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                                    });
                                    return;
                                }
                                
                                if (account.isFriend(target)) {
                                    Core.getPlatform().runSync(() -> {
                                        player.sendMessage("§cEsse jogador já é seu amigo.");
                                        player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                                    });
                                    return;
                                }
                                
                                if (!target.getToggle().isAllowFriendRequests()) {
                                    Core.getPlatform().runSync(() -> {
                                        player.sendMessage("§cO jogador " + target.getName() + " não está recebendo pedidos de amizade!");
                                        player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                                    });
                                    return;
                                }
                                
                                if (account.hasFriendRequest(target)) {
                                    Core.getPlatform().runSync(() -> {
                                        player.sendMessage("§cVocê já enviou uma solicitação de amizade para " + target.getName() + ".");
                                        player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                                    });
                                    return;
                                }
                                
                                // Enviar pedido de amizade (mesma lógica do comando)
                                Core.getPlatform().runSync(() -> {
                                    player.sendMessage("§aVocê enviou uma solicitação de amizade para " + target.getName() + ".");
                                    player.playSound(player.getLocation(), MenuSound.SUCCESS.getSound(), 2f, 2.5f);
                                    
                                    target.addFriendRequest(account);
                                    target.send("§b" + account.getName() + "§e enviou uma solicitação de amizade.");
                                });
                                
                            } catch (Exception e) {
                                Core.getPlatform().runSync(() -> {
                                    player.sendMessage("§cOcorreu um erro ao enviar o pedido de amizade!");
                                    player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                                });
                                e.printStackTrace();
                            }
                        });
                    });
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getWhoClicked().equals(player)) return;
        
        if (event.getInventory() instanceof CraftInventoryAnvil) {
            // Cancelar qualquer drag no anvil
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getPlayer().equals(player)) return;
        
        // Cancelar a task primeiro para evitar atualizações
        taskCancelled = true;
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        // Limpar o slot de resultado do anvil ANTES de fechar
        if (event.getInventory() instanceof CraftInventoryAnvil) {
            CraftInventoryAnvil anvil = (CraftInventoryAnvil) event.getInventory();
            anvil.setItem(2, null);
        }
        
        // Limpar cursor imediatamente
        player.setItemOnCursor(null);
        
        // Limpar qualquer papel que possa ter sido movido para o inventário
        // Executar de forma síncrona e imediata
        for (int i = 0; i < 36; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == org.bukkit.Material.PAPER) {
                ItemMeta itemMeta = item.getItemMeta();
                if (itemMeta != null && itemMeta.hasDisplayName()) {
                    // Remover TODOS os papéis com displayName (incluindo o placeholder)
                    player.getInventory().setItem(i, null);
                }
            }
        }
        
        // Atualizar inventário imediatamente
        player.updateInventory();
        
        // Verificar novamente após um pequeno delay para garantir que nada foi movido
        Core.getPlatform().runSync(() -> {
            player.setItemOnCursor(null);
            
            for (int i = 0; i < 36; i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && item.getType() == org.bukkit.Material.PAPER) {
                    ItemMeta itemMeta = item.getItemMeta();
                    if (itemMeta != null && itemMeta.hasDisplayName()) {
                        // Remover TODOS os papéis com displayName
                        player.getInventory().setItem(i, null);
                    }
                }
            }
            player.updateInventory();
        }, 1L);
        
        // Verificar mais uma vez após um delay maior
        Core.getPlatform().runSync(() -> {
            player.setItemOnCursor(null);
            
            for (int i = 0; i < 36; i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && item.getType() == org.bukkit.Material.PAPER) {
                    ItemMeta itemMeta = item.getItemMeta();
                    if (itemMeta != null && itemMeta.hasDisplayName()) {
                        // Remover TODOS os papéis com displayName
                        player.getInventory().setItem(i, null);
                    }
                }
            }
            player.updateInventory();
        }, 5L);
        
        HandlerList.unregisterAll(this);
    }
}
