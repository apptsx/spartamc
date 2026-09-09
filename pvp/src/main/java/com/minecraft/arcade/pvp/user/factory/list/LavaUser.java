package com.minecraft.arcade.pvp.user.factory.list;

import com.minecraft.arcade.pvp.arcade.arena.Arena;
import com.minecraft.arcade.pvp.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.member.list.pvp.PvPMember;
import com.minecraft.core.member.list.pvp.stats.list.LavaStats;
import org.bukkit.entity.Player;

public class LavaUser extends User {

    public LavaUser(PvPMember member, Arena arena, Join join) {
        super(member, arena, join);
    }

    public void conclude(LavaStats.LavaLevel level) {
        Player player = getAccount().player();

        PvPMember member = getMember();

        LavaStats stats = member.getLavaStats();

        int xp = Core.RANDOM.ints(15, 35).findFirst().orElse(15),
                coins = Core.RANDOM.ints(70, 150).findFirst().orElse(70);

        stats.incrementLevel(level);
        member.updateStats(member.getPvPStats());
        member.addCoins(coins);

        player.sendMessage(new String[]{
                "§aO nível " + level.getColoredName() + "§a foi concluído.",
                "§b+" + xp + " XP's",
                "§6+" + coins + " Coins"});

        getArena().spawn(player);
    }
}
