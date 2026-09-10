const { 
    SlashCommandBuilder, 
    PermissionFlagsBits,
    ChannelType,
    ContainerBuilder, 
    TextDisplayBuilder, 
    ThumbnailBuilder,
    SectionBuilder,
    SeparatorBuilder, 
    SeparatorSpacingSize,
    StringSelectMenuBuilder,
    StringSelectMenuOptionBuilder,
    ActionRowBuilder,
    ModalBuilder,
    TextInputBuilder,
    TextInputStyle,
    MessageFlags
} = require('discord.js');

module.exports = {
    help: {
        name: 'sendappealpanel',
        description: 'Sparta » Enviar painel de revisão de punições'
    },
    data: new SlashCommandBuilder()
        .setName('sendappealpanel')
        .setDescription('Sparta » Enviar painel de revisão de punições')
        .addChannelOption(option => 
            option.setName('canal')
                .setDescription('Canal para enviar o painel')
                .setRequired(true)
        )
        .setDefaultMemberPermissions(PermissionFlagsBits.Administrator),

    async execute(interaction) {
        if (!interaction.member.permissions.has(PermissionFlagsBits.Administrator)) {
            return interaction.reply({ 
                content: '❌ Apenas administradores podem usar este comando!', 
                flags: MessageFlags.Ephemeral 
            });
        }

        const canal = interaction.options.getChannel('canal');

        if (canal.type === ChannelType.GuildForum) {
            const modal = new ModalBuilder()
                .setCustomId(`appeal_forum_modal_${canal.id}`)
                .setTitle('Criar Tópico de Appeal')
                .addComponents(
                    new ActionRowBuilder()
                        .addComponents(
                            new TextInputBuilder()
                                .setCustomId('appeal_topic_title')
                                .setLabel('Título do Tópico')
                                .setPlaceholder('Digite o título do tópico')
                                .setStyle(TextInputStyle.Short)
                                .setRequired(true)
                                .setMinLength(3)
                                .setMaxLength(100)
                        )
                );

            await interaction.showModal(modal);
            return;
        }

        try {
            const container = new ContainerBuilder()
                .addSectionComponents(
                    new SectionBuilder()
                        .setThumbnailAccessory(
                            new ThumbnailBuilder()
                                .setURL("https://cdn.discordapp.com/attachments/1492296145165353150/1494094525889777845/logo.png?ex=69e4a735&is=69e355b5&hm=d68fea7ae8ef89826ef0e37187d77ace2d4c63ca5b6883e19739a63db6777d99&")
                        )
                        .addTextDisplayComponents(
                            new TextDisplayBuilder().setContent("**<:Corante_laranjaH:1494067487027761295> Central de Revisão**"),
                        ),
                )
                .addSeparatorComponents(
                    new SeparatorBuilder()
                        .setSpacing(SeparatorSpacingSize.Small)
                        .setDivider(true)
                )
                .addTextDisplayComponents(
                    new TextDisplayBuilder().setContent(
                        "Foi punido injustamente? Selecione o tipo de punição abaixo para solicitar uma revisão.\n\n" +
                        "❙ Descreva detalhadamente o ocorrido e aguarde a análise da equipe."
                    ),
                )
                .addSeparatorComponents(
                    new SeparatorBuilder()
                        .setSpacing(SeparatorSpacingSize.Small)
                        .setDivider(true)
                )
                .addTextDisplayComponents(
                    new TextDisplayBuilder().setContent("-# *spartamc.com.br*"),
                )
                .addActionRowComponents(
                    new ActionRowBuilder()
                        .addComponents(
                            new StringSelectMenuBuilder()
                                .setCustomId("selecionar_appeal")
                                .setPlaceholder("Selecione o tipo de punição")
                                .addOptions(
                                    new StringSelectMenuOptionBuilder()
                                        .setLabel("🔇 Mute")
                                        .setValue("appeal_mute")
                                        .setDescription("Sparta » Revisão de mute"),
                                    new StringSelectMenuOptionBuilder()
                                        .setLabel("🚫 Ban")
                                        .setValue("appeal_ban")
                                        .setDescription("Sparta » Revisão de ban"),
                                    new StringSelectMenuOptionBuilder()
                                        .setLabel("⚠️ Warn")
                                        .setValue("appeal_warn")
                                        .setDescription("Sparta » Revisão de warn"),
                                    new StringSelectMenuOptionBuilder()
                                        .setLabel("⛔ Kick")
                                        .setValue("appeal_kick")
                                        .setDescription("Sparta » Revisão de kick")
                                ),
                        ),
                );

            await canal.send({
                components: [container],
                flags: [MessageFlags.IsComponentsV2]
            });

            await interaction.reply({ 
                content: `✅ Painel de appeal enviado para ${canal}!`, 
                flags: MessageFlags.Ephemeral 
            });

        } catch (error) {
            console.error('❌ Erro ao enviar painel de appeal:', error);
            await interaction.reply({ 
                content: `❌ Erro: ${error.message}`, 
                flags: MessageFlags.Ephemeral 
            });
        }
    }
};