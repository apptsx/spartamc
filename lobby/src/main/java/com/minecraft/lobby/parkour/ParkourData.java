package com.minecraft.lobby.parkour;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ParkourData {
    
    private String courseName;
    private int currentCheckpoint;
    private long startTime;
    private ItemStack[] savedContents;
    private ItemStack[] savedArmor;
    
    public ParkourData(String courseName) {
        this.courseName = courseName;
        this.currentCheckpoint = 0;
        this.startTime = System.currentTimeMillis();
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public int getCurrentCheckpoint() {
        return currentCheckpoint;
    }
    
    public void setCurrentCheckpoint(int currentCheckpoint) {
        this.currentCheckpoint = currentCheckpoint;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public void saveInventory(Player player) {
        this.savedContents = player.getInventory().getContents().clone();
        this.savedArmor = player.getInventory().getArmorContents().clone();
    }
    
    public void restoreInventory(Player player) {
        player.getInventory().clear();
        if (savedContents != null) {
            player.getInventory().setContents(savedContents);
        }
        if (savedArmor != null) {
            player.getInventory().setArmorContents(savedArmor);
        }
        player.updateInventory();
    }
}
