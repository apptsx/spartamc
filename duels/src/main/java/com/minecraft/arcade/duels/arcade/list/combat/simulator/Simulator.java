package com.minecraft.arcade.duels.arcade.list.combat.simulator;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.member.list.duels.DuelMember;
import com.minecraft.core.member.context.stats.ArcadeStats;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.DateUtil;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import com.minecraft.core.util.list.serialization.Serialization;
import com.minecraft.arcade.duels.arcade.Arcade;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.arcade.duels.arcade.list.combat.simulator.kit.menu.KitMenu;
import com.minecraft.arcade.duels.arcade.list.combat.simulator.kit.model.Kit;
import com.minecraft.arcade.duels.arcade.objects.style.SidebarStyle;
import com.minecraft.arcade.duels.user.User;
import com.minecraft.arcade.duels.user.factory.list.SimulatorUser;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

@IgnoreEvent
public class Simulator extends Arcade {

    public Simulator(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.DUELS_SIMULATOR);

        setStyle(SidebarStyle.LATENCY);
    }

    @Override
    public void handleSidebar(User userObj) {
        SimulatorUser user = (SimulatorUser) userObj;

        Kit kit = user.getKit();

        Sidebar sidebar = user.getSidebar();

        Arena arena = user.getArena();

        DuelMember member = user.getMember();

        sidebar.clear();
        sidebar.addRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + user.getArena().getIdentifier());
        sidebar.blankRow();

        sidebar.addRow("mode", "Modo: §a" + arena.getModeName());

        if (arena.isPhase(RoomPhase.PLAYING)) {
            sidebar.blankRow();

            handleTeamStyle(arena, sidebar);

            if (user.isPlayer()) {
                ArcadeStats stats = member.getStats(getCategory());
                sidebar.addRow("winstreak", "Winstreak: §a" + Util.formatNumber(stats.getWinStreak()));
            } else
                sidebar.addRow("state", user.isVanish() ? "§cMODO VANISH" : "§eMODO ESPECTADOR");

        } else {
            sidebar.addRow("map", "Mapa: §a" + arena.getMap().getName());
            sidebar.blankRow();

            if (user.isPlayer()) {
                arena.createTimer(sidebar);

                sidebar.blankRow();
                sidebar.addRow("kit", "Kit: §a" + (kit != null ? kit.getName() : "Nenhum"));
            } else
                sidebar.addRow("state", user.isVanish() ? "§cMODO VANISH" : "§eMODO ESPECTADOR");

            sidebar.blankRow();
            sidebar.addRow("players", "Jogadores: §a" + arena.getMatchUsers().size() + "/" + arena.getMaxPlayers());
        }

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(user.getAccount());
    }

    @Override
    public void handleHotbar(Player player, Arena arena) {
        SimulatorUser user = (SimulatorUser) SimulatorUser.of(player.getUniqueId());

        if (arena.isValid(player)) {
            String base64 = user.getMember().getBase64(ArcadeCategory.DUELS_SIMULATOR);

            if (base64 != null)
                Serialization.sendInventoryToPlayerFromBase64(user.getAccount().player(), base64);

            BukkitUtil.sendArmor(player, BukkitUtil.ArmorType.IRON);

            // Enviar itens customizados
            Kit kit = user.getKit();

            if (kit != null && !kit.getItems().isEmpty()) {
                int initialSlot = 4;
                for (ItemStack item : kit.getItems()) {
                    if (item == null || item.getType() == Material.AIR) continue;

                    // Checar se o slot é válido antes de tentar acessá-lo
                    if (initialSlot >= player.getInventory().getSize()) break;

                    ItemStack index = player.getInventory().getItem(initialSlot);

                    if (index == null || index.getType() == Material.AIR || index.getType() == Material.MUSHROOM_SOUP)
                        player.getInventory().setItem(initialSlot, item);

                    initialSlot++;
                }
            }

        } else {
            handleDefaultHotbar(player);

            if (arena.isPreGame() && user.isPlayer())
                player.getInventory().setItem(0, Item.of(Material.CHEST, "§aSelecionar Kit")
                        .interact(event -> new KitMenu(event.getPlayer()).handle()));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        ItemStack item = event.getItemDrop().getItemStack();

        if (item == null || item.getType() == Material.AIR) return;

        if (isValid(player)) {
            SimulatorUser user = (SimulatorUser) SimulatorUser.of(player.getUniqueId());

            Kit kit = user.getKit();

            if (kit != null && kit.isKitItem(item))
                event.setCancelled(true);
        }
    }
}
