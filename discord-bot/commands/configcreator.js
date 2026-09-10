const { SlashCommandBuilder, PermissionFlagsBits, MessageFlags } = require('discord.js');
const fs = require('fs');
const path = require('path');

const configPath = path.join(__dirname, '../creator_config.json');

function readConfig() {
    if (!fs.existsSync(configPath)) return {};
    try {
        return JSON.parse(fs.readFileSync(configPath, 'utf8'));
    } catch (e) {
        return {};
    }
}

function writeConfig(data) {
    fs.writeFileSync(configPath, JSON.stringify(data, null, 2));
}

module.exports = {
    help: {
        name: 'configcreator',
        description: 'Sparta » Configurar sistema de creator'
    },
    data: new SlashCommandBuilder()
        .setName('configcreator')
        .setDescription('Sparta » Configurar sistema de creator')
        .setDefaultMemberPermissions(PermissionFlagsBits.Administrator)
        .addChannelOption(option =>
            option.setName('log_channel')
                .setDescription('Sparta » Configurar sistema de creator')
                .setRequired(true))
        .addRoleOption(option =>
            option.setName('creator_role')
                .setDescription('Sparta » Configurar sistema de creator')
                .setRequired(true)),

    async execute(interaction) {
        if (!interaction.member.permissions.has(PermissionFlagsBits.Administrator)) {
            return interaction.reply({ 
                content: '❌ Apenas administradores podem usar este comando!', 
                flags: MessageFlags.Ephemeral 
            });
        }

        const logChannel = interaction.options.getChannel('log_channel');
        const creatorRole = interaction.options.getRole('creator_role');

        if (!logChannel.isTextBased()) {
            return interaction.reply({ 
                content: '❌ O canal precisa ser um canal de texto!', 
                flags: MessageFlags.Ephemeral 
            });
        }

        const config = readConfig();
        
        config[interaction.guild.id] = {
            logChannelId: logChannel.id,
            creatorRoleId: creatorRole.id,
            configuredBy: interaction.user.id,
            configuredAt: new Date().toISOString()
        };
        
        writeConfig(config);

        await interaction.reply({ 
            content: `✅ **Sistema de Creator configurado com sucesso!**\n\n📢 **Canal de logs:** ${logChannel}\n🎥 **Cargo de Creator:** ${creatorRole}\n\nAgora use \`/creator\` para enviar o painel de solicitações.`, 
            flags: MessageFlags.Ephemeral 
        });

        try {
            await logChannel.send({ 
                content: `✅ **Sistema de solicitações de Creator configurado!**\nAs solicitações serão registradas aqui.\nCargo a ser dado: ${creatorRole}` 
            });
        } catch (e) {
            console.error('Erro ao enviar mensagem de teste:', e);
        }
    }
};