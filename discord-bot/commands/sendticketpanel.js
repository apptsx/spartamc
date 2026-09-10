const { 
    SlashCommandBuilder, 
    PermissionFlagsBits,
    ContainerBuilder, 
    TextDisplayBuilder, 
    ThumbnailBuilder,
    SectionBuilder,
    SeparatorBuilder, 
    SeparatorSpacingSize,
    MediaGalleryBuilder, 
    MediaGalleryItemBuilder,
    StringSelectMenuBuilder,
    StringSelectMenuOptionBuilder,
    ActionRowBuilder,
    MessageFlags
} = require('discord.js');

const SelectMenuOptionBuilder = StringSelectMenuOptionBuilder;

module.exports = {
    help: {
        name: 'sendticketpanel',
        description: 'Sparta » Envia o painel de tickets'
    },
    data: new SlashCommandBuilder()
        .setName('sendticketpanel')
        .setDescription('Sparta » Envia o painel de tickets no canal')
        .setDefaultMemberPermissions(PermissionFlagsBits.Administrator),

    async execute(interaction) {
        if (!interaction.member.permissions.has(PermissionFlagsBits.Administrator)) {
            return interaction.reply({ 
                content: '❌ Apenas administradores podem usar este comando!', 
                flags: MessageFlags.Ephemeral 
            });
        }

        await interaction.deferReply({ flags: MessageFlags.Ephemeral });

        try {
            const container = new ContainerBuilder()
                .addSectionComponents(
                    new SectionBuilder()
                        .setThumbnailAccessory(
                            new ThumbnailBuilder()
                                .setURL("https://cdn.discordapp.com/attachments/1492296145165353150/1494094525889777845/logo.png?ex=69e4a735&is=69e355b5&hm=d68fea7ae8ef89826ef0e37187d77ace2d4c63ca5b6883e19739a63db6777d99&")
                        )
                        .addTextDisplayComponents(
                            new TextDisplayBuilder().setContent("### <:book:1494067518279651338> Central de Atendimento"),
                            new TextDisplayBuilder().setContent("> Bem-vindo(a) a nossa central de atendimento, abra um ticket de atendimento conforme sua necessidade!"),
                            new TextDisplayBuilder().setContent("### <:Corante_vermelhoH:1494067495613501501> Diretrizes de Atendimento:\n• Abra ticket só quando necessário;\n• Forneça o máximo de informações possível;\n• Aguarde pacientemente o atendimento!"),
                        ),
                )
                .addMediaGalleryComponents(
                    new MediaGalleryBuilder()
                        .addItems(
                            new MediaGalleryItemBuilder()
                                .setURL("https://cdn.discordapp.com/attachments/1492296145165353150/1494094236390527056/41_Sem_Titulo_20260227084613.png?ex=69e4a6f0&is=69e35570&hm=9277827cea79816dbc7d16293964ff8463ac488075e8363693d2de0b51c501de&"),
                        ),
                )
                .addActionRowComponents(
                    new ActionRowBuilder()
                        .addComponents(
                            new StringSelectMenuBuilder()
                                .setCustomId("selecionar_ticket_novo")
                                .setPlaceholder("» Selecione uma categoria de atendimento!")
                                .addOptions(
                                    new SelectMenuOptionBuilder()
                                        .setLabel("» Suporte")
                                        .setValue("suporte")
                                        .setDescription("Abra um ticket para suporte geral")
                                        .setEmoji({ id: "1494067514500452535", name: "blink" }),
                                    new SelectMenuOptionBuilder()
                                        .setLabel("» Compras")
                                        .setValue("compras")
                                        .setDescription("Dúvidas sobre compras ou produtos")
                                        .setEmoji({ id: "1494068036620128297" }),
                                    new SelectMenuOptionBuilder()
                                        .setLabel("» Dúvidas")
                                        .setValue("duvidas")
                                        .setDescription("Dúvidas sobre o servidor ou funcionamento")
                                        .setEmoji({ id: "1494740212796887144" }),
                                    new SelectMenuOptionBuilder()
                                        .setLabel("» Revisão de Punição")
                                        .setValue("revisao_punicao")
                                        .setDescription("Solicite a revisão de uma punição")
                                        .setEmoji({ id: "1516068047582920926", name: "block" }),
                                ),
                        ),
                );

            await interaction.channel.send({
                components: [container],
                flags: [MessageFlags.IsComponentsV2]
            });

            await interaction.editReply({ 
                content: '✅ Painel de tickets enviado com sucesso!' 
            });

        } catch (error) {
            console.error('❌ Erro ao enviar painel:', error);
            await interaction.editReply({ 
                content: `❌ Erro: ${error.message}` 
            });
        }
    }
};
