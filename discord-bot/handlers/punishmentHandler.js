const { EmbedBuilder } = require('discord.js');

const LOG_CHANNEL_ID = '1473385327841509457';
let client = null;

function setClient(discordClient) {
    client = discordClient;
}

async function sendPunishmentLog(player, tipo, motivo, duracao, punicaoId, expiraEm, punidoPor) {
    if (!client) return;

    try {
        const channel = await client.channels.fetch(LOG_CHANNEL_ID);
        if (!channel) return;

        const isBan = tipo === 'ban';
        
        const embed = new EmbedBuilder()
            .setAuthor({ 
                name: isBan ? '🔨 BANIDO!' : '🔇 SILENCIADO!',
                iconURL: 'https://cdn.discordapp.com/attachments/1473385327841509457/1473385327841509457/logo.png'
            })
            .setColor(isBan ? 0xFF0000 : 0xFFD700)
            .setTitle(player)
            .setThumbnail(`https://mc-heads.net/avatar/${player}`)
            .setDescription(
                `### 📢 Motivo\n${motivo} (${punicaoId})\n\n` +
                (duracao && duracao.toLowerCase() !== 'permanente' 
                    ? `### ⏳ Expira\n<t:${Math.floor(expiraEm/1000)}:R>` 
                    : '### ⏳ Expira\n**Permanente**')
            )
            .setFooter({ text: `Aplicado por: ${punidoPor || 'Sistema'}` })
            .setTimestamp();

        await channel.send({ embeds: [embed] });
        console.log(`✅ Log de ${tipo} enviado para ${player}`);
    } catch (error) {
        console.error('❌ Erro ao enviar log de punição:', error);
    }
}

async function processPunishment(player, tipo, motivo, duracao, punidoPor = 'Sistema') {
    const punicaoId = '#' + Math.floor(100000 + Math.random() * 900000).toString();
    
    function calculateExpiration(duracao) {
        if (!duracao || duracao.toLowerCase() === 'permanente') return null;
        
        const valor = parseInt(duracao);
        const unidade = duracao.replace(/[0-9]/g, '');
        
        const agora = Date.now();
        switch(unidade) {
            case 'h': return agora + (valor * 60 * 60 * 1000);
            case 'd': return agora + (valor * 24 * 60 * 60 * 1000);
            case 'm': return agora + (valor * 30 * 24 * 60 * 60 * 1000);
            default: return null;
        }
    }
    
    const expiraEm = calculateExpiration(duracao);
    await sendPunishmentLog(player, tipo, motivo, duracao, punicaoId, expiraEm, punidoPor);
    
    return punicaoId;
}

module.exports = {
    setClient,
    processPunishment,
    sendPunishmentLog
};
