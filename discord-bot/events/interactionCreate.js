const ticketsHandler = require('../handlers/tickets');
const appealsHandler = require('../handlers/appeals');
const callHandler = require('../handlers/callHandler');
const shopHandler = require('../handlers/shopHandler');
const shopAdmin = require('../commands/shopadmin');
const creatorCommand = require('../commands/creator');
const eventoCommand = require('../commands/evento');
const { 
    MessageFlags, 
    ActionRowBuilder,
    ContainerBuilder,
    TextDisplayBuilder,
    ThumbnailBuilder,
    SectionBuilder,
    SeparatorBuilder,
    SeparatorSpacingSize,
    StringSelectMenuBuilder,
    StringSelectMenuOptionBuilder
} = require('discord.js');



module.exports = {
    name: 'interactionCreate',
    async execute(interaction, client) {
        try {
            if (interaction.isChatInputCommand()) {
                const command = client.commands.get(interaction.commandName);
                
                if (!command) return;
                
                try {
                    await command.execute(interaction);
                } catch (error) {
                    console.error(`❌ Erro no comando ${interaction.commandName}:`, error);
                    if (!interaction.replied && !interaction.deferred) {
                        await interaction.reply({ 
                            content: `❌ Erro: ${error.message}`, 
                            flags: MessageFlags.Ephemeral 
                        }).catch(() => {});
                    }
                }
            }
            
            else if (interaction.isStringSelectMenu() && interaction.customId === 'selecionar_ticket_novo') {
                await ticketsHandler.handleMenu(interaction);
            }
            
            else if (interaction.isStringSelectMenu() && interaction.customId === 'selecionar_appeal') {
                await appealsHandler.handleMenu(interaction);
            }
            
            else if (interaction.isStringSelectMenu() && interaction.customId === 'shop_selecionar_produto') {
                await shopHandler.handleProductSelect(interaction);
            }
            
            else if (interaction.isStringSelectMenu() && interaction.customId.startsWith('shop_variante_')) {
                await shopHandler.handleVariantSelect(interaction);
            }
            
            else if (interaction.isStringSelectMenu() && interaction.customId === 'shop_admin_menu') {
                await shopAdmin.handleAdminMenu(interaction);
            }
            
            else if (interaction.isStringSelectMenu() && interaction.customId === 'shop_admin_produto') {
                await shopAdmin.handleAdminProduto(interaction);
            }
            
            else if (interaction.isStringSelectMenu() && interaction.customId.startsWith('shop_admin_campo_')) {
                await shopAdmin.handleAdminCampo(interaction);
            }
            
            else if (interaction.isStringSelectMenu() && interaction.customId === 'shop_admin_cupom_produto_select') {
                await shopAdmin.handleCupomProdutoSelect(interaction);
            }
            
            else if (interaction.isModalSubmit() && interaction.customId === 'modal_evento_criar') {
                await eventoCommand.modal(interaction);
            }

            else if (interaction.isModalSubmit() && interaction.customId.startsWith('modal_ticket_')) {
                await ticketsHandler.handleModal(interaction, client);
            }
            
            else if (interaction.isModalSubmit() && interaction.customId.startsWith('modal_appeal_')) {
                await appealsHandler.handleModal(interaction, client);
            }
            
            else if (interaction.isModalSubmit() && interaction.customId.startsWith('appeal_forum_modal_')) {
                const canalId = interaction.customId.replace('appeal_forum_modal_', '');
                const canal = await client.channels.fetch(canalId);
                
                if (!canal || canal.type !== 15) {
                    return interaction.reply({ content: 'Canal não encontrado!', flags: MessageFlags.Ephemeral });
                }

                const topicTitle = interaction.fields.getTextInputValue('appeal_topic_title');
                
                await interaction.deferReply({ flags: MessageFlags.Ephemeral });
                
                try {
                    const container = new ContainerBuilder()
                        .addSectionComponents(
                            new SectionBuilder()
                                .setThumbnailAccessory(
                                    new ThumbnailBuilder()
                                        .setURL("https://cdn.discordapp.com/attachments/1480857269989015776/1481043444766871562/68_Sem_Titulo_20260310182521.png?ex=69b1e0b1&is=69b08f31&hm=f70bcd85f10642be00f696d9eca0cfda87cbf850b2d719c0884ba6037d98dec1&")
                                )
                                .addTextDisplayComponents(
                                    new TextDisplayBuilder().setContent("**⚖️ Central de Revisão**"),
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

                    const thread = await canal.threads.create({
                        name: topicTitle,
                        message: {
                            components: [container],
                            flags: [MessageFlags.IsComponentsV2]
                        },
                        reason: `Appeal criado por ${interaction.user.tag}`
                    });

                    await interaction.editReply({ 
                        content: `✅ Tópico "${topicTitle}" criado com sucesso!`, 
                        flags: MessageFlags.Ephemeral 
                    });

                } catch (error) {
                    console.error('Erro ao criar tópico:', error);
                    await interaction.editReply({ 
                        content: `❌ Erro: ${error.message}`, 
                        flags: MessageFlags.Ephemeral 
                    });
                }
            }
            
            else if (interaction.isModalSubmit() && interaction.customId.startsWith('shop_comprar_modal_')) {
                await shopHandler.handleCompraSubmit(interaction);
            }

            else if (interaction.isModalSubmit() && interaction.customId.startsWith('shop_modal_cupom_')) {
                await shopHandler.handleCupomSubmit(interaction);
            }

            else if (interaction.isModalSubmit() && ['shop_modal_criar_produto', 'shop_modal_criar_campo', 'shop_modal_editar_campo'].includes(interaction.customId)) {
                await shopHandler.handleAdminModal(interaction);
            }

            else if (interaction.isModalSubmit() && (interaction.customId.startsWith('shop_modal_estoque_') || interaction.customId.startsWith('shop_modal_mp') || interaction.customId.startsWith('shop_modal_cupom_produto_') || interaction.customId === 'shop_modal_marca')) {
                await shopAdmin.handleAdminModal(interaction);
            }

            else if (interaction.isModalSubmit() && interaction.customId.startsWith('call_rename_')) {
                await callHandler.handleModals(interaction, client);
            }
            
            else if (interaction.isButton()) {
                if (interaction.customId.startsWith('assumir_')) {
                    await ticketsHandler.assumirTicket(interaction, client);
                }
                else if (interaction.customId.startsWith('fechar_')) {
                    await ticketsHandler.fecharTicket(interaction, client);
                }
                else if (interaction.customId.startsWith('aceitar_appeal_')) {
                    await appealsHandler.aceitarAppeal(interaction, client);
                }
                else if (interaction.customId.startsWith('negar_appeal_')) {
                    await appealsHandler.negarAppeal(interaction, client);
                }
                else if (interaction.customId.startsWith('call_')) {
                    await callHandler.handleButtons(interaction, client);
                }
                else if (interaction.customId.startsWith('shop_pagar_')) {
                    await shopHandler.handlePagar(interaction);
                }
                else if (interaction.customId.startsWith('shop_cupom_')) {
                    await shopHandler.handleCupom(interaction);
                }
                else if (interaction.customId.startsWith('shop_cancelar_')) {
                    await shopHandler.handleCancelar(interaction);
                }
                else if (interaction.customId.startsWith('shop_copiarpix_')) {
                    await shopHandler.handleCopiarPix(interaction);
                }
                else if (interaction.customId.startsWith('shop_admin_')) {
                    await shopAdmin.handleAdminButtons(interaction);
                }
                else if (interaction.customId === 'solicitar_creator') {
                    await creatorCommand.handleSolicitacaoButton(interaction);
                }
                else if (interaction.customId.startsWith('aceitar_punicao_')) {
                    await ticketsHandler.aceitarRevisao(interaction, client);
                }
                else if (interaction.customId.startsWith('negar_punicao_')) {
                    await ticketsHandler.negarRevisao(interaction, client);
                }
                else if (interaction.customId.startsWith('aceitar_')) {
                    const idSolicitacao = interaction.customId.replace('aceitar_', '');
                    await creatorCommand.handleAcaoButton(interaction, 'aceitar', idSolicitacao);
                }
                else if (interaction.customId.startsWith('recusar_')) {
                    const idSolicitacao = interaction.customId.replace('recusar_', '');
                    await creatorCommand.handleAcaoButton(interaction, 'recusar', idSolicitacao);
                }
                // NOTA: 'aceitar_appeal_' e 'negar_appeal_' devem vir ANTES de 'aceitar_'/'recusar_'
                // para evitar conflito de prefixo
            }
            
        } catch (error) {
            console.error('❌ ERRO GERAL:', error);
            if (!interaction.replied && !interaction.deferred) {
                await interaction.reply({ 
                    content: `❌ Erro: ${error.message}`, 
                    flags: MessageFlags.Ephemeral 
                }).catch(() => {});
            }
        }
    }
};
