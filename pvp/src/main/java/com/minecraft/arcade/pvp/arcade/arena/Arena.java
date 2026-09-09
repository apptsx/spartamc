package com.minecraft.arcade.pvp.arcade.arena;

import com.minecraft.arcade.pvp.arcade.Arcade;
import com.minecraft.arcade.pvp.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.bukkit.BukkitCore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Arena extends Room {

    public Arena(int id, Arcade arcade, Map map, Slot slot) {
        super(id, arcade, map, slot);
    }

    public Arcade getArcade() {
        return (Arcade) super.getArcade();
    }

    @Override
    public World getWorld() {
        return Bukkit.getWorld(getArcade().getName() + "-" + getId());
    }

    @Override
    public void join(Player player) {
        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();

        Join join = user.getJoin();

        Arcade arcade = getArcade();

        player.updateInventory();

        account.setRoute(this, join);
        arcade.join(user);

        account.title("");

        if (join.equals(Join.PLAYER)) {
            BukkitCore.getManager().getCooldown().resetCooldown(player);

            hideAndShow(player);
        } else
            handleVanish(player);

        getPlayers().add(player);
    }

    @Override
    public void quit(Player player) {
        getPlayers().remove(player);

        BukkitCore.getManager().getHologram().removeClients(player);
    }

    public void spawn(Player player) {
        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        user.setProtected(true);
        player.updateInventory();

        BukkitCore.getManager().getCooldown().resetCooldown(player);

        Arcade arcade = user.getArcade();

        arcade.handleSidebar(user);
        arcade.buildHotbar(player);

        player.teleport(getLocation("spawn"));
    }

    protected void hideAndShow(Player player) {
        Core.getPlatform().runSync(() -> {
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (player.equals(target)) continue;

                if (!player.getWorld().equals(target.getWorld()) || !target.getWorld().equals(player.getWorld())) {
                    player.hidePlayer(target);
                    target.hidePlayer(player);
                } else {
                    player.showPlayer(target);
                    target.showPlayer(player);
                }
            }
        }, 5);
    }
}
