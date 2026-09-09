package com.minecraft.arcade.bedwars.structure.team.objects.upgrade;

import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.armor.ArmorProtection;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.haste.Haste;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.sword.Sword;
import com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.trap.Trap;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;

@Getter
@Setter
public class TeamUpgrade {

    private Haste haste;
    private Sword sword;

    private ArmorProtection armor;

    private boolean autoRegeneration = false;
    private boolean quickRespawn = false;
    private boolean permanentSword = false;

    private final Queue<Trap> traps = new LinkedList<>();
}
