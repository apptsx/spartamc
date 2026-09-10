const { REST, Routes } = require('discord.js');
const config = require('../config.json');
const { carregarTickets } = require('../handlers/tickets.js');

module.exports = {
    name: 'ready',
    once: true,
    async execute(client) {
        console.log(`✅ Bot logado como ${client.user.tag}`);
        
        // Carregar tickets do arquivo
        carregarTickets();
        
        const commands = [];
        client.commands.forEach(command => {
            commands.push(command.data.toJSON());
        });

        const rest = new REST({ version: '10' }).setToken(config.token);

        try {
            console.log('🔄 Registrando comandos em todos os servidores...');
            const guilds = client.guilds.cache;
            if (guilds.size > 0) {
                for (const [guildId] of guilds) {
                    try {
                        await rest.put(
                            Routes.applicationGuildCommands(config.clientId, guildId),
                            { body: commands }
                        );
                        console.log(`✅ Comandos registrados para guild ${guildId}`);
                    } catch (gErr) {
                        console.error(`❌ Erro ao registrar comandos para guild ${guildId}:`, gErr.message);
                    }
                }
            } else {
                await rest.put(
                    Routes.applicationCommands(config.clientId),
                    { body: commands }
                );
                console.log('✅ Comandos registrados globalmente');
            }
        } catch (error) {
            console.error('❌ Erro ao registrar comandos:', error);
        }

        // Iniciar verificação de expiração de cargos Creator
        try {
            const { iniciarVerificacaoExpiracao } = require('../commands/creator');
            iniciarVerificacaoExpiracao(client);
        } catch (e) {
            console.log('Sistema de creator não disponível:', e.message);
        }
    }
};
