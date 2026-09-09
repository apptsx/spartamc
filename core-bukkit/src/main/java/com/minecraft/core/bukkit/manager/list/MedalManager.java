package com.minecraft.core.bukkit.manager.list;

import com.minecraft.core.Constant;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.medal.Medal;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MedalManager {

    public static List<Medal> getAvailableMedals(Account account) {
        return Stream.of(Medal.values())
                .filter(account::hasMedal)
                .collect(Collectors.toList());
    }

    public static void sendMedals(Account account) {
        List<Medal> medals = getAvailableMedals(account);

        if (medals.isEmpty() || medals.stream().allMatch(medal -> medal.equals(Medal.NONE))) {
            account.send("§cVocê ainda não possui medalhas ;(",
                    "§cAdquira medalhas em: §e" + Constant.SERVER_STORE);
            return;
        }

        TextComponent message = new TextComponent("§aSuas medalhas: ");

        int index = 1;
        boolean end = false;
        for (Medal medal : medals) {
            if (index >= medals.size()) end = true;

            TextComponent component = new TextComponent(medal.getColoredSymbol());

            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    TextComponent.fromLegacyText("§eClique para selecionar a medalha " + medal.getColoredName())));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/medal " + medal.name()));

            message.addExtra(component);
            message.addExtra("§r" + (end ? "." : ", "));

            index++;
        }

        account.send(message);
    }
}
