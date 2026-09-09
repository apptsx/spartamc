package com.minecraft.core.api.geography;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.util.list.http.HttpRequest;
import com.google.gson.JsonObject;

import java.util.logging.Level;

public class GeographyService {

    public static GeographyBuilder getGeography(String address) {
        try {
            HttpRequest request = HttpRequest.get("https://ipinfo.io/" + address + "/json")
                    .connectTimeout(5000)
                    .readTimeout(5000)
                    .userAgent(Constant.SERVER_NAME.toLowerCase() + "/1.0.0")
                    .accept(HttpRequest.CONTENT_TYPE_JSON);

            int code = request.code();

            if (code == 200 || request.ok()) {
                JsonObject data = Core.PARSER.parse(request.reader()).getAsJsonObject();

                if (data.has("bogon") && data.get("bogon").getAsBoolean())
                    return GeographyBuilder.builder().build();

                return GeographyBuilder.builder()
                        .address(address)
                        .country(data.get("country").getAsString())
                        .city(data.get("city").getAsString())
                        .region(data.get("region").getAsString())
                        .asn(data.has("org") ? data.get("org").getAsString() : "N/A")
                        .build();
            } else
                return GeographyBuilder.builder().build();

        } catch (Exception e) {
            Core.getLogger().log(Level.INFO, "Não foi possível requisitar os dados do IP: " + address);
        }

        return GeographyBuilder.builder().build();
    }
}
