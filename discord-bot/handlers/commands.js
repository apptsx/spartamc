const { MessageFlags } = require('discord.js');

module.exports = {
    async execute(interaction, client) {
        const command = client.commands.get(interaction.commandName);
        if (!command) return;
        
        try {
            await command.execute(interaction);
        } catch (error) {
            console.error(error);
            await interaction.reply({ 
                content: '❌ Erro ao executar comando.', 
                flags: MessageFlags.Ephemeral 
            });
        }
    }
};
