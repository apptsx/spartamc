const { 
    ModalBuilder, 
    TextInputBuilder, 
    TextInputStyle, 
    ActionRowBuilder, 
    ChannelType, 
    PermissionFlagsBits, 
    ButtonBuilder, 
    ButtonStyle, 
    MessageFlags,
    EmbedBuilder
} = require('discord.js');
const CONFIG = require('../config/tickets');
const fs = require('fs');
const path = require('path');

const activeAppeals = new Map();
const appealsFile = path.join(__dirname, '../data/appeals_active.json');

function salvarAppeals() {
    try {
        const data = Array.from(activeAppeals.entries()).map(([userId, appeal]) => ({ userId, appeal }));
        fs.writeFileSync(appealsFile, JSON.stringify(data, null, 2));
    } catch (e) {
        console.error('Erro ao salvar appeals:', e);
    }
}

function carregarAppeals() {
    try {
        if (fs.existsSync(appealsFile)) {
            const data = JSON.parse(fs.readFileSync(appealsFile, 'utf8'));
            data.forEach(({ userId, appeal }) => activeAppeals.set(userId, appeal));
            console.log(`✅ ${data.length} appeals carregados do arquivo.`);
        }
    } catch (e) {
        console.error('Erro ao carregar appeals:', e);
    }
}

carregarAppeals();

// Função para criar modal baseado no tipo de punição
function criarModal(tipo) {
    const modal = new ModalBuilder()
        .setCustomId(`modal_appeal_${tipo}`)
        .setTitle(`Revisão de ${tipo.charAt(0).toUpperCase() + tipo.slice(1)}`);
    
    // Título do tópico
    const tituloInput = new TextInputBuilder()
        .setCustomId('titulo_topico')
        .setLabel('Título do tópico')
        .setPlaceholder('Ex: Revisão de punição injusta')
        .setStyle(TextInputStyle.Short)
        .setRequired(true)
        .setMaxLength(100);
    
    // Campo de nick
    const nickInput = new TextInputBuilder()
        .setCustomId('nick')
        .setLabel('Seu nick no Minecraft')
        .setPlaceholder('Digite seu nick')
        .setStyle(TextInputStyle.Short)
        .setRequired(true)
        .setMaxLength(16);
    
    // ID da punição
    const idInput = new TextInputBuilder()
        .setCustomId('id_punicao')
        .setLabel('ID da punição')
        .setPlaceholder('Ex: #123456 (se não souber, coloque "não sei")')
        .setStyle(TextInputStyle.Short)
        .setRequired(true);
    
    // Motivo da punição
    const motivoInput = new TextInputBuilder()
        .setCustomId('motivo_punicao')
        .setLabel('Motivo da punição')
        .setPlaceholder('Por que você foi punido?')
        .setStyle(TextInputStyle.Short)
        .setRequired(true);
    
    // Defesa
    const defesaInput = new TextInputBuilder()
        .setCustomId('defesa')
        .setLabel('Sua defesa')
        .setPlaceholder('Por que a punição deve ser revista? Seja detalhado')
        .setStyle(TextInputStyle.Paragraph)
        .setRequired(true);
    
    modal.addComponents(
        new ActionRowBuilder().addComponents(tituloInput),
        new ActionRowBuilder().addComponents(nickInput),
        new ActionRowBuilder().addComponents(idInput),
        new ActionRowBuilder().addComponents(motivoInput),
        new ActionRowBuilder().addComponents(defesaInput)
    );
    
    return modal;
}

// Handler para o menu de seleção
async function handleMenu(interaction) {
    const valor = interaction.values[0];
    const tipo = valor.replace('appeal_', ''); // appeal_mute -> mute
    
    const modal = criarModal(tipo);
    await interaction.showModal(modal);
}

// Handler para o modal
async function handleModal(interaction, client) {
    await interaction.deferReply({ flags: MessageFlags.Ephemeral });
    
    const tipo = interaction.customId.replace('modal_appeal_', '');
    const userId = interaction.user.id;
    const userTag = interaction.user.tag;
    const tituloTopico = interaction.fields.getTextInputValue('titulo_topico');
    const nick = interaction.fields.getTextInputValue('nick');
    const idPunicao = interaction.fields.getTextInputValue('id_punicao');
    const motivoPunicao = interaction.fields.getTextInputValue('motivo_punicao');
    const defesa = interaction.fields.getTextInputValue('defesa');
    
    try {
        // Verificar se já tem appeal ativo
        if (activeAppeals.has(userId)) {
            return interaction.editReply({ 
                content: `${CONFIG.emojis.errado} Você já possui um appeal em análise.` 
            });
        }

        // Verificar DM
        try {
            await interaction.user.send('🔄 Verificando...').then(msg => msg.delete().catch(() => {}));
        } catch {
            return interaction.editReply({ 
                content: `${CONFIG.emojis.errado} Não foi possível enviar DM. Habilite mensagens de membros do servidor.` 
            });
        }

        // Buscar cargo de staff
        const staffRole = await interaction.guild.roles.fetch(CONFIG.staffRoleId);
        if (!staffRole) {
            return interaction.editReply({ 
                content: `${CONFIG.emojis.errado} Erro: Cargo de staff não encontrado.` 
            });
        }

        // Buscar canal de forum
        const forumChannel = await interaction.guild.channels.fetch(CONFIG.appealForumChannelId);
        if (!forumChannel || forumChannel.type !== ChannelType.GuildForum) {
            return interaction.editReply({ 
                content: `${CONFIG.emojis.errado} Erro: Canal de forum de appeals não encontrado ou inválido.` 
            });
        }

        // Criar tópico no forum
        const thread = await forumChannel.threads.create({
            name: tituloTopico,
            message: {
                content: `<@&${CONFIG.staffRoleId}>`,
                embeds: [new EmbedBuilder()
                    .setColor(CONFIG.colors[tipo] || 0x808080)
                    .setAuthor({ 
                        name: `${userTag} • ${nick}`, 
                        iconURL: interaction.user.displayAvatarURL() 
                    })
                    .setTitle(`${CONFIG.emojis[tipo] || '⚖️'} REVISÃO DE ${tipo.toUpperCase()}`)
                    .setDescription(
                        `**🆔 ID da Punição:** ${idPunicao}\n` +
                        `**⚠️ Motivo:** ${motivoPunicao}\n\n` +
                        `**📝 Defesa:**\n${defesa}`
                    )
                    .setFooter({ text: `ID do Usuário: ${userId}` })
                    .setTimestamp()]
            }
        });

        // Registrar appeal ativo
        activeAppeals.set(userId, {
            tipo,
            nick,
            userTag,
            threadId: thread.id,
            userId: userId,
            status: 'pendente',
            analisadoPor: null
        });
        salvarAppeals();

        // Embed para usuário (DM)
        const userEmbed = new EmbedBuilder()
            .setColor(CONFIG.colors[tipo] || 0x808080)
            .setAuthor({ name: interaction.guild.name, iconURL: interaction.guild.iconURL() })
            .setTitle(`${CONFIG.emojis[tipo] || '⚖️'} Revisão de ${tipo}`)
            .setDescription(
                `Olá **${interaction.user.username}**,\n\n` +
                `Sua solicitação de revisão foi enviada para análise!\n\n` +
                `**Nick:** ${nick}\n` +
                `**ID da Punição:** ${idPunicao}\n` +
                `**Motivo:** ${motivoPunicao}\n` +
                `**Título:** ${tituloTopico}\n\n` +
                `**Sua defesa:**\n${defesa}`
            )
            .setFooter({ text: 'Aguardando análise da equipe' })
            .setTimestamp();

        await interaction.user.send({ embeds: [userEmbed] });

        // Log
        const logChannel = interaction.guild.channels.cache.get(CONFIG.logChannelId);
        if (logChannel) {
            const logEmbed = new EmbedBuilder()
                .setColor(CONFIG.colors[tipo] || 0x808080)
                .setTitle('📨 Novo Appeal')
                .setDescription(
                    `**Tipo:** ${tipo}\n` +
                    `**Usuário:** ${userTag}\n` +
                    `**Nick:** ${nick}\n` +
                    `**ID Punição:** ${idPunicao}\n` +
                    `**Tópico:** ${thread.name}\n` +
                    `**Link:** ${thread.url}`
                )
                .setTimestamp();

            await logChannel.send({ embeds: [logEmbed] });
        }

        await interaction.editReply({ 
            content: `${CONFIG.emojis.correto} Solicitação de revisão enviada! Você será notificado no privado quando for analisada.` 
        });

    } catch (error) {
        console.error('❌ Erro ao criar appeal:', error);
        activeAppeals.delete(userId);
        salvarAppeals();
        await interaction.editReply({ 
            content: `${CONFIG.emojis.errado} Erro: ${error.message}` 
        });
    }
}

// Função para aceitar appeal
async function aceitarAppeal(interaction, client) {
    const userId = interaction.customId.replace('aceitar_appeal_', '');
    const appeal = activeAppeals.get(userId);
    
    if (!appeal) {
        return interaction.reply({ 
            content: `${CONFIG.emojis.errado} Este appeal não está mais ativo.`, 
            flags: MessageFlags.Ephemeral 
        });
    }
    
    if (appeal.analisadoPor) {
        return interaction.reply({ 
            content: `${CONFIG.emojis.errado} Este appeal já foi analisado por **${appeal.analisadoPor}**.`, 
            flags: MessageFlags.Ephemeral 
        });
    }
    
    appeal.analisadoPor = interaction.user.tag;
    appeal.analisadoPorId = interaction.user.id;
    appeal.status = 'aceito';
    activeAppeals.set(userId, appeal);
    salvarAppeals();
    
    // Atualizar embed
    const originalTitle = interaction.message.embeds[0]?.title || '';
    const embed = EmbedBuilder.from(interaction.message.embeds[0])
        .setColor(0x00FF00)
        .setTitle(`✅ ACEITO - ${originalTitle}`)
        .addFields({ name: '✅ Aceito por', value: interaction.user.tag });
    
    // Desabilitar botões
    const row = ActionRowBuilder.from(interaction.message.components[0]);
    row.components[0].setDisabled(true);
    row.components[1].setDisabled(true);
    
    await interaction.update({ embeds: [embed], components: [row] });
    
    // Notificar usuário
    const user = await client.users.fetch(userId).catch(() => null);
    if (user) {
        await user.send(`✅ **${interaction.user.tag}** aceitou sua solicitação de revisão!\nSua punição será removida em breve.`).catch(() => {});
    }
    
    // Log
    const logChannel = interaction.guild.channels.cache.get(CONFIG.logChannelId);
    if (logChannel) {
        await logChannel.send(`✅ Appeal de **${appeal.nick}** aceito por **${interaction.user.tag}**.`);
    }
}

// Função para negar appeal
async function negarAppeal(interaction, client) {
    const userId = interaction.customId.replace('negar_appeal_', '');
    const appeal = activeAppeals.get(userId);
    
    if (!appeal) {
        return interaction.reply({ 
            content: `${CONFIG.emojis.errado} Este appeal não está mais ativo.`, 
            flags: MessageFlags.Ephemeral 
        });
    }
    
    if (appeal.analisadoPor) {
        return interaction.reply({ 
            content: `${CONFIG.emojis.errado} Este appeal já foi analisado por **${appeal.analisadoPor}**.`, 
            flags: MessageFlags.Ephemeral 
        });
    }
    
    appeal.analisadoPor = interaction.user.tag;
    appeal.analisadoPorId = interaction.user.id;
    appeal.status = 'negado';
    activeAppeals.set(userId, appeal);
    salvarAppeals();
    
    // Atualizar embed
    const originalTitle = interaction.message.embeds[0]?.title || '';
    const embed = EmbedBuilder.from(interaction.message.embeds[0])
        .setColor(0xFF0000)
        .setTitle(`❌ NEGADO - ${originalTitle}`)
        .addFields({ name: '❌ Negado por', value: interaction.user.tag });
    
    // Desabilitar botões
    const row = ActionRowBuilder.from(interaction.message.components[0]);
    row.components[0].setDisabled(true);
    row.components[1].setDisabled(true);
    
    await interaction.update({ embeds: [embed], components: [row] });
    
    // Notificar usuário
    const user = await client.users.fetch(userId).catch(() => null);
    if (user) {
        await user.send(`❌ **${interaction.user.tag}** negou sua solicitação de revisão.\nA punição aplicada está correta.`).catch(() => {});
    }
    
    // Log
    const logChannel = interaction.guild.channels.cache.get(CONFIG.logChannelId);
    if (logChannel) {
        await logChannel.send(`❌ Appeal de **${appeal.nick}** negado por **${interaction.user.tag}**.`);
    }
}

// Função para fechar canal do appeal (após análise)
async function fecharAppeal(interaction, client) {
    // Esta função pode ser chamada após a análise para deletar o canal
    const userId = interaction.customId.replace('fechar_appeal_', '');
    const appeal = activeAppeals.get(userId);
    
    if (!appeal) return;
    
    activeAppeals.delete(userId);
    salvarAppeals();
    
    setTimeout(() => {
        interaction.channel.delete().catch(() => {});
    }, 3000);
}

function getActiveAppeals() {
    return activeAppeals;
}

module.exports = {
    handleMenu,
    handleModal,
    aceitarAppeal,
    negarAppeal,
    fecharAppeal,
    getActiveAppeals
};
