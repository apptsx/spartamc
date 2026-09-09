package com.minecraft.core.bukkit.menu.account.skin;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.skin.Skin;
import com.minecraft.core.api.skin.objects.SkinType;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.core.controller.list.SkinController;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@IgnoreEvent
public class AnvilSkinMenu implements Listener {

    // Cache estático de skins para evitar buscas repetidas na API
    private static final Map<String, Skin> SKIN_CACHE = new HashMap<>();
    private static final int MAX_CACHE_SIZE = 100;

    private final Player player;
    private final Account account;
    private ContainerAnvil anvilContainer;
    private int containerId;
    private BukkitRunnable updateTask;
    private boolean taskCancelled = false;
    
    // Flag para evitar processamento duplicado
    private boolean isProcessingSkin = false;

    public AnvilSkinMenu(Player player) {
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
            private String lastSkinName = "";
            
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
                                String itemName = org.bukkit.ChatColor.stripColor(itemMeta.getDisplayName());
                                // Remover papéis que não sejam "Insira o usuário"
                                if (!itemName.equals("Insira o usuário")) {
                                    player.getInventory().setItem(i, null);
                                }
                            }
                        }
                    }
                    
                    // Limpar cursor se tiver papel
                    if (player.getItemOnCursor() != null && player.getItemOnCursor().getType() == org.bukkit.Material.PAPER) {
                        ItemMeta cursorMeta = player.getItemOnCursor().getItemMeta();
                        if (cursorMeta != null && cursorMeta.hasDisplayName()) {
                            String cursorName = org.bukkit.ChatColor.stripColor(cursorMeta.getDisplayName());
                            if (!cursorName.equals("Insira o usuário")) {
                                player.setItemOnCursor(null);
                            }
                        }
                    }
                    
                    String currentSkinName = "";
                    try {
                        EntityPlayer ep = ((CraftPlayer) player).getHandle();
                        if (ep.activeContainer == anvilContainer) {
                            java.lang.reflect.Field renameField = ContainerAnvil.class.getDeclaredField("renameText");
                            renameField.setAccessible(true);
                            String renameText = (String) renameField.get(anvilContainer);
                            
                            if (renameText != null && !renameText.isEmpty()) {
                                currentSkinName = renameText;
                            } else {
                                ItemStack input = anvilInv.getItem(0);
                                if (input != null && input.hasItemMeta() && input.getItemMeta().hasDisplayName()) {
                                    currentSkinName = input.getItemMeta().getDisplayName();
                                }
                            }
                        } else {
                            ItemStack input = anvilInv.getItem(0);
                            if (input != null && input.hasItemMeta() && input.getItemMeta().hasDisplayName()) {
                                currentSkinName = input.getItemMeta().getDisplayName();
                            }
                        }
                    } catch (Exception e) {
                        ItemStack input = anvilInv.getItem(0);
                        if (input != null && input.hasItemMeta() && input.getItemMeta().hasDisplayName()) {
                            currentSkinName = input.getItemMeta().getDisplayName();
                        }
                    }
                    
                    currentSkinName = org.bukkit.ChatColor.stripColor(currentSkinName);
                    if (!currentSkinName.equals(lastSkinName)) {
                        lastSkinName = currentSkinName;
                        
                        if (!currentSkinName.isEmpty() && !currentSkinName.equals("Insira o usuário")) {
                            ItemStack result = new ItemStack(org.bukkit.Material.PAPER);
                            ItemMeta resultMeta = result.getItemMeta();
                            resultMeta.setDisplayName(currentSkinName);
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
                    String rawSkinName = meta.getDisplayName();
                    final String skinName = org.bukkit.ChatColor.stripColor(rawSkinName);
                    
                    // Cancelar o evento ANTES de qualquer coisa
                    event.setCancelled(true);
                    
                    // Remover o item do slot de resultado IMEDIATAMENTE
                    anvil.setItem(2, null);
                    
                    // Limpar o cursor e atualizar inventário de forma síncrona
                    player.setItemOnCursor(null);
                    player.updateInventory();
                    
                    // Fechar o anvil imediatamente para evitar que o item seja movido
                    player.closeInventory();
                    
                    // Processar o skin e limpar itens em uma task
                    Core.getPlatform().runSync(() -> {
                        // Limpar cursor
                        player.setItemOnCursor(null);
                        
                        // Verificar e remover qualquer papel do inventário que tenha o skin name
                        for (int i = 0; i < 36; i++) {
                            ItemStack item = player.getInventory().getItem(i);
                            if (item != null && item.getType() == org.bukkit.Material.PAPER) {
                                ItemMeta itemMeta = item.getItemMeta();
                                if (itemMeta != null && itemMeta.hasDisplayName()) {
                                    String itemName = org.bukkit.ChatColor.stripColor(itemMeta.getDisplayName());
                                    if (itemName.equals(skinName) || itemName.equals("Insira o usuário")) {
                                        player.getInventory().setItem(i, null);
                                    }
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
                                        String itemName = org.bukkit.ChatColor.stripColor(itemMeta.getDisplayName());
                                        if (itemName.equals(skinName) || itemName.equals("Insira o usuário")) {
                                            player.getInventory().setItem(i, null);
                                        }
                                    }
                                }
                            }
                            player.updateInventory();
                        }, 2L);
                        
                        // Validações
                        if (!account.isVIP()) {
                            player.sendMessage("§cVocê não pode customizar sua skin! ;(");
                            player.sendMessage("§cAdquira o rank " + RankType.VIP.getColoredName() + "§c em: §e" + Constant.SERVER_STORE);
                            player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                            return;
                        }
                        
                        if (account.hasCooldown(Constant.SKIN_CHANGE_COOLDOWN_KEY)) {
                            player.sendMessage("§cAguarde " + account.getFormattedCooldown(Constant.SKIN_CHANGE_COOLDOWN_KEY) + " para mudar de skin novamente.");
                            player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                            return;
                        }
                        
                        if (!Validator.isNickname(skinName)) {
                            player.sendMessage("§cO nome inserido é inválido, tente novamente.");
                            player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                            return;
                        }
                        
                        // Evitar processamento duplicado
                        if (isProcessingSkin) {
                            player.sendMessage("§cAguarde o processamento anterior finalizar.");
                            return;
                        }
                        
                        isProcessingSkin = true;
                        
                        // Verificar cache primeiro (INSTANTÂNEO)
                        String skinNameLower = skinName.toLowerCase();
                        Skin cachedSkin = SKIN_CACHE.get(skinNameLower);
                        
                        if (cachedSkin != null) {
                            // SKIN NO CACHE - APLICAÇÃO INSTANTÂNEA!
                            if (account.hasSkin(cachedSkin)) {
                                player.sendMessage("§cA skin §7" + cachedSkin.getDisplayName() + "§c já está selecionada.");
                                player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                                isProcessingSkin = false;
                                return;
                            }
                            
                            ProtocolHandler.changePlayerSkin(player, cachedSkin);
                            account.setSkin(cachedSkin);
                            
                            player.sendMessage("§aA skin §7" + cachedSkin.getDisplayName() + "§a foi aplicada!");
                            player.playSound(player.getLocation(), MenuSound.SUCCESS.getSound(), 2f, 2.5f);
                            
                            if (!account.isStaffer()) {
                                account.setCooldown(Constant.SKIN_CHANGE_COOLDOWN_KEY, TimeUnit.MINUTES.toMillis(3));
                            }
                            
                            isProcessingSkin = false;
                            return;
                        }
                        
                        // Buscar skin de forma assíncrona OTIMIZADA
                        CompletableFuture.runAsync(() -> {
                            try {
                                // Buscar UUID e skin de forma otimizada
                                UUID id = Core.MOJANG_API.getUUID(skinName);
                                
                                if (id == null) {
                                    Core.getPlatform().runSync(() -> {
                                        player.sendMessage("§cJogador não encontrado.");
                                        player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                                        isProcessingSkin = false;
                                    });
                                    return;
                                }
                                
                                Skin skin = SkinController.getSkin(id, skinName, SkinType.CUSTOM);
                                
                                if (skin == null) {
                                    Core.getPlatform().runSync(() -> {
                                        player.sendMessage("§cA skin solicitada não foi encontrada.");
                                        player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                                        isProcessingSkin = false;
                                    });
                                    return;
                                }
                                
                                // Adicionar ao cache
                                synchronized (SKIN_CACHE) {
                                    if (SKIN_CACHE.size() >= MAX_CACHE_SIZE) {
                                        // Limpar cache se estiver muito grande
                                        SKIN_CACHE.clear();
                                        Core.getLogger().info("[AnvilSkinMenu] Cache de skins limpo (limite atingido)");
                                    }
                                    SKIN_CACHE.put(skinNameLower, skin);
                                }
                                
                                // Verificar se já tem a skin (de forma otimizada)
                                if (account.hasSkin(skin)) {
                                    Core.getPlatform().runSync(() -> {
                                        player.sendMessage("§cA skin §7" + skin.getDisplayName() + "§c já está selecionada.");
                                        player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                                        isProcessingSkin = false;
                                    });
                                    return;
                                }
                                
                                // Aplicar skin de forma síncrona - RÁPIDO
                                Core.getPlatform().runSync(() -> {
                                    try {
                                        ProtocolHandler.changePlayerSkin(player, skin);
                                        account.setSkin(skin);
                                        
                                        player.sendMessage("§aA skin §7" + skin.getDisplayName() + "§a foi aplicada!");
                                        player.playSound(player.getLocation(), MenuSound.SUCCESS.getSound(), 2f, 2.5f);
                                        
                                        if (!account.isStaffer()) {
                                            account.setCooldown(Constant.SKIN_CHANGE_COOLDOWN_KEY, TimeUnit.MINUTES.toMillis(3));
                                        }
                                    } finally {
                                        isProcessingSkin = false;
                                    }
                                });
                                
                            } catch (Exception e) {
                                Core.getLogger().warning("[AnvilSkinMenu] Erro ao processar skin '" + skinName + "': " + e.getMessage());
                                Core.getPlatform().runSync(() -> {
                                    player.sendMessage("§cOcorreu um erro ao processar a skin!");
                                    player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                                    isProcessingSkin = false;
                                });
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
        
        // Resetar flag de processamento
        isProcessingSkin = false;
        
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

