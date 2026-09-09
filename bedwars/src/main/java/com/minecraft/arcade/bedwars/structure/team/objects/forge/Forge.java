package com.minecraft.arcade.bedwars.structure.team.objects.forge;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.structure.generator.objects.level.GeneratorLevel;
import com.minecraft.arcade.bedwars.structure.generator.objects.type.forge.ForgeGenerator;
import com.minecraft.arcade.bedwars.structure.generator.objects.type.forge.list.EmeraldForge;
import com.minecraft.arcade.bedwars.structure.generator.objects.type.forge.list.GoldForge;
import com.minecraft.arcade.bedwars.structure.generator.objects.type.forge.list.IronForge;
import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class Forge {

    private final Arena arena;
    private final Team team;

    private GeneratorLevel level = GeneratorLevel.NONE;

    private final List<ForgeGenerator> generators = new ArrayList<>();

    public boolean canPurchase(Player player) {
        if (isHighest()) return false;

        return BukkitUtil.getItemAmount(player, Material.DIAMOND) >= getNextCost();
    }

    public int getNextCost() {
        GeneratorLevel next = GeneratorLevel.values()[level.ordinal() + 1];

        return arena.getSlot().ordinal() >= Slot.TRIO.ordinal() ? next.getCost() * 2 : next.getCost();
    }

    public boolean isLevel(GeneratorLevel level) {
        return this.level.ordinal() >= level.ordinal();
    }

    public boolean isHighest() {
        return isLevel(GeneratorLevel.FOUR);
    }

    public void spawn() {
        generators.addAll(Arrays.asList(
                new IronForge(arena, team),
                new GoldForge(arena, team)
        ));
    }

    public void upgrade() {
        GeneratorLevel level = getLevel();

        if (isHighest()) return;

        GeneratorLevel next = GeneratorLevel.values()[level.ordinal() + 1];

        // Adicionar gerador de esmeraldas
        if (next.equals(GeneratorLevel.THREE))
            generators.add(new EmeraldForge(arena, team));

        for (ForgeGenerator generator : generators) {
            if (!next.equals(GeneratorLevel.THREE)) {
                int percentage = next.equals(GeneratorLevel.ONE) ? 50 : next.equals(GeneratorLevel.TWO) ? 100 : 200;
                double productionFactor = 1 + (percentage / 100.0); // fator de aumento de produção

                long time = generator.getOriginTime();
                long newTime = (long) (time / productionFactor); // diminuir o tempo de geração

                generator.setOriginTime(newTime);
                generator.setUpdateAt(newTime);
            }

            generator.setLevel(next);
        }

        this.level = next;
    }
}
