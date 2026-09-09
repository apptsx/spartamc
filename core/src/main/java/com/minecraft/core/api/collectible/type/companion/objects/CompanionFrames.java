package com.minecraft.core.api.collectible.type.companion.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class CompanionFrames {

    private static final double MIN_ANGLE = 6.283185307179586D;

    private final int quality;

    private final Map<Integer, List<CompanionAnimation>>
            keyFrames = new HashMap<>(), idleKeyFrames = new HashMap<>(), frames = new HashMap<>(), idleFrames = new HashMap<>();

    public void createKeyFrame(int index, List<CompanionAnimation> animations) {
        this.keyFrames.put(index, animations);
    }

    public void addFrames(int part, int... keyFrames) {
        for (int keyFrame : keyFrames) {
            this.addFrame(part, keyFrame);
        }
    }

    public void addFrame(int part, int keyFrame) {
        CompanionAnimation animationKeyFrame = this.keyFrames.get(keyFrame).get(part);
        CompanionAnimation animationFrame =
                this.frames.get(part) == null || this.frames.get(part).isEmpty() ? this.keyFrames.get(0).get(part) : this.frames.get(part).get(this.frames.get(part).size() - 1);
        this.frames.computeIfAbsent(part, list -> new ArrayList<>()).addAll(this.getFrames(animationFrame, animationKeyFrame));
    }

    public void createIdleKeyFrame(int index, List<CompanionAnimation> animations) {
        this.idleKeyFrames.put(index, animations);
    }

    public void addIdleFrames(int part, int... keyFrames) {
        for (int keyFrame : keyFrames) {
            this.addIdleFrame(part, keyFrame);
        }
    }

    public void addIdleFrame(int part, int keyFrame) {
        CompanionAnimation animationKeyFrame = this.idleKeyFrames.get(keyFrame).get(part);
        CompanionAnimation animationFrame = this.idleFrames.get(part) == null || this.idleFrames.get(part).isEmpty() ?
                this.idleKeyFrames.get(0).get(part) :
                this.idleFrames.get(part).get(this.idleFrames.get(part).size() - 1);
        this.idleFrames.computeIfAbsent(part, list -> new ArrayList<>()).addAll(this.getFrames(animationFrame, animationKeyFrame));
    }

    private List<CompanionAnimation> getFrames(CompanionAnimation animationFrame, CompanionAnimation animationKeyFrame) {
        return this.createEulerList(animationFrame.getAngle(), animationKeyFrame.getAngle()).stream()
                .map(eulerAngle -> new CompanionAnimation(eulerAngle, animationFrame.getName(), animationFrame.getType())).collect(Collectors.toList());
    }

    private List<EulerAngle> createEulerList(EulerAngle angle1, EulerAngle angle2) {
        List<EulerAngle> eulerAngles = new ArrayList<>();

        int maxAmount = this.calculateMaxAmount(angle1, angle2);
        double x = this.calculateShortestAngle(angle1.getX(), angle2.getX()) / maxAmount;
        double y = this.calculateShortestAngle(angle1.getY(), angle2.getY()) / maxAmount;
        double z = this.calculateShortestAngle(angle1.getZ(), angle2.getZ()) / maxAmount;

        double x2 = angle2.getX(), x1 = angle1.getX();
        double y2 = angle2.getY(), y1 = angle1.getY();
        double z2 = angle2.getZ(), z1 = angle1.getZ();

        int equalsX = x2 == x1 ? 1 : 0;
        int equalsY = y2 == y1 ? 1 : 0;
        int equalsZ = z2 == z1 ? 1 : 0;

        for (int i = 0; i < maxAmount; i++) {
            if ((x1 == x2 && equalsX == 0) || (y1 == y2 && equalsY == 0) || (z1 == z2 && equalsZ == 0)) {
                break;
            }

            x1 += x;
            y1 += y;
            z1 += z;

            eulerAngles.add(new EulerAngle(x1, y1, z1));
        }

        return eulerAngles;
    }

    private double absMin(double abs1, double abs2) {
        return Math.abs(abs1) > Math.abs(abs2) ? abs2 : abs1;
    }

    private double calculateShortestAngle(double angle1, double angle2) {
        angle1 %= MIN_ANGLE;
        angle2 %= MIN_ANGLE;
        double diff = angle2 - angle1;
        double absMin = diff - MIN_ANGLE;
        double absMax = diff + MIN_ANGLE;
        return this.absMin(this.absMin(diff, absMin), absMax);
    }

    private int calculateMaxAmount(EulerAngle angle1, EulerAngle angle2) {
        double max = 0.0D;
        if (Math.abs(angle2.getX() - angle1.getX()) > max) {
            max = (int) Math.abs(angle2.getX() - angle1.getX()) + (Math.abs(angle2.getX() - angle1.getX()) - (int) Math.abs(angle2.getX() - angle1.getX()) > 0.0D ? 1 : 0);
        }
        if (Math.abs(angle2.getY() - angle1.getY()) > max) {
            max = (int) Math.abs(angle2.getY() - angle1.getY()) + (Math.abs(angle2.getY() - angle1.getY()) - (int) Math.abs(angle2.getY() - angle1.getY()) > 0.0D ? 1 : 0);
        }
        if (Math.abs(angle2.getZ() - angle1.getZ()) > max) {
            max = (int) Math.abs(angle2.getZ() - angle1.getZ()) + (Math.abs(angle2.getZ() - angle1.getZ()) - (int) Math.abs(angle2.getZ() - angle1.getZ()) > 0.0D ? 1 : 0);
        }

        return (int) max * this.quality;
    }
}
