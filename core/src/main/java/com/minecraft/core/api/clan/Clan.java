package com.minecraft.core.api.clan;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.clan.color.ClanColor;
import com.minecraft.core.api.clan.member.ClanMember;
import com.minecraft.core.api.clan.metadata.ClanMetadata;
import com.minecraft.core.api.clan.metadata.emblem.ClanEmblem;
import com.minecraft.core.api.clan.role.ClanRole;
import com.minecraft.core.backend.database.redis.message.types.account.AccountClanChangeMessage;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
public class Clan {

    private final UUID id;
    private UUID leader;

    private String name, tag;
    private String tagColor;

    private ClanMetadata metadata;

    public Clan(Account leader, UUID id, String name, String tag) {
        this.id = id;

        this.name = name;
        this.tag = tag;

        this.leader = leader.getId();

        ClanMetadata metadata = new ClanMetadata();

        metadata.getMembers().put(leader.getId(), new ClanMember(leader, ClanRole.LEADER));

        this.metadata = metadata;
    }

    protected void save(String... fields) {
        for (String field : fields) {
            Core.getClanData().update(this, field);
        }
    }

    public void disband() {
        Core.getLogger().info("Deletando o clan " + name + " (" + tag + ")...");

        for (UUID id : metadata.getMembers().keySet()) {
            Account account = Core.getAccountData().of(id);

            if (account == null) continue;

            account.setClan(Constant.DEFAULT_ID);

            new AccountClanChangeMessage(account).send();
        }

        send("§7O clan foi desfeito.");

        Core.getClanData().delete(this);

        Core.getLogger().info("O clan " + name + " (" + tag + ") foi deletado!");
    }

    public void send(String message) {
        getOnlineMembers().forEach(account -> account.send(Constant.CLAN_CHAT_PREFIX + message));
    }

    public void saveMetadata(ClanMetadata metadata) {
        this.metadata = metadata;
        save("context");
    }

    /* Clan Methods */
    public void setName(String name) {
        this.name = name;
        save("name");
    }

    public void setTag(String tag) {
        this.tag = tag;
        save("tag");
    }
    
    /**
     * Retorna a tag com a cor aplicada
     */
    public String getColoredTag() {
        if (tagColor == null || tagColor.isEmpty()) {
            return "§7" + tag;
        }

        ClanColor clanColor = ClanColor.getByCode(tagColor);
        if (clanColor != null && clanColor.hasGradient()) {
            return applyGradientToTag(tag, clanColor);
        }

        return tagColor + tag;
    }

    /**
     * Aplica gradiente a tag do clan
     */
    private String applyGradientToTag(String tag, ClanColor clanColor) {
        String gradientStart = clanColor.getGradientStart();
        String gradientEnd = clanColor.getGradientEnd();
        StringBuilder result = new StringBuilder("[");

        int length = tag.length();
        for (int i = 0; i < length; i++) {
            char c = tag.charAt(i);
            if (c == '[' || c == ']') {
                result.append(c);
                continue;
            }

            float ratio = length > 1 ? (float) i / (length - 1) : 0;
            String color = interpolateColor(gradientStart, gradientEnd, ratio);
            result.append(color).append(c);
        }

        result.append("]");
        return result.toString();
    }

    /**
     * Interpolar entre duas cores
     */
    private String interpolateColor(String color1, String color2, float ratio) {
        return ratio < 0.5 ? color1 : color2;
    }
    
    /**
     * Retorna a tag sem códigos de cor (útil para validação e busca)
     */
    public String getTagWithoutColors() {
        if (tag == null) return "";
        return tag.replaceAll("§[0-9a-fA-F]", "").replaceAll("&[0-9a-fA-F]", "");
    }

    public void setTagColor(String tagColor) {
        this.tagColor = tagColor;
        save("tagColor");
    }

    public boolean isLeader(UUID leader) {
        return this.leader.equals(leader);
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
        save("leader");
    }

    /* Member Methods */
    public Map<UUID, ClanMember> getMembers() {
        return metadata.getMembers();
    }

    public boolean isMember(UUID id) {
        return getMembers().containsKey(id);
    }

    public ClanMember getMember(UUID id) {
        return getMembers().get(id);
    }

    public List<Account> getOnlineMembers() {
        return Core.getAccountData().filteredList(account -> account.isOnline() && isMember(account.getId()));
    }

    public void addMember(Account member) {
        if (isMember(member.getId())) return;

        send("§7" + member.getName() + " entrou.");

        member.setClan(id);

        getMembers().put(member.getId(), new ClanMember(member, ClanRole.MEMBER));
        saveMetadata(metadata);
    }

    public void removeMember(Account member) {
        if (!isMember(member.getId())) return;

        send("§7" + member.getName() + " saiu.");

        member.setClan(Constant.DEFAULT_ID);

        getMembers().remove(member.getId());
        saveMetadata(metadata);
    }

    public void updateMember(ClanMember member) {
        if (!isMember(member.getId())) return;

        getMembers().put(member.getId(), member);
        saveMetadata(metadata);
    }

    /* Emblems Methods */
    public Set<ClanEmblem> getEmblems() {
        return metadata.getEmblems();
    }

    public boolean hasEmblem(ClanEmblem emblem) {
        return getEmblems().contains(emblem);
    }

    public void setEmblem(ClanEmblem emblem) {
        getEmblems().add(emblem);
        saveMetadata(metadata);
    }

    /* Power Methods */
    public int getPower() {
        return metadata.getPower();
    }

    public void setPower(int power) {
        metadata.setPower(power);
        saveMetadata(metadata);
    }

    public void addPower(int power) {
        if (power > 0)
            setPower(getPower() + power);
    }
}