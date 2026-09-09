package com.minecraft.core.bungee.service.task;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.bungee.event.list.update.UpdateEvent;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;

import java.util.Arrays;
import java.util.List;

public class UpdateTask implements Runnable {

    private final List<String> customMessages = Arrays.asList(
            "&6Achou algum hacker? &eUtilize &b/report <jogador> <motivo>",
            "&eVeja suas estatísticas utlizando &b/stats",
            "&eConsiga &6ranks &eem nosso site &b" + Constant.SERVER_STORE,
            "&6Evite ser &cbanido! &eConfira nossas regras &b" + Constant.SERVER_WEBSITE + "/regras",
            "&6Entre em nosso Discord! &eE fique por dentro de &dpromoções e novidades &b" + Constant.SERVER_DISCORD_GG,
            "&6Cansou de achar hacker? &eVenha participar de nossa equipe &b" + Constant.SERVER_WEBSITE + "/aplicar",
            "&6Está tudo bem? &eO CVV realiza apoio emocional e prevenção ao suicídio, atendendo de forma voluntária todos que precisam conversar &bhttps://www.cvv.org.br/",
            "&6Cansado da mesma &cvelha skin&6? &eConfira nossas opções em &b/skin",
            "&eUtilize &b/block &epara bloquear jogadores indesejados.",
            "&eEntre em nosso &9Discord! &eAcesse: &b" + Constant.SERVER_DISCORD_GG,
            "&eSaiba sobre o &dcronograma&e de eventos no servidor! &6" + Constant.SERVER_DOMAIN + "/eventos"
    );

    private final int messageSize = customMessages.size();
    private final int delay = 120;
    private int cleanupCounter = 0;
    int time = delay, index = 0;

    @Override
    public void run() {
        new UpdateEvent().call();
        cleanupCounter++;
        int cleanupInterval = 10;
        if (cleanupCounter >= cleanupInterval) {
            cleanupCounter = 0;
            Core.getServerData().cleanupDeadServers();
        }

        time--;

        if (time == 0) {
            Core.getAccountController()
                    .filter(account -> !account.inServer(ServerType.AUTH))
                    .forEach(account -> account.send(Util.color(Constant.SERVER_TITLE + " §7» " + customMessages.get(index))));

            time = delay;
            index++;

            if (index >= messageSize)
                index = 0;
        }
    }
}