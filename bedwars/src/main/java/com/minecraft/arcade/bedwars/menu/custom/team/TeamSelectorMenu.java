package com.minecraft.arcade.bedwars.menu.custom.team;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.util.list.bukkit.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class TeamSelectorMenu extends Menu {

    private final User user;

    public TeamSelectorMenu(Player player) {
        super(player, "Selecionar time", 4);

        this.user = (User) User.of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        Arena arena = user.getArena();

        List<Team> teams = arena.getTeamList();

        int slot = 10, last = slot;
        for (Team team : teams) {

            boolean isFull = team.isFull(), inTeam = user.getTeam() != null && user.getTeam().equals(team);

            final int finalSlot = slot;
            addItem(slot, Item.of(Material.WOOL, ColorUtil.getIdByColor(team.getColor()), team.getColoredName(),
                            "",
                            "§7Jogadores: §a" + team.getMembers().size() + "/" + team.getMaxPlayers(),
                            "",
                            inTeam ? "§cSelecionado." : !isFull ? "§eClique para entrar." : "§cCheio.")
                    .updater(view -> {
                        if (!view.getTitle().equalsIgnoreCase(getTitle())) return;

                        Item item = getContents().get(finalSlot);

                        if (item != null) {
                            item.lore(
                                    "",
                                    "§7Jogadores: §a" + team.getMembers().size() + "/" + team.getMaxPlayers(),
                                    "",
                                    inTeam ? "§cSelecionado." : !isFull ? "§eClique para entrar." : "§cCheio."
                            );

                            view.setItem(finalSlot, item);

                            getPlayer().updateInventory();
                        }
                    })
                    .click(event -> {

                        if (team.isFull()) {
                            sound(MenuSound.ERROR);

                            getPlayer().sendMessage("§cO time " + team.getColoredName() + "§c está cheio.");
                            return;
                        }

                        if (inTeam) {
                            sound(MenuSound.ERROR);

                            getPlayer().sendMessage("§cVocê já está no time " + team.getColoredName() + "§c.");
                            return;
                        }

                        close();
                        sound(MenuSound.SUCCESS);

                        if (user.getTeam() != null)
                            user.getTeam().getMembers().remove(getPlayer().getUniqueId());

                        team.getMembers().add(getPlayer().getUniqueId());

                        user.setTeam(team);

                        // Não aplicar tag do time até o jogo começar - manter tag padrão do jogador
                        // A tag do time será aplicada quando o jogo iniciar em ArenaPhaseExecutor

                        user.setState(ArcadeState.ALIVE);
                        user.setJoin(Join.PLAYER);

                        getPlayer().sendMessage("§aVocê entrou no time " + team.getColoredName() + "§a.");
                    }));

            slot++;
            if (slot == (last + 7)) {
                slot += 2;
                last = slot;
            }
        }

        int specSlot = slot;
        addItem(specSlot, Item.of(Material.COMPASS, "§aEspectador",
                        "",
                        "§7Jogadores: §a" + (int) arena.getAllUsers().stream().filter(UserModel::isVanish).count(),
                        "",
                        "§eClique para entrar.")
                .updater(view -> {
                    if (!view.getTitle().equalsIgnoreCase(getTitle())) return;

                    Item item = getContents().get(specSlot);

                    if (item != null) {
                        item.lore("",
                                "§7Jogadores: §a" + (int) arena.getAllUsers().stream().filter(UserModel::isVanish).count(),
                                "",
                                "§eClique para entrar.");

                        view.setItem(specSlot, item);

                        getPlayer().updateInventory();
                    }
                })
                .click(event -> {

                    close();

                    if (!user.isJoin(Join.PLAYER)) {
                        sound(MenuSound.ERROR);

                        getPlayer().sendMessage("§cVocê já está no modo espectador.");
                        return;
                    }

                    sound(MenuSound.DONE);

                    if (user.getTeam() != null) {
                        user.getTeam().getMembers().remove(getPlayer().getUniqueId());

                        user.setTeam(null);
                        user.setTag(user.getAccount().getTag());
                    }

                    user.setJoin(Join.VANISH);

                    getPlayer().sendMessage("§aVocê entrou no modo espectador.");
                }));

        addCloseButton();

        display();
    }
}
