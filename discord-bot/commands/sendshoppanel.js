const {
  SlashCommandBuilder,
  PermissionFlagsBits,
  ContainerBuilder,
  TextDisplayBuilder,
  ThumbnailBuilder,
  SectionBuilder,
  SeparatorBuilder,
  SeparatorSpacingSize,
  StringSelectMenuBuilder,
  StringSelectMenuOptionBuilder,
  ActionRowBuilder,
  MessageFlags
} = require('discord.js');
const shop = require('../handlers/shopHandler');

const SelectMenuOptionBuilder = StringSelectMenuOptionBuilder;

module.exports = {
  help: {
    name: 'sendshoppanel',
    description: 'Sparta » Envia o painel da loja'
  },
  data: new SlashCommandBuilder()
    .setName('sendshoppanel')
    .setDescription('Sparta » Envia o painel da loja no canal')
    .setDefaultMemberPermissions(PermissionFlagsBits.Administrator),

  async execute(interaction) {
    if (!interaction.member.permissions.has(PermissionFlagsBits.Administrator)) {
      return interaction.reply({ content: '❌ Apenas administradores podem usar este comando!', flags: MessageFlags.Ephemeral });
    }

    try {
      await interaction.deferReply({ flags: MessageFlags.Ephemeral });
      const produtos = shop.getProdutos();
      const entries = Object.entries(produtos);

      const container = new ContainerBuilder()
        .addSectionComponents(
          new SectionBuilder()
            .setThumbnailAccessory(
              new ThumbnailBuilder()
                .setURL("https://cdn.discordapp.com/attachments/1492296145165353150/1494094525889777845/logo.png?ex=69e4a735&is=69e355b5&hm=d68fea7ae8ef89826ef0e37187d77ace2d4c63ca5b6883e19739a63db6777d99&")
            )
            .addTextDisplayComponents(
              new TextDisplayBuilder().setContent("### <:sparta:1482175426506784781> Lojas Sparta"),
              new TextDisplayBuilder().setContent("> Escolha um dos planos abaixo para adquirir e melhorar sua experiência no servidor!"),
            ),
        )
        .addSeparatorComponents(
          new SeparatorBuilder()
            .setSpacing(SeparatorSpacingSize.Small)
            .setDivider(true)
        )
        .addTextDisplayComponents(
          new TextDisplayBuilder().setContent(
            "### <:Corante_vermelhoH:1494067495613501501> Informações\n" +
            "• Pagamento processado via **Mercado Pago**\n" +
            "• Pagamentos via **Pix**\n" +
            "• Ao finalizar, o rank é enviado automaticamente"
          ),
        )
        .addSeparatorComponents(
          new SeparatorBuilder()
            .setSpacing(SeparatorSpacingSize.Small)
            .setDivider(true)
        );

      if (entries.length > 0) {
        const select = new StringSelectMenuBuilder()
          .setCustomId("shop_selecionar_produto")
          .setPlaceholder("» Selecione um produto para comprar!")
          .addOptions(
            entries.map(([id, p]) => {
              const precos = Object.values(p.campos).map(c => parseFloat(c.preco));
              const minPreco = Math.min(...precos);
              const maxPreco = Math.max(...precos);
              const precoStr = precos.length === 1
                ? `R$ ${minPreco.toFixed(2)}`
                : `R$ ${minPreco.toFixed(2)} - R$ ${maxPreco.toFixed(2)}`;
              return new SelectMenuOptionBuilder()
                .setLabel(p.nome)
                .setValue(id)
                .setDescription(`${precoStr} — ${p.desc || 'Sem descrição'}`)
                .setEmoji("💎");
            })
          );
        container.addActionRowComponents(new ActionRowBuilder().addComponents(select));
      } else {
        container.addTextDisplayComponents(
          new TextDisplayBuilder().setContent("> Nenhum produto disponível no momento.")
        );
      }

      container.addTextDisplayComponents(
        new TextDisplayBuilder().setContent("-# > *spartamc.com.br*"),
      );

      await interaction.channel.send({
        components: [container],
        flags: [MessageFlags.IsComponentsV2]
      });

      await interaction.editReply({ content: '✅ Painel da loja enviado com sucesso!' });

    } catch (error) {
      console.error('❌ Erro ao enviar painel da loja:', error);
      if (interaction.deferred || interaction.replied) {
        await interaction.editReply({ content: `❌ Erro: ${error.message}` }).catch(() => {});
      } else {
        await interaction.reply({ content: `❌ Erro: ${error.message}`, flags: MessageFlags.Ephemeral }).catch(() => {});
      }
    }
  }
};
