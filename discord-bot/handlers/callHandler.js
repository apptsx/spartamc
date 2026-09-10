const { 
    ChannelType, 
    PermissionFlagsBits,
    EmbedBuilder,
    ActionRowBuilder,
    ButtonBuilder,
    ButtonStyle,
    MessageFlags,
    ModalBuilder,
    TextInputBuilder,
    TextInputStyle
} = require('discord.js');

const activeCalls = new Map();

// Canal de voz onde os usuários entram para criar sua call
const ENTRY_VOICE_CHANNEL_ID = '1480857275026509964';
// Categoria onde as calls serão criadas
const CALL_CATEGORY_ID = '1480857229795135519';
// ID do cargo de staff (opcional, para permissões especiais)
const STAFF_ROLE_ID = '1480857036580192399';

// Função para criar o painel de controle (embed)
function criarPainelEmbed(user, callChannel) {
    const embed = new EmbedBuilder()
        .setColor(0x00FF00)
        .setAuthor({ 
            name: user.username, 
            iconURL: user.displayAvatarURL() 
        })
        .setTitle('💻 Interface | Monitorar Chamadas')
        .setDescription(
            '🔒 — [`Bloquear`]\n' +
            '🔐 — [`Desbloquear`]\n' +
            '👻 — [`Ocultar/Revelar`]\n' +
            '🎙️ — [`Reivindicar`]\n' +
            '🖊️ — [`Renomear`]\n' +
            '🔌 — [`Desconectar`]\n' +
            '✅ — [`Permitir`]\n' +
            '🔨 — [`Banir/Desbanir`]\n' +
            '👀 — [`Ver Informações`]\n' +
            '🚹 — [`Ajustar Limite`]'
        )
        .addFields(
            { name: '📞 Canal', value: `${callChannel}`, inline: true },
            { name: '👤 Dono', value: `${user}`, inline: true },
            { name: '🔊 Limite', value: `${callChannel.userLimit || 'Ilimitado'}`, inline: true }
        )
        .setFooter({ text: 'Feito por nyko' })
        .setTimestamp();

    return embed;
}

// Função para criar os botões de controle
function criarBotoes(userId, channelId) {
    const row1 = new ActionRowBuilder()
        .addComponents(
            new ButtonBuilder()
                .setCustomId(`call_bloquear_${userId}_${channelId}`)
                .setEmoji('🔒')
                .setLabel('Bloquear')
                .setStyle(ButtonStyle.Danger),
            new ButtonBuilder()
                .setCustomId(`call_desbloquear_${userId}_${channelId}`)
                .setEmoji('🔐')
                .setLabel('Desbloquear')
                .setStyle(ButtonStyle.Success),
            new ButtonBuilder()
                .setCustomId(`call_ocultar_${userId}_${channelId}`)
                .setEmoji('👻')
                .setLabel('Ocultar/Revelar')
                .setStyle(ButtonStyle.Secondary),
            new ButtonBuilder()
                .setCustomId(`call_reivindicar_${userId}_${channelId}`)
                .setEmoji('🎙️')
                .setLabel('Reivindicar')
                .setStyle(ButtonStyle.Primary),
            new ButtonBuilder()
                .setCustomId(`call_renomear_${userId}_${channelId}`)
                .setEmoji('🖊️')
                .setLabel('Renomear')
                .setStyle(ButtonStyle.Secondary)
        );

    const row2 = new ActionRowBuilder()
        .addComponents(
            new ButtonBuilder()
                .setCustomId(`call_desconectar_${userId}_${channelId}`)
                .setEmoji('🔌')
                .setLabel('Desconectar')
                .setStyle(ButtonStyle.Danger),
            new ButtonBuilder()
                .setCustomId(`call_permitir_${userId}_${channelId}`)
                .setEmoji('✅')
                .setLabel('Permitir')
                .setStyle(ButtonStyle.Success),
            new ButtonBuilder()
                .setCustomId(`call_banir_${userId}_${channelId}`)
                .setEmoji('🔨')
                .setLabel('Banir/Desbanir')
                .setStyle(ButtonStyle.Danger),
            new ButtonBuilder()
                .setCustomId(`call_info_${userId}_${channelId}`)
                .setEmoji('👀')
                .setLabel('Ver Informações')
                .setStyle(ButtonStyle.Secondary),
            new ButtonBuilder()
                .setCustomId(`call_limite_${userId}_${channelId}`)
                .setEmoji('🚹')
                .setLabel('Ajustar Limite')
                .setStyle(ButtonStyle.Primary)
        );

    return [row1, row2];
}

// Função para enviar o painel no chat da call
async function enviarPainelNoChat(channel, user, userId) {
    try {
        const painelEmbed = criarPainelEmbed(user, channel);
        const botoes = criarBotoes(userId, channel.id);

        await channel.send({
            content: `👑 **${user.username}**, este é seu painel de controle exclusivo!`,
            embeds: [painelEmbed],
            components: botoes
        });
        
        console.log(`✅ Painel de controle enviado no canal ${channel.name}`);
    } catch (error) {
        console.error('❌ Erro ao enviar painel no chat:', error);
    }
}

// Função para apagar a call
async function apagarCall(guild, channelId, userId) {
    try {
        const channel = guild.channels.cache.get(channelId);
        if (channel) {
            await channel.delete();
            activeCalls.delete(userId);
            console.log(`🗑️ Call de ${userId} apagada`);
        }
    } catch (error) {
        console.error('❌ Erro ao apagar call:', error);
    }
}

// Evento quando usuário entra no canal de entrada
async function onVoiceStateUpdate(oldState, newState, client) {
    try {
        // Usuário entrou em um canal de voz
        if (newState.channelId === ENTRY_VOICE_CHANNEL_ID) {
            const user = newState.member.user;
            const guild = newState.guild;
            
            // Verificar se já tem uma call ativa
            if (activeCalls.has(user.id)) {
                // Se já tem, mover para a call existente
                const callData = activeCalls.get(user.id);
                const existingChannel = guild.channels.cache.get(callData.channelId);
                if (existingChannel) {
                    await newState.setChannel(existingChannel);
                }
                return;
            }

            // Criar novo canal de voz
            const category = guild.channels.cache.get(CALL_CATEGORY_ID);
            if (!category) {
                console.error('❌ Categoria de calls não encontrada!');
                return;
            }

            // Criar canal com nome do usuário
            const callChannel = await guild.channels.create({
                name: user.username,
                type: ChannelType.GuildVoice,
                parent: CALL_CATEGORY_ID,
                userLimit: 10,
                permissionOverwrites: [
                    {
                        id: guild.id,
                        allow: [PermissionFlagsBits.Connect, PermissionFlagsBits.Speak],
                        deny: []
                    },
                    {
                        id: user.id,
                        allow: [
                            PermissionFlagsBits.Connect,
                            PermissionFlagsBits.Speak,
                            PermissionFlagsBits.MuteMembers,
                            PermissionFlagsBits.DeafenMembers,
                            PermissionFlagsBits.MoveMembers
                        ],
                        deny: []
                    }
                ]
            });

            // Registrar call ativa
            activeCalls.set(user.id, {
                userId: user.id,
                channelId: callChannel.id,
                createdAt: Date.now(),
                ownerId: user.id
            });

            // Mover usuário para o novo canal
            await newState.setChannel(callChannel);

            // Enviar painel de controle no chat da call
            await enviarPainelNoChat(callChannel, user, user.id);
        }

        // Verificar se alguém saiu de uma call
        if (oldState.channelId) {
            const channel = oldState.channel;
            
            // Verificar se é uma call gerenciada pelo bot
            const callData = Array.from(activeCalls.entries()).find(([_, data]) => data.channelId === oldState.channelId);
            
            if (callData) {
                const [ownerId, data] = callData;
                
                // Se o dono saiu, apagar a call imediatamente
                if (oldState.member.user.id === ownerId) {
                    console.log(`👑 Dono ${oldState.member.user.tag} saiu da call, apagando...`);
                    await apagarCall(oldState.guild, oldState.channelId, ownerId);
                }
                // Se não é o dono, apenas verificar se a call ficou vazia
                else if (channel && channel.members.size === 0) {
                    // Usar o channelId do data, não o oldState.channelId (pode estar deletado)
                    const currentChannelId = data.channelId;
                    setTimeout(async () => {
                        const updatedChannel = oldState.guild.channels.cache.get(currentChannelId);
                        if (updatedChannel && updatedChannel.members.size === 0) {
                            await apagarCall(oldState.guild, currentChannelId, ownerId);
                        }
                    }, 10000);
                }
            }
        }
    } catch (error) {
        console.error('❌ Erro no sistema de calls:', error);
    }
}

// Handlers para os botões
async function handleButtons(interaction, client) {
    if (!interaction.customId.startsWith('call_')) return;

    const partes = interaction.customId.split('_');
    const acao = partes[1];
    const userId = partes[2];
    const channelId = partes[3];
    
    const channel = interaction.guild.channels.cache.get(channelId);
    
    if (!channel) {
        return interaction.reply({
            content: '❌ Canal não encontrado!',
            flags: MessageFlags.Ephemeral
        });
    }

    // Verificar permissões (apenas dono ou staff podem controlar)
    const isOwner = interaction.user.id === userId;
    const isStaff = interaction.member.roles.cache.has(STAFF_ROLE_ID);

    if (!isOwner && !isStaff) {
        return interaction.reply({
            content: '❌ Você não tem permissão para controlar esta call!',
            flags: MessageFlags.Ephemeral
        });
    }

    switch(acao) {
        case 'bloquear':
            await channel.permissionOverwrites.edit(interaction.guild.id, {
                Connect: false
            });
            await interaction.reply({
                content: '🔒 Canal bloqueado! Apenas quem tem permissão pode entrar.',
                flags: MessageFlags.Ephemeral
            });
            break;

        case 'desbloquear':
            await channel.permissionOverwrites.edit(interaction.guild.id, {
                Connect: true
            });
            await interaction.reply({
                content: '🔐 Canal desbloqueado! Todos podem entrar.',
                flags: MessageFlags.Ephemeral
            });
            break;

        case 'ocultar':
            const isHidden = channel.permissionOverwrites.cache.get(interaction.guild.id)?.deny.has(PermissionFlagsBits.ViewChannel) || false;
            
            await channel.permissionOverwrites.edit(interaction.guild.id, {
                ViewChannel: isHidden ? null : false
            });
            
            await interaction.reply({
                content: isHidden ? '👻 Canal revelado!' : '👻 Canal oculto!',
                flags: MessageFlags.Ephemeral
            });
            break;

        case 'reivindicar':
            if (!channel.members.has(userId)) {
                return interaction.reply({
                    content: '❌ O dono original não está mais no canal!',
                    flags: MessageFlags.Ephemeral
                });
            }

            // Transferir ownership
            const callData = activeCalls.get(userId);
            if (callData) {
                // Remover o antigo e adicionar o novo
                activeCalls.delete(userId);
                activeCalls.set(interaction.user.id, {
                    ...callData,
                    ownerId: interaction.user.id,
                    userId: interaction.user.id
                });
            }

            await interaction.reply({
                content: `🎙️ Você agora é o dono da call!`,
                flags: MessageFlags.Ephemeral
            });
            break;

        case 'renomear':
            // Abrir modal para renomear
            const modal = new ModalBuilder()
                .setCustomId(`call_rename_${userId}_${channelId}`)
                .setTitle('Renomear Canal');

            const nomeInput = new TextInputBuilder()
                .setCustomId('novo_nome')
                .setLabel('Novo nome do canal')
                .setPlaceholder('Digite o novo nome')
                .setStyle(TextInputStyle.Short)
                .setRequired(true)
                .setMaxLength(32);

            const row = new ActionRowBuilder().addComponents(nomeInput);
            modal.addComponents(row);

            await interaction.showModal(modal);
            break;

        case 'desconectar':
            // Desconectar um usuário específico
            await interaction.reply({
                content: '🔌 Mencione o usuário que deseja desconectar:',
                flags: MessageFlags.Ephemeral
            });
            
            // Criar coletor para aguardar a menção
            const filter = m => m.author.id === interaction.user.id;
            const collector = interaction.channel.createMessageCollector({ filter, max: 1, time: 30000 });
            
            collector.on('collect', async msg => {
                const target = msg.mentions.members.first();
                if (target && target.voice.channelId === channel.id) {
                    await target.voice.disconnect();
                    await interaction.followUp({
                        content: `🔌 ${target.user.tag} foi desconectado!`,
                        flags: MessageFlags.Ephemeral
                    });
                } else {
                    await interaction.followUp({
                        content: '❌ Usuário não encontrado ou não está no canal!',
                        flags: MessageFlags.Ephemeral
                    });
                }
            });
            break;

        case 'permitir':
            await interaction.reply({
                content: '✅ Mencione o usuário que você quer permitir:',
                flags: MessageFlags.Ephemeral
            });
            
            const filterPermitir = m => m.author.id === interaction.user.id;
            const collectorPermitir = interaction.channel.createMessageCollector({ filter: filterPermitir, max: 1, time: 30000 });
            
            collectorPermitir.on('collect', async msg => {
                const target = msg.mentions.members.first();
                if (target) {
                    await channel.permissionOverwrites.edit(target.id, {
                        Connect: true,
                        Speak: true
                    });
                    await interaction.followUp({
                        content: `✅ ${target.user.tag} agora pode entrar na call!`,
                        flags: MessageFlags.Ephemeral
                    });
                }
            });
            break;

        case 'banir':
            await interaction.reply({
                content: '🔨 Mencione o usuário que você quer banir da call:',
                flags: MessageFlags.Ephemeral
            });
            
            const filterBanir = m => m.author.id === interaction.user.id;
            const collectorBanir = interaction.channel.createMessageCollector({ filter: filterBanir, max: 1, time: 30000 });
            
            collectorBanir.on('collect', async msg => {
                const target = msg.mentions.members.first();
                if (target) {
                    // Desconectar se estiver no canal
                    if (target.voice.channelId === channel.id) {
                        await target.voice.disconnect();
                    }
                    
                    // Banir (negar permissão de conectar)
                    await channel.permissionOverwrites.edit(target.id, {
                        Connect: false
                    });
                    
                    await interaction.followUp({
                        content: `🔨 ${target.user.tag} foi banido da call!`,
                        flags: MessageFlags.Ephemeral
                    });
                }
            });
            break;

        case 'info':
            const memberCount = channel.members.size;
            const members = channel.members.map(m => `- ${m.user.tag}`).join('\n') || 'Ninguém';

            const infoEmbed = new EmbedBuilder()
                .setColor(0x00FF00)
                .setTitle('👀 Informações da Call')
                .addFields(
                    { name: '📞 Canal', value: channel.name, inline: true },
                    { name: '👥 Membros', value: `${memberCount}/${channel.userLimit || '∞'}`, inline: true },
                    { name: '🔊 Bitrate', value: `${channel.bitrate / 1000} kbps`, inline: true },
                    { name: '📋 Lista de membros', value: members }
                )
                .setTimestamp();

            await interaction.reply({
                embeds: [infoEmbed],
                flags: MessageFlags.Ephemeral
            });
            break;

        case 'limite':
            // Ajustar limite de usuários
            const limites = [0, 5, 10, 15, 20, 25, 30];
            const limiteAtual = channel.userLimit || 0;
            
            const rowLimites = new ActionRowBuilder();
            limites.forEach(limite => {
                rowLimites.addComponents(
                    new ButtonBuilder()
                        .setCustomId(`call_setlimit_${userId}_${channelId}_${limite}`)
                        .setLabel(limite === 0 ? '∞' : limite.toString())
                        .setStyle(limite === limiteAtual ? ButtonStyle.Primary : ButtonStyle.Secondary)
                );
            });

            await interaction.reply({
                content: '🚹 Selecione o novo limite:',
                components: [rowLimites],
                flags: MessageFlags.Ephemeral
            });
            break;

        case 'setlimit':
            const limite = partes[4];
            await channel.setUserLimit(parseInt(limite) || 0);
            await interaction.update({
                content: `✅ Limite alterado para ${limite === '0' ? 'Ilimitado' : limite}`,
                components: [],
                flags: MessageFlags.Ephemeral
            });
            break;
    }
}

// Handler para modals de renomear
async function handleModals(interaction, client) {
    if (!interaction.customId.startsWith('call_rename_')) return;

    const partes = interaction.customId.split('_');
    const userId = partes[2];
    const channelId = partes[3];
    const novoNome = interaction.fields.getTextInputValue('novo_nome');
    const channel = interaction.guild.channels.cache.get(channelId);

    if (!channel) {
        return interaction.reply({
            content: '❌ Canal não encontrado!',
            flags: MessageFlags.Ephemeral
        });
    }

    await channel.setName(novoNome);

    await interaction.reply({
        content: `🖊️ Canal renomeado para **${novoNome}**!`,
        flags: MessageFlags.Ephemeral
    });
}

module.exports = {
    onVoiceStateUpdate,
    handleButtons,
    handleModals,
    activeCalls
};
