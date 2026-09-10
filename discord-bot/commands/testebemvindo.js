const { SlashCommandBuilder, AttachmentBuilder } = require('discord.js');
const { createCanvas, loadImage } = require('canvas');
const path = require('path');

module.exports = {
    data: new SlashCommandBuilder()
        .setName('teste')
        .setDescription('Testar sistemas do bot')
        .addSubcommand(sub =>
            sub
                .setName('bemvindo')
                .setDescription('Testar a imagem de boas-vindas')),

    async execute(interaction) {
        await interaction.deferReply({ flags: 64 });

        try {
            const background = await loadImage(path.join(__dirname, '../assets/bemvindo.png'));
            const canvas = createCanvas(background.width, background.height);
            const ctx = canvas.getContext('2d');

            ctx.drawImage(background, 0, 0, canvas.width, canvas.height);

            const avatarURL = interaction.user.displayAvatarURL({ extension: 'png', size: 1024 });
            const avatar = await loadImage(avatarURL);

            const centerX = 1180;
            const centerY = 360;
            const radius = 190;

            ctx.save();
            ctx.beginPath();
            ctx.arc(centerX, centerY, radius, 0, Math.PI * 2);
            ctx.closePath();
            ctx.clip();

            ctx.drawImage(avatar, centerX - radius, centerY - radius, radius * 2, radius * 2);
            ctx.restore();

            ctx.save();
            ctx.globalCompositeOperation = 'screen';
            ctx.globalAlpha = 0.5;
            const glowSize = radius * 2.8;
            ctx.drawImage(avatar, centerX - glowSize / 2, centerY - glowSize / 2, glowSize, glowSize);
            ctx.restore();

            const attachment = new AttachmentBuilder(canvas.toBuffer('image/png'), { name: 'bem-vindo.png' });

            await interaction.editReply({
                content: '📸 Prévia da imagem de boas-vindas:',
                files: [attachment]
            });
        } catch (err) {
            console.error('[ERRO TESTE BEMVINDO]', err);
            await interaction.editReply({ content: `❌ Erro: ${err.message}` });
        }
    }
};