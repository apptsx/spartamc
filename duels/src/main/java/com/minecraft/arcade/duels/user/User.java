package com.minecraft.arcade.duels.user;

import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.arcade.duels.arcade.Arcade;
import com.minecraft.arcade.duels.arcade.arena.Arena;
import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.member.list.duels.DuelMember;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

@Getter
@Setter
@ToString
public class User extends UserModel {

    private DuelMember member;

    private Arena arena;
    private TeamPreset team;

    /* Extra */
    private String inventoryBase64 = "";

    private int kills, assists;
    private int respawnTime, gainXp;

    public User(DuelMember member, Arena arena, Join join) {
        super(member.getId(), join);

        this.member = member;
        this.arena = arena;
    }

    @Override
    public void handle() {
        Account account = getAccount();

        Player player = account.player();

        if (arena != null) {
            setSidebar(new Sidebar(player, "§6§lPRACTICE"));

            arena.join(player);
        }
    }

    public boolean isProtected() {
        if (!isPlayer() || !inState(ArcadeState.ALIVE)) return true;

        return !arena.isPhase(RoomPhase.PLAYING);
    }

    public void setState(ArcadeState state) {
        super.setState(state);

        Player player = getAccount().player();

        switch (state) {
            case ALIVE: {
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
                player.setFlying(false);
                break;
            }

            case DEAD: {
                setTag(Tag.SPECTATOR);

                player.addPotionEffects(Arrays.asList(
                        new PotionEffect(PotionEffectType.BLINDNESS, 45, 3),
                        new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 3)));

                player.setFlying(true);

                player.playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 1.0f, 1.0f);
                break;
            }

            case RESPAWN: {
                int respawnTime = getArcade().isCategory(ArcadeCategory.DUELS_THE_BRIDGE)
                        ? 3 : 5;

                setRespawnTime(respawnTime);

                player.getInventory().clear();
                getArcade().handleDefaultHotbar(player);

                player.addPotionEffects(Arrays.asList(
                        new PotionEffect(PotionEffectType.BLINDNESS, 45, 3),
                        new PotionEffect(PotionEffectType.INVISIBILITY, respawnTime == 3 ? 90 : 110, 3)));

                player.setFlying(true);

                player.playSound(player.getLocation(), Sound.BLAZE_DEATH, 1.0f, 1.0f);
                break;
            }
        }
    }

    public Arcade getArcade() {
        return arena.getArcade();
    }
}
