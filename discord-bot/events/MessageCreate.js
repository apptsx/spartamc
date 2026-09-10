const xpSystem = require('../handlers/xpSystem');

module.exports = {
    name: 'messageCreate',
    async execute(message, client) {
        // Ignorar mensagens de bots
        if (message.author.bot) return;
        
        // ============================================
        // SISTEMA DE XP
        // ============================================
        await xpSystem.handleMessage(message, client);
    }
};
