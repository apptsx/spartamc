package com.minecraft.core.api.collectible.type.companion.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.core.api.collectible.type.companion.objects.CompanionAnimation;
import com.minecraft.core.api.collectible.util.math.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class MagicDragonCompanion extends CompanionCollectible {

    public MagicDragonCompanion() {
        super("Dragão Mágico", CollectibleRarity.MYTHICAL, new ArrayList<>(), 5, 1726514967370L,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzA2ZjQ3ZDNiNzUxNTdjNDU1Njk0MjE2YWU0OGM0NjE5ZGJkNTZjMTMxOWFiYTdiYTY2OWU0NTQxMDkwIn19fQ==");

        this.frames.createKeyFrame(0, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(90, 70, 165), "l_wing1", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 110, 165), "l_wing2", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 70, 180), "l_wing3", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 110, 180), "l_wing4", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 90, 164), "l_wing5", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 90, 178), "l_wing6", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -70, 15), "r_wing1", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -110, 15), "r_wing2", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -70, 0), "r_wing3", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -110, 0), "r_wing4", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -90, 16), "r_wing5", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -90, 2), "r_wing6", CompanionAnimation.MovementType.HEAD)));
        this.frames.createKeyFrame(1, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(90, 70, 150), "l_wing1", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 110, 150), "l_wing2", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 70, 165), "l_wing3", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 110, 165), "l_wing4", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 90, 149), "l_wing5", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 90, 163), "l_wing6", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -70, 30), "r_wing1", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -110, 30), "r_wing2", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -70, 15), "r_wing3", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -110, 15), "r_wing4", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -90, 31), "r_wing5", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -90, 17), "r_wing6", CompanionAnimation.MovementType.HEAD)));
        this.frames.createKeyFrame(2, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(90, 70, 135), "l_wing1", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 110, 135), "l_wing2", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 70, 150), "l_wing3", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 110, 150), "l_wing4", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 90, 134), "l_wing5", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 90, 148), "l_wing6", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -70, 45), "r_wing1", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -110, 45), "r_wing2", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -70, 30), "r_wing3", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -110, 30), "r_wing4", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -90, 46), "r_wing5", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -90, 32), "r_wing6", CompanionAnimation.MovementType.HEAD)));
        this.frames.createKeyFrame(3, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(90, 70, 150), "l_wing1", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 110, 150), "l_wing2", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 70, 165), "l_wing3", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 110, 165), "l_wing4", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 90, 149), "l_wing5", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 90, 163), "l_wing6", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -70, 30), "r_wing1", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -110, 30), "r_wing2", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -70, 15), "r_wing3", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -110, 15), "r_wing4", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -90, 31), "r_wing5", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -90, 17), "r_wing6", CompanionAnimation.MovementType.HEAD)));

        this.frames.addFrames(0, 0, 1, 2, 3);
        this.frames.addFrames(1, 0, 1, 2, 3);
        this.frames.addFrames(2, 0, 1, 2, 3);
        this.frames.addFrames(3, 0, 1, 2, 3);
        this.frames.addFrames(4, 0, 1, 2, 3);
        this.frames.addFrames(5, 0, 1, 2, 3);
        this.frames.addFrames(6, 0, 1, 2, 3);
        this.frames.addFrames(7, 0, 1, 2, 3);
        this.frames.addFrames(8, 0, 1, 2, 3);
        this.frames.addFrames(9, 0, 1, 2, 3);
        this.frames.addFrames(10, 0, 1, 2, 3);
        this.frames.addFrames(11, 0, 1, 2, 3);

        this.frames.createIdleKeyFrame(0, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(90, 70, 165), "l_wing1", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 110, 165), "l_wing2", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 70, 180), "l_wing3", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 110, 180), "l_wing4", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 90, 164), "l_wing5", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 90, 178), "l_wing6", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -70, 15), "r_wing1", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -110, 15), "r_wing2", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -70, 0), "r_wing3", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -110, 0), "r_wing4", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -90, 16), "r_wing5", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -90, 2), "r_wing6", CompanionAnimation.MovementType.HEAD)));
        this.frames.createIdleKeyFrame(1, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(90, 70, 150), "l_wing1", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 110, 150), "l_wing2", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 70, 165), "l_wing3", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 110, 165), "l_wing4", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 90, 149), "l_wing5", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 90, 163), "l_wing6", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -70, 30), "r_wing1", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -110, 30), "r_wing2", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -70, 15), "r_wing3", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -110, 15), "r_wing4", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -90, 31), "r_wing5", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -90, 17), "r_wing6", CompanionAnimation.MovementType.HEAD)));
        this.frames.createIdleKeyFrame(2, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(90, 70, 135), "l_wing1", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 110, 135), "l_wing2", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 70, 150), "l_wing3", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 110, 150), "l_wing4", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 90, 134), "l_wing5", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 90, 148), "l_wing6", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -70, 45), "r_wing1", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -110, 45), "r_wing2", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -70, 30), "r_wing3", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -110, 30), "r_wing4", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -90, 46), "r_wing5", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -90, 32), "r_wing6", CompanionAnimation.MovementType.HEAD)));
        this.frames.createIdleKeyFrame(3, Arrays.asList(
                new CompanionAnimation(MathUtils.angle(90, 70, 150), "l_wing1", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 110, 150), "l_wing2", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 70, 165), "l_wing3", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 110, 165), "l_wing4", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 90, 149), "l_wing5", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(90, 90, 163), "l_wing6", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -70, 30), "r_wing1", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -110, 30), "r_wing2", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -70, 15), "r_wing3", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -110, 15), "r_wing4", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -90, 31), "r_wing5", CompanionAnimation.MovementType.HEAD),
                new CompanionAnimation(MathUtils.angle(-90, -90, 17), "r_wing6", CompanionAnimation.MovementType.HEAD)));

        this.frames.addIdleFrames(0, 0, 1, 2, 3);
        this.frames.addIdleFrames(1, 0, 1, 2, 3);
        this.frames.addIdleFrames(2, 0, 1, 2, 3);
        this.frames.addIdleFrames(3, 0, 1, 2, 3);
        this.frames.addIdleFrames(4, 0, 1, 2, 3);
        this.frames.addIdleFrames(5, 0, 1, 2, 3);
        this.frames.addIdleFrames(6, 0, 1, 2, 3);
        this.frames.addIdleFrames(7, 0, 1, 2, 3);
        this.frames.addIdleFrames(8, 0, 1, 2, 3);
        this.frames.addIdleFrames(9, 0, 1, 2, 3);
        this.frames.addIdleFrames(10, 0, 1, 2, 3);
        this.frames.addIdleFrames(11, 0, 1, 2, 3);
    }
}
