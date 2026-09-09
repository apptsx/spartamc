package com.minecraft.core.bukkit.menu.account.nickname;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.mojang.MojangApi;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.backend.database.redis.message.types.account.AccountNickChangeMessage;
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

import java.util.concurrent.TimeUnit;

@IgnoreEvent
public class AnvilNicknameMenu implements Listener {

    private final Player player;
    private final Account account;
    private ContainerAnvil anvilContainer;
    private int containerId;
    private BukkitRunnable updateTask;
    private boolean taskCancelled = false;

    public AnvilNicknameMenu(Player player) {
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
            private String lastNickname = "";
            
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
                    
                    String currentNickname = "";
                    try {
                        EntityPlayer ep = ((CraftPlayer) player).getHandle();
                        if (ep.activeContainer == anvilContainer) {
                            java.lang.reflect.Field renameField = ContainerAnvil.class.getDeclaredField("renameText");
                            renameField.setAccessible(true);
                            String renameText = (String) renameField.get(anvilContainer);
                            
                            if (renameText != null && !renameText.isEmpty()) {
                                currentNickname = renameText;
                            } else {
                                ItemStack input = anvilInv.getItem(0);
                                if (input != null && input.hasItemMeta() && input.getItemMeta().hasDisplayName()) {
                                    currentNickname = input.getItemMeta().getDisplayName();
                                }
                            }
                        } else {
                            ItemStack input = anvilInv.getItem(0);
                            if (input != null && input.hasItemMeta() && input.getItemMeta().hasDisplayName()) {
                                currentNickname = input.getItemMeta().getDisplayName();
                            }
                        }
                    } catch (Exception e) {
                        ItemStack input = anvilInv.getItem(0);
                        if (input != null && input.hasItemMeta() && input.getItemMeta().hasDisplayName()) {
                            currentNickname = input.getItemMeta().getDisplayName();
                        }
                    }
                    
                    currentNickname = org.bukkit.ChatColor.stripColor(currentNickname);
                    if (!currentNickname.equals(lastNickname)) {
                        lastNickname = currentNickname;
                        
                        if (!currentNickname.isEmpty() && !currentNickname.equals("Insira o usuário")) {
                            ItemStack result = new ItemStack(org.bukkit.Material.PAPER);
                            ItemMeta resultMeta = result.getItemMeta();
                            resultMeta.setDisplayName(currentNickname);
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
                    String rawNickname = meta.getDisplayName();
                    final String nickname = org.bukkit.ChatColor.stripColor(rawNickname);
                    
                    // Cancelar o evento ANTES de qualquer coisa
                    event.setCancelled(true);
                    
                    // Remover o item do slot de resultado IMEDIATAMENTE
                    anvil.setItem(2, null);
                    
                    // Limpar o cursor e atualizar inventário de forma síncrona
                    player.setItemOnCursor(null);
                    player.updateInventory();
                    
                    // Fechar o anvil imediatamente para evitar que o item seja movido
                    player.closeInventory();
                    
                    // Processar o nickname e limpar itens em uma task
                    Core.getPlatform().runSync(() -> {
                        // Limpar cursor
                        player.setItemOnCursor(null);
                        
                        // Verificar e remover qualquer papel do inventário que tenha o nickname
                        for (int i = 0; i < 36; i++) {
                            ItemStack item = player.getInventory().getItem(i);
                            if (item != null && item.getType() == org.bukkit.Material.PAPER) {
                                ItemMeta itemMeta = item.getItemMeta();
                                if (itemMeta != null && itemMeta.hasDisplayName()) {
                                    String itemName = org.bukkit.ChatColor.stripColor(itemMeta.getDisplayName());
                                    if (itemName.equals(nickname) || itemName.equals("Insira o usuário")) {
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
                                        if (itemName.equals(nickname) || itemName.equals("Insira o usuário")) {
                                            player.getInventory().setItem(i, null);
                                        }
                                    }
                                }
                            }
                            player.updateInventory();
                        }, 2L);
                        
                        if (isNotValidName(nickname)) {
                            player.sendMessage("§cVocê não pode usar o nickname de um jogador original.");
                            player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                            return;
                        }
                        
                        if (account.hasCooldown(Constant.NICK_CHANGE_COOLDOWN_KEY)) {
                            player.sendMessage("§cAguarde " + account.getFormattedCooldown(Constant.NICK_CHANGE_COOLDOWN_KEY) + " para trocar de nick novamente.");
                            player.playSound(player.getLocation(), MenuSound.ERROR.getSound(), 2f, 2.5f);
                            return;
                        }
                        
                        account.setFake(nickname);
                        new AccountNickChangeMessage(account, nickname).send();
                        
                        player.sendMessage("§aNickname alterado para: §f" + nickname);
                        player.playSound(player.getLocation(), MenuSound.SUCCESS.getSound(), 2f, 2.5f);
                        
                        if (!account.hasRank(RankType.HELPER))
                            account.setCooldown(Constant.NICK_CHANGE_COOLDOWN_KEY, TimeUnit.SECONDS.toMillis(15));
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

    private boolean isNotValidName(String name) {
        return !Validator.isNickname(name) || (Core.MOJANG_API.isPremium(name).equals(MojangApi.ResponseCode.DONE)
                || Core.getAccountController().isPresent(acc -> acc.getNickname().equalsIgnoreCase(name)));
    }
}

