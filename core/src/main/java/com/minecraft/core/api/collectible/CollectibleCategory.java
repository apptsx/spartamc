package com.minecraft.core.api.collectible;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

@Getter
@AllArgsConstructor
public enum CollectibleCategory {

    EMOTION("Emoções", asList("§7Selecione uma emoção para", "§7exibir diversas reações.")),
    HAT("Chapéus", asList("§7Selecione um chapéu para", "§7andar com estilo.")),
    BALLOON("Balões", asList("§7Selecione um balão para", "§7mostrar seus exclusivos.")),
    COMPANION("Companheiros", asList("§7Selecione um companheiro", "§7para andar com você.")),
    ARTIFACT("Artefatos", asList("§7Selecione um artefato", "§7para se divertir nos lobbies.")),
    PARTICLE("Partículas", asList("§7Selecione uma partícula", "§7para se destacar.")),
    BANNER("Bandeiras", asList("§7Selecione uma bandeira para", "§7mostrar suas diversidades.")),

    CAPES("Cape", asList("§7Selecione uma capa para", "§7mostrar suas diversidades.")),
    TITLE("Títulos", asList("§7Selecione um título", "§7para esbanjar exclusividade.")),
    PET("Pets", new ArrayList<>()),
    CLOTHING("Trajes", new ArrayList<>()),
    MUTATION("Mutações", new ArrayList<>()),
    JOIN_MESSAGE("Mensagens de Entrada", asList("§7Selecione uma mensagem de entrada", "§7para personalizar sua chegada."));

    private final String name;
    private final List<String> lore;

    public static CollectibleCategory of(String name) {
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(name) || category.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public boolean isRespawnRequiredInWorldChange() {
        switch (this) {
            case BALLOON:
            case COMPANION:
            case PET:
            case ARTIFACT:
                return true;
            default:
                return false;
        }
    }
}
