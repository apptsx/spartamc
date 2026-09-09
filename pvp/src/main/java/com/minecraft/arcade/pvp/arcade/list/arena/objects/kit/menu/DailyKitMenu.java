package com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.menu;

import com.minecraft.arcade.pvp.PvP;
import com.minecraft.arcade.pvp.arcade.list.arena.Arena;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.controller.KitController;
import com.minecraft.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.assignment.Assignment;
import com.minecraft.core.account.context.objects.permission.Permission;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DailyKitMenu extends Menu {

    public DailyKitMenu(Player player) {
        super(player, "Kit Diário", 3);
    }

    @Override
    public void handle() {
        clear();

        for (int i = 0; i < 9; i++)
            addItem(i, Item.of(Material.STAINED_GLASS_PANE, 5).name("§r"));

        addItem(13, Item.of(Material.NETHER_STAR, "§aDescobrir Kit",
                        "§7Clique para girar a roleta",
                        "§7e ver o seu kit diário.",
                        "",
                        "§eClique para ver!")
                .click(event -> handleKitSelector()));

        for (int i = 18; i < 27; i++)
            addItem(i, Item.of(Material.STAINED_GLASS_PANE, 5).name("§r"));

        display();
    }

    public void handleKitSelector() {
        AtomicBoolean isRunning = new AtomicBoolean(true);

        List<Kit> kitsToGive = KitController.getKitsUserDontHave(getPlayer());

        if (kitsToGive.isEmpty()) {
            sound(MenuSound.ERROR);
            getPlayer().sendMessage("§cVocê já possui todos os kits!");
            return;
        }

        removeItem(13);

        int[] slotsToAnimate = {9, 10, 11, 12, 13, 14, 15, 16, 17}; // Slots a serem animados
        int interval = 3; // Intervalo em ticks entre as animações (1 tick = 1/20 de segundo)

        BukkitRunnable runnable = new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (!isRunning.get()) {
                    this.cancel();
                    return;
                }

                // Atualizar os slots com os itens em sequência
                for (int i = 0; i < slotsToAnimate.length; i++) {
                    int itemIndex = (index + i) % kitsToGive.size();

                    Kit kit = kitsToGive.get(itemIndex);

                    getHolder().setItem(slotsToAnimate[i], Item.fromStack(kit.getIcon())
                            .flags(ItemFlag.values())
                            .name("§a" + kit.getName()));
                }

                getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_PLING, 2.0f, 2.0f);

                // Incrementar o índice para a próxima iteração
                index = (index + 1) % kitsToGive.size();
            }
        };

        // Agendar a tarefa para rodar repetidamente
        runnable.runTaskTimer(PvP.getInstance(), 0, interval);

        // Agendar a parada da animação após um tempo (exemplo: 3 segundos)
        Core.getPlatform().runSync(() -> {
            isRunning.set(false);

            // Escolher um item final aleatoriamente quando a animação parar com base nas probabilidades
            Kit kit = kitsToGive.get(Core.RANDOM.nextInt(kitsToGive.size()));

            Item item = Item.fromStack(kit.getIcon())
                    .flags(ItemFlag.values())
                    .name("§a" + kit.getName());

            // Colocar o item final no slot do meio
            getHolder().setItem(slotsToAnimate[slotsToAnimate.length / 2], item);

            // Limpar os outros slots animados
            for (int i = 0; i < slotsToAnimate.length; i++) {
                if (i != slotsToAnimate.length / 2) {
                    getHolder().setItem(slotsToAnimate[i], new ItemStack(Material.AIR));
                }
            }

            Account account = Core.getAccountController().of(getPlayer().getUniqueId());

            if (account != null) {
                account.setCooldown(Arena.DAILY_KIT_COOLDOWN, TimeUnit.DAYS.toMillis(1));

                account.setPermission(new Permission(kit.getPermission(), Assignment.CONSOLE, Constant.DEFAULT_ID,
                        System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)));

                account.send("§aO seu kit diário é o " + kit.getName() + ".");
                account.sound(Sound.EXPLODE);

                Core.getPlatform().runSync(this::close, 20L);
            } else
                getPlayer().sendMessage("§cOcorreu um problema ao definir o seu kit diário!");

        }, 60L);
    }
}
