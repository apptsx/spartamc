package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.skin.Skin;
import com.minecraft.core.api.skin.objects.SkinType;
import com.minecraft.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.bukkit.menu.account.preference.PreferenceMenu;
import com.minecraft.core.bukkit.menu.account.skin.library.SkinLibraryCategoryMenu;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.controller.list.SkinController;
import com.minecraft.core.util.list.Validator;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class NormalCommands implements CommandInheritor {

    @Command(name = "prefs", aliases = {"preferencias"})
    public void prefs(BukkitCommandContext context) {
        new PreferenceMenu(context.getPlayer(), null).handle();
    }

    @Command(name = "skin", aliases = {"skinn"}, runAsync = true)
    public void skin(BukkitCommandContext context) {
        Account account = context.getAccount();

        Player player = context.getPlayer();

        String[] args = context.getArgs();

        if (args.length == 0) {
            new SkinLibraryCategoryMenu(player, null).handle();
            return;
        }

        if (account.hasCooldown(Constant.SKIN_CHANGE_COOLDOWN_KEY)) {
            account.send("§cAguarde " + account.getFormattedCooldown(Constant.SKIN_CHANGE_COOLDOWN_KEY) + " para mudar de skin novamente.");
            return;
        }

        String displayName = args[0];

        if (displayName.equalsIgnoreCase("#") || displayName.equalsIgnoreCase("reset")) {
            if (!account.isPremium()) {
                account.send("§cContas piratas não possuem uma skin padrão.");
                return;
            }

            if (account.isUsingProfileSkin()) {
                account.send("§cVocê já está usando a sua skin padrão.");
                return;
            }

            account.send("§eRecolhendo dados...");

            try {
                Skin skin = account.getDefaultProfileSkin();

                if (skin == null) {
                    account.send("§cNão foi possível encontrar a sua skin padrão.");
                    return;
                }

                ProtocolHandler.changePlayerSkin(player, skin);

                account.setSkin(skin);
                account.send("§aA sua skin foi restaurada.");
            } catch (Exception e) {
                account.send("§cNão foi possível restaurar a sua skin!");
            }

            return;
        }

        if (!account.isVIP()) {
            account.send("§cVocê não pode customizar sua skin! ;(",
                    "§cAdquira o rank " + RankType.VIP.getColoredName() + "§c em: §e" + Constant.SERVER_STORE);
            return;
        }

        if (!Validator.isNickname(displayName)) {
            account.send("§cO nome inserido é inválido, tente novamente.");
            return;
        }

        account.send("§eRecolhendo dados...");

        try {
            UUID id = Core.MOJANG_API.getUUID(displayName);

            Skin skin = SkinController.getSkin(id, displayName, SkinType.CUSTOM);

            if (skin == null) {
                account.send("§cA skin solicitada não foi encontrada.");
                return;
            }

            if (account.hasSkin(skin)) {
                account.send("§cA skin " + skin.getDisplayName() + " já foi selecionada.");
                return;
            }

            ProtocolHandler.changePlayerSkin(player, skin);

            account.setSkin(skin);
            account.send("§aA skin §7" + skin.getDisplayName() + "§a foi selecionada.");

            account.sound(Sound.NOTE_PLING);

            if (!account.isStaffer())
                account.setCooldown(Constant.SKIN_CHANGE_COOLDOWN_KEY, TimeUnit.MINUTES.toMillis(3));

        } catch (Exception e) {
            account.send("§cOcorreu um erro ao recolher os dados da skin solicitada!");
        }
    }
}
