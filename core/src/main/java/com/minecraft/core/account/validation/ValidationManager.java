package com.minecraft.core.account.validation;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ValidationManager {

    private static final Map<UUID, ValidationCode> pendingValidations = new ConcurrentHashMap<>();
    private static final Map<String, ValidationCode> codeMap = new ConcurrentHashMap<>();
    private static final SecureRandom random = new SecureRandom();
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;

    /**
     * Gera um código de validação único para um jogador
     */
    public static ValidationCode generateCode(UUID playerId, String playerName) {
        // Remove validação anterior se existir
        removeValidation(playerId);
        
        // Gera código único
        String code = generateUniqueCode();
        
        ValidationCode validation = new ValidationCode(playerId, playerName, code);
        pendingValidations.put(playerId, validation);
        codeMap.put(code, validation);
        
        return validation;
    }

    /**
     * Valida um código para um jogador específico
     */
    public static boolean validateCode(String playerName, String code) {
        ValidationCode validation = codeMap.get(code.toUpperCase());
        
        if (validation == null) {
            return false;
        }
        
        if (validation.isExpired()) {
            removeValidation(validation.getPlayerId());
            return false;
        }
        
        if (!validation.getPlayerName().equalsIgnoreCase(playerName)) {
            return false;
        }
        
        validation.setValidated(true);
        return true;
    }

    /**
     * Verifica se um jogador tem validação pendente
     */
    public static boolean hasPendingValidation(UUID playerId) {
        ValidationCode validation = pendingValidations.get(playerId);
        return validation != null && !validation.isExpired();
    }

    /**
     * Verifica se um jogador está validado
     */
    public static boolean isValidated(UUID playerId) {
        ValidationCode validation = pendingValidations.get(playerId);
        return validation != null && validation.isValid();
    }

    /**
     * Obtém o código de validação de um jogador
     */
    public static ValidationCode getValidation(UUID playerId) {
        return pendingValidations.get(playerId);
    }

    /**
     * Remove a validação de um jogador
     */
    public static void removeValidation(UUID playerId) {
        ValidationCode validation = pendingValidations.remove(playerId);
        if (validation != null) {
            codeMap.remove(validation.getCode());
        }
    }

    /**
     * Limpa validações expiradas
     */
    public static void cleanExpired() {
        pendingValidations.entrySet().removeIf(entry -> {
            ValidationCode validation = entry.getValue();
            if (validation.isExpired()) {
                codeMap.remove(validation.getCode());
                return true;
            }
            return false;
        });
    }

    /**
     * Gera um código único que não existe
     */
    private static String generateUniqueCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (codeMap.containsKey(code));
        return code;
    }

    /**
     * Gera um código aleatório
     */
    private static String generateRandomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    /**
     * Retorna o número de validações pendentes
     */
    public static int getPendingCount() {
        return pendingValidations.size();
    }
}
