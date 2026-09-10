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

const activeTickets = new Map();
const ticketsFile = path.join(__dirname, '../data/tickets_active.json');

// Função para salvar tickets em arquivo
function salvarTickets() {
    const ticketsData = Array.from(activeTickets.entries()).map(([userId, ticket]) => ({
        userId,
        ticket
    }));
    fs.writeFileSync(ticketsFile, JSON.stringify(ticketsData, null, 2));
}

// Função para carregar tickets do arquivo
function carregarTickets() {
    try {
        if (fs.existsSync(ticketsFile)) {
            const ticketsData = JSON.parse(fs.readFileSync(ticketsFile, 'utf8'));
            ticketsData.forEach(({ userId, ticket }) => {
                activeTickets.set(userId, ticket);
            });
            console.log(`✅ ${ticketsData.length} tickets carregados do arquivo.`);
        }
    } catch (error) {
        console.error('Erro ao carregar tickets:', error);
    }
}

// Função para criar modal baseado no tipo
function criarModal(tipo) {
    const modal = new ModalBuilder()
        .setCustomId(`modal_ticket_${tipo}`)
        .setTitle(`Ticket: ${tipo.charAt(0).toUpperCase() + tipo.slice(1)}`);
    
    const nickInput = new TextInputBuilder()
        .setCustomId('nick')
        .setLabel('Seu nick no Minecraft')
        .setPlaceholder('Digite seu nick')
        .setStyle(TextInputStyle.Short)
        .setRequired(true)
        .setMaxLength(16);
    
    const row1 = new ActionRowBuilder().addComponents(nickInput);
    modal.addComponents(row1);
    
    if (tipo === 'suporte') {
        const necessidadeInput = new TextInputBuilder()
            .setCustomId('necessidade')
            .setLabel('Do que você precisa?')
            .setPlaceholder('Descreva sua necessidade de suporte')
            .setStyle(TextInputStyle.Paragraph)
            .setRequired(true);
        
        modal.addComponents(new ActionRowBuilder().addComponents(necessidadeInput));
    }
    else if (tipo === 'compras') {
        const produtoInput = new TextInputBuilder()
            .setCustomId('produto')
            .setLabel('Produto comprado')
            .setPlaceholder('Qual produto você comprou?')
            .setStyle(TextInputStyle.Short)
            .setRequired(true);
        
        const problemaInput = new TextInputBuilder()
            .setCustomId('problema')
            .setLabel('Problema')
            .setPlaceholder('Descreva o problema com a compra')
            .setStyle(TextInputStyle.Paragraph)
            .setRequired(true);
        
        modal.addComponents(
            new ActionRowBuilder().addComponents(produtoInput),
            new ActionRowBuilder().addComponents(problemaInput)
        );
    }
    else if (tipo === 'duvidas') {
        const duvidaInput = new TextInputBuilder()
            .setCustomId('duvida')
            .setLabel('Sua dúvida')
            .setPlaceholder('Digite sua dúvida detalhadamente')
            .setStyle(TextInputStyle.Paragraph)
            .setRequired(true);
        
        modal.addComponents(new ActionRowBuilder().addComponents(duvidaInput));
    }
    else if (tipo === 'revisao') {
        const idInput = new TextInputBuilder()
            .setCustomId('id_punicao')
            .setLabel('ID da punição')
            .setPlaceholder('Ex: #123456')
            .setStyle(TextInputStyle.Short)
            .setRequired(true);
        
        const motivoInput = new TextInputBuilder()
            .setCustomId('motivo_punicao')
            .setLabel('Motivo da punição')
            .setPlaceholder('Por que você foi punido?')
            .setStyle(TextInputStyle.Short)
            .setRequired(true);
        
        const defesaInput = new TextInputBuilder()
            .setCustomId('defesa')
            .setLabel('Sua defesa')
            .setPlaceholder('Por que a punição deve ser revista?')
            .setStyle(TextInputStyle.Paragraph)
            .setRequired(true);
        
        modal.addComponents(
            new ActionRowBuilder().addComponents(idInput),
            new ActionRowBuilder().addComponents(motivoInput),
            new ActionRowBuilder().addComponents(defesaInput)
        );
    }
    else if (tipo === 'revisao_punicao') {
        const tipoIdInput = new TextInputBuilder()
            .setCustomId('tipo_id')
            .setLabel('Tipo e ID da punição')
            .setPlaceholder('Ex: Mute - #123456')
            .setStyle(TextInputStyle.Short)
            .setRequired(true);

        const motivoTempoInput = new TextInputBuilder()
            .setCustomId('motivo_tempo')
            .setLabel('Motivo e tempo da punição')
            .setPlaceholder('Ex: Capa de spawn - 7 dias')
            .setStyle(TextInputStyle.Short)
            .setRequired(true);

        const defesaInput = new TextInputBuilder()
            .setCustomId('defesa')
            .setLabel('Por que acha que foi engano?')
            .setPlaceholder('Explique detalhadamente')
            .setStyle(TextInputStyle.Paragraph)
            .setRequired(true);

        const provasInput = new TextInputBuilder()
            .setCustomId('provas')
            .setLabel('Provas (opcional)')
            .setPlaceholder('Links de imagens/vídeos')
            .setStyle(TextInputStyle.Paragraph)
            .setRequired(false);

        modal.addComponents(
            new ActionRowBuilder().addComponents(tipoIdInput),
            new ActionRowBuilder().addComponents(motivoTempoInput),
            new ActionRowBuilder().addComponents(defesaInput),
            new ActionRowBuilder().addComponents(provasInput)
        );
    }
    else if (tipo === 'denuncias') {
        const denunciadoInput = new TextInputBuilder()
            .setCustomId('denunciado')
            .setLabel('Nick do denunciado')
            .setPlaceholder('Quem você está denunciando?')
            .setStyle(TextInputStyle.Short)
            .setRequired(true);
        
        const motivoInput = new TextInputBuilder()
            .setCustomId('motivo')
            .setLabel('Motivo da denúncia')
            .setPlaceholder('Por que está denunciando?')
            .setStyle(TextInputStyle.Short)
            .setRequired(true);
        
        const descricaoInput = new TextInputBuilder()
            .setCustomId('descricao')
            .setLabel('Descrição')
            .setPlaceholder('Descreva o ocorrido')
            .setStyle(TextInputStyle.Paragraph)
            .setRequired(true);
        
        modal.addComponents(
            new ActionRowBuilder().addComponents(denunciadoInput),
            new ActionRowBuilder().addComponents(motivoInput),
            new ActionRowBuilder().addComponents(descricaoInput)
        );
    }
    else if (tipo === 'bugs') {
        const localInput = new TextInputBuilder()
            .setCustomId('local')
            .setLabel('Onde ocorreu?')
            .setPlaceholder('Ex: Spawn, Warp, etc')
            .setStyle(TextInputStyle.Short)
            .setRequired(true);
        
        const bugInput = new TextInputBuilder()
            .setCustomId('bug')
            .setLabel('Descrição do bug')
            .setPlaceholder('Descreva o bug')
            .setStyle(TextInputStyle.Paragraph)
            .setRequired(true);
        
        const reproduzirInput = new TextInputBuilder()
            .setCustomId('reproduzir')
            .setLabel('Como reproduzir?')
            .setPlaceholder('Passo a passo para reproduzir')
            .setStyle(TextInputStyle.Paragraph)
            .setRequired(true);
        
        modal.addComponents(
            new ActionRowBuilder().addComponents(localInput),
            new ActionRowBuilder().addComponents(bugInput),
            new ActionRowBuilder().addComponents(reproduzirInput)
        );
    }
    
    return modal;
}

async function handleMenu(interaction) {
    const tipo = interaction.values[0];
    const modal = criarModal(tipo);
    await interaction.showModal(modal);
}

async function handleModal(interaction, client) {
    await interaction.deferReply({ flags: MessageFlags.Ephemeral });
    
    const tipo = interaction.customId.replace('modal_ticket_', '');
    const userId = interaction.user.id;
    const userTag = interaction.user.tag;
    const nick = interaction.fields.getTextInputValue('nick');
    
    try {
        if (activeTickets.has(userId)) {
            return interaction.editReply({ 
                content: `${CONFIG.emojis.errado} Você já possui um ticket ativo.` 
            });
        }

        const staffRole = await interaction.guild.roles.fetch(CONFIG.staffRoleId);
        if (!staffRole) {
            return interaction.editReply({ 
                content: `${CONFIG.emojis.errado} Erro: Cargo de staff não encontrado.` 
            });
        }

        const category = await interaction.guild.channels.fetch(CONFIG.ticketCategoryId);
        if (!category) {
            return interaction.editReply({ 
                content: `${CONFIG.emojis.errado} Erro: Categoria de tickets não encontrada.` 
            });
        }

        // Mapa de emojis por tipo de ticket
        const emojiPorTipo = {
            'suporte': '🎫',
            'financeiro': '🛒',
            'compras': '💰',
            'duvidas': '❓',
            'revisao': '⚖️',
            'revisao_punicao': '🔍',
            'denuncias': '🚫',
            'bugs': '🐛'
        };

        const emojiCanal = emojiPorTipo[tipo] || '📞';

        const isRevisaoPunicao = tipo === 'revisao_punicao';

        let informacoes = '';
        const campoIds = ['necessidade', 'produto', 'problema', 'duvida', 'id_punicao', 'motivo_punicao',
                         'defesa', 'denunciado', 'motivo', 'descricao', 'local', 'bug', 'reproduzir',
                         'tipo_id', 'motivo_tempo', 'provas'];

        campoIds.forEach(id => {
            try {
                const value = interaction.fields.getTextInputValue(id);
                if (value) {
                    const nomeCampo = {
                        'produto': '📦 Produto',
                        'problema': '❌ Problema',
                        'duvida': '❓ Dúvida',
                        'id_punicao': '🆔 ID Punição',
                        'motivo_punicao': '⚠️ Motivo',
                        'defesa': '⚖️ Defesa',
                        'denunciado': '👤 Denunciado',
                        'motivo': '📋 Motivo',
                        'descricao': '📝 Descrição',
                        'local': '📍 Local',
                        'bug': '🐛 Bug',
                        'reproduzir': '🔄 Reproduzir',
                        'tipo_id': '🔇 Tipo e ID',
                        'motivo_tempo': '⚠️ Motivo e Tempo',
                        'provas': '📎 Provas'
                    }[id] || id;

                    informacoes += `${nomeCampo}: ${value}\n`;
                }
            } catch (e) {}
        });

        if (isRevisaoPunicao) {
            const forum = await interaction.guild.channels.fetch(CONFIG.appealForumChannelId);
            if (!forum) {
                return interaction.editReply({ content: `${CONFIG.emojis.errado} Erro: Canal de revisões não encontrado.` });
            }

            const thread = await forum.threads.create({
                name: `🔍 revisao-${nick}`,
                message: {
                    content: `${staffRole}`,
                    embeds: [
                        new EmbedBuilder()
                            .setColor(CONFIG.colors.revisao_punicao || 0xFF4500)
                            .setTitle('🔍 Revisão de Punição')
                            .setThumbnail(`https://mc-heads.net/avatar/${nick}`)
                            .setDescription(
                                `**Solicitante:** ${interaction.user}\n**Nick:** ${nick}\n\n${informacoes}`
                            )
                            .setFooter({ text: 'SpartaMC' })
                            .setTimestamp()
                    ],
                    components: [
                        new ActionRowBuilder().addComponents(
                            new ButtonBuilder()
                                .setCustomId(`aceitar_punicao_${userId}`)
                                .setLabel('ACEITAR')
                                .setEmoji('✅')
                                .setStyle(ButtonStyle.Success),
                            new ButtonBuilder()
                                .setCustomId(`negar_punicao_${userId}`)
                                .setLabel('NEGAR')
                                .setEmoji('❌')
                                .setStyle(ButtonStyle.Danger)
                        )
                    ]
                },
                reason: `Revisão de punição de ${nick}`
            });

            activeTickets.set(userId, {
                tipo, nick, userTag,
                channelId: thread.id,
                userId,
                status: 'aberto',
                assumidoPor: null
            });
            salvarTickets();

            const confirmEmbed = new EmbedBuilder()
                .setColor(CONFIG.colors.revisao_punicao || 0xFF4500)
                .setTitle('<:check:1500966707961921657> Revisão de Punição')
                .setDescription(`Sua solicitação de revisão de punição foi enviada para análise.\n\nA equipe analisará e você receberá uma resposta no privado.`)
                .setFooter({ text: 'SpartaMC' })
                .setTimestamp();

            return interaction.editReply({ embeds: [confirmEmbed] });
        }

        // Criar canal para o ticket (tipos normais)
        const channel = await interaction.guild.channels.create({
            name: `${emojiCanal}・${nick}`,
            type: ChannelType.GuildText,
            parent: CONFIG.ticketCategoryId,
            permissionOverwrites: [
                {
                    id: interaction.guild.id,
                    deny: [PermissionFlagsBits.ViewChannel]
                },
                {
                    id: staffRole.id,
                    allow: [
                        PermissionFlagsBits.ViewChannel,
                        PermissionFlagsBits.SendMessages,
                        PermissionFlagsBits.ReadMessageHistory,
                        PermissionFlagsBits.ManageChannels,
                        PermissionFlagsBits.AttachFiles,
                        PermissionFlagsBits.EmbedLinks
                    ]
                },
                {
                    id: interaction.user.id,
                    allow: [
                        PermissionFlagsBits.ViewChannel,
                        PermissionFlagsBits.SendMessages,
                        PermissionFlagsBits.ReadMessageHistory,
                        PermissionFlagsBits.AttachFiles,
                        PermissionFlagsBits.EmbedLinks
                    ]
                }
            ]
        });

        // Registrar ticket ativo
        activeTickets.set(userId, {
            tipo,
            nick,
            userTag,
            channelId: channel.id,
            userId: userId,
            status: 'aberto',
            assumidoPor: null
        });
        salvarTickets();

        // EMBED MODERNA E LIMPA
        const ticketEmbed = new EmbedBuilder()
            .setColor(CONFIG.colors[tipo] || 0x5865F2)
            .setTitle(`📋 Ticket #${channel.id.slice(-4)} • ${tipo.toUpperCase()}`)
            .setThumbnail(`https://mc-heads.net/avatar/${nick}`)
            .setDescription(
                `**Solicitante:** ${interaction.user}\n` +
                `**Nick:** ${nick}\n\n` +
                (informacoes ? `${informacoes}` : '`Sem informações adicionais`')
            )
            .setFooter({ text: `SpartaMC` })
            .setTimestamp();

        // Botões
        const buttons = new ActionRowBuilder().addComponents(
            new ButtonBuilder()
                .setCustomId(`assumir_${userId}`)
                .setLabel('ASSUMIR TICKET')
                .setEmoji('1494615781260922951')
                .setStyle(ButtonStyle.Success),
            new ButtonBuilder()
                .setCustomId(`fechar_${userId}`)
                .setLabel('FECHAR TICKET')
                .setEmoji('1494615783207207053')
                .setStyle(ButtonStyle.Danger)
        );

        // Mensagem de boas-vindas no canal
        await channel.send({ 
            content: `${interaction.user} | ${staffRole}`,
            embeds: [ticketEmbed],
            components: [buttons]
        });

        // Mensagem de confirmação para o usuário
        const confirmEmbed = new EmbedBuilder()
            .setColor(0x5865F2)
            .setTitle('<:check:1500966707961921657> Ticket Criado com Sucesso!')
            .setDescription(
                `Seu ticket foi criado e está disponível em ${channel}.\n\n` +
                `A equipe responderá em breve. Por favor, aguarde.`
            )
            .setFooter({ text: 'SpartaMC' })
            .setTimestamp();

        await interaction.editReply({ 
            embeds: [confirmEmbed]
        });

        // Log profissional
        const logChannel = interaction.guild.channels.cache.get(CONFIG.logChannelId);
        if (logChannel) {
            const logEmbed = new EmbedBuilder()
                .setColor(CONFIG.colors[tipo] || 0x5865F2)
                .setTitle(`📨 Novo Ticket #${channel.id.slice(-4)}`)
                .setThumbnail(`https://mc-heads.net/avatar/${nick}`)
                .setDescription(
                    `**Tipo:** ${tipo.toUpperCase()}\n` +
                    `**Usuário:** ${interaction.user}\n` +
                    `**Nick:** ${nick}\n` +
                    `**Canal:** ${channel}`
                )
                .setFooter({ text: 'SpartaMC' })
                .setTimestamp();

            await logChannel.send({ embeds: [logEmbed] });
        }

    } catch (error) {
        console.error('❌ Erro ao criar ticket:', error);
        activeTickets.delete(userId);
        salvarTickets();
        await interaction.editReply({ 
            content: `${CONFIG.emojis.errado} Erro: ${error.message}` 
        });
    }
}

async function assumirTicket(interaction, client) {
    const userId = interaction.customId.replace('assumir_', '');
    const ticket = activeTickets.get(userId);
    
    if (!ticket) {
        return interaction.reply({ 
            content: `${CONFIG.emojis.errado} Este ticket não está mais ativo.`, 
            flags: MessageFlags.Ephemeral 
        });
    }
    
    if (ticket.assumidoPor) {
        return interaction.reply({ 
            content: `${CONFIG.emojis.errado} Este ticket já foi assumido por **${ticket.assumidoPor}**.`, 
            flags: MessageFlags.Ephemeral 
        });
    }
    
    ticket.assumidoPor = interaction.user.tag;
    ticket.assumidoPorId = interaction.user.id;
    activeTickets.set(userId, ticket);
    salvarTickets();
    
    await interaction.channel.setName(`📞・${ticket.nick}`);
    
    // Enviar webhook 3 segundos depois
    setTimeout(async () => {
        try {
            // Criar webhook com nome de perfil e foto do staff
            const webhook = await interaction.channel.createWebhook({
                name: interaction.user.displayName,
                avatar: interaction.user.avatarURL()
            });
            
            // Enviar mensagem através do webhook
            await webhook.send({
                content: `Olá! Me chamo **${interaction.user.displayName}** e ficarei responsável pelo seu atendimento!`
            });
            
            // Deletar o webhook após o envio
            await webhook.delete();
        } catch (e) {
            console.error('Erro ao criar webhook:', e);
        }
    }, 3000);
    
    const embed = EmbedBuilder.from(interaction.message.embeds[0])
        .setColor(0x00FF00)
        .addFields({ name: '<:check:1500966707961921657> Ticket Assumido Por', value: interaction.user.tag });
    
    const row = ActionRowBuilder.from(interaction.message.components[0]);
    row.components[0].setDisabled(true);
    
    await interaction.update({ embeds: [embed], components: [row] });
    
    const assumiuEmbed = new EmbedBuilder()
        .setColor(0x00FF00)
        .setTitle('<:check:1500966707961921657> Ticket Assumido')
        .setDescription(`**${interaction.user}** assumiu o atendimento deste ticket.`)
        .setTimestamp();

    await interaction.channel.send({ embeds: [assumiuEmbed] });
    
    const logChannel = interaction.guild.channels.cache.get(CONFIG.logChannelId);
    if (logChannel) {
        const logEmbed = new EmbedBuilder()
            .setColor(0x00FF00)
            .setTitle('<:check:1500966707961921657> Ticket Assumido')
            .setDescription(
                `**Nick:** ${ticket.nick}\n` +
                `**Assumido por:** ${interaction.user.tag}`
            )
            .setFooter({ text: 'SpartaMC' })
            .setTimestamp();
        
            await logChannel.send({ embeds: [logEmbed] });
        }
    }

async function fecharTicket(interaction, client) {
    const userId = interaction.customId.replace('fechar_', '');
    const ticket = activeTickets.get(userId);
    
    if (!ticket) {
        return interaction.reply({ 
            content: `${CONFIG.emojis.errado} Este ticket não está mais ativo.`, 
            flags: MessageFlags.Ephemeral 
        });
    }
    
    const isStaff = interaction.member.roles.cache.has(CONFIG.staffRoleId);
    const isOwner = interaction.user.id === userId;
    
    if (!isStaff && !isOwner) {
        return interaction.reply({ 
            content: `${CONFIG.emojis.errado} Você não tem permissão para fechar este ticket.`, 
            flags: MessageFlags.Ephemeral 
        });
    }
    
    const closeEmbed = new EmbedBuilder()
        .setColor(0xFF0000)
        .setTitle('<:cross:1500966712525324288> Ticket Fechado')
        .setDescription(`**${interaction.user}** fechou o ticket.\nEste canal será deletado em **3 segundos**.`)
        .setTimestamp();

    await interaction.reply({ 
        embeds: [closeEmbed]
    });
    
    const logChannel = interaction.guild.channels.cache.get(CONFIG.logChannelId);
    if (logChannel) {
        const fechadoPor = interaction.user.id === userId ? 'usuário' : interaction.user.tag;
        const logEmbed = new EmbedBuilder()
            .setColor(0xFF0000)
            .setTitle('<:cross:1500966712525324288> Ticket Fechado')
            .setDescription(
                `**Nick:** ${ticket.nick}\n` +
                `**Fechado por:** ${fechadoPor}`
            )
            .setFooter({ text: 'SpartaMC' })
            .setTimestamp();
        
        await logChannel.send({ embeds: [logEmbed] });
    }
    
    activeTickets.delete(userId);
    salvarTickets();
    
    setTimeout(() => {
        interaction.channel.delete().catch(() => {});
    }, 3000);
}

async function aceitarRevisao(interaction, client) {
    const userId = interaction.customId.replace('aceitar_punicao_', '');
    const ticket = activeTickets.get(userId);

    if (!ticket) {
        return interaction.reply({ content: `${CONFIG.emojis.errado} Esta revisão não está mais ativa.`, flags: MessageFlags.Ephemeral });
    }

    const embed = EmbedBuilder.from(interaction.message.embeds[0])
        .setColor(0x00FF00)
        .setTitle('✅ Revisão Aceita')
        .setDescription(`${interaction.user} aceitou esta revisão.\n\nO usuário será notificado.`)
        .setFooter({ text: `Aceito por ${interaction.user.tag}` });

    const row = ActionRowBuilder.from(interaction.message.components[0]);
    row.components.forEach(c => c.setDisabled(true));

    await interaction.update({ embeds: [embed], components: [row] });

    try {
        const usuario = await client.users.fetch(userId);
        const aceitoEmbed = new EmbedBuilder()
            .setColor(0x00FF00)
            .setTitle('✅ Revisão de Punição Aceita')
            .setDescription('Sua solicitação de revisão de punição foi **aceita**! Sua punição foi revisada e ajustada pela equipe.')
            .setFooter({ text: 'SpartaMC' })
            .setTimestamp();

        await usuario.send({ embeds: [aceitoEmbed] });
    } catch {
        console.log('Não foi possível notificar o usuário');
    }

    await interaction.channel.send({ content: `${CONFIG.emojis.correto} Revisão aceita por ${interaction.user}.` });

    activeTickets.delete(userId);
    salvarTickets();
}

async function negarRevisao(interaction, client) {
    const userId = interaction.customId.replace('negar_punicao_', '');
    const ticket = activeTickets.get(userId);

    if (!ticket) {
        return interaction.reply({ content: `${CONFIG.emojis.errado} Esta revisão não está mais ativa.`, flags: MessageFlags.Ephemeral });
    }

    const embed = EmbedBuilder.from(interaction.message.embeds[0])
        .setColor(0xFF0000)
        .setTitle('❌ Revisão Negada')
        .setDescription(`${interaction.user} negou esta revisão.\n\nO usuário será notificado.`)
        .setFooter({ text: `Negado por ${interaction.user.tag}` });

    const row = ActionRowBuilder.from(interaction.message.components[0]);
    row.components.forEach(c => c.setDisabled(true));

    await interaction.update({ embeds: [embed], components: [row] });

    try {
        const usuario = await client.users.fetch(userId);
        const negadoEmbed = new EmbedBuilder()
            .setColor(0xFF0000)
            .setTitle('❌ Revisão de Punição Negada')
            .setDescription('Sua solicitação de revisão de punição foi **negada**. A punição aplicada permanece conforme o registro.')
            .setFooter({ text: 'SpartaMC' })
            .setTimestamp();

        await usuario.send({ embeds: [negadoEmbed] });
    } catch {
        console.log('Não foi possível notificar o usuário');
    }

    await interaction.channel.send({ content: `${CONFIG.emojis.errado} Revisão negada por ${interaction.user}.` });

    activeTickets.delete(userId);
    salvarTickets();
}

function getActiveTickets() {
    return activeTickets;
}

module.exports = {
    handleMenu,
    handleModal,
    assumirTicket,
    fecharTicket,
    aceitarRevisao,
    negarRevisao,
    getActiveTickets,
    carregarTickets,
    salvarTickets
};
