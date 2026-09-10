const { SlashCommandBuilder, PermissionFlagsBits } = require('discord.js');
const { createPool } = require('../utils/database');

module.exports = {
    help: {
        name: 'revogue',
        description: 'Sparta » Admin: revoga o uso do /recebertag de um usuario'
    },
    data: new SlashCommandBuilder()
        .setName('revogue')
        .setDescription('Sparta » Admin: revoga o uso do /recebertag de um usuario')
        .setDefaultMemberPermissions(PermissionFlagsBits.Administrator)
        .addUserOption(option =>
            option.setName('usuario')
                .setDescription('Usuario que usou o /recebertag')
                .setRequired(true)),

    async execute(interaction) {
        if (!interaction.member.permissions.has(PermissionFlagsBits.Administrator)) {
            return interaction.reply({ content: '❌ Apenas administradores podem usar este comando!', ephemeral: true });
        }

        await interaction.deferReply({ ephemeral: true });

        const target = interaction.options.getUser('usuario');
        const discordId = target.id;

        try {
            const pool = await createPool();

            const [accounts] = await pool.execute(
                "SELECT id, name, data FROM accounts WHERE JSON_EXTRACT(data, '$.discord.id') = ?",
                [discordId]
            );

            const affected = accounts.filter(acc => {
                let d;
                try { d = typeof acc.data === 'string' ? JSON.parse(acc.data) : acc.data; } catch { d = {}; }
                return d.recebertag_used === true;
            });

            if (affected.length === 0) {
                return interaction.editReply({
                    content: '❌ Este usuario nao possui registros do `/recebertag` para revogar.'
                });
            }

            for (const acc of affected) {
                let data;
                try { data = typeof acc.data === 'string' ? JSON.parse(acc.data) : acc.data; } catch { data = {}; }

                delete data.recebertag_used;

                if (data.context?.rankInfo?.rank) {
                    data.context.rankInfo.rank = {
                        type: 'MEMBER',
                        assignment: 'REVOKED',
                        author: interaction.user.id,
                        assignedAt: data.context.rankInfo.rank.assignedAt || Date.now(),
                        expiresAt: -1
                    };
                    data.context.rankInfo.rankSetTimestamp = Date.now();
                }

                await pool.execute(
                    'UPDATE accounts SET data = ? WHERE id = ?',
                    [JSON.stringify(data), acc.id]
                );
            }

            await interaction.editReply({
                content: `<:Correto:1478405275529777163> Revogado o uso do \`/recebertag\` de **${target.tag}** (${affected.length} conta${affected.length > 1 ? 's' : ''} afetada${affected.length > 1 ? 's' : ''}). Ele(a) ja pode usar o comando novamente.`
            });

        } catch (error) {
            console.error('Erro no revogue:', error);
            await interaction.editReply({
                content: `❌ Erro ao revogar: ${error.message}`
            });
        }
    }
};
