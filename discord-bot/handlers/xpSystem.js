const { MessageFlags } = require('discord.js');
const fs = require('fs');
const fsPromises = require('fs').promises;
const path = require('path');

const XP_CONFIG = {
    COOLDOWN: 60000, // 1 minuto
    XP_POR_MENSAGEM: 15,
    ARQUIVO: path.join(__dirname, '../data/xp.json')
};

// Lock simples para evitar race condition
const xpLock = new Map();

// Função para calcular XP necessário para um nível
function xpNecessario(nivel) {
    return Math.floor(100 * Math.pow(nivel, 1.5));
}

// Função para calcular nível baseado no XP
function calcularNivel(xp) {
    let nivel = 1;
    while (xp >= xpNecessario(nivel + 1)) {
        nivel++;
    }
    return nivel;
}

// Ler dados do JSON
async function lerDados() {
    try {
        const data = await fsPromises.readFile(XP_CONFIG.ARQUIVO, 'utf8');
        return JSON.parse(data);
    } catch (error) {
        return {};
    }
}

// Salvar dados no JSON com lock por usuário
async function salvarDados(dados) {
    await fsPromises.writeFile(XP_CONFIG.ARQUIVO, JSON.stringify(dados, null, 2));
}

async function lerESalvar(userId, fn) {
    while (xpLock.get(userId)) {
        await new Promise(r => setTimeout(r, 50));
    }
    xpLock.set(userId, true);
    try {
        const dados = await lerDados();
        fn(dados);
        await salvarDados(dados);
    } finally {
        xpLock.delete(userId);
    }
}

async function handleMessage(message, client) {
    // Ignorar mensagens de bots
    if (message.author.bot) return;
    
    const discordId = message.author.id;
    const discordTag = message.author.tag;
    const guildId = message.guild?.id;
    
    if (!guildId) return; // Ignorar DMs
    
    try {
        const agora = Date.now();
        let levelUp = false;
        let novoNivel = 1;
        
        await lerESalvar(discordId, (dados) => {
            // Inicializar dados do usuário se não existir
            if (!dados[discordId]) {
                dados[discordId] = {
                    discord_tag: discordTag,
                    xp: 0,
                    nivel: 1,
                    ultima_mensagem: 0,
                    historico: []
                };
            }
            
            const usuario = dados[discordId];
            
            // Verificar cooldown
            if (agora - usuario.ultima_mensagem < XP_CONFIG.COOLDOWN) {
                return;
            }
            
            // Atualizar XP
            usuario.xp += XP_CONFIG.XP_POR_MENSAGEM;
            usuario.ultima_mensagem = agora;
            usuario.discord_tag = discordTag;
            
            // Calcular novo nível
            novoNivel = calcularNivel(usuario.xp);
            
            if (novoNivel > usuario.nivel) {
                levelUp = true;
                usuario.nivel = novoNivel;
                
                // Adicionar ao histórico
                if (!usuario.historico) usuario.historico = [];
                usuario.historico.push({
                    tipo: 'level_up',
                    nivel: novoNivel,
                    data: agora
                });
            }
        });
        
        // Se subiu de nível, enviar mensagem
        if (levelUp) {
            await message.author.send(`🎉 **Parabéns!** Você subiu para o nível **${novoNivel}**!`).catch(() => {});
            console.log(`📊 ${discordTag} subiu para nível ${novoNivel}`);
        }
        
    } catch (error) {
        console.error('❌ Erro no sistema de XP:', error);
    }
}

// Comando para ver nível
async function verNivel(interaction) {
    const usuario = interaction.options.getUser('usuario') || interaction.user;
    const discordId = usuario.id;
    
    try {
        const dados = await lerDados();
        
        if (!dados[discordId]) {
            return interaction.reply({
                content: `📊 **${usuario.tag}** ainda não possui XP.`,
                flags: MessageFlags.Ephemeral
            });
        }
        
        const userData = dados[discordId];
        const xpAtual = userData.xp;
        const nivel = userData.nivel;
        const xpProxNivel = xpNecessario(nivel + 1);
        const xpNivelAtual = xpNecessario(nivel);
        const xpNoNivel = xpAtual - xpNivelAtual;
        const xpPreciso = xpProxNivel - xpNivelAtual;
        const progresso = Math.floor((xpNoNivel / xpPreciso) * 100);
        
        // Criar barra de progresso
        const tamanhoBarra = 20;
        const progressoCompleto = Math.floor((progresso / 100) * tamanhoBarra);
        const barra = '█'.repeat(progressoCompleto) + '░'.repeat(tamanhoBarra - progressoCompleto);
        
        const mensagem = `**📊 Nível de ${usuario.tag}**\n\n` +
                        `**Nível:** ${nivel}\n` +
                        `**XP Total:** ${xpAtual.toLocaleString()}\n` +
                        `**Progresso para nível ${nivel + 1}:**\n` +
                        `\`${barra}\` ${progresso}% (${xpNoNivel.toLocaleString()}/${xpPreciso.toLocaleString()})`;
        
        await interaction.reply({ content: mensagem, flags: MessageFlags.Ephemeral });
        
    } catch (error) {
        console.error('❌ Erro ao ver nível:', error);
        await interaction.reply({
            content: '❌ Erro ao buscar nível do usuário.',
            flags: MessageFlags.Ephemeral
        });
    }
}

// Comando para ver ranking
async function ranking(interaction) {
    try {
        const dados = await lerDados();
        
        const rankingArray = Object.entries(dados)
            .map(([id, data]) => ({
                id,
                tag: data.discord_tag || 'Desconhecido',
                nivel: data.nivel || 1,
                xp: data.xp || 0
            }))
            .sort((a, b) => b.xp - a.xp)
            .slice(0, 10);
        
        if (rankingArray.length === 0) {
            return interaction.reply({
                content: '📊 Nenhum usuário no ranking ainda.',
                flags: MessageFlags.Ephemeral
            });
        }
        
        let mensagem = '🏆 **RANKING DE XP** 🏆\n\n';
        
        rankingArray.forEach((user, index) => {
            const medalha = index === 0 ? '🥇' : index === 1 ? '🥈' : index === 2 ? '🥉' : `${index + 1}.`;
            mensagem += `${medalha} **${user.tag}** | Nível ${user.nivel} | ${user.xp.toLocaleString()} XP\n`;
        });
        
        await interaction.reply({ content: mensagem, flags: MessageFlags.Ephemeral });
        
    } catch (error) {
        console.error('❌ Erro ao buscar ranking:', error);
        await interaction.reply({
            content: '❌ Erro ao buscar ranking.',
            flags: MessageFlags.Ephemeral
        });
    }
}

module.exports = {
    handleMessage,
    verNivel,
    ranking
};
