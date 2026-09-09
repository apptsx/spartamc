package com.minecraft.core.account.context.objects.rank;

import com.google.gson.*;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.assignment.Assignment;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.rank.type.role.RankRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Type;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
public class Rank {

    @Builder.Default
    private final RankType type = RankType.MEMBER;

    @Builder.Default
    private final Assignment assignment = Assignment.AUTO;
    @Builder.Default
    private final UUID author = Constant.DEFAULT_ID;

    private final long assignedAt = System.currentTimeMillis();

    @Builder.Default
    private final long expiresAt = -1L;

    public String getName() {
        return getType() != null ? getType().getName() : RankType.MEMBER.getName();
    }

    public String getColoredName() {
        return getType() != null ? getType().getColoredName() : RankType.MEMBER.getColoredName();
    }

    public String getAuthorName() {
        Account account = Core.getAccountData().of(author, false);

        return account != null ? account.getName() : author.equals(Constant.DEFAULT_ID) ? "CONSOLE" : "...";
    }

    public String getColor() {
        if (getType() == null) return RankType.MEMBER.getColor().toString();
        return getType().getColor().toString() + (getType().ordinal() > RankType.PARTNER.ordinal() ? "§o" : "");
    }

    public boolean isPermanent() {
        return expiresAt <= -1L;
    }

    public boolean isPayment() {
        return getType().getRole().equals(RankRole.VIP);
    }

    public boolean hasExpired() {
        return !isPermanent() && expiresAt <= System.currentTimeMillis();
    }

    public static class RankDeserializer implements JsonDeserializer<Rank> {
        @Override
        public Rank deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            
            RankType rankType = null;
            if (object.has("type") && !object.get("type").isJsonNull()) {
                try {
                    rankType = RankType.valueOf(object.get("type").getAsString());
                } catch (IllegalArgumentException e) {
                    Core.getLogger().warning("Rank type inválido encontrado: " + object.get("type").getAsString() + ", usando MEMBER como padrão");
                    rankType = RankType.MEMBER;
                }
            }
            
            return Rank.builder()
                    .type(rankType != null ? rankType : RankType.MEMBER)
                    .build();
        }
    }
}
