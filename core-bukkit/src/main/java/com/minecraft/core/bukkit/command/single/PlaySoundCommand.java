package com.minecraft.core.bukkit.command.single;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlaySoundCommand implements CommandInheritor {

    @Completer(name = "playsound", subCommands = {"psp"}, rank = RankType.ADMIN)
    public List<String> playSoundCompleter(BukkitCommandContext context) {
        List<String> list = new ArrayList<>(),
                soundNames = Stream.of(Sound.values()).map(Sound::name).collect(Collectors.toList());

        String[] args = context.getArgs();

        if (args.length > 0 && !args[0].isEmpty()) {
            String sound = args[0].toLowerCase();

            for (String name : soundNames) {
                if (name.toLowerCase().startsWith(sound))
                    list.add(name);
            }
        } else
            list.addAll(soundNames);

        return list;
    }

    @Command(name = "playsound", aliases = {"psp"}, rank = RankType.ADMIN)
    public void playSound(BukkitCommandContext context) {
        Player player = context.getPlayer();

        String[] args = context.getArgs();

        if (args.length <= 2) {
            player.sendMessage("§cUso: /" + context.getLabel() + " (som) (volume) (tonalidade).");
            return;
        }

        Sound sound = Arrays.stream(Sound.values()).filter(s -> s.name().equalsIgnoreCase(args[0])).findFirst().orElse(null);

        if (sound == null) {
            player.sendMessage("§cO som solicitado não foi encontrado.");
            return;
        }

        float volume = Float.parseFloat(args[1]), pitch = Float.parseFloat(args[2]);

        player.sendMessage(new String[]{"§aTocando: §f" + sound.name(),
                "§aVolume: §f" + volume,
                "§aTonalidade: §f" + pitch});

        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
