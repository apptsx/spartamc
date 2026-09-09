package com.minecraft.auth.listener;

import com.minecraft.auth.menu.CaptchaMenu;
import com.minecraft.auth.user.User;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.auth.state.AuthState;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.util.list.StringUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.List;

import static com.minecraft.core.util.list.bukkit.BukkitUtil.updateLevelBar;

public class CaptchaListener implements Listener {

    private final List<String> AUTH_COMMANDS = Arrays.asList("/login", "/logar", "/register", "/registrar");

    @EventHandler(priority = EventPriority.LOW)
    public void command(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        String message = event.getMessage();

        boolean isNotAuthCommand = AUTH_COMMANDS.stream().noneMatch(command -> message.toLowerCase().startsWith(command.toLowerCase()));

        if (isNotAuthCommand) {
            event.setCancelled(true);

            player.sendMessage("§4§lAVISO §cVocê não pode usar outros comandos.");
        }
    }

    @EventHandler
    public void closeCaptcha(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        Menu menu = BukkitCore.getManager().getMenu().of(player.getUniqueId());

        User user = (User) User.of(player.getUniqueId());

        if (menu instanceof CaptchaMenu && user.isCaptcha())
            Core.getPlatform().runSync(menu::handle, 10);
    }

    @EventHandler
    public void check(SyncUpdateEvent event) {
        if (event.isType(UpdateType.TICK)) {
            User.getList().forEach(model -> {
                User user = (User) model;

                Player player = user.getAccount().player();

                if (player != null && (user.isCaptcha() || user.isLocked()))
                    updateLevelBar(player, user.getEndTime(), 20);
            });
        }

        if (event.isType(UpdateType.SECOND)) {
            for (UserModel object : User.getList()) {
                if (!(object instanceof User)) continue;

                User user = (User) object;

                Account account = user.getAccount();

                Player player = account.player();

                if (player.getLevel() > 0 && !(user.isCaptcha() || user.isLocked()))
                    player.setLevel(0);

                if (user.isCaptcha() || user.isLocked()) {
                    if (!user.isCaptcha() && event.getTicks() % 40 == 0)
                        account.send(account.isAuthState(AuthState.OK)
                                ? "§cUtilize §e/login (senha)§c para se autenticar."
                                : "§cUtilize §e/register (senha) (confirmar-senha)§c para se cadastrar.");

                    if (user.getEndTime() <= System.currentTimeMillis()) {
                        player.kickPlayer(StringUtil.StringHelper.makeCenteredMessage(Constant.SERVER_TITLE + "\n\n"
                                + "§cOcorreu um erro ao autenticar a sua conta."
                                + "\n§cTente novamente mais tarde!"
                                + "\n\n§cEm caso de problemas, contate: §e" + Constant.SERVER_DISCORD));
                        return;
                    }
                }
            }
        }
    }
}
