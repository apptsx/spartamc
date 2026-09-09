/* ============================================================
    SpartaMC - Discord Webhook Verification
    ============================================================ */

class DiscordWebhook {
    constructor() {
        // Seu webhook URL
        this.webhookUrl = 'https://discord.com/api/webhooks/1462499183837757440/Tkq6VrqVrp2eX9pUYOXmX4oE8iG7qNjZm2L9qE3xYv5kQ3L9xKm4nL2pQr9sTm8nLk2pQr9sTm8nLk2pQr'; // Substitua pelo seu webhook
    }
    
    // Enviar mensagem via webhook
    async sendVerificationCode(userId, username, code) {
        const payload = {
            embeds: [
                {
                    title: '🔐 Código de Verificação - SpartaMC',
                    description: `Olá ${username}! Aqui está seu código de verificação:`,
                    color: 0xf97316,
                    fields: [
                        {
                            name: '📝 Código',
                            value: `\`${code}\``,
                            inline: true
                        },
                        {
                            name: '⏱️ Expira em',
                            value: '15 minutos',
                            inline: true
                        }
                    ],
                    footer: {
                        text: 'SpartaMC - Sistema de Verificação'
                    },
                    timestamp: new Date().toISOString()
                }
            ]
        };
        
        try {
            const response = await fetch(this.webhookUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });
            
            return response.ok;
        } catch (error) {
            console.error('Erro ao enviar webhook:', error);
            return false;
        }
    }
    
    // Notificar verificação concluída
    async notifyVerificationComplete(username) {
        const payload = {
            embeds: [
                {
                    title: '✅ Conta Verificada!',
                    description: `${username} verificou sua conta no SpartaMC!`,
                    color: 0x23a55a,
                    footer: {
                        text: 'SpartaMC'
                    },
                    timestamp: new Date().toISOString()
                }
            ]
        };
        
        try {
            await fetch(this.webhookUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });
        } catch (e) {
            console.error('Erro:', e);
        }
    }
}

const discordWebhook = new DiscordWebhook();

// Funções globais
window.sendDiscordCode = async (userId, username, code) => {
    return await discordWebhook.sendVerificationCode(userId, username, code);
};
