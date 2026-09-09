package com.minecraft.core.bukkit.event.type.account;

import com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class AccountProfileChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final ProfileType oldProfile;
    private final ProfileType newProfile;

    public AccountProfileChangeEvent(Player player, ProfileType oldProfile, ProfileType newProfile) {
        this.player = player;
        this.oldProfile = oldProfile;
        this.newProfile = newProfile;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
