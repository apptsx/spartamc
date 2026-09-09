package com.minecraft.core.member.context.level.symbol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LevelSymbol {

    // Níveis 1-99 (símbolos básicos)
    ONE("✩"),
    TWO("✫"),
    THREE("✬"),
    FOUR("✮"),
    FIVE("✯"),
    SIX("✰"),
    SEVEN("✱"),
    EIGHT("✲"),
    NINE("✳"),
    TEN("✴"),

    // Níveis 100-999 (símbolos especiais)
    ELEVEN("✵"),
    TWELVE("✶"),
    THIRTEEN("✷"),
    FOURTEEN("✸"),
    FIFTEEN("✹"),
    SIXTEEN("✺"),
    SEVENTEEN("✻"),
    EIGHTEEN("✼"),
    NINETEEN("✽"),

    // Níveis 1000+ (símbolos especiais altos)
    TWENTY("✾"),
    TWENTYONE("✿"),
    TWENTYTWO("❀"),
    TWENTYTHREE("❁"),
    TWENTYFOUR("❂"),
    TWENTYFIVE("❃"),
    TWENTYSIX("❇"),
    TWENTYSEVEN("❈"),
    TWENTYEIGHT("❉"),
    TWENTYNINE("❊");

    private final String symbol;
}