package com.minecraft.core.api.skin;

import com.google.gson.JsonObject;
import com.minecraft.core.Core;
import com.minecraft.core.api.skin.objects.SkinType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.json.JSONException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@ToString
public class Skin {

    private final SkinType type;

    private final UUID id;
    private final String displayName;

    private final String value, signature;

    public static Skin library(String displayName, String value, String signature) {
        return new Skin(SkinType.LIBRARY, UUID.randomUUID(), displayName, value, signature);
    }

    public static Skin unknown() {
        return new Skin(SkinType.CUSTOM, UUID.randomUUID(), "Unknown",
                "ewogICJ0aW1lc3RhbXAiIDogMTU5ODUzOTYwMTgxNywKICAicHJvZmlsZUlkIiA6ICI5MThhMDI5NTU5ZGQ0Y2U2YjE2ZjdhNWQ1M2VmYjQxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJCZWV2ZWxvcGVyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJiNzhmODZkNTdhZjQ1ZTA2ODQyN2NlNzUwZGRiYTdmM2ZmYmRiMmZlMTgxM2Y0MTY5NGE4N2NmOWFlZmU3MjYiCiAgICB9CiAgfQp9",
                "jQ/uFhYLSpDIdJmtyyq1vMWw9RDV+fxiREuG16bbODYSrGWNn5uPbGToAcKbhx3hBsZAbHpo/Tpr1/pMRQQy6H1MeICHmKohSXm/cbnf9lVSztxZfhu7+iMvqNzE1nAP7LMCF2hr+A6USOXsMldsIhtoR/g7x8ikQmWsNPfBb2VYFguhBHlk/nJYtYyjm7QC5TTeIja7OLv6MWzCTSZlMsEXzpJ6WHRoj6V0p5Z9sEihewhkMerYBOGFMlSxTDkD6wSinOxp3mkvcrEjHEDifrNx6QeiHcvq8DDrDdKgtYkIb3aCqXEz9GrOBxl2gVkd5b3XfTLYHGT5AaYaI1gXRG5TMSjAvmNysAheDK6xGQohPzmwyHanf9TgcmeX2FiqZaO6rgN98sgzqYBKdeV5MNoWbB3etK0OFXDBgvWTymtzc1tqq6B3XxQLNNqEwzKYrlF75Xs+Qj5y1OsqvKTDrY67NN5mWQz86BtFPhbIoG9kl6/QmlXwsY0qllwVGETT+pUq4hof4eHt5skx9ZbSJ6w3NwIzGP3rEl5XKMUPbCvmbW6E3w+UpAtDna0NqJnF1s8yD9N7Eo4fSPHPAUrwNbCpjQs1dmzTR8wltqpODbUs67ubLpz26reFO4Uf2EFwyRslaeYNU48wgYWxrYvf1iTxQooIZwbC62Jmdim9ABY=");
    }

    public static Skin empty() {
        return new Skin(SkinType.CUSTOM, UUID.randomUUID(), "Nula", "", "");
    }

    @Override
    public boolean equals(Object skinObj) {
        if (this == skinObj) return true;
        if (skinObj == null || getClass() != skinObj.getClass()) return false;

        Skin otherSkin = (Skin) skinObj;

        try {
            String thisTextureUrl = extractTextureUrl(this.value);
            String otherTextureUrl = extractTextureUrl(otherSkin.value);

            return thisTextureUrl.equalsIgnoreCase(otherTextureUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String extractTextureUrl(String base64Value) throws IllegalArgumentException {
        try {
            String decodedJson = new String(Base64.getDecoder().decode(base64Value.trim()), StandardCharsets.UTF_8);

            JsonObject json = Core.GSON.fromJson(decodedJson, JsonObject.class);

            return json.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
        } catch (JSONException e) {
            throw new IllegalArgumentException("Formato inválido de JSON no valor Base64: " + base64Value, e);
        }
    }

    public boolean isValid() {
        if (value == null || signature == null || displayName == null) {
            return false;
        }
        
        if (value.isEmpty() || signature.isEmpty() || displayName.isEmpty()) {
            return false;
        }
        
        try {
            String decodedJson = new String(Base64.getDecoder().decode(value.trim()), StandardCharsets.UTF_8);
            JsonObject json = Core.GSON.fromJson(decodedJson, JsonObject.class);
            
            if (!json.has("textures")) {
                return false;
            }
            
            JsonObject textures = json.getAsJsonObject("textures");
            if (!textures.has("SKIN")) {
                return false;
            }
            
            JsonObject skin = textures.getAsJsonObject("SKIN");
            return skin.has("url") && !skin.get("url").getAsString().isEmpty();
            
        } catch (Exception e) {
            return false;
        }
    }
}
