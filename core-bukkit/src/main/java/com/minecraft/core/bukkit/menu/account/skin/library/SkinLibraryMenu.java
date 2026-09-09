package com.minecraft.core.bukkit.menu.account.skin.library;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.skin.Skin;
import com.minecraft.core.api.skin.objects.SkinCategory;
import com.minecraft.core.api.skin.library.SkinLibrary;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.core.util.list.DateUtil;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

@Setter
public class SkinLibraryMenu extends Menu {

    private final Account account;
    private final SkinCategory category;

    public SkinLibraryMenu(Player player, SkinCategory category, Menu last) {
        super(player, "Biblioteca - " + category.getName(), last, 5, 21);

        this.account = Core.getAccountController().of(player.getUniqueId());
        this.category = category;
    }

    @Override
    public void handle() {
        clear();

        List<SkinLibrary> list = SkinLibrary.getLibrarySkins(category);

        if (list.isEmpty())
            addItem(22, Item.of(Material.WEB, "§cNão há skins..."));
        else {
            buildPageItems(list, 10, (library, slot) -> {
                Skin skin = library.getSkin();

                boolean using = account.hasSkin(skin);
                boolean hasVariations = SkinLibrary.hasVariations(library);

                String[] lore = hasVariations
                    ? new String[]{
                        "§8" + library.getGender().getName(),
                        "",
                        "§7Categoria: §a" + library.getCategory().getName(),
                        "",
                        !using ? "§eClique para ver variações!" : "§cJá escolhida."
                    }
                    : new String[]{
                        "§8" + library.getGender().getName(),
                        "",
                        "§7Categoria: §a" + library.getCategory().getName(),
                        "",
                        !using ? "§eClique para escolher!" : "§cJá escolhida."
                    };

                addItem(slot, Item.of(Material.SKULL_ITEM, 3, "§a" + skin.getDisplayName(), lore)
                        .skullByBase64(skin.getValue())
                        .click(event -> {
                            if (using && !hasVariations) {
                                sound(MenuSound.ERROR);
                                account.send("§cVocê já está usando a skin " + skin.getDisplayName() + ".");
                                return;
                            }

                            if (hasVariations) {
                                sound(MenuSound.PAGINATED);
                                new SkinVariationMenu(getPlayer(), library, this).handle();
                                return;
                            }

                            close();
                            sound(MenuSound.SUCCESS);

                            account.setSkin(skin);
                            ProtocolHandler.changePlayerSkin(getPlayer(), skin);

                            account.send("§aVocê selecionou a skin §7" + skin.getDisplayName() + "§a.");
                        }));
            });
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}

