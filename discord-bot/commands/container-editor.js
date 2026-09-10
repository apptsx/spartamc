const { 
    SlashCommandBuilder, 
    PermissionFlagsBits,
    ContainerBuilder, 
    TextDisplayBuilder,
    SeparatorBuilder, 
    SeparatorSpacingSize,
    MediaGalleryBuilder, 
    MediaGalleryItemBuilder,
    ThumbnailBuilder,
    SectionBuilder,
    ButtonBuilder,
    ButtonStyle,
    ActionRowBuilder,
    ModalBuilder,
    TextInputBuilder,
    TextInputStyle,
    MessageFlags,
    StringSelectMenuBuilder,
    StringSelectMenuOptionBuilder,
    ChannelSelectMenuBuilder,
    ChannelType
} = require('discord.js');

const editores = new Map();

module.exports = {
    help: {
        name: 'container-editor',
        description: 'Sparta » Editor de Container V2'
    },
    data: new SlashCommandBuilder()
        .setName('container-editor')
        .setDescription('Sparta » Editor de Container V2')
        .setDefaultMemberPermissions(PermissionFlagsBits.Administrator),

    async execute(interaction) {
        if (!interaction.member.permissions.has(PermissionFlagsBits.Administrator)) {
            return interaction.reply({ 
                content: '❌ Apenas administradores podem usar!', 
                flags: MessageFlags.Ephemeral 
            });
        }

        const sessaoId = `${interaction.user.id}-${Date.now()}`;
        
        editores.set(sessaoId, {
            componentes: [],
            cor: null,
            titulo: 'Container V2',
            canalId: null,
            sessaoId
        });
        
        await enviarPainelEdicao(interaction, sessaoId);
    }
};

function buildPreviewContainer(estado, incluirTitulo = false) {
    const container = new ContainerBuilder();
    
    if (estado.cor) {
        container.setAccentColor(parseInt(estado.cor.replace('#', ''), 16));
    }
    
    if (estado.componentes.length === 0) {
        container.addTextDisplayComponents(
            new TextDisplayBuilder().setContent(`*Preview vazio - adicione componentes*`)
        );
    } else {
        for (const comp of estado.componentes) {
            if (comp.tipo === 'texto') {
                container.addTextDisplayComponents(
                    new TextDisplayBuilder().setContent(comp.conteudo)
                );
            } else if (comp.tipo === 'separador') {
                container.addSeparatorComponents(
                    new SeparatorBuilder().setSpacing(SeparatorSpacingSize.Small).setDivider(true)
                );
            } else if (comp.tipo === 'banner') {
                container.addMediaGalleryComponents(
                    new MediaGalleryBuilder().addItems(
                        new MediaGalleryItemBuilder().setURL(comp.url)
                    )
                );
            } else if (comp.tipo === 'thumbnail') {
                container.addSectionComponents(
                    new SectionBuilder().setThumbnailAccessory(
                        new ThumbnailBuilder().setURL(comp.url)
                    )
                );
            } else if (comp.tipo === 'botao') {
                const row = new ActionRowBuilder().addComponents(
                    new ButtonBuilder()
                        .setCustomId(comp.customId || `btn_${Date.now()}`)
                        .setLabel(comp.label)
                        .setStyle(ButtonStyle[comp.estilo] || ButtonStyle.Secondary)
                );
                container.addActionRowComponents(row);
            } else if (comp.tipo === 'link') {
                const row = new ActionRowBuilder().addComponents(
                    new ButtonBuilder()
                        .setURL(comp.url)
                        .setLabel(comp.label)
                        .setStyle(ButtonStyle.Link)
                );
                container.addActionRowComponents(row);
            } else if (comp.tipo === 'select') {
                const options = comp.opcoes.map((o, i) => 
                    new StringSelectMenuOptionBuilder()
                        .setLabel(o.label)
                        .setValue(o.value || `opt_${i}`)
                );
                const row = new ActionRowBuilder().addComponents(
                    new StringSelectMenuBuilder()
                        .setCustomId(comp.customId || `select_${Date.now()}`)
                        .addOptions(options)
                );
                container.addActionRowComponents(row);
            }
        }
    }
    
    return container;
}

function buildEditorContainer(estado, sessaoId) {
    const container = new ContainerBuilder();
    
    container.setAccentColor(0x5865F2);
    
    container.addTextDisplayComponents(
        new TextDisplayBuilder().setContent(`🎨 **EDITOR DE CONTAINER V2**`)
    );
    
    container.addSeparatorComponents(
        new SeparatorBuilder().setSpacing(SeparatorSpacingSize.Small).setDivider(true)
    );
    
    container.addTextDisplayComponents(
        new TextDisplayBuilder().setContent(`**Título:** ${estado.titulo}\n**Cor:** ${estado.cor || 'Nenhuma'}\n**Componentes:** ${estado.componentes.length}`)
    );
    
    container.addSeparatorComponents(
        new SeparatorBuilder().setSpacing(SeparatorSpacingSize.Small).setDivider(true)
    );
    
    container.addActionRowComponents(
        new ActionRowBuilder().addComponents(
            new StringSelectMenuBuilder()
                .setCustomId(`add_${sessaoId}`)
                .setPlaceholder('➕ Adicionar componente')
                .addOptions(
                    new StringSelectMenuOptionBuilder().setLabel('Texto').setValue('texto').setEmoji('📝'),
                    new StringSelectMenuOptionBuilder().setLabel('Separador').setValue('separador').setEmoji('➖'),
                    new StringSelectMenuOptionBuilder().setLabel('Banner/Imagem').setValue('banner').setEmoji('🖼️'),
                    new StringSelectMenuOptionBuilder().setLabel('Thumbnail').setValue('thumbnail').setEmoji('🪟'),
                    new StringSelectMenuOptionBuilder().setLabel('Botão').setValue('botao').setEmoji('🔘'),
                    new StringSelectMenuOptionBuilder().setLabel('Link').setValue('link').setEmoji('🔗'),
                    new StringSelectMenuOptionBuilder().setLabel('Select Menu').setValue('select').setEmoji('📋')
                )
        )
    );
    
    if (estado.componentes.length > 0) {
        const editOptions = estado.componentes.map((comp, index) => 
            new StringSelectMenuOptionBuilder()
                .setLabel(`${index + 1}. ${comp.tipo}`)
                .setValue(`edit_${index}`)
                .setEmoji('✏️')
        );
        
        container.addActionRowComponents(
            new ActionRowBuilder().addComponents(
                new StringSelectMenuBuilder()
                    .setCustomId(`edit_${sessaoId}`)
                    .setPlaceholder('✏️ Editar')
                    .addOptions(editOptions)
            )
        );
        
        const removeOptions = estado.componentes.map((comp, index) => 
            new StringSelectMenuOptionBuilder()
                .setLabel(`${index + 1}. ${comp.tipo}`)
                .setValue(`${index}`)
                .setEmoji('🗑️')
        );
        
        container.addActionRowComponents(
            new ActionRowBuilder().addComponents(
                new StringSelectMenuBuilder()
                    .setCustomId(`remove_${sessaoId}`)
                    .setPlaceholder('🗑️ Remover')
                    .addOptions(removeOptions)
            )
        );
    }
    
    container.addSeparatorComponents(
        new SeparatorBuilder().setSpacing(SeparatorSpacingSize.Large).setDivider(true)
    );
    
    container.addActionRowComponents(
        new ActionRowBuilder().addComponents(
            new ChannelSelectMenuBuilder()
                .setCustomId(`canal_${sessaoId}`)
                .setPlaceholder('📨 Canal/Tópico Forum')
                .setChannelTypes(ChannelType.GuildText, ChannelType.GuildForum)
        )
    );
    
    container.addActionRowComponents(
        new ActionRowBuilder().addComponents(
            new ButtonBuilder().setCustomId(`cor_${sessaoId}`).setLabel('🎨 Cor').setStyle(ButtonStyle.Secondary),
            new ButtonBuilder().setCustomId(`titulo_${sessaoId}`).setLabel('📝 Título').setStyle(ButtonStyle.Secondary),
            new ButtonBuilder().setCustomId(`enviar_${sessaoId}`).setLabel('📤 ENVIAR').setStyle(ButtonStyle.Success),
            new ButtonBuilder().setCustomId(`limpar_${sessaoId}`).setLabel('🗑️ Limpar').setStyle(ButtonStyle.Danger)
        )
    );
    
    return container;
}

async function enviarPainelEdicao(interaction, sessaoId) {
    const estado = editores.get(sessaoId);
    const editor = buildEditorContainer(estado, sessaoId);
    
    const previewContainer = new ContainerBuilder();
    if (estado.cor) {
        previewContainer.setAccentColor(parseInt(estado.cor.replace('#', ''), 16));
    }
    if (estado.componentes.length === 0) {
        previewContainer.addTextDisplayComponents(
            new TextDisplayBuilder().setContent(`*Preview vazio - adicione componentes*`)
        );
    }
    
    await interaction.reply({
        components: [editor, previewContainer],
        flags: MessageFlags.Ephemeral | MessageFlags.IsComponentsV2
    });
    
    criarColetor(interaction, sessaoId);
}

function criarColetor(interaction, sessaoId) {
    const collector = interaction.channel.createMessageComponentCollector({
        filter: i => i.customId.endsWith(sessaoId) && i.user.id === interaction.user.id && !i.isModalSubmit(),
        time: 300000
    });

    collector.on('collect', async i => {
        const [acao, ...rest] = i.customId.split('_');
        
        try {
            if (acao === 'add') {
                await abrirModalAdicionar(i, sessaoId, i.values[0]);
            } else if (acao === 'edit') {
                const index = parseInt(i.values[0].split('_')[1]);
                await abrirModalEditar(i, sessaoId, index);
            } else if (acao === 'remove') {
                await removerComponente(i, sessaoId, parseInt(i.values[0]));
            } else if (acao === 'canal') {
                await selecionarCanal(i, sessaoId, i.values[0]);
            } else if (acao === 'cor') {
                await abrirModalCor(i, sessaoId);
            } else if (acao === 'titulo') {
                await abrirModalTitulo(i, sessaoId);
            } else if (acao === 'enviar') {
                await enviarContainer(i, sessaoId, null);
            } else if (acao === 'forum') {
                await enviarParaForum(i, sessaoId);
            } else if (acao === 'limpar') {
                await limparContainer(i, sessaoId);
            }
        } catch (error) {
            console.error('Erro no coletor:', error);
            if (!i.replied) {
                await i.followUp({ 
                    content: `❌ Erro: ${error.message}`, 
                    flags: MessageFlags.Ephemeral 
                });
            }
        }
    });

    collector.on('end', () => {
        editores.delete(sessaoId);
    });
}

async function abrirModalAdicionar(interaction, sessaoId, tipo) {
    const modal = new ModalBuilder()
        .setCustomId(`modal_add_${tipo}_${sessaoId}`)
        .setTitle(`Adicionar ${tipo}`);
    
    if (tipo === 'texto') {
        modal.addComponents(
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('conteudo')
                    .setLabel('Conteúdo:')
                    .setStyle(TextInputStyle.Paragraph)
                    .setRequired(true)
                    .setPlaceholder('Digite o texto...')
            )
        );
    } else if (tipo === 'separador') {
        modal.addComponents(
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('tamanho')
                    .setLabel('Tamanho (Pequeno, Grande):')
                    .setStyle(TextInputStyle.Short)
                    .setValue('Pequeno')
            )
        );
    } else if (tipo === 'botao') {
        modal.addComponents(
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('label')
                    .setLabel('Label do botão:')
                    .setStyle(TextInputStyle.Short)
                    .setRequired(true)
                    .setPlaceholder('Clique aqui')
            ),
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('estilo')
                    .setLabel('Estilo (Primary, Secondary, Success, Danger):')
                    .setStyle(TextInputStyle.Short)
                    .setValue('Secondary')
            ),
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('customId')
                    .setLabel('Custom ID (opcional):')
                    .setStyle(TextInputStyle.Short)
                    .setRequired(false)
                    .setPlaceholder('my_button')
            )
        );
    } else if (tipo === 'link') {
        modal.addComponents(
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('label')
                    .setLabel('Label do link:')
                    .setStyle(TextInputStyle.Short)
                    .setRequired(true)
                    .setPlaceholder('Clique aqui')
            ),
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('url')
                    .setLabel('URL:')
                    .setStyle(TextInputStyle.Short)
                    .setRequired(true)
                    .setPlaceholder('https://...')
            )
        );
    } else if (tipo === 'select') {
        modal.addComponents(
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('customId')
                    .setLabel('Custom ID:')
                    .setStyle(TextInputStyle.Short)
                    .setRequired(true)
                    .setPlaceholder('my_select')
            ),
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('opcoes')
                    .setLabel('Opções (cada linha = uma opção):')
                    .setStyle(TextInputStyle.Paragraph)
                    .setRequired(true)
                    .setPlaceholder('Opção 1\nOpção 2\nOpção 3')
            )
        );
    } else {
        modal.addComponents(
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('url')
                    .setLabel('URL da imagem:')
                    .setStyle(TextInputStyle.Short)
                    .setRequired(true)
                    .setPlaceholder('https://...')
            )
        );
    }
    
    await interaction.showModal(modal);
    
    const modalInteraction = await interaction.awaitModalSubmit({ time: 60000 });
    const estado = editores.get(sessaoId);
    const novoComponente = { tipo };
    
    if (tipo === 'texto') {
        novoComponente.conteudo = modalInteraction.fields.getTextInputValue('conteudo');
    } else if (tipo === 'separador') {
        novoComponente.tamanho = modalInteraction.fields.getTextInputValue('tamanho') || 'Pequeno';
    } else if (tipo === 'botao') {
        novoComponente.label = modalInteraction.fields.getTextInputValue('label');
        novoComponente.estilo = modalInteraction.fields.getTextInputValue('estilo') || 'Secondary';
        novoComponente.customId = modalInteraction.fields.getTextInputValue('customId') || `btn_${Date.now()}`;
    } else if (tipo === 'link') {
        novoComponente.label = modalInteraction.fields.getTextInputValue('label');
        novoComponente.url = modalInteraction.fields.getTextInputValue('url');
        if (novoComponente.url && !/^https?:\/\/.+/.test(novoComponente.url)) {
            await modalInteraction.reply({ content: '❌ URL inválida. Use http:// ou https://', flags: MessageFlags.Ephemeral });
            return;
        }
    } else if (tipo === 'select') {
        novoComponente.customId = modalInteraction.fields.getTextInputValue('customId');
        const opcoesTexto = modalInteraction.fields.getTextInputValue('opcoes');
        novoComponente.opcoes = opcoesTexto.split('\n').map((text, i) => ({
            label: text.trim(),
            value: `opt_${i}`
        }));
    } else {
        novoComponente.url = modalInteraction.fields.getTextInputValue('url');
        if (novoComponente.url && !/^https?:\/\/.+/.test(novoComponente.url)) {
            await modalInteraction.reply({ content: '❌ URL inválida. Use http:// ou https://', flags: MessageFlags.Ephemeral });
            return;
        }
    }
    
    estado.componentes.push(novoComponente);
    
    await modalInteraction.deferUpdate();
    await atualizarPainel(modalInteraction, sessaoId);
}

async function abrirModalEditar(interaction, sessaoId, index) {
    const estado = editores.get(sessaoId);
    const comp = estado.componentes[index];
    
    if (!comp) return;
    
    const modal = new ModalBuilder()
        .setCustomId(`modal_edit_${index}_${sessaoId}`)
        .setTitle(`Editar ${comp.tipo}`);
    
    if (comp.tipo === 'texto') {
        modal.addComponents(
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('conteudo')
                    .setLabel('Conteúdo:')
                    .setStyle(TextInputStyle.Paragraph)
                    .setValue(comp.conteudo || '')
            )
        );
    } else if (comp.tipo === 'separador') {
        modal.addComponents(
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('tamanho')
                    .setLabel('Tamanho:')
                    .setStyle(TextInputStyle.Short)
                    .setValue(comp.tamanho || 'Pequeno')
            )
        );
    } else if (comp.tipo === 'botao') {
        modal.addComponents(
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('label')
                    .setLabel('Label:')
                    .setStyle(TextInputStyle.Short)
                    .setValue(comp.label)
            ),
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('estilo')
                    .setLabel('Estilo:')
                    .setStyle(TextInputStyle.Short)
                    .setValue(comp.estilo)
            )
        );
    } else if (comp.tipo === 'link') {
        modal.addComponents(
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('label')
                    .setLabel('Label:')
                    .setStyle(TextInputStyle.Short)
                    .setValue(comp.label)
            ),
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('url')
                    .setLabel('URL:')
                    .setStyle(TextInputStyle.Short)
                    .setValue(comp.url)
            )
        );
    } else if (comp.tipo === 'select') {
        modal.addComponents(
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('customId')
                    .setLabel('Custom ID:')
                    .setStyle(TextInputStyle.Short)
                    .setValue(comp.customId)
            ),
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('opcoes')
                    .setLabel('Opções (cada linha = uma):')
                    .setStyle(TextInputStyle.Paragraph)
                    .setValue(comp.opcoes.map(o => o.label).join('\n'))
            )
        );
    } else {
        modal.addComponents(
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('url')
                    .setLabel('URL:')
                    .setStyle(TextInputStyle.Short)
                    .setValue(comp.url || '')
            )
        );
    }
    
    await interaction.showModal(modal);
    
    const modalInteraction = await interaction.awaitModalSubmit({ time: 60000 });
    
    if (comp.tipo === 'texto') {
        comp.conteudo = modalInteraction.fields.getTextInputValue('conteudo');
    } else if (comp.tipo === 'separador') {
        comp.tamanho = modalInteraction.fields.getTextInputValue('tamanho') || 'Pequeno';
    } else if (comp.tipo === 'botao') {
        comp.label = modalInteraction.fields.getTextInputValue('label');
        comp.estilo = modalInteraction.fields.getTextInputValue('estilo');
    } else if (comp.tipo === 'link') {
        comp.label = modalInteraction.fields.getTextInputValue('label');
        comp.url = modalInteraction.fields.getTextInputValue('url');
        if (comp.url && !/^https?:\/\/.+/.test(comp.url)) {
            await modalInteraction.reply({ content: '❌ URL inválida. Use http:// ou https://', flags: MessageFlags.Ephemeral });
            return;
        }
    } else if (comp.tipo === 'select') {
        comp.customId = modalInteraction.fields.getTextInputValue('customId');
        const opcoesTexto = modalInteraction.fields.getTextInputValue('opcoes');
        comp.opcoes = opcoesTexto.split('\n').map((text, i) => ({
            label: text.trim(),
            value: `opt_${i}`
        }));
    } else {
        comp.url = modalInteraction.fields.getTextInputValue('url');
        if (comp.url && !/^https?:\/\/.+/.test(comp.url)) {
            await modalInteraction.reply({ content: '❌ URL inválida. Use http:// ou https://', flags: MessageFlags.Ephemeral });
            return;
        }
    }
    
    await modalInteraction.deferUpdate();
    await atualizarPainel(modalInteraction, sessaoId);
}

async function removerComponente(interaction, sessaoId, index) {
    const estado = editores.get(sessaoId);
    if (index >= 0 && index < estado.componentes.length) {
        estado.componentes.splice(index, 1);
    }
    await atualizarPainel(interaction, sessaoId);
}

async function abrirModalCor(interaction, sessaoId) {
    const estado = editores.get(sessaoId);
    const modal = new ModalBuilder()
        .setCustomId(`modal_cor_${sessaoId}`)
        .setTitle('Escolher cor');
    
    modal.addComponents(
        new ActionRowBuilder().addComponents(
            new TextInputBuilder()
                .setCustomId('cor')
                .setLabel('Cor (#RRGGBB) ou vazio:')
                .setStyle(TextInputStyle.Short)
                .setRequired(false)
                .setPlaceholder('#FF5733')
        )
    );
    
    await interaction.showModal(modal);
    
    const modalInteraction = await interaction.awaitModalSubmit({ time: 60000 });
    const cor = modalInteraction.fields.getTextInputValue('cor');
    
    if (cor && /^#[0-9A-Fa-f]{6}$/.test(cor)) {
        estado.cor = cor;
    } else {
        estado.cor = null;
    }
    
    await modalInteraction.deferUpdate();
    await atualizarPainel(modalInteraction, sessaoId);
}

async function abrirModalTitulo(interaction, sessaoId) {
    const estado = editores.get(sessaoId);
    const modal = new ModalBuilder()
        .setCustomId(`modal_titulo_${sessaoId}`)
        .setTitle('Definir título');
    
    modal.addComponents(
        new ActionRowBuilder().addComponents(
            new TextInputBuilder()
                .setCustomId('titulo')
                .setLabel('Título:')
                .setStyle(TextInputStyle.Short)
                .setValue(estado.titulo)
                .setRequired(true)
        )
    );
    
    await interaction.showModal(modal);
    
    const modalInteraction = await interaction.awaitModalSubmit({ time: 60000 });
    estado.titulo = modalInteraction.fields.getTextInputValue('titulo');
    
    await modalInteraction.deferUpdate();
    await atualizarPainel(modalInteraction, sessaoId);
}

async function selecionarCanal(interaction, sessaoId, canalId) {
    const estado = editores.get(sessaoId);
    
    if (estado.componentes.length === 0) {
        await interaction.reply({ 
            content: '❌ Adicione componentes antes de enviar!', 
            flags: MessageFlags.Ephemeral 
        });
        return;
    }
    
    estado.canalId = canalId;
    editores.set(sessaoId, estado);
    
    const canal = await interaction.guild.channels.fetch(canalId);
    
    if (canal.type === ChannelType.GuildForum) {
        estado.forumCanalId = canalId;
        editores.set(sessaoId, estado);
        
        const modal = new ModalBuilder()
            .setCustomId(`forum_${sessaoId}`)
            .setTitle('Criar Tópico no Forum');
        
        modal.addComponents(
            new ActionRowBuilder().addComponents(
                new TextInputBuilder()
                    .setCustomId('titulo')
                    .setLabel('Título do tópico:')
                    .setStyle(TextInputStyle.Short)
                    .setRequired(true)
                    .setPlaceholder('Título do tópico...')
            )
        );
        
        await interaction.showModal(modal);
        
        const modalInteraction = await interaction.awaitModalSubmit({ time: 60000 });
        
        const titulo = modalInteraction.fields.getTextInputValue('titulo');
        
    const container = buildPreviewContainer(estado);
        
        await canal.threads.create({
            name: titulo,
            message: {
                components: [container],
                flags: MessageFlags.IsComponentsV2
            }
        });
        
        await modalInteraction.reply({ 
            content: `✅ Tópico criado no forum ${canal}!`, 
            flags: MessageFlags.Ephemeral 
        });
    } else {
        await enviarContainer(interaction, sessaoId, canalId);
    }
}

async function limparContainer(interaction, sessaoId) {
    const estado = editores.get(sessaoId);
    editores.set(sessaoId, {
        componentes: [],
        cor: estado.cor,
        titulo: estado.titulo,
        sessaoId
    });
    await atualizarPainel(interaction, sessaoId);
}

async function enviarContainer(interaction, sessaoId, canalId) {
    const estado = editores.get(sessaoId);
    
    if (estado.componentes.length === 0) {
        await interaction.reply({ 
            content: '❌ Adicione componentes antes de enviar!', 
            flags: MessageFlags.Ephemeral 
        });
        return;
    }
    
    if (!canalId) canalId = estado.canalId;
    if (!canalId) {
        await interaction.reply({ 
            content: '❌ Selecione um canal primeiro!', 
            flags: MessageFlags.Ephemeral 
        });
        return;
    }
    
    const canal = await interaction.guild.channels.fetch(canalId);
    if (!canal?.isTextBased()) {
        await interaction.reply({ 
            content: '❌ Canal inválido!', 
            flags: MessageFlags.Ephemeral 
        });
        return;
    }
    
    const container = buildPreviewContainer(estado, true);
    
    await canal.send({
        components: [container],
        flags: MessageFlags.IsComponentsV2
    });
    
    await interaction.reply({ 
        content: `✅ Container V2 enviado para ${canal}!`, 
        flags: MessageFlags.Ephemeral 
    });
}

async function atualizarPainel(interaction, sessaoId) {
    const estado = editores.get(sessaoId);
    const editor = buildEditorContainer(estado, sessaoId);
    const preview = buildPreviewContainer(estado);
    
    await interaction.editReply({
        components: [editor, preview]
    });
}
