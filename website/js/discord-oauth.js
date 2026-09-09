/* ============================================================
    SpartaMC - Discord OAuth2 Verification
    ============================================================ */

class DiscordOAuth {
    constructor() {
        this.clientId = 'SEU_CLIENT_ID';
        this.redirectUri = window.location.origin + '/verify-discord.html';
        this.guildId = 'SEU_GUILD_ID';
        this.configLoaded = this.loadConfig();
        
        this.verifyDB = new SecureDB('sparta_discord_verify');
    }
    
    async loadConfig() {
        try {
            const res = await fetch('config.json');
            const config = await res.json();
            if (config.discord) {
                if (config.discord.client_id) this.clientId = config.discord.client_id;
                if (config.discord.guild_id) this.guildId = config.discord.guild_id;
            }
        } catch (e) {
            console.error('Falha ao carregar config.json (usa valores padrão):', e);
        }
    }
    
    // Gerar URL de autorização
    getAuthUrl() {
        const state = Math.random().toString(36).substring(2, 15);
        sessionStorage.setItem('discord_oauth_state', state);
        
        const scopes = ['identify', 'guilds'];
        
        return `https://discord.com/api/oauth2/authorize?client_id=${this.clientId}&redirect_uri=${encodeURIComponent(this.redirectUri)}&response_type=code&scope=${encodeURIComponent(scopes.join(' '))}&state=${state}`;
    }
    
    // Verificar se o usuário está no servidor
    async checkGuildMembership(accessToken) {
        try {
            const response = await fetch(`https://discord.com/api/v10/users/@me/guilds/${this.guildId}/member`, {
                headers: {
                    'Authorization': `Bearer ${accessToken}`
                }
            });
            
            return response.ok;
        } catch (error) {
            console.error('Erro ao verificar membership:', error);
            return false;
        }
    }
    
    // Obter informações do usuário
    async getUserInfo(accessToken) {
        try {
            const response = await fetch('https://discord.com/api/v10/users/@me', {
                headers: {
                    'Authorization': `Bearer ${accessToken}`
                }
            });
            
            if (response.ok) {
                return await response.json();
            }
            return null;
        } catch (error) {
            console.error('Erro ao obter info:', error);
            return null;
        }
    }
    
    // Salvar verificação
    saveVerification(userId, discordData) {
        const verifyData = {
            discordId: discordData.id,
            discordUsername: discordData.username,
            discordDiscriminator: discordData.discriminator,
            discordAvatar: discordData.avatar,
            verifiedAt: Date.now()
        };
        
        this.verifyDB.set(userId, verifyData);
        return verifyData;
    }
    
    // Verificar se usuário está verificado
    isVerified(userId) {
        const data = this.verifyDB.get(userId);
        return data && data.discordId;
    }
    
    // Obter dados de verificação
    getVerification(userId) {
        return this.verifyDB.get(userId);
    }
}

const discordOAuth = new DiscordOAuth();

// Funções globais
window.getDiscordAuthUrl = () => discordOAuth.getAuthUrl();
window.isDiscordVerified = (userId) => discordOAuth.isVerified(userId);
window.getDiscordVerification = (userId) => discordOAuth.getVerification(userId);
