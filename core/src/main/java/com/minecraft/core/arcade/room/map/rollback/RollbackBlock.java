package com.minecraft.core.arcade.room.map.rollback;

import com.minecraft.core.arcade.room.map.rollback.pattern.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.block.Block;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class RollbackBlock {

    private final Block block;

    private Pattern pattern;
    private RollbackType type;

    public enum RollbackType {
        REMOVE_BLOCK,
        PLACE_BLOCK
    }

    public boolean isType(RollbackType type) {
        return this.type.equals(type);
    }
}