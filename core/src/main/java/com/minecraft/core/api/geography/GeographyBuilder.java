package com.minecraft.core.api.geography;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeographyBuilder {
    private final String address, country, city, region, asn;

    public boolean isValid() {
        return address != null && country != null && city != null && region != null && asn != null;
    }
}
