package com.minecraft.core.arcade.room.event;

import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@RequiredArgsConstructor
public class ArenaSearchedEvent extends Event implements Cancellable {

    private final Account sender;
    private final Room arena;

    private final ArcadeRouteContext route;

    @Setter
    private boolean cancelled = false;

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
