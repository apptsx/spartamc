const { EmbedBuilder } = require('discord.js');
const axios = require('axios');
const sharp = require('sharp');
const fs = require('fs');
const path = require('path');
const YAML = require('yaml');

// IDs
const EMOJI_SERVER_ID = '1474968255679692964';
const OWNER_ID = '1470064045418741932';

// Caminho do arquivo YAML
const DATA_FILE = path.join(__dirname, '../data/equipe.yml');

// Garantir que a pasta data existe
const dataDir = path.join(__dirname, '../data');
if (!fs.existsSync(dataDir)) {
    fs.mkdirSync(dataDir, { recursive: true });
}

// Cache dos emojis criados
let staffEmojis = new Map();

// Armazenamento da mensagem do mural
let muralMessage = {
    channelId: null,
    messageId: null
};

// Mapeamento dos cargos
const roleMapping = {
    'admin': 'admin',
    'chef': 'chef',
    'mod+': 'modplus',
    'mod': 'moderador',
    'trial': 'trial',
    'helper': 'helper'
};

// Função para carregar dados do YAML
function loadData() {
    try {
        if (fs.existsSync(DATA_FILE)) {
            const fileContent = fs.readFileSync(DATA_FILE, 'utf8');
            const data = YAML.parse(fileContent);
            return {
                admin: data.admin || [],
                chef: data.chef || [],
                modplus: data.modplus || [],
                moderador: data.moderador || [],
                trial: data.trial || [],
                helper: data.helper || []
            };
        }
    } catch (error) {
        console.error('Erro ao carregar dados YAML:', error);
    }
    return {
        admin: [],
        chef: [],
        modplus: [],
        moderador: [],
        trial: [],
        helper: []
    };
}

// Função para salvar dados no YAML
function saveData(data) {
    try {
        const yamlString = YAML.stringify(data);
        fs.writeFileSync(DATA_FILE, yamlString, 'utf8');
    } catch (error) {
        console.error('Erro ao salvar dados YAML:', error);
    }
}

// Carregar dados ao iniciar
let staffMembers = loadData();

function isOwner(userId) {
    return userId === OWNER_ID;
}

// Criar emoji da cabeça do staff
async function createStaffHeadEmoji(client, nick) {
    try {
        if (staffEmojis.has(nick)) return staffEmojis.get(nick);

        const emojiGuild = await client.guilds.fetch(EMOJI_SERVER_ID);
        if (!emojiGuild) throw new Error('Servidor de emojis não encontrado!');

        const response = await axios({
            url: `https://mc-heads.net/head/${nick}`,
            responseType: 'arraybuffer',
            timeout: 10000
        });

        const imageBuffer = await sharp(response.data).png().toBuffer();
        const emojiName = `head_${nick.toLowerCase()}`;
        const createdEmoji = await emojiGuild.emojis.create({
            attachment: imageBuffer,
            name: emojiName
        });

        staffEmojis.set(nick, createdEmoji.id);
        return createdEmoji.id;
    } catch (error) {
        console.error(`Erro ao criar emoji para ${nick}:`, error.message);
        return null;
    }
}

// Função para gerar o texto IGUALZINHO O CONTAINER
async function generateTeamText() {
    let teamText = '';
    
    // Título com o emoji fyre
    teamText += `# <:fyre:1473532463757262889> EQUIPE FYRE\nNossa equipe é composta por 5 cargos com suas respectivas funções, veja abaixo.\n\n`;
    
    // ADMINISTRADORES
    teamText += `### ADMINISTRADORES (${staffMembers.admin.length}):\n`;
    if (staffMembers.admin.length === 0) {
        teamText += '<:A1:1477829627350224970><:A2:1477829628830679209><:A3:1477829630202220586> Nenhum administrador cadastrado\n\n';
    } else {
        for (let i = 0; i < staffMembers.admin.length; i++) {
            const member = staffMembers.admin[i];
            const headEmoji = member.emojiId ? `<:head_${member.nick.toLowerCase()}:${member.emojiId}>` : '👤';
            const funcaoText = member.funcao ? ` ➡ ${member.funcao}` : '';
            teamText += `<:A1:1477829627350224970><:A2:1477829628830679209><:A3:1477829630202220586> ${headEmoji} • **${member.nick}**${funcaoText}\n`;
        }
        teamText += '\n';
    }

    // MODERADORES+
    teamText += `### MODERADORES+ (${staffMembers.modplus.length}):\n`;
    if (staffMembers.modplus.length === 0) {
        teamText += '<:MP1:1477829731645653085><:MP2:1477829733281693878><:MP3:1477829734405505091> Nenhum moderador+ cadastrado\n\n';
    } else {
        for (let i = 0; i < staffMembers.modplus.length; i++) {
            const member = staffMembers.modplus[i];
            const headEmoji = member.emojiId ? `<:head_${member.nick.toLowerCase()}:${member.emojiId}>` : '👤';
            const funcaoText = member.funcao ? ` ➡ ${member.funcao}` : '';
            teamText += `<:MP1:1477829731645653085><:MP2:1477829733281693878><:MP3:1477829734405505091> ${headEmoji} • **${member.nick}**${funcaoText}\n`;
        }
        teamText += '\n';
    }

    // MODERADORES
    teamText += `### MODERADORES (${staffMembers.moderador.length}):\n`;
    if (staffMembers.moderador.length === 0) {
        teamText += '<:M1:1477829966967345162><:M2:1477829969479733369> Nenhum moderador cadastrado\n\n';
    } else {
        for (let i = 0; i < staffMembers.moderador.length; i++) {
            const member = staffMembers.moderador[i];
            const headEmoji = member.emojiId ? `<:head_${member.nick.toLowerCase()}:${member.emojiId}>` : '👤';
            const funcaoText = member.funcao ? ` ➡ ${member.funcao}` : '';
            teamText += `<:M1:1477829966967345162><:M2:1477829969479733369> ${headEmoji} • **${member.nick}**${funcaoText}\n`;
        }
        teamText += '\n';
    }

    // TRIAIS MODERADORES
    teamText += `### TRIAIS MODERADORES (${staffMembers.trial.length}):\n`;
    if (staffMembers.trial.length === 0) {
        teamText += '<:T1:1477829996352503811><:T2:1477829998059589784><:T3:1477829999825522720> Nenhum trial cadastrado\n\n';
    } else {
        for (let i = 0; i < staffMembers.trial.length; i++) {
            const member = staffMembers.trial[i];
            const headEmoji = member.emojiId ? `<:head_${member.nick.toLowerCase()}:${member.emojiId}>` : '👤';
            const funcaoText = member.funcao ? ` ➡ ${member.funcao}` : '';
            teamText += `<:T1:1477829996352503811><:T2:1477829998059589784><:T3:1477829999825522720> ${headEmoji} • **${member.nick}**${funcaoText}\n`;
        }
        teamText += '\n';
    }

    // HELPERS
    teamText += `### HELPERS (${staffMembers.helper.length}):\n`;
    if (staffMembers.helper.length === 0) {
        teamText += '<:H1:1477830329179046090><:H2:1477830330642596013><:H3:1477830332064727141> Nenhum helper cadastrado\n\n';
    } else {
        for (let i = 0; i < staffMembers.helper.length; i++) {
            const member = staffMembers.helper[i];
            const headEmoji = member.emojiId ? `<:head_${member.nick.toLowerCase()}:${member.emojiId}>` : '👤';
            const funcaoText = member.funcao ? ` ➡ ${member.funcao}` : '';
            teamText += `<:H1:1477830329179046090><:H2:1477830330642596013><:H3:1477830332064727141> ${headEmoji} • **${member.nick}**${funcaoText}\n`;
        }
        teamText += '\n';
    }

    teamText += '-# <a:relogio:1474188164703850561> Mural sendo atualizado constantemente!';
    return teamText;
}

// Criar o embed
async function createTeamEmbed() {
    const teamText = await generateTeamText();
    
    const embed = new EmbedBuilder()
        .setColor('#3498db')
        .setDescription(teamText)
        .setTimestamp();

    return embed;
}

// Atualizar o mural
async function updateMural(client) {
    try {
        if (!muralMessage.channelId || !muralMessage.messageId) return;

        const channel = await client.channels.fetch(muralMessage.channelId);
        if (!channel) return;

        const message = await channel.messages.fetch(muralMessage.messageId);
        if (!message) return;

        const embed = await createTeamEmbed();
        await message.edit({ embeds: [embed] });
        
        console.log('📋 Mural atualizado!');
    } catch (error) {
        console.error('Erro ao atualizar mural:', error.message);
    }
}

// Criar o mural
async function createMural(client, channel) {
    try {
        if (muralMessage.channelId && muralMessage.messageId) {
            try {
                const oldChannel = await client.channels.fetch(muralMessage.channelId);
                const oldMessage = await oldChannel.messages.fetch(muralMessage.messageId);
                await oldMessage.delete();
            } catch (e) {}
        }

        const embed = await createTeamEmbed();
        const message = await channel.send({ embeds: [embed] });
        
        muralMessage = {
            channelId: channel.id,
            messageId: message.id
        };
        
        return true;
    } catch (error) {
        console.error('Erro ao criar mural:', error);
        return false;
    }
}

// Função para adicionar staff (agora com save)
function addStaff(cargo, staffData) {
    staffMembers[cargo].push(staffData);
    saveData(staffMembers);
}

// Função para remover staff (agora com save)
function removeStaff(cargo, index) {
    staffMembers[cargo].splice(index, 1);
    saveData(staffMembers);
}

module.exports = {
    staffMembers,
    roleMapping,
    isOwner,
    createStaffHeadEmoji,
    updateMural,
    createMural,
    generateTeamText,
    addStaff,
    removeStaff
};