require('dotenv').config();
const { Client, GatewayIntentBits, Routes, EmbedBuilder, PermissionFlagsBits } = require('discord.js');
const { REST } = require('discord.js');
const crypto = require('crypto');
const fetch = (...args) => import('node-fetch').then(m => m.default(...args));

const DISCORD_TOKEN   = process.env.DISCORD_TOKEN;
const CLIENT_ID       = process.env.DISCORD_CLIENT_ID  || '';
const GUILD_ID        = process.env.DISCORD_GUILD_ID   || '';
const BOT_SECRET      = process.env.BOT_SECRET         || 'troque_este_secret';
const API_URL         = process.env.API_URL            || 'http://localhost:3000';
const ADMIN_ROLE_ID   = process.env.ADMIN_ROLE_ID      || null;
const ADMIN_ROLE_NAME = process.env.ADMIN_ROLE_NAME    || 'Admin';

const RANKS = ['laranja', 'helper', 'trial', 'mod', 'mod+', 'admin'];

const client = new Client({
    intents: [
        GatewayIntentBits.Guilds,
        GatewayIntentBits.GuildMembers,
        GatewayIntentBits.DirectMessages,
        GatewayIntentBits.GuildMessages
    ]
});

const verificationCodes = new Map();

// ── Verificar se é admin ──────────────────────────────────────────────────────
function isAdmin(member) {
    if (!member) return false;
    if (member.guild.ownerId === member.id) return true;
    if (ADMIN_ROLE_ID && member.roles.cache.has(ADMIN_ROLE_ID)) return true;
    if (member.roles.cache.some(r => r.name.toLowerCase() === ADMIN_ROLE_NAME.toLowerCase())) return true;
    return false;
}

// ── Comandos ──────────────────────────────────────────────────────────────────
const commands = [
    {
        name: 'verificar',
        description: 'Gere um código para vincular sua conta do SpartaMC'
    },
    {
        name: 'setrank',
        description: '🔒 [Admin] Define o rank de um jogador no site',
        options: [
            { name: 'nickname', description: 'Nickname do jogador no site', type: 3, required: true },
            {
                name: 'rank', description: 'Rank a definir', type: 3, required: true,
                choices: RANKS.map(r => ({ name: r, value: r }))
            }
        ]
    },
    {
        name: 'removerank',
        description: '🔒 [Admin] Remove o rank de um jogador no site',
        options: [
            { name: 'nickname', description: 'Nickname do jogador no site', type: 3, required: true }
        ]
    },
    {
        name: 'rank',
        description: 'Consulta o rank de um jogador no site',
        options: [
            { name: 'nickname', description: 'Nickname do jogador', type: 3, required: true }
        ]
    }
];

async function registerCommands() {
    const rest = new REST({ version: '10' }).setToken(DISCORD_TOKEN);
    try {
        await rest.put(Routes.applicationGuildCommands(CLIENT_ID, GUILD_ID), { body: commands });
        console.log('✅ Comandos do bot registrados');
    } catch (e) {
        console.error('❌ Erro ao registrar comandos:', e.message);
    }
}

async function apiSetRank(nickname, rank) {
    const res = await fetch(`${API_URL}/api/admin/setrank`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ bot_secret: BOT_SECRET, nickname, rank })
    });
    return res.json();
}

// ── Eventos ───────────────────────────────────────────────────────────────────
client.on('ready', () => {
    console.log(`🤖 Bot online: ${client.user.tag}`);
    registerCommands();
});

client.on('interactionCreate', async (interaction) => {
    if (!interaction.isChatInputCommand()) return;

    // /verificar
    if (interaction.commandName === 'verificar') {
        const code = crypto.randomInt(100000, 999999).toString();
        verificationCodes.set(interaction.user.id, { code, expires: Date.now() + 15 * 60 * 1000 });

        try {
            const dm = await interaction.user.createDM();
            await dm.send({
                embeds: [new EmbedBuilder()
                    .setTitle('🔐 Código de Verificação - SpartaMC')
                    .setDescription(`Seu código: **${code}**`)
                    .setColor(0xf97316)
                    .addFields(
                        { name: '⏱️ Expira em', value: '15 minutos', inline: true },
                        { name: '📝 Como usar', value: 'Insira no site em Configurações → Discord', inline: true }
                    )
                    .setFooter({ text: 'SpartaMC' })]
            });
            await interaction.reply({ content: '✅ Código enviado via DM!', ephemeral: true });
        } catch {
            await interaction.reply({ content: '❌ Não consegui enviar DM. Abra suas mensagens privadas.', ephemeral: true });
        }
    }

    // /setrank
    if (interaction.commandName === 'setrank') {
        await interaction.deferReply({ ephemeral: true });

        if (!isAdmin(interaction.member)) {
            return interaction.editReply({ content: '🚫 Apenas administradores podem usar este comando.' });
        }

        const nickname = interaction.options.getString('nickname');
        const rank     = interaction.options.getString('rank');

        try {
            const data = await apiSetRank(nickname, rank);
            if (!data.success) return interaction.editReply({ content: `❌ ${data.error}` });

            await interaction.editReply({
                embeds: [new EmbedBuilder()
                    .setTitle('✅ Rank Definido')
                    .setDescription(`**${nickname}** agora tem o rank **${rank}** no site.`)
                    .setColor(0xf97316)
                    .setThumbnail(`https://mc-heads.net/avatar/${nickname}/64`)
                    .addFields({ name: 'Alterado por', value: interaction.user.username, inline: true })
                    .setTimestamp()]
            });
        } catch (e) {
            console.error('setrank error:', e);
            await interaction.editReply({ content: '❌ Erro ao conectar com o servidor.' });
        }
    }

    // /removerank
    if (interaction.commandName === 'removerank') {
        await interaction.deferReply({ ephemeral: true });

        if (!isAdmin(interaction.member)) {
            return interaction.editReply({ content: '🚫 Apenas administradores podem usar este comando.' });
        }

        const nickname = interaction.options.getString('nickname');

        try {
            const data = await apiSetRank(nickname, 'none');
            if (!data.success) return interaction.editReply({ content: `❌ ${data.error}` });

            await interaction.editReply({
                embeds: [new EmbedBuilder()
                    .setTitle('🗑️ Rank Removido')
                    .setDescription(`O rank de **${nickname}** foi removido.`)
                    .setColor(0xed4245)
                    .setThumbnail(`https://mc-heads.net/avatar/${nickname}/64`)
                    .addFields({ name: 'Removido por', value: interaction.user.username, inline: true })
                    .setTimestamp()]
            });
        } catch (e) {
            console.error('removerank error:', e);
            await interaction.editReply({ content: '❌ Erro ao conectar com o servidor.' });
        }
    }

    // /rank
    if (interaction.commandName === 'rank') {
        await interaction.deferReply();

        const nickname = interaction.options.getString('nickname');

        try {
            const res  = await fetch(`${API_URL}/api/users/${encodeURIComponent(nickname)}`);
            const data = await res.json();

            if (data.error) return interaction.editReply({ content: `❌ ${data.error}` });

            await interaction.editReply({
                embeds: [new EmbedBuilder()
                    .setTitle(`🎖️ Rank de ${data.nickname}`)
                    .addFields(
                        { name: 'Rank',              value: data.rank || 'Sem rank', inline: true },
                        { name: 'Discord vinculado', value: data.discord_id ? '✅ Sim' : '❌ Não', inline: true }
                    )
                    .setColor(0xf97316)
                    .setThumbnail(`https://mc-heads.net/avatar/${data.nickname}/64`)
                    .setFooter({ text: 'SpartaMC' })]
            });
        } catch (e) {
            await interaction.editReply({ content: '❌ Erro ao buscar rank.' });
        }
    }
});

// ── Verificação de código (usada pelo site) ───────────────────────────────────
function verifyCode(userId, code) {
    const stored = verificationCodes.get(userId);
    if (!stored) return { success: false, error: 'Código não encontrado. Use /verificar no Discord.' };
    if (Date.now() > stored.expires) { verificationCodes.delete(userId); return { success: false, error: 'Código expirado.' }; }
    if (stored.code !== code) return { success: false, error: 'Código incorreto!' };
    verificationCodes.delete(userId);
    return { success: true };
}

async function checkGuildMember(userId) {
    try {
        const guild  = await client.guilds.fetch(GUILD_ID);
        const member = await guild.members.fetch(userId).catch(() => null);
        return !!member;
    } catch {
        return false;
    }
}

// ── Start ─────────────────────────────────────────────────────────────────────
if (DISCORD_TOKEN) {
    client.login(DISCORD_TOKEN).catch(e => console.error('❌ Erro ao logar bot:', e.message));
} else {
    console.warn('⚠️  DISCORD_TOKEN não definido');
}

module.exports = { verifyCode, checkGuildMember };
