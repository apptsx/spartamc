package com.minecraft.core.bukkit.menu.account.skin.library;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.skin.Skin;
import com.minecraft.core.api.skin.library.SkinLibrary;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.api.protocol.ProtocolHandler;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

@Setter
public class SkinVariationMenu extends Menu {

    private final Account account;
    private final SkinLibrary skinLibrary;
    private final String baseSkinName;

    public SkinVariationMenu(Player player, SkinLibrary skinLibrary, Menu last) {
        super(player, "Variações - " + skinLibrary.getSkin().getDisplayName(), last, 4);

        this.account = Core.getAccountController().of(player.getUniqueId());
        this.skinLibrary = skinLibrary;
        this.baseSkinName = skinLibrary.getSkin().getDisplayName();
    }

    @Override
    public void handle() {
        clear();

        List<SkinLibrary.SkinVariation> variations = SkinLibrary.getVariations(skinLibrary);

        if (variations.isEmpty()) {
            addItem(22, Item.of(Material.WEB, "§cNão há variações disponíveis..."));
            if (isReturnable())
                addBackButton(getTotalSlots() - 6);
            display();
            return;
        }

        int validCount = variations.size();
        int startSlot = 13 - (validCount / 2);
        int currentSlot = startSlot;

        for (SkinLibrary.SkinVariation variation : variations) {
            Skin variationSkin = variation.toSkin(baseSkinName);
            boolean using = account.hasSkin(variationSkin);

            addItem(currentSlot, Item.of(Material.SKULL_ITEM, 3, "§a" + variation.getName(),
                            "§8" + skinLibrary.getCategory().getName(),
                            "",
                            !using ? "§eClique para escolher!" : "§cJá escolhida.")
                    .skullByBase64(variation.getValue())
                    .click(event -> {
                        if (using) {
                            sound(MenuSound.ERROR);
                            account.send("§cVocê já está usando esta variação.");
                            return;
                        }

                        close();
                        sound(MenuSound.SUCCESS);

                        account.setSkin(variationSkin);
                        ProtocolHandler.changePlayerSkin(getPlayer(), variationSkin);

                        account.send("§aVocê selecionou a variação §7" + variation.getName() + "§a da skin §7" + baseSkinName + "§a.");
                    }));

            currentSlot += 1;
        }

        if (isReturnable())
            addBackButton(getTotalSlots() - 6);

        display();
    }
}

