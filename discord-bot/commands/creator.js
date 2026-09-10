const { 
    SlashCommandBuilder, 
    PermissionFlagsBits,
    ContainerBuilder, 
    TextDisplayBuilder, 
    ButtonBuilder, 
    ButtonStyle, 
    ActionRowBuilder,
    ModalBuilder,
    TextInputBuilder,
    TextInputStyle,
    MessageFlags,
    EmbedBuilder,
    MediaGalleryBuilder,
    MediaGalleryItemBuilder
} = require('discord.js');
const fs = require('fs');
const path = require('path');

// Caminhos dos arquivos JSON
const configPath = path.join(__dirname, '../creator_config.json');
const solicitacoesPath = path.join(__dirname, '../creator_solicitacoes.json');
const cargosAtivosPath = path.join(__dirname, '../creator_cargos.json');

// Funções auxiliares para ler/escrever JSON
function readJSON(filePath, defaultValue = {}) {
    if (!fs.existsSync(filePath)) {
        return defaultValue;
    }
    try {
        return JSON.parse(fs.readFileSync(filePath, 'utf8'));
    } catch (e) {
        console.error(`Erro ao ler ${filePath}:`, e);
        return defaultValue;
    }
}

function writeJSON(filePath, data) {
    try {
        fs.writeFileSync(filePath, JSON.stringify(data, null, 2));
    } catch (e) {
        console.error(`Erro ao escrever ${filePath}:`, e);
    }
}

module.exports = {
    help: {
        name: 'creator',
        description: 'Envia o painel de solicitação de rank Creator (Apenas Administradores)'
    },
    data: new SlashCommandBuilder()
        .setName('creator')
        .setDescription('Envia o painel de solicitação do rank Creator')
        .setDefaultMemberPermissions(PermissionFlagsBits.Administrator),

    async execute(interaction) {
        if (!interaction.member.permissions.has(PermissionFlagsBits.Administrator)) {
            return interaction.reply({ 
                content: '❌ Apenas administradores podem usar este comando!', 
                flags: MessageFlags.Ephemeral 
            });
        }

        try {
            const guildConfig = readJSON(configPath, {});
            const guildId = interaction.guild.id;
            const config = guildConfig[guildId] || {};

            const creatorRoleId = config.creatorRoleId || process.env.CREATOR_ROLE_ID || '1508520449443758311';
            const logChannelId = config.logChannelId || process.env.CREATOR_LOG_CHANNEL_ID || '1500407880774582282';

            const creatorRole = interaction.guild.roles.cache.get(creatorRoleId);
            if (!creatorRole) {
                return interaction.reply({ 
                    content: '❌ Cargo de Creator não encontrado no servidor!', 
                    flags: MessageFlags.Ephemeral 
                });
            }

            await interaction.deferReply({ flags: MessageFlags.Ephemeral });

            const container = new ContainerBuilder()
                .addMediaGalleryComponents(
                    new MediaGalleryBuilder()
                        .addItems(
                            new MediaGalleryItemBuilder()
                                .setURL("https://cdn.discordapp.com/attachments/1494922740178620616/1510895686311346296/creator.png?ex=6a1e7ac2&is=6a1d2942&hm=71cb98c9c93f3204e94a6c4f15d6e5b2d62f34aebd6f1ba94f01d78e8799a4af&")
                        )
                )
                .addTextDisplayComponents(
                    new TextDisplayBuilder().setContent(
                        "<:cogumelo:1509646021565747230> **CRIADORES DE CONTEÚDO**\n\n" +
                        "Se você gosta de criar vídeos, lives ou outros conteúdos e quer ter a chance de representar a Sparta oficialmente sendo nosso criador de conteúdo, veja nossos requisitos abaixo:\n\n" +
                        "<:YouTube:1510895239357796442> **Requisitos para Youtube/Shorts:**\n" +
                        "• 500 visualizações e 50 likes em um único shorts gravado no servidor;\n" +
                        "• 100 visualizações e 35 likes em um único vídeo gravado no servidor;\n" +
                        "• Uma somatória de 1.200 visualizações com média de 60 likes em vídeos nos últimos 15 dias.\n\n" +
                        "<:tiktok:1510894790802276394> **Requisitos para TikTok:**\n" +
                        "• Alcançar 500 visualizações e 50 likes em um único vídeo gravado no servidor;\n" +
                        "• Ter no mínimo 100 seguidores.\n\n" +
                        "<:Twitch:1510894794292068522> **Requisitos para Twitch/Streamer:**\n" +
                        "• Um mínimo de duas lives por semana no servidor;\n" +
                        "• VODS com soma de 50 visualizações nos últimos 15 dias;\n" +
                        "• É preciso realizar pelo menos duas horas de live no servidor;\n" +
                        "• OBRIGATÓRIO VODS do canal ativado.\n\n" +
                        "<:Corante_vermelhoH:1494067495613501501> **Observações:**\n" +
                        "• É necessário ter a rede social vinculada ao Discord;\n" +
                        "• Os requisitos são considerados a cada 15 dias;\n" +
                        "• Será levada em conta a conduta dentro e fora do servidor;\n" +
                        "• Caso sua solicitação seja negada, aguarde 72 horas para refazê-la;\n" +
                        "• Não é permitido uso de bots ou auxílios no aumento de visualizações."
                    )
                )
                .addActionRowComponents(
                    new ActionRowBuilder()
                        .addComponents(
                            new ButtonBuilder()
                                .setCustomId('solicitar_creator')
                                .setLabel('Solicitar Rank Creator')
                                .setEmoji('🎥')
                                .setStyle(ButtonStyle.Primary)
                        )
                );

            await interaction.channel.send({
                components: [container],
                flags: [MessageFlags.IsComponentsV2]
            });

            await interaction.editReply({ 
                content: `✅ Painel enviado com sucesso!\n📢 Canal de logs: <#${logChannelId}>\n🎥 Cargo: <@&${creatorRoleId}>`
            });

        } catch (error) {
            console.error('❌ Erro detalhado:', error);
            if (interaction.deferred) {
                await interaction.editReply({ content: `❌ Ocorreu um erro: ${error.message}` });
            } else if (!interaction.replied) {
                await interaction.reply({ 
                    content: `❌ Ocorreu um erro: ${error.message}`, 
                    flags: MessageFlags.Ephemeral 
                });
            }
        }
    }
};

// Função para verificar e remover cargos expirados
async function verificarCargosExpirados(client) {
    try {
        const cargos = readJSON(cargosAtivosPath, {});
        const agora = Date.now();
        let modificado = false;

        for (const key in cargos) {
            const cargo = cargos[key];
            if (cargo.expires_at <= agora) {
                try {
                    const guild = await client.guilds.fetch(cargo.guild_id);
                    const member = await guild.members.fetch(cargo.user_id);
                    const role = guild.roles.cache.get(cargo.role_id);
                    
                    if (member && role) {
                        await member.roles.remove(role);
                        
                        const expiradoEmbed = new EmbedBuilder()
                            .setColor('#FF0000')
                            .setTitle('⏰ Cargo Expirado')
                            .setDescription(
                                `Seu cargo de **Creator** no servidor **${guild.name}** expirou após 15 dias.\n\n` +
                                `Se deseja continuar com o cargo, faça uma nova solicitação no servidor!`
                            );
                        
                        await member.send({ embeds: [expiradoEmbed] }).catch(() => {});
                    }
                    
                    delete cargos[key];
                    modificado = true;
                    console.log(`✅ Cargo de Creator removido de ${cargo.user_id} (expirado)`);
                    
                } catch (error) {
                    console.error('Erro ao remover cargo expirado:', error);
                }
            }
        }

        if (modificado) {
            writeJSON(cargosAtivosPath, cargos);
        }
    } catch (error) {
        console.error('Erro na verificação de expiração:', error);
    }
}

// Handler para botão de solicitação
module.exports.handleSolicitacaoButton = async function(interaction) {
    try {
        console.log('🎥 handleSolicitacaoButton chamado');
        
        const guildConfigData = readJSON(configPath, {});
        const guildId = interaction.guild.id;
        const guildConfig = guildConfigData[guildId] || {
            creatorRoleId: process.env.CREATOR_ROLE_ID || '1508520449443758311',
            logChannelId: process.env.CREATOR_LOG_CHANNEL_ID || '1500407880774582282'
        };

        // Criar e mostrar o modal
        const modal = new ModalBuilder()
            .setCustomId('modal_solicitacao_creator')
            .setTitle('Solicitação de Rank Creator');

        const nickInput = new TextInputBuilder()
            .setCustomId('nick')
            .setLabel('Qual seu nickname?')
            .setStyle(TextInputStyle.Short)
            .setRequired(true)
            .setPlaceholder('Ex: xisteey');

        const plataformaInput = new TextInputBuilder()
            .setCustomId('plataforma')
            .setLabel('Qual sua plataforma de criação?')
            .setStyle(TextInputStyle.Short)
            .setRequired(true)
            .setPlaceholder('Ex: YouTube, TikTok, Twitch');

        const linkInput = new TextInputBuilder()
            .setCustomId('link')
            .setLabel('Cole o link do seu perfil:')
            .setStyle(TextInputStyle.Short)
            .setRequired(true)
            .setPlaceholder('https://...');

        const requisitosInput = new TextInputBuilder()
            .setCustomId('requisitos')
            .setLabel('Você possui os requisitos? (Liste)')
            .setStyle(TextInputStyle.Paragraph)
            .setRequired(true)
            .setPlaceholder('Descreva como você atende aos requisitos...');

        modal.addComponents(
            new ActionRowBuilder().addComponents(nickInput),
            new ActionRowBuilder().addComponents(plataformaInput),
            new ActionRowBuilder().addComponents(linkInput),
            new ActionRowBuilder().addComponents(requisitosInput)
        );

        await interaction.showModal(modal).catch(err => {
            console.error('❌ Erro ao exibir modal (interaction expirou):', err.message);
            return;
        });
        console.log('✅ Modal exibido com sucesso');

        // Aguardar o envio do modal
        try {
            const modalInteraction = await interaction.awaitModalSubmit({
                filter: i => i.customId === 'modal_solicitacao_creator' && i.user.id === interaction.user.id,
                time: 300000 // 5 minutos
            });

            console.log('📝 Modal recebido');
            await processarModalSolicitacao(modalInteraction, guildConfig);

        } catch (error) {
            // Ignorar erro de timeout (usuário não respondeu)
            if (error.code === 'InteractionCollectorError') {
                console.log('⏰ Usuário não respondeu ao modal a tempo');
            } else {
                console.error('❌ Erro no modal:', error);
            }
        }

    } catch (error) {
        console.error('❌ Erro no handleSolicitacaoButton:', error);
        if (!interaction.replied && !interaction.deferred) {
            await interaction.reply({ 
                content: '❌ Erro ao abrir formulário. Tente novamente.', 
                flags: MessageFlags.Ephemeral 
            }).catch(() => {});
        }
    }
};

// Função para processar o modal
async function processarModalSolicitacao(interaction, guildConfig) {
    await interaction.deferReply({ flags: MessageFlags.Ephemeral });

    const nick = interaction.fields.getTextInputValue('nick');
    const plataforma = interaction.fields.getTextInputValue('plataforma');
    const link = interaction.fields.getTextInputValue('link');
    const requisitos = interaction.fields.getTextInputValue('requisitos');

    const solicitacaoId = `${Date.now()}-${interaction.user.id}`;

    const logEmbed = new EmbedBuilder()
        .setColor('#FFA500')
        .setTitle('🎥 Nova Solicitação - Rank Creator')
        .setDescription(`**Solicitante:** ${interaction.user} (${interaction.user.tag})\n**ID:** ${interaction.user.id}`)
        .addFields(
            { name: '📝 Nickname', value: nick, inline: true },
            { name: '📱 Plataforma', value: plataforma, inline: true },
            { name: '🔗 Link', value: `[Clique aqui](${link})`, inline: true },
            { name: '📋 Requisitos', value: requisitos.substring(0, 1024) },
            { name: '⏰ Duração', value: '15 dias (se aprovado)', inline: true }
        )
        .setTimestamp()
        .setFooter({ text: `Solicitação #${solicitacaoId}` });

    const row = new ActionRowBuilder()
        .addComponents(
            new ButtonBuilder()
                .setCustomId(`aceitar_${solicitacaoId}`)
                .setLabel('✅ Aceitar')
                .setStyle(ButtonStyle.Success),
            new ButtonBuilder()
                .setCustomId(`recusar_${solicitacaoId}`)
                .setLabel('❌ Recusar')
                .setStyle(ButtonStyle.Danger)
        );

    const logChannel = await interaction.guild.channels.fetch(guildConfig.logChannelId);
    const logMessage = await logChannel.send({
        embeds: [logEmbed],
        components: [row]
    });

    // Salvar solicitação
    const solicitacoes = readJSON(solicitacoesPath, {});
    solicitacoes[solicitacaoId] = {
        userId: interaction.user.id,
        nick,
        plataforma,
        link,
        requisitos,
        messageId: logMessage.id,
        channelId: guildConfig.logChannelId,
        guildId: interaction.guild.id,
        status: 'pendente',
        createdAt: Date.now()
    };
    writeJSON(solicitacoesPath, solicitacoes);

    await interaction.editReply({ 
        content: '✅ Sua solicitação foi enviada para análise! Você receberá uma resposta em breve.' 
    });
}

// Handler para botões de aceitar/recusar
module.exports.handleAcaoButton = async function(interaction, acao, idSolicitacao) {
    try {
        console.log(`🔘 handleAcaoButton chamado: ${acao} - ${idSolicitacao}`);
        
        const solicitacoes = readJSON(solicitacoesPath, {});
        const solicitacao = solicitacoes[idSolicitacao];
        
        if (!solicitacao || solicitacao.status !== 'pendente') {
            return interaction.reply({ 
                content: '❌ Esta solicitação não existe ou já foi processada.', 
                flags: MessageFlags.Ephemeral 
            });
        }

        await interaction.deferReply({ flags: MessageFlags.Ephemeral });

        const usuario = await interaction.guild.members.fetch(solicitacao.userId);
        const configData = readJSON(configPath, {});
        const guildId = interaction.guild.id;
        const config = configData[guildId] || {
            creatorRoleId: process.env.CREATOR_ROLE_ID || '1508520449443758311',
            logChannelId: process.env.CREATOR_LOG_CHANNEL_ID || '1500407880774582282'
        };

        if (acao === 'aceitar') {
            console.log('✅ Processando aceitação...');
            
            const creatorRole = interaction.guild.roles.cache.get(config.creatorRoleId);
            if (!creatorRole) {
                return interaction.editReply({ 
                    content: '❌ Cargo de Creator não encontrado no servidor!' 
                });
            }

            // Adicionar cargo
            await usuario.roles.add(creatorRole);
            console.log('✅ Cargo adicionado');
            
            // Salvar cargo com data de expiração
            const cargos = readJSON(cargosAtivosPath, {});
            const expiresAt = Date.now() + (15 * 24 * 60 * 60 * 1000); // 15 dias em ms
            const cargoKey = `${usuario.id}-${interaction.guild.id}-${creatorRole.id}`;
            
            cargos[cargoKey] = {
                user_id: usuario.id,
                guild_id: interaction.guild.id,
                role_id: creatorRole.id,
                assigned_at: Date.now(),
                expires_at: expiresAt
            };
            writeJSON(cargosAtivosPath, cargos);
            console.log('✅ Cargo salvo no banco de dados');

            const dataExpiracao = new Date(expiresAt);
            const dataFormatada = dataExpiracao.toLocaleDateString('pt-BR');

            // Mensagem de aceite no privado
            const aceiteEmbed = new EmbedBuilder()
                .setColor('#00FF00')
                .setTitle('🎉 Parabéns!')
                .setDescription(
                    'Você foi aceito para o cargo de **Creator** em nosso servidor **Sparta**.\n\n' +
                    'Essa posição exige responsabilidade, comprometimento e constância na criação de conteúdo, ' +
                    'representando não apenas sua marca pessoal, mas também os valores da nossa comunidade. ' +
                    'Sua aprovação demonstra que seu perfil atende aos critérios e ao padrão que buscamos em nossos criadores.\n\n' +
                    `**⏰ CARGO TEMPORÁRIO:** Seu cargo expira em **${dataFormatada}** (15 dias).\n` +
                    'Após esse período, você precisará fazer uma nova solicitação para continuar.\n\n' +
                    'Desejamos sucesso nessa nova etapa e contamos com sua dedicação! 🚀'
                );

            await usuario.send({ embeds: [aceiteEmbed] }).catch(() => {
                console.log('⚠️ Não foi possível enviar DM para o usuário');
            });

            // Atualizar embed da mensagem original
            const updatedEmbed = EmbedBuilder.from(interaction.message.embeds[0])
                .setColor('#00FF00')
                .setTitle('✅ Solicitação Aceita')
                .spliceFields(-1, 1) // Remove o último campo (Duração)
                .addFields({ name: '⏰ Expira em', value: `15 dias (${dataFormatada})`, inline: true })
                .setFooter({ text: `Solicitação #${idSolicitacao} • Aceita por ${interaction.user.tag}` });

            await interaction.message.edit({ 
                embeds: [updatedEmbed], 
                components: [] 
            });
            console.log('✅ Mensagem original atualizada');

            // Atualizar status da solicitação
            solicitacao.status = 'aceito';
            solicitacao.processedAt = Date.now();
            solicitacao.processedBy = interaction.user.id;
            writeJSON(solicitacoesPath, solicitacoes);

            await interaction.editReply({ content: '✅ Solicitação aceita! O usuário foi notificado.' });

        } else if (acao === 'recusar') {
            console.log('❌ Processando recusa...');

            // Mensagem de recusa no privado
            const recusaEmbed = new EmbedBuilder()
                .setColor('#FF0000')
                .setTitle('❌ Informativo')
                .setDescription(
                    'Agradecemos pelo seu interesse em integrar o time de **Creator** do servidor **Sparta**.\n\n' +
                    'Após a análise do seu perfil, informamos que, neste momento, você não foi aprovado para o cargo de criação de conteúdo. ' +
                    'Nosso processo considera critérios como alinhamento com a proposta do servidor, constância, qualidade do conteúdo e engajamento.\n\n' +
                    'Isso não significa que você não tenha potencial, recomendamos que continue evoluindo seu trabalho e, futuramente, poderá realizar uma nova candidatura.\n\n' +
                    'Agradecemos pelo interesse e desejamos sucesso na sua jornada como criador de conteúdo.'
                );

            await usuario.send({ embeds: [recusaEmbed] }).catch(() => {
                console.log('⚠️ Não foi possível enviar DM para o usuário');
            });

            // Atualizar embed da mensagem original
            const updatedEmbed = EmbedBuilder.from(interaction.message.embeds[0])
                .setColor('#FF0000')
                .setTitle('❌ Solicitação Recusada')
                .setFooter({ text: `Solicitação #${idSolicitacao} • Recusada por ${interaction.user.tag}` });

            await interaction.message.edit({ 
                embeds: [updatedEmbed], 
                components: [] 
            });
            console.log('✅ Mensagem original atualizada');

            // Atualizar status da solicitação
            solicitacao.status = 'recusado';
            solicitacao.processedAt = Date.now();
            solicitacao.processedBy = interaction.user.id;
            writeJSON(solicitacoesPath, solicitacoes);

            await interaction.editReply({ content: '✅ Solicitação recusada! O usuário foi notificado.' });
        }

    } catch (error) {
        console.error('❌ Erro detalhado no handleAcaoButton:', error);
        
        if (interaction.deferred) {
            await interaction.editReply({ 
                content: `❌ Erro ao processar: ${error.message}` 
            }).catch(() => {});
        } else if (!interaction.replied) {
            await interaction.reply({ 
                content: `❌ Erro ao processar: ${error.message}`, 
                flags: MessageFlags.Ephemeral 
            }).catch(() => {});
        }
    }
};

// Função para iniciar o sistema de verificação de expiração
module.exports.iniciarVerificacaoExpiracao = function(client) {
    setInterval(() => verificarCargosExpirados(client), 60 * 60 * 1000);
    setTimeout(() => verificarCargosExpirados(client), 5000);
    console.log('⏰ Sistema de verificação de cargos expirados iniciado!');
};
