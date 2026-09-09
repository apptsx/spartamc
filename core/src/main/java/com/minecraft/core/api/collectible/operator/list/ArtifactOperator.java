package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ArtifactOperator extends CollectibleOperator {

    public ArtifactOperator(Player host, ArtifactCollectible collectible) {
        super(host, CollectibleCategory.ARTIFACT);

        this.handle(collectible);
    }

    @Override
    public void handle(Collectible collectible) {
        ArtifactCollectible artifact = (ArtifactCollectible) collectible;

        setCollectible(artifact);

        PlayerInventory inventory = getHost().getInventory();

        ItemStack item = artifact.getArtifactItem();

        if (item != null) {
            if (inventory.getItem(5) != null) {
                ItemStack currentItem = inventory.getItem(5);
                if (!BukkitUtil.isSimilarStack(currentItem, item)) {
                    inventory.setItem(5, artifact.getArtifactItem());
                }
            } else {
                inventory.setItem(5, artifact.getArtifactItem());
            }
        }

        getHost().updateInventory();
    }

    @Override
    public void cancel() {
        if (getCollectible() != null && getHost() != null) {
            ArtifactCollectible artifact = (ArtifactCollectible) getCollectible();

            if (artifact.getArtifactItem() != null) {
                Inventory inventory = getHost().getInventory();

                for (ItemStack content : inventory.getContents())
                    if (content != null && BukkitUtil.isSimilarStack(content, artifact.getArtifactItem()))
                        inventory.remove(content);

                getHost().updateInventory();
            }
        }
    }
}
