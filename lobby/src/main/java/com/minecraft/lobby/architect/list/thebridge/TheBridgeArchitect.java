package com.minecraft.lobby.architect.list.thebridge;

import com.minecraft.core.Core;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.api.npc.type.server.NpcServer;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.member.list.thebridge.TheBridgeMember;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import com.minecraft.lobby.architect.Architect;
import com.minecraft.lobby.user.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;

public class TheBridgeArchitect extends Architect {

    public TheBridgeArchitect() {
        super(ServerType.HUB_THE_BRIDGE);
    }

    @Override
    public void join(Player player) {
        TheBridgeMember member = Core.getTheBridgeData().of(player.getUniqueId(), true);

        if (member == null)
            member = Core.getTheBridgeData().save(new TheBridgeMember(player.getUniqueId(), player.getName()));

        Core.getMemberController().save(member);
        Core.getTheBridgeData().cancelExpiration(player.getUniqueId());

        User user = (User) User.of(player.getUniqueId());
        super.join(player);
    }

    @Override
    public void quit(Player player) {
        super.quit(player);

        Core.getTheBridgeData().startExpiration(player.getUniqueId());
    }

    @Override
    public void handleSidebar(User user) {
        TheBridgeMember member = user.getMember(TheBridgeMember.class);

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle("§b§lTHE BRIDGE");
        sidebar.blankRow();

        sidebar.addRow("solo", "§eSolo:");
        sidebar.addRow("solo_wins", " Vitórias: §a" + Util.formatNumber(member.getStats(ArcadeCategory.THE_BRIDGE_SOLO).getWins()));
        sidebar.addRow("solo_winstreak", " Winstreak: §a" + Util.formatNumber(member.getStats(ArcadeCategory.THE_BRIDGE_SOLO).getWinStreak()));

        sidebar.blankRow();
        sidebar.addRow("players", "Players: §b" + Util.formatNumber(Core.getServerData().getOnlinePlayers()));

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(user.getAccount());
    }

    @Override
    public void handleHotbar(Player player) {
        super.handleHotbar(player);

        PlayerInventory inv = player.getInventory();

//        inv.setItem(2, Item.of(Material.EMERALD, "§aMenu do " + ServerType.THE_BRIDGE.getName()).interact(event -> new TheBridgeNavigationMenu(event.getPlayer(), ArcadeCategory.THE_BRIDGE_SOLO, null).handle()));
    }

    @Override
    public void handleEntities() {
        super.handleEntities();

        Core.getLogger().info("[TheBridgeArchitect] Carregando NPCs no mundo '" + getWorld().getName() + "' com " + getLocations().size() + " locations configuradas.");
        
        Location soloLoc = getLocation("npc_solo");

        Core.getLogger().info("[TheBridgeArchitect] Locations encontradas: solo=" + (soloLoc != null));

        if (soloLoc != null) {
            handleArcadeNpc(ArcadeCategory.THE_BRIDGE_SOLO, soloLoc,
                    "ewogICJ0aW1lc3RhbXAiIDogMTYxOTI5NDc0NjQ5NiwKICAicHJvZmlsZUlkIiA6ICJjMGYzYjI3YTUwMDE0YzVhYjIxZDc5ZGRlMTAxZGZlMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDEzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2VmNDFlNDIwMmI4YTFhMTMzZjBiYjVlZWFhM2IyZTA2NmJiZWYzOTZlNTkxMTVkODg2OTliZTE5NTlkY2M3MTciCiAgICB9CiAgfQp9",
                    "RD0qdkGjt0Dl7VD2zq9gM0BuZPcRxrXyQG8YEXxyZVdw6pbDt/v51PeONCG42RutEGjCWgsu31UWnqjEDMXKT8dAfELUtZtJmEkYosm73037oLFY8jMvJIwrw+sOIOVIQXg+jzyo6/ySAxj217yLDA7eXfWYIPWYD8Nnd/CfojlcsgrKtYytSoy5py4g+AjX5P+v15kEHP/NxGxmULo7Fa4EFUInoNh0B0ZEi7fDa66LnnLekaeCDdD+PqWihnosq4r88/GJSPgRGJbfLTEwN/WHZqGMVoOgKEuSu7x3emOQFU4/Hw26KdOtHdFXAFNNwVeDjD8JU9+Sd/bpD74K9zjDNJaPSgiGld3San0vQHEUEstF8CffpUCNwR5EdX3fRAaNmPPG2OaNre0IylWrH6FeivZ6iDQjnKV2zNJh9+0GPtmwiwjwO28Sn4jhAbmo90oWiZbxuXbem/x4UEpKCrgeYo+gNkwW1+fYZszNwca9IPb+YSxR/K2WyzYLaA5NYcevAwuuPhbVFRBa0fVySN1KAav4ReByBQMV+olgDZNJWqBxfK9h/W1WFASp0wwLDD5GFWqeEoUoPfFMXOrRDjET6uLXl7tsi7iEcdWtrwxAh4b0D3siYQ6cNaoYNuSMa1mpsDBaPYGbKwXJNl+ViTSqJQKPcKvCDMhCuiVe9lM=");
        }
    }

    @Override
    public void handleArcadeNpc(ArcadeCategory arcade, Location location, String value, String signature) {
        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        npc.setContact(true);
//        npc.setAction((player, action) -> new TheBridgeNavigationMenu(player, arcade, null).handle());

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(arcade.getId(), location);

        hologram.setText(Arrays.asList(
                "§6§l" + arcade.getName().toUpperCase(),
                "§e" + Util.formatNumber(arcade.getPlayingNow()) + " jogando."
        ));
    }
}
