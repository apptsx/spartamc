/* ============================================================
    SpartaMC - Discord Verification System
    ============================================================ */

class DiscordVerification {
    constructor() {
        this.verifyDB = new SecureDB('sparta_verify_secure');
    }
    
    // Gerar código de verificação
    generateCode(userId) {
        const code = Math.floor(100000 + Math.random() * 900000).toString();
        
        const verifyData = {
            userId: userId,
            code: code,
            createdAt: new Date().toISOString(),
            verified: false
        };
        
        this.verifyDB.set('pending_' + userId, verifyData);
        
        return code;
    }
    
    // Verificar código
    verifyCode(userId, code) {
        const pending = this.verifyDB.get('pending_' + userId);
        
        if (!pending) {
            return { success: false, error: 'Nenhum código pendente. Gere um novo código.' };
        }
        
        if (pending.verified) {
            return { success: false, error: 'Conta já verificada!' };
        }
        
        // Verificar se expirou (15 minutos)
        const created = new Date(pending.createdAt);
        const now = new Date();
        const diff = (now - created) / (1000 * 60);
        
        if (diff > 15) {
            this.verifyDB.delete('pending_' + userId);
            return { success: false, error: 'Código expirado. Gere um novo código.' };
        }
        
        if (pending.code !== code) {
            return { success: false, error: 'Código incorreto!' };
        }
        
        // Verificação bem-sucedida
        pending.verified = true;
        this.verifyDB.set('pending_' + userId, pending);
        
        return { success: true };
    }
    
    // Verificar se usuário está verificado
    isVerified(userId) {
        const pending = this.verifyDB.get('pending_' + userId);
        return pending && pending.verified;
    }
    
    // Obter código atual
    getPendingCode(userId) {
        return this.verifyDB.get('pending_' + userId);
    }
    
    // Limpar código
    clearCode(userId) {
        this.verifyDB.delete('pending_' + userId);
    }
}

const discordVerify = new DiscordVerification();

// Funções globais
window.generateDiscordCode = (userId) => discordVerify.generateCode(userId);
window.verifyDiscordCode = (userId, code) => discordVerify.verifyCode(userId, code);
window.isDiscordVerified = (userId) => discordVerify.isVerified(userId);
