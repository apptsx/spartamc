package com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.trap;

import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.trap.enums.TrapCategory;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.trap.enums.TrapType;
import com.minecraft.core.api.item.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
@AllArgsConstructor
public class Trap {

    private final TrapType type;
    private final TrapCategory category;

    public void execute(Player executor, Team team) {
        type.getAction().execute(executor, team);
    }

    public String getName() {
        return type.getName();
    }

    public Item getIcon() {
        return type.getIcon();
    }

    public int getCost() {
        return type.getCost();
    }

    public List<String> getDescription() {
        return type.getDescription();
    }

    public int id() {
        return category.ordinal() + 1;
    }
}
