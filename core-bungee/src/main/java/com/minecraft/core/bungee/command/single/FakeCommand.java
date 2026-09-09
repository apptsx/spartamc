package com.minecraft.core.bungee.command.single;

import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.mojang.MojangApi;
import com.minecraft.core.backend.database.redis.message.types.account.AccountNickChangeMessage;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.util.list.Validator;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class FakeCommand implements CommandInheritor {

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
    
    private final Set<String> verifiedNames = Collections.synchronizedSet(new HashSet<>());

    @Command(name = "fake", aliases = {"nick"}, rank = RankType.PARTNER, runAsync = true)
    public void fake(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cComo usar: /" + context.getLabel() + ":",
                    "§c* /" + context.getLabel() + " (apelido)",
                    "§c* /" + context.getLabel() + " (reset)",
                    "§c* /" + context.getLabel() + " (random)");
            return;
        }

        String nickName = args[0];

        if (nickName.equalsIgnoreCase("list")) {
            List<Account> fakeList = Core.getAccountController().filter(Account::isUsingFake);

            if (fakeList.isEmpty()) {
                account.send("§cNão há jogadores com nomes falsos.");
                return;
            }

            TextComponent builder = new TextComponent("§aLista de fakes:");

            int index = 1;
            boolean end = false;
            for (Account faker : fakeList) {
                if (index >= fakeList.size()) end = true;

                TextComponent name = new TextComponent("§f" + faker.getNickname());

                name.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7" + faker.getName())));

                builder.addExtra(name);
                builder.addExtra("§r" + (end ? "." : ", "));

                index++;
            }

            account.send(builder);
            return;
        }

        if (nickName.equalsIgnoreCase("#") || nickName.equalsIgnoreCase("reset")) {
            if (!account.isUsingFake()) {
                account.send("§cVocê não está usando fake.");
                return;
            }

            account.setFake("");

            new AccountNickChangeMessage(account, account.getNickname()).send();
            return;
        }

        if (nickName.equalsIgnoreCase("random")) {
            do {
                nickName = RANDOM_NAMES.get(Core.RANDOM.nextInt(RANDOM_NAMES.size()));
            } while (isNameInUseLocally(nickName));
            
            applyFakeNick(account, nickName, context.getLabel());
            return;
        }

        if (account.hasCooldown(Constant.NICK_CHANGE_COOLDOWN_KEY)) {
            account.send("§cAguarde " + account.getFormattedCooldown(Constant.NICK_CHANGE_COOLDOWN_KEY) + " para trocar de nick novamente.");
            return;
        }

        if (!Validator.isNickname(nickName)) {
            account.send("§cOps! O nome solicitado não é válido.");
            return;
        }
        
        if (isNameInUseLocally(nickName)) {
            account.send("§cEste nome já está em uso no servidor.");
            return;
        }

        String finalNickName = nickName;
        CompletableFuture.runAsync(() -> {
            if (isMojangPremiumName(finalNickName)) {
                account.send("§cEste nome pertence a uma conta premium da Mojang.");
                return;
            }
            
            applyFakeNick(account, finalNickName, context.getLabel());
        });
    }
    
    /**
     * Aplica o fake nick no jogador
     */
    private void applyFakeNick(Account account, String nickName, String commandLabel) {
        account.setFake(nickName);
        new AccountNickChangeMessage(account, nickName).send();

        if (!account.hasRank(RankType.HELPER))
            account.setCooldown(Constant.NICK_CHANGE_COOLDOWN_KEY, TimeUnit.SECONDS.toMillis(15));
    }
    
    /**
     * Verifica se o nome já está em uso localmente (instantâneo)
     */
    private boolean isNameInUseLocally(String name) {
        return Core.getAccountController().isPresent(account -> 
            account.getNickname().equalsIgnoreCase(name));
    }
    
    /**
     * Verifica se o nome é premium na Mojang (com cache)
     */
    private boolean isMojangPremiumName(String name) {
        // Se já verificamos este nome antes, usar cache
        if (verifiedNames.contains(name.toLowerCase())) {
            return false;
        }
        
        try {
            MojangApi.ResponseCode response = Core.MOJANG_API.isPremium(name);
            
            if (response.equals(MojangApi.ResponseCode.DONE)) {
                return true; // É premium
            }
            
            // Adicionar ao cache de nomes verificados (não premium)
            verifiedNames.add(name.toLowerCase());
            
            // Limpar cache se ficar muito grande (evitar memory leak)
            if (verifiedNames.size() > 500) {
                verifiedNames.clear();
            }
            
            return false;
        } catch (Exception e) {
            Core.getLogger().warning("[FakeCommand] Erro ao verificar nome na API Mojang: " + e.getMessage());
            return false; // Em caso de erro, permitir o uso
        }
    }

}
