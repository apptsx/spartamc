const { SlashCommandBuilder, MessageFlags } = require('discord.js');
const { isOwner, createMural } = require('../handlers/teamSystem');

module.exports = {
  data: new SlashCommandBuilder()
    .setName('painel')
    .setDescription('Sparta » Enviar painel administrativo')
    .addChannelOption(option =>
      option.setName('canal')
        .setDescription('Sparta » Enviar painel administrativo')
        .setRequired(false)),

  async execute(interaction) {
    if (!isOwner(interaction.user.id)) {
      return interaction.reply({ content: '❌ Só o dono!', flags: MessageFlags.Ephemeral });
    }

    await interaction.deferReply({ ephemeral: true });

    try {
      const channel = interaction.options.getChannel('canal') || interaction.channel;
      const success = await createMural(interaction.client, channel);
      
      if (success) {
        await interaction.editReply(`✅ Painel criado em ${channel}!`);
      } else {
        await interaction.editReply('❌ Erro!');
      }
    } catch (error) {
      console.error(error);
      await interaction.editReply('❌ Erro!');
    }
  },
};