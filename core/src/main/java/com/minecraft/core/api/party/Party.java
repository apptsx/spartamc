package com.minecraft.core.api.party;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.party.connection.PartyConnection;
import com.minecraft.core.api.party.member.PartyMember;
import com.minecraft.core.account.context.objects.route.RouteContext;
import com.minecraft.core.api.party.type.PartyType;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.backend.database.redis.message.types.route.arcade.ArenaPartyWarpMessage;
import com.minecraft.core.util.list.StringUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@Setter
public class Party {

    private final String identifier;

    private UUID authorId;
    private PartyType type;

    private final List<PartyMember> members;

    private int slots;

    public Party(Account author, PartyType type, int slots) {
        this.identifier = StringUtil.generateLetterCode(5) + Core.getPartyData().list().size() + 1;
        this.authorId = author.getId();

        this.type = type;
        this.slots = slots;

        this.members = new CopyOnWriteArrayList<>(Collections.singletonList(new PartyMember(authorId)));

        author.setParty(identifier);

        Core.getPartyData().save(this);
    }

    public void save() {
        Core.getPartyData().update(this);
    }

    public void disband() {
        for (Account member : getOnlineMembers()) {
            if (member == null) continue;

            member.resetParty();
        }

        /* Limpar Dados */
        Core.getPartyData().clearData(this);

        send("§cA party acabou.");

        Core.getPartyData().delete(this);
    }

    public void redirect(RouteContext route) {
        if (route == null) {
            Core.getLogger().warning("[Party] Tentativa de redirecionar party com rota nula.");
            return;
        }

        // Se for uma rota de arcade, usar o método específico
        if (route.isValidArcade()) {
            redirect(route.getArcade());
            return;
        }

        // Redirecionar para lobby
        for (PartyMember member : getMembers()) {
            if (member == null) continue;

            Account account = member.getAccount();

            if (account != null && account.isOnline()) {
                if (account.getId().equals(authorId)) continue;

                // Remover de arenas se estiver jogando
                removeFromArena(account);

                // Verificar se já está no mesmo servidor
                if (!account.inServer(route.getServerType())) {
                    account.sendGlobal("§aEnviando para o lobby...");
                    account.redirect(route);
                }
            }
        }
    }

    public void redirect(ArcadeRouteContext route) {
        if (route == null || route.getArcade() == null) {
            Core.getLogger().warning("[Party] Tentativa de redirecionar party com rota inválida.");
            return;
        }

        if (route.getMaxPlayers() == 2) {
            PartyMember member = getMembers().get(Core.RANDOM.nextInt(getMembersCount()));

            while (member.getId().equals(authorId))
                member = getMembers().get(Core.RANDOM.nextInt(getMembersCount()));

            Account account = member.getAccount();

            if (account != null && account.isOnline()) {
                // Remover de arenas se estiver jogando
                removeFromArena(account);

                if (!account.inServer(route.getArcade().getServer())) {
                    account.sendGlobal("§aEnviando para " + route.getArenaIdentifier() + "...");

                    account.setArcadeRoute(route);

                    account.redirect(route);
                } else {
                    /* Caso já esteja no servidor, solicitar busca da arena e enviá-lo. */
                    new ArenaPartyWarpMessage(account, route).send();
                }
            }

        } else {
            for (PartyMember member : getMembers()) {
                if (member == null) continue;

                Account account = member.getAccount();

                if (account != null && account.isOnline()) {
                    if (account.getId().equals(authorId)) continue;

                    // Remover de arenas se estiver jogando
                    removeFromArena(account);

                    if (!account.inServer(route.getArcade().getServer())) {
                        account.sendGlobal("§aEnviando para " + route.getArenaIdentifier() + "...");

                        account.setArcadeRoute(route);

                        account.redirect(route);
                    } else {
                        /* Caso já esteja no servidor, solicitar busca da arena e enviá-lo. */
                        new ArenaPartyWarpMessage(account, route).send();
                    }
                }
            }
        }
    }

    /**
     * Remove o jogador de uma arena antes de redirecioná-lo
     * Quando o jogador for redirecionado, ele automaticamente sai da arena ao trocar de servidor.
     * Se estiver no mesmo servidor, o redirect já lida com isso.
     */
    private void removeFromArena(Account account) {
        RouteContext currentRoute = account.getRoute();
        
        if (currentRoute != null && currentRoute.isValidArcade()) {
            // O jogador será removido automaticamente da arena quando for redirecionado
            // porque ele vai trocar de servidor. Se já estiver no servidor correto,
            // o método redirect já lida com a mudança de arena.
        }
    }

    /* --------------------------
         Métodos Utilitários
    ---------------------------- */
    public void transfer(Account account) {
        this.authorId = account.getId();

        send(account.getColoredName() + "§e é o novo dono da party.");
        save();
    }

    public boolean isAuthor(UUID uuid) {
        return this.authorId.equals(uuid);
    }

    public Account getAuthor() {
        return Core.getAccountController().of(this.authorId);
    }

    public void send(String message) {
        getOnlineMembers().forEach(member -> member.send(Constant.PARTY_CHAT_PREFIX + "§7" + message));
    }

    public boolean isType(PartyType type) {
        return this.type.equals(type);
    }

    public void setType(PartyType type) {
        this.type = type;
        save();
    }

    public void setSlots(int slots) {
        this.slots = slots;
        save();
    }

    public boolean isFull() {
        return members.size() >= slots;
    }

    /* --------------------------
           Gerenciamento de Membros
        ---------------------------- */
    public List<Account> getOnlineMembers() {
        return Core.getAccountController().filter(account -> account.isOnline() && isMember(account.getId()));
    }

    public List<UUID> getMembersId() {
        return this.members.stream().map(PartyMember::getId).collect(Collectors.toList());
    }

    public int getMembersCount() {
        return this.members.size();
    }

    public boolean isMember(UUID uuid) {
        return this.members.stream().anyMatch(member -> member.getId().equals(uuid));
    }

    public PartyMember getMember(UUID uuid) {
        return getMembers().stream().filter(member -> member.getId().equals(uuid)).findFirst().orElse(null);
    }

    public void disconnect(Account account) {
        PartyMember member = getMember(account.getId());

        account.resetParty();

        /* Atualizando membro */
        member.setExpiresAt(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(3));

        int indexOf = this.members.indexOf(member);
        this.members.set(indexOf, member);

        send(account.getColoredName() + "§c desconectou-se e será removido em 3 minutos.");
        save();

        Core.getPartyData().disconnect(new PartyConnection(identifier, account.getId()));
    }

    public void reconnect(Account account) {
        PartyMember member = getMember(account.getId());

        if (isFull())
            setSlots(slots + 1);

        account.setParty(identifier);

        /* Atualizando Membro */
        member.setExpiresAt(-1L);

        int indexOf = this.members.indexOf(member);
        this.members.set(indexOf, member);

        send(account.getColoredName() + "§a reconectou.");
        save();
    }

    public void add(Account member) {
        if (isMember(member.getId())) return;

        send(member.getColoredName() + "§a entrou na party.");

        this.members.add(new PartyMember(member.getId()));
        member.setParty(identifier);

        save();
    }

    public void kick(Account member) {
        if (!isMember(member.getId())) return;

        send(member.getColoredName() + "§c foi expulso da party.");

        this.members.removeIf(partyMember -> partyMember.getId().equals(member.getId()));
        member.resetParty();

        save();
    }

    public void remove(Account member) {
        if (!isMember(member.getId())) return;

        send(member.getColoredName() + "§c saiu da party.");

        this.members.removeIf(partyMember -> partyMember.getId().equals(member.getId()));
        member.resetParty();

        save();
    }
}
