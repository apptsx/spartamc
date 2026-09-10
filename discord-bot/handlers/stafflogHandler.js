const { AttachmentBuilder } = require('discord.js');
const axios = require('axios');
const path = require('path');
const fs = require('fs');

// Cores dos cargos (em formato HEX sem # para a URL)
const ROLE_COLORS = {
    'Administrador': 'AB0000',
    'Moderador+': '6B0045',
    'Moderador': '850066',
    'Trial': 'AC25AC',
    'Helper': '5555FF'
};

// Canal fixo para logs
const LOG_CHANNEL_ID = '1473385298636439786';

// Mapeamento de tipos para textos
const TIPO_TEXTO = {
    entrou: 'ENTROU COMO',
    promovido: 'PROMOVIDO A',
    removido: 'REMOVIDO DA EQUIPE'
};

module.exports = {
    async handleStafflog(interaction) {
        // Verificar permissão
        if (!interaction.member.permissions.has('Administrator')) {
            return interaction.reply({ 
                content: '❌ Apenas administradores podem usar este comando!', 
                ephemeral: true 
            });
        }

        const subcommand = interaction.options.getSubcommand();
        const nick = interaction.options.getString('nick');
        const cargo = interaction.options.getRole('cargo');

        // Validar cargo
        if (!cargo) {
            return interaction.reply({
                content: '❌ Cargo não encontrado ou não especificado.',
                ephemeral: true
            });
        }
        const cargosPermitidos = ['Administrador', 'Moderador+', 'Moderador', 'Trial', 'Helper'];
        const cargoNome = cargo.name;
        
        if (!cargosPermitidos.includes(cargoNome) && subcommand !== 'removido') {
            return interaction.reply({
                content: '❌ Cargo inválido! Use: Administrador, Moderador+, Moderador, Trial, Helper',
                ephemeral: true
            });
        }

        await interaction.deferReply({ ephemeral: true });

        try {
            console.log(`🎨 Criando imagem para ${subcommand} - ${nick}`);

            // Definir cor
            let corHex = ROLE_COLORS[cargoNome] || 'FFFFFF';
            if (subcommand === 'removido') {
                corHex = 'FF0000'; // Vermelho para removido
            }

            // Construir URL da imagem com texto
            let imageUrl;
            
            if (subcommand === 'removido') {
                // Para removido: só o nick em vermelho
                imageUrl = `https://minecraft-heads.com/scripts/ra?img=di&head=${nick}&text=${nick}&color=${corHex}`;
            } else {
                // Para entrou/promovido: nick e cargo com texto formatado
                // Nota: Esta é uma API simulada, precisamos de uma API real que suporte texto
                // Por enquanto, vamos usar uma combinação de imagens
                imageUrl = `https://minecraft-heads.com/scripts/ra?img=di&head=${nick}&text=${nick} ${TIPO_TEXTO[subcommand]} ${cargoNome}&color=${corHex}`;
            }

            // Como a API acima pode não funcionar, vamos usar uma abordagem diferente:
            // Criar uma imagem composta com a head e texto usando HTML/CSS e screenshot?
            // Mas isso é complexo...
            
            // Solução simples: Enviar a head do jogador + embed com texto
            const headUrl = `https://mc-heads.net/head/${nick}`;
            
            // Buscar a head
            const headResponse = await axios.get(headUrl, { 
                responseType: 'arraybuffer',
                timeout: 5000 
            });
            
            const headBuffer = Buffer.from(headResponse.data, 'binary');

            // Criar embed com as informações
            const { EmbedBuilder } = require('discord.js');
            
            const embed = new EmbedBuilder()
                .setColor(subcommand === 'removido' ? 0xFF0000 : parseInt(corHex, 16))
                .setTitle(`📋 Movimentação da Staff`)
                .setDescription(
                    `**${TIPO_TEXTO[subcommand]}**\n\n` +
                    `**Nick:** ${nick}\n` +
                    `${subcommand !== 'removido' ? `**Cargo:** ${cargoNome}\n` : ''}` +
                    `**Data:** <t:${Math.floor(Date.now()/1000)}:F>`
                )
                .setThumbnail(`https://mc-heads.net/avatar/${nick}`)
                .setFooter({ text: `Registrado por ${interaction.user.tag}` })
                .setTimestamp();

            // Buscar canal
            const logChannel = await interaction.guild.channels.fetch(LOG_CHANNEL_ID);
            if (!logChannel) {
                return interaction.editReply('❌ Canal de logs não encontrado!');
            }

            // Enviar embed + head
            await logChannel.send({
                embeds: [embed],
                files: [{
                    attachment: headBuffer,
                    name: `head_${nick}.png`
                }]
            });

            await interaction.editReply('✅ Log de staff enviado com sucesso!');

        } catch (error) {
            console.error('❌ Erro detalhado:', error);
            
            // Fallback: enviar apenas embed sem imagem
            try {
                const { EmbedBuilder } = require('discord.js');
                const logChannel = await interaction.guild.channels.fetch(LOG_CHANNEL_ID);
                
                const embed = new EmbedBuilder()
                    .setColor(subcommand === 'removido' ? 0xFF0000 : 0x00FF00)
                    .setTitle(`📋 ${TIPO_TEXTO[subcommand]}`)
                    .setDescription(
                        `**Nick:** ${nick}\n` +
                        `${subcommand !== 'removido' ? `**Cargo:** ${cargoNome}\n` : ''}` +
                        `**Registrado por:** ${interaction.user.tag}`
                    )
                    .setThumbnail(`https://mc-heads.net/avatar/${nick}`)
                    .setTimestamp();

                await logChannel.send({ embeds: [embed] });
                await interaction.editReply('✅ Log enviado (modo fallback)');
            } catch (fallbackError) {
                await interaction.editReply(`❌ Erro: ${error.message}`);
            }
        }
    }
};