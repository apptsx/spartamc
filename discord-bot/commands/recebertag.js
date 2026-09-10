const { SlashCommandBuilder } = require('discord.js');
const { createPool } = require('../utils/database');

const ROLE_RANK_MAP = {
    '1508520440442785866': 'PARTNERPLUS',
    '1508520449443758311': 'PARTNER',
    '1508520475398246551': 'MAX',
    '1508520476408807506': 'VIP',
    '1508520451595571361': 'BETA'
};

const ROLE_PRIORITY = [
    '1508520440442785866',
    '1508520449443758311',
    '1508520475398246551',
    '1508520476408807506',
    '1508520451595571361'
];

module.exports = {
    help: {
        name: 'recebertag',
        description: 'Sparta » Receba seu rank do Discord no jogo'
    },
    data: new SlashCommandBuilder()
        .setName('recebertag')
        .setDescription('Sparta » Receba seu rank do Discord no jogo')
        .addStringOption(option =>
            option.setName('nick')
                .setDescription('Seu nickname no Minecraft')
                .setRequired(true)),

    async execute(interaction) {
        await interaction.deferReply({ ephemeral: true });

        const nick = interaction.options.getString('nick').trim();
        const discordId = interaction.user.id;

        if (nick.length < 2 || nick.length > 16) {
            return interaction.editReply({
                content: '<:cross:1500966712525324288> Nickname inválido! Deve ter entre 2 e 16 caracteres.'
            });
        }

        try {
            const pool = await createPool();

            const [allUserAccounts] = await pool.execute(
                "SELECT id, name, data FROM accounts WHERE JSON_EXTRACT(data, '$.discord.id') = ?",
                [discordId]
            );

            for (const acc of allUserAccounts) {
                let accData;
                try { accData = typeof acc.data === 'string' ? JSON.parse(acc.data) : acc.data; } catch { accData = {}; }
                if (accData.recebertag_used) {
                    return interaction.editReply({
                        content: '<:cross:1500966712525324288> Você já utilizou o comando `/recebertag` uma vez!'
                    });
                }
            }

            const [targetAccounts] = await pool.execute(
                'SELECT id, name, data FROM accounts WHERE name = ?',
                [nick]
            );

            if (targetAccounts.length > 0) {
                let targetData;
                try { targetData = typeof targetAccounts[0].data === 'string' ? JSON.parse(targetAccounts[0].data) : targetAccounts[0].data; } catch { targetData = {}; }

                const linkedDiscordId = targetData.discord?.id;
                if (linkedDiscordId && linkedDiscordId !== discordId) {
                    return interaction.editReply({
                        content: '<:cross:1500966712525324288> Este nickname já está vinculado a outro Discord!'
                    });
                }

                if (targetData.recebertag_used) {
                    return interaction.editReply({
                        content: '<:cross:1500966712525324288> Este nickname já recebeu o rank pelo `/recebertag`!'
                    });
                }
            }

            const member = await interaction.guild.members.fetch(interaction.user.id);
            const memberRoles = member.roles.cache;

            let matchedRank = null;

            for (const roleId of ROLE_PRIORITY) {
                if (memberRoles.has(roleId)) {
                    matchedRank = ROLE_RANK_MAP[roleId];
                    matchedRoleId = roleId;
                    break;
                }
            }

            if (!matchedRank) {
                return interaction.editReply({
                    content: '<:cross:1500966712525324288> Você não possui nenhum cargo de rank no Discord!'
                });
            }

            const now = Date.now();

            if (targetAccounts.length === 0) {
                const crypto = require('crypto');
                const uuid = crypto.randomUUID();
                const accountData = {
                    id: uuid,
                    name: nick,
                    discord: { id: discordId, tag: interaction.user.tag, linkedAt: now },
                    context: {
                        route: { serverId: 0, serverPort: 0, updatedAt: 0 },
                        rankInfo: {
                            rank: { type: matchedRank, assignment: 'AUTO', author: '00000000-0000-0000-0000-000000000000', assignedAt: now, expiresAt: -1 },
                            availableRanks: [],
                            rankSetTimestamp: now
                        },
                        fake: { nick: '', lastNick: '', updatedAt: now },
                        skin: {
                            skin: { type: 'CUSTOM', id: uuid, displayName: 'Nula', value: '', signature: '', url: '' },
                            skinSetTimestamp: 0
                        }
                    },
                    recebertag_used: true
                };
                await pool.execute(
                    'INSERT INTO accounts (id, name, data) VALUES (?, ?, ?)',
                    [uuid, nick, JSON.stringify(accountData)]
                );
            } else {
                let accountData;
                try { accountData = typeof targetAccounts[0].data === 'string' ? JSON.parse(targetAccounts[0].data) : targetAccounts[0].data; } catch { accountData = {}; }

                if (!accountData.context) accountData.context = {};
                if (!accountData.context.rankInfo) {
                    accountData.context.rankInfo = {
                        rank: { type: 'MEMBER', assignment: 'AUTO', author: '00000000-0000-0000-0000-000000000000', assignedAt: 0, expiresAt: -1 },
                        availableRanks: [],
                        rankSetTimestamp: 0
                    };
                }

                accountData.context.rankInfo.rank = {
                    type: matchedRank,
                    assignment: 'AUTO',
                    author: '00000000-0000-0000-0000-000000000000',
                    assignedAt: now,
                    expiresAt: -1
                };
                accountData.context.rankInfo.rankSetTimestamp = now;
                accountData.discord = { id: discordId, tag: interaction.user.tag, linkedAt: accountData.discord?.linkedAt || now };
                accountData.recebertag_used = true;

                await pool.execute(
                    'UPDATE accounts SET data = ? WHERE id = ?',
                    [JSON.stringify(accountData), targetAccounts[0].id]
                );
            }

            await interaction.editReply({
                content: '<:Correto:1478405275529777163> Seu rank foi setado com sucesso no servidor, aguarde até o lançamento.'
            });

        } catch (error) {
            console.error('Erro no recebertag:', error);
            await interaction.editReply({
                content: `<:cross:1500966712525324288> Erro ao processar: ${error.message}`
            });
        }
    }
};
