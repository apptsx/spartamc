package com.minecraft.arcade.bedwars.listener;

import com.minecraft.core.member.list.bedwars.objects.ability.Ability;
import com.minecraft.core.member.list.bedwars.objects.ability.enums.AbilityType;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.AsyncUpdateEvent;
import com.minecraft.core.bukkit.user.UserModel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class AbilityListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        User user = (User) User.of(player.getUniqueId());

        if (user == null || user.getArena() == null) return;

        if (user.getContext().hasAbility(AbilityType.PERMANENT_SWORD)) {
            ItemStack sword = player.getInventory().getItem(0);
            if (sword != null && sword.getType().name().contains("SWORD")) {
                user.getContext().setPermanentSword(sword.clone());
            }
        }
    }

    @EventHandler
    public void onUpdate(AsyncUpdateEvent event) {
        if (!event.isType(UpdateType.SECOND)) return;

        Core.getPlatform().runSync(() -> {
            for (UserModel userModel : UserModel.list(user -> true)) {
                if (!(userModel instanceof User user)) continue;
                if (user == null || !user.inState(ArcadeState.ALIVE)) continue;

                Player player = user.getPlayer();
                if (player == null || !player.isOnline()) continue;

                // Verificar se a habilidade NO_FALL expirou
                if (user.getContext().hasAbility(AbilityType.NO_FALL)) {
                    Ability noFallAbility = user.getContext().getAbility(AbilityType.NO_FALL);
                    if (noFallAbility != null && noFallAbility.getMatchStartTime() >= 0) {
                        boolean isCurrentlyActive = noFallAbility.isActive();
                        boolean wasActive = user.getContext().isNoFallWasActive();
                        
                        // Se estava ativa antes e agora não está mais, significa que expirou
                        if (wasActive && !isCurrentlyActive) {
                            player.sendMessage("§c[Queda nula] §eSua habilidade foi desativada.");
                        }
                        
                        // Atualizar o estado atual
                        user.getContext().setNoFallWasActive(isCurrentlyActive);
                    }
                } else {
                    // Se não tem mais a habilidade, resetar o estado
                    user.getContext().setNoFallWasActive(false);
                }

                // Restaurar espada permanente
                if (user.getContext().hasAbility(AbilityType.PERMANENT_SWORD)) {
                    if (user.getContext().hasPermanentSword()) {
                        ItemStack currentSword = player.getInventory().getItem(0);
                        if (currentSword == null || !currentSword.getType().name().contains("SWORD")) {
                            player.getInventory().setItem(0, user.getContext().getPermanentSword().clone());
                            player.updateInventory();
                        }
                    }
                }
            }
        });
    }
}

