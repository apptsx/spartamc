package com.minecraft.core.api.collectible.type.artifact;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.artifact.listener.ArtifactListener;
import com.minecraft.core.api.collectible.type.artifact.listener.TeleportBowListener;
import com.minecraft.core.api.collectible.type.trail.TrailListener;
import com.minecraft.core.api.item.Item;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
public abstract class ArtifactCollectible extends Collectible implements Listener {

    public ArtifactCollectible(String name, CollectibleRarity rarity, List<RankType> ranks, long releasedAt) {
        super(name, CollectibleCategory.ARTIFACT, rarity, ranks, releasedAt);

        Bukkit.getPluginManager().registerEvents(this, Core.getJavaPlugin());

        Core.getCollectibleController().save(this);
    }

    public abstract void handle(Player host);

    public ItemStack getArtifactItem() {
        if (getIcon() == null) return null;

        return Item.of(getIcon().getType()).name("§a" + getName() + " §7(Clique)");
    }

    public String getCooldownKey() {
        return "artifact-collectible-cooldown-" + getName().toLowerCase();
    }

    public boolean hasCooldown(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account == null) return false;

        if (account.hasCooldown(getCooldownKey())) {
            account.send("§cAguarde " + account.getFormattedCooldown(getCooldownKey()) + " para usar " + getName() + " novamente.");
            return true;
        }

        return false;
    }

    public void setCooldown(Player player, long time) {
        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account == null) return;

        if (!account.isStaffer())
            account.setCooldown(getCooldownKey(), time);
    }

    public static void handle() {
        Bukkit.getPluginManager().registerEvents(new ArtifactListener(), Core.getJavaPlugin());
        Bukkit.getPluginManager().registerEvents(new TrailListener(), Core.getJavaPlugin());
        Bukkit.getPluginManager().registerEvents(new TeleportBowListener(), Core.getJavaPlugin());
    }
}
