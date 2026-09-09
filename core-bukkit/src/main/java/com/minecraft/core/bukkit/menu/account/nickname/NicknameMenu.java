package com.minecraft.core.bukkit.menu.account.nickname;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.mojang.MojangApi;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.backend.database.redis.message.types.account.AccountNickChangeMessage;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.util.list.Validator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NicknameMenu extends Menu {

    private final Account account;
    private final List<String> RANDOM_NAMES = Arrays.asList(
            "JoaoZika_47", "Brav0Tezão", "XablauPikao21", "ZezinDaQuebra", "CachorroLokoZ",
            "FogueteDoido12", "MlkDoSertao87", "ZicaDoBagui21", "TropaSombraZZ", "CoringaBRv",
            "JorginTopZera", "RatoLocoBR", "MlkDoBonde77", "SombraTigre21", "VagabundoDoMorro",
            "Lendario_15", "MacacoAtomic", "AzulaoBR_v2", "VyytorZK", "FuraOlho99",
            "CapitaoM4to", "FuscaoPreto_77", "FantasmaRoxo", "BrisaSincera", "PegasusBR99",
            "TigreNegroZX", "TonhaoDoRole", "VerdaoDoMorumbi", "PanteraBrava44", "GaludoDaRoça",
            "FuracaoBR_01", "LeoDoValeXX", "Skullzinho123", "JucaTatu_vip", "PirataRoxo",
            "BRNinja_42", "ZePorrada_", "TigreManco88", "UrubuNervoso", "RaposaVeloz_",
            "JacareDoPantano", "CaveiraDoMorrao", "BrasilBala_", "PatraoMalucoX", "AranhaFumante",
            "DragaoZord_v2", "TropinhaMonstro", "CabraMacho_", "Pantheraz_", "GoianoBruto_7"
    );

    public NicknameMenu(Player player, Menu last) {
        super(player, "Nickname personalizado", last, 3);

        this.account = Core.getAccountController().of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        boolean isUsingFake = account.isUsingFake();
        String currentNick = isUsingFake ? account.getFake().getNick() : account.getName();

        if (isUsingFake) {
            addItem(13, Item.of(Material.NAME_TAG, "§a" + currentNick));

            addItem(22, Item.of(Material.BARRIER, "§cResetar Nickname")
                    .click(event -> {
                        account.setFake("");
                        new AccountNickChangeMessage(account, account.getNickname()).send();

                        sound(MenuSound.SUCCESS);
                        account.send("§aNickname resetado para o nome real.");

                        handle();
                    }));
        } else {
            addItem(13, Item.of(Material.NAME_TAG, "§a" + currentNick));

            addItem(11, Item.of(Material.ANVIL, "§aEscolher o nickname",
                    "§7Escolha um nickname personalizado",
                    "§7para se disfarçar no servidor.",
                    "",
                    "§eClique para escolher!")
                    .click(event -> {
                        if (!account.hasRank(RankType.MAX)) {
                            sound(MenuSound.ERROR);
                            account.send("§cVocê não tem permissão para usar nickname personalizado!", "§cAdquira o rank " + RankType.MAX.getColoredName() + "§c em: §e" + Constant.SERVER_STORE);
                            return;
                        }

                        close();
                        sound(MenuSound.SUCCESS);
                        
                        new AnvilNicknameMenu(getPlayer()).open();
                    }));

            addItem(15, Item.of(Material.NETHER_STAR, "§aNickname aleatório",
                    "§7Use um nickname aleatório",
                    "§7da lista disponível.",
                    "",
                    "§eClique para sortear!")
                    .click(event -> {
                        if (!account.hasRank(RankType.MAX)) {
                            sound(MenuSound.ERROR);
                            account.send("§cVocê não tem permissão para usar nickname personalizado!", "§cAdquira o rank " + RankType.MAX.getColoredName() + "§c em: §e" + Constant.SERVER_STORE);
                            return;
                        }

                        if (account.hasCooldown(Constant.NICK_CHANGE_COOLDOWN_KEY)) {
                            sound(MenuSound.ERROR);
                            account.send("§cAguarde " + account.getFormattedCooldown(Constant.NICK_CHANGE_COOLDOWN_KEY) + " para trocar de nick novamente.");
                            return;
                        }

                        String randomNick;
                        do {
                            randomNick = RANDOM_NAMES.get(Core.RANDOM.nextInt(RANDOM_NAMES.size()));
                        } while (isNotValidName(randomNick));

                        account.setFake(randomNick);
                        new AccountNickChangeMessage(account, randomNick).send();

                        sound(MenuSound.SUCCESS);
                        account.send("§aNickname alterado para: §f" + randomNick);

                        if (!account.hasRank(RankType.HELPER))
                            account.setCooldown(Constant.NICK_CHANGE_COOLDOWN_KEY, TimeUnit.SECONDS.toMillis(15));

                        handle();
                    }));
        }

        if (isReturnable())
            addBackButton();

        display();
    }

    private boolean isNotValidName(String name) {
        return !Validator.isNickname(name) || (Core.MOJANG_API.isPremium(name).equals(MojangApi.ResponseCode.DONE)
                || Core.getAccountController().isPresent(acc -> acc.getNickname().equalsIgnoreCase(name)));
    }
}

