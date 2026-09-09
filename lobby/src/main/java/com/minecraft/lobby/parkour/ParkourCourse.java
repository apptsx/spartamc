package com.minecraft.lobby.parkour;

import org.bukkit.Location;

public class ParkourCourse {
    
    private final String name;
    private final double startX, startY, startZ;
    private final double[] checkpointX, checkpointY, checkpointZ;
    private final double finishX, finishY, finishZ;
    private double bestTime;
    
    public ParkourCourse(String name, Location start, Location[] checkpoints, Location finish) {
        this.name = name;
        this.startX = start.getX();
        this.startY = start.getY();
        this.startZ = start.getZ();
        
        this.checkpointX = new double[checkpoints.length];
        this.checkpointY = new double[checkpoints.length];
        this.checkpointZ = new double[checkpoints.length];
        for (int i = 0; i < checkpoints.length; i++) {
            this.checkpointX[i] = checkpoints[i].getX();
            this.checkpointY[i] = checkpoints[i].getY();
            this.checkpointZ[i] = checkpoints[i].getZ();
        }
        
        this.finishX = finish.getX();
        this.finishY = finish.getY();
        this.finishZ = finish.getZ();
        
        this.bestTime = 0.0;
    }
    
    public String getName() {
        return name;
    }
    
    public Location getStart() {
        return new Location(null, startX, startY, startZ);
    }
    
    public Location getCheckpoint(int number) {
        if (number >= 1 && number <= checkpointX.length) {
            return new Location(null, checkpointX[number - 1], checkpointY[number - 1], checkpointZ[number - 1]);
        }
        return getStart();
    }
    
    public Location getFinish() {
        return new Location(null, finishX, finishY, finishZ);
    }
    
    public int getTotalCheckpoints() {
        return checkpointX.length;
    }
    
    public double getBestTime() {
        return bestTime;
    }
    
    public void setBestTime(double bestTime) {
        if (this.bestTime == 0 || bestTime < this.bestTime) {
            this.bestTime = bestTime;
        }
    }
    
    public boolean isFinishCheckpoint(int checkpoint) {
        return checkpoint >= checkpointX.length;
    }
}
