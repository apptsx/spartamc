package com.minecraft.core.controller.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.skin.Skin;
import com.minecraft.core.api.skin.objects.SkinType;
import com.mojang.authlib.properties.Property;

import java.util.UUID;
import java.util.logging.Level;

public class SkinController {

    public static Skin getSkin(UUID id, String displayName, SkinType type) {
        try {
            Property property = Core.MOJANG_API.getTextures(id);

            return new Skin(type, id, displayName, property.getValue(), property.getSignature());
        } catch (Exception e) {
            Core.getLogger().log(Level.SEVERE, "Não foi possível carregar a skin " + id, e);
            return null;
        }
    }
}
