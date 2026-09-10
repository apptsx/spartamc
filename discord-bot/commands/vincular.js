const { SlashCommandBuilder, EmbedBuilder } = require('discord.js');
const { createPool } = require('../utils/database');

function generateCode() {
    return Math.random().toString(36).substring(2, 8).toUpperCase();
}

module.exports = {
    help: {
        name: 'vincular',
        description: 'Sparta » Vincular conta do Discord ao Minecraft'
    },
    data: new SlashCommandBuilder()
        .setName('vincular')
        .setDescription('Sparta » Vincular conta do Discord ao Minecraft')
        .addStringOption(option =>
            option.setName('codigo')
                .setDescription('Código recebido no jogo')
                .setRequired(true)),

    async execute(interaction) {
        await interaction.deferReply({ ephemeral: true });
        
        const code = interaction.options.getString('codigo').toUpperCase();
        const discordId = interaction.user.id;
        const discordTag = interaction.user.tag;
        
        try {
            const pool = await createPool();
            
            const [codes] = await pool.execute(
                'SELECT * FROM link_codes WHERE code = ? AND used = 0',
                [code]
            );
            
            if (codes.length === 0) {
                return interaction.editReply({
                    content: '<:cross:1500966712525324288> Código inválido ou já utilizado!'
                });
            }
            
            const codeData = codes[0];
            const expiresAt = codeData.expires_at;
            
            if (Date.now() > expiresAt) {
                return interaction.editReply({
                    content: '<:cross:1500966712525324288> Código expirado! Solicite um novo código no jogo.'
                });
            }
            
            const [accounts] = await pool.execute(
                'SELECT * FROM accounts WHERE name = ?',
                [codeData.player_name]
            );
            
            if (accounts.length === 0) {
                return interaction.editReply({
                    content: '<:cross:1500966712525324288> Conta não encontrada!'
                });
            }
            
            const account = accounts[0];
            let data;
            try {
                data = JSON.parse(account.data);
            } catch {
                data = {};
            }
            
            if (data.discord && data.discord.id) {
                return interaction.editReply({
                    content: '<:cross:1500966712525324288> Esta conta já está vinculada a outro Discord!'
                });
            }
            
            data.discord = {
                id: discordId,
                tag: discordTag,
                linkedAt: Date.now()
            };
            
            await pool.execute(
                'UPDATE accounts SET data = ? WHERE name = ?',
                [JSON.stringify(data), codeData.player_name]
            );
            
            await pool.execute(
                'UPDATE link_codes SET used = 1, linked_discord_id = ?, used_at = ? WHERE code = ?',
                [discordId, Date.now(), code]
            );
            
            const embed = new EmbedBuilder()
                .setColor(0x55FF55)
                .setTitle('<:check:1500966707961921657> Conta Vinculada!')
                .setDescription(
                    `✅ Sua conta foi vinculada com sucesso!\n\n` +
                    `**Jogador:** ${account.name}\n` +
                    `**Discord:** ${interaction.user}\n\n` +
                    `Agora você pode usar todos os comandos do bot!`
                )
                .setFooter({ text: 'Sparta MC' })
                .setTimestamp();
            
            await interaction.editReply({ embeds: [embed] });
            
        } catch (error) {
            console.error('Erro ao vincular:', error);
            await interaction.editReply({
                content: `<:cross:1500966712525324288> Erro ao vincular conta: ${error.message}`
            });
        }
    }
};
