package com.minecraft.core.api.collectible.nms.v1_8_R3.module.firework;

import net.minecraft.server.v1_8_R3.EntityFireworks;
import net.minecraft.server.v1_8_R3.World;

public class CustomEntityFirework extends EntityFireworks {

    private boolean gone;

    public CustomEntityFirework(World world) {
        super(world);
        this.gone = false;
    }

    public void t_() {
        if (!this.gone) {
            if (!this.world.isClientSide) {
                this.gone = true;
                this.world.broadcastEntityEffect(this, (byte) 17);
                this.die();
            }
        }
    }
}