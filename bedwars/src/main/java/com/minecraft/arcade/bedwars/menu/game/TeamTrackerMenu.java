package com.minecraft.arcade.bedwars.menu.game;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.arcade.bedwars.user.context.objects.tracker.UserTracker;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import com.minecraft.core.util.list.bukkit.ColorUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TeamTrackerMenu extends Menu {

    private final User user;
    private final Arena arena;

    public TeamTrackerMenu(Player player, Arena arena) {
        super(player, "Rastreador", Math.min((arena.getAliveTeams().size() / 9) + 3, 4), 8);

        this.user = (User) User.of(player.getUniqueId());
        this.arena = arena;
    }

    @Override
    public void handle() {
        clear();

        UserTracker tracker = user.getContext().getTracker();

        if (!tracker.isActive()) {
            setTitle("Comprar rastreador");

            BedWarsItem item = BedWarsItem.UTIL_TRACKING;

            boolean canBuy = user.canBuyItem(item);

            List<String> lore = new ArrayList<>(Arrays.asList(
                    "§7Preço: " + item.getOre().getValue(item.getPrice()),
                    ""
            ));

            lore.addAll(item.getLore());

            lore.add("");

            if (arena.hasTeamsWithBed())
                lore.addAll(Arrays.asList(
                        "§cDisponível apenas quando",
                        "§cnão há camas vivas."
                ));
            else
                lore.add(canBuy ? "§eClique para adquirir!" : "§cVocê não tem " + item.getOre().getName().toLowerCase() + " suficientes.");

            addItem(13, Item.fromStack(item.getStack())
                    .name((canBuy ? "§a" : "§c") + item.getName())
                    .lore(lore)
                    .click(event -> {

                        if (arena.hasTeamsWithBed()) {
                            sound(MenuSound.ERROR);

                            getPlayer().sendMessage("§cDisponível apenas quando não houver mais camas vivas.");
                            return;
                        }

                        if (!user.canBuyItem(item)) {
                            sound(MenuSound.ERROR);

                            getPlayer().sendMessage("§cVocê não tem " + item.getOre().getName().toLowerCase() + " suficientes.");
                            return;
                        }

                        close();
                        sound(MenuSound.DONE);

                        user.removeOre(item);

                        tracker.setActive(true);
                        getPlayer().getInventory().addItem(item.getStack());

                        getPlayer().sendMessage("§eO item §6" + item.getName() + "§e foi adquirido.");
                    }));

        } else {
            setTitle("Rastreador");

            List<Team> teamList = arena.getAliveTeams()
                    .stream().filter(team -> !team.equals(user.getTeam()))
                    .collect(Collectors.toList());

            if (teamList.isEmpty())
                addErrorButton("§cNão há times vivos...");
            else {

                buildPageItems(teamList, 10, (team, slot) -> {
                    addItem(slot, Item.of(Material.WOOL, ColorUtil.getIdByColor(team.getColor()),
                                    team.getColor() + team.getName(),
                                    "§7Clique para rastrear.")
                            .click(event -> {

                                if (!arena.getAliveTeams().contains(team)) {
                                    sound(MenuSound.ERROR);

                                    getPlayer().sendMessage("§cO " + team.getColor() + "Time " + team.getName() + "§c não está mais vivo.");
                                    return;
                                }

                                User tracking = team.getAliveUsers().stream().findFirst().orElse(null);

                                if (tracking == null) {
                                    sound(MenuSound.ERROR);

                                    getPlayer().sendMessage("§cNão há jogadores vivos no " + team.getColor() + "Time " + team.getName() + "§c.");
                                    return;
                                }

                                close();
                                sound(MenuSound.DONE);

                                tracker.handle(team, tracking.getAccount().getId());

                                Item compass = Item.fromStack(BedWarsItem.UTIL_TRACKING.getStack())
                                        .name("§cRastreador")
                                        .flags(ItemFlag.values())
                                        .enchantment(Enchantment.DURABILITY, 1)
                                        .lore("§7Rastreando: " + team.getColor() + team.getName());

                                BukkitUtil.replaceItemByType(getPlayer(), compass.getType(), compass);

                                getPlayer().sendMessage("§aVocê iniciou um rastreio por " + team.getColor() + tracking.getAccount().getNickname() + "§a.");
                            }));
                });
            }
        }

        display();
    }
}
