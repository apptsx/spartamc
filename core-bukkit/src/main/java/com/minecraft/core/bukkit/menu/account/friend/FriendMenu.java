package com.minecraft.core.bukkit.menu.account.friend;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.friend.Friend;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.util.list.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Setter
public class FriendMenu extends Menu {

    private FriendFilter filter;

    private boolean blink = true;

    public FriendMenu(Player player, Menu last) {
        super(player, "Amigos", last, 6, 21);

        this.filter = FriendFilter.DATE;
    }

    @Override
    public void handle() {
        clear();

        Account account = Core.getAccountController().of(getPlayer().getUniqueId());
        List<Friend> list = filter.getAction().handle(account.getFriends());

        if (list.isEmpty())

            addItem(22, Item.of(Material.ANVIL, "§aFaça novas amizades", "§7No momento, você não tem",
                    "§7nenhum amigo...",
                    "",
                    "§eClique para adicionar!"
            ).click(event -> {
            sound(MenuSound.PAGINATED);
            new AnvilFriendMenu(getPlayer()).open();
            }));
        else {
            buildPageItems(list, 10, (friend, slot) -> {
                Account target = Core.getAccountData().of(friend.getId());

                addItem(slot, Item.of(Material.SKULL_ITEM, 3, "§a" + target.getName(),
                                "§7Tipo: §f" + friend.getType().getName(),
                                "§7Início em: §f" + DateUtil.getSimpleDate(friend.getStartedAt()))
                        .skullByBase64(target.getSkin().getValue()));
            });
        }

        int requestSize = account.getFriendsRequests().size();

        addItem(getTotalSlots() - 5, Item.of(Material.PAPER, "§aSolicitações",
                        "§7Veja as solicitações",
                        "§7pendentes de amizade.",
                        "",
                        (requestSize == 0 ? "§cVocê não possui convites." : "§a" + Constant.ARROW_ALT_SYMBOL + " " + requestSize + " convite" + (requestSize > 1 ? "s" : "")),
                        "",
                        "§eClique para ver!")
                .updater(view -> {
                    Item item = getContents().get(getTotalSlots() - 4);

                    if (item != null && requestSize > 0) {
                        List<String> lore = new ArrayList<>(item.getItemMeta().getLore());
                        String pointSymbol = Constant.ARROW_ALT_SYMBOL;

                        this.blink = !blink;

                        lore.set(lore.size() - 3, "§a" + (blink ? pointSymbol : "") + " " + requestSize + " convite" + (requestSize > 1 ? "s" : ""));

                        item.lore(lore);

                        view.setItem(getTotalSlots() - 4, item);

                        getPlayer().updateInventory();
                    }
                })
                .click(event -> {
                    if (requestSize == 0)
                        sound(MenuSound.ERROR);
                    else {
                        sound(MenuSound.PAGINATED);
                        new FriendRequestMenu(getPlayer(), this).handle();
                    }
                }));

        List<String> filterLore = new ArrayList<>();

        filterLore.add("");

        for (FriendFilter filter : FriendFilter.values())
            filterLore.add((this.filter.equals(filter) ? "§a" + Constant.ARROW_ALT_SYMBOL + " " : "§7") + filter.getName());

        filterLore.addAll(Arrays.asList(
                "",
                "§eClique para alternar!"));

        addItem(getTotalSlots() - 4, Item.of(Material.HOPPER, "§bFiltrando por:", filterLore)
                .click(event -> {
                    setFilter(filter.next());

                    sound(MenuSound.PAGINATED);
                    handle();
                }));

        if (isReturnable())
            addBackButton(getTotalSlots() - 6);

        display();
    }

    @Getter
    @AllArgsConstructor
    public enum FriendFilter {

        DATE("Mais recentes", list -> list.stream().sorted(Comparator.comparing(Friend::getStartedAt)).collect(Collectors.toList())),
        A_Z("A-Z", list -> list.stream().sorted(Comparator.comparing(Friend::getName)).collect(Collectors.toList())),
        Z_A("Z-A", list -> list.stream().sorted(Comparator.comparing(Friend::getName).reversed()).collect(Collectors.toList()));

        private final String name;
        private final FriendFilterAction action;

        public interface FriendFilterAction {
            List<Friend> handle(List<Friend> list);
        }

        public FriendFilter next() {
            return this != Z_A ? values()[ordinal() + 1] : DATE;
        }
    }
}
