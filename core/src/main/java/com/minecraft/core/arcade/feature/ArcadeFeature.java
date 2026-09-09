package com.minecraft.core.arcade.feature;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArcadeFeature {

    CUSTOM_USER("Usuário Customizado"),
    DAMAGE("Dano"),
    FALL_DAMAGE("Dano de Queda"),
    EDITABLE_MENU("Menu Editável"),
    BUILD("Construções"),
    CABINS("Cabines"),
    LIQUID("Líquidos"),
    NOT_TAKE_LIFE("Sem tirar vida"),
    DROPS_ALL("Dropar todos itens"),
    WITHOUT_MOVING_STARTING("Sem se mexer"),
    NOT_TELEPORT_STARTING("Não teleportar ao iniciar"),
    LEVEL_BAR_XP("Barra de Progresso"),
    SCORE_DEATH("Pontuar na Morte"),
    NOT_PULL_BACK("Sem voltar pra trás"),
    CAN_MOVE_ARMOR("Mexer na armadura");

    private final String name;
}
