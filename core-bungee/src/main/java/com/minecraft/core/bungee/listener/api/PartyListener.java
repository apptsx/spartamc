package com.minecraft.core.bungee.listener.api;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.party.Party;
import com.minecraft.core.api.party.connection.PartyConnection;
import com.minecraft.core.api.party.member.PartyMember;
import com.minecraft.core.backend.data.list.api.PartyData;
import com.minecraft.core.bungee.event.list.update.UpdateEvent;
import com.minecraft.core.controller.list.AccountController;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PartyListener implements Listener {

    private final AccountController accountController = Core.getAccountController();
    private final PartyData partyData = Core.getPartyData();

    @EventHandler
    public void onPartiesConnections(UpdateEvent event) {
        CompletableFuture.runAsync(() -> {
            for (Party party : partyData.list()) {
                if (party == null) continue;

                Iterator<PartyMember> iterator = party.getMembers().iterator();

                while (iterator.hasNext()) {
                    PartyMember member = iterator.next();

                    if (member.hasExpired()) {
                        Account account = member.getAccount();

                        boolean changedOwner = false;

                        iterator.remove();
                        party.save();

                        if (partyData.hasConnection(member.getId()))
                            partyData.cancelConnection(member.getId());

                        account.resetParty();

                        party.send(account.getColoredName() + "§c foi removido da party.");

                        if (party.isAuthor(member.getId())) {
                            List<Account> onlineMembers = party.getOnlineMembers();

                            if (!onlineMembers.isEmpty()) {
                                changedOwner = true;

                                party.setAuthorId(onlineMembers.get(0).getId());
                                party.save();
                            }
                        }

                        if (changedOwner) {
                            Account author = party.getAuthor();

                            party.send(author.getColoredName() + "§e é o novo dono da party.");

                            Core.getLogger().info(author.getNickname() + " virou dono de uma party.");
                        }
                    }
                }
            }
        });
    }

    @EventHandler
    public void onReconnect(PostLoginEvent event) {
        Account account = accountController.of(event.getPlayer().getUniqueId());

        if (account != null && partyData.hasConnection(account.getId())) {
            PartyConnection connection = partyData.getConnection(account.getId());

            Party party = partyData.of(connection.getPartyIdentifier());

            partyData.cancelConnection(account.getId());

            if (party != null) {
                party.reconnect(account);

                Core.getLogger().info(account.getNickname() + " reconectou-se na party de " + party.getAuthor().getNickname() + ".");
            } else
                account.send("§cA sua party antiga não foi encontrada.");
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        Account account = accountController.of(player.getUniqueId());

        if (account != null && account.hasParty()) {
            Party party = account.getParty();

            if (party == null) {
                Core.getLogger().warning("Não foi possível encontrar a party de " + account.getNickname() + ".");
                return;
            }

            if (party.getMembersCount() == 1) {
                party.disband();

                Core.getLogger().info("A party de " + account.getNickname() + " foi desfeita.");
            } else {
                party.disconnect(account);

                Core.getLogger().info(account.getNickname() + " desconectou-se da party de " + party.getAuthor().getNickname() + ".");
            }
        }
    }
}
