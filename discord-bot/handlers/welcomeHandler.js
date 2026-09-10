const { 
    ContainerBuilder, 
    TextDisplayBuilder, 
    SeparatorBuilder, 
    SeparatorSpacingSize,
    ButtonBuilder,
    ButtonStyle,
    ActionRowBuilder,
    MediaGalleryBuilder,
    MediaGalleryItemBuilder,
    MessageFlags,
    AttachmentBuilder
} = require('discord.js');
const { createCanvas, loadImage } = require('canvas');
const { request } = require('undici');

const WELCOME_CHANNEL_ID = '1474305586374905918';

async function createWelcomeImage(member) {
    const canvas = createCanvas(700, 250);
    const ctx = canvas.getContext('2d');
    
    const gradient = ctx.createLinearGradient(0, 0, canvas.width, 0);
    gradient.addColorStop(0, '#1E3C72');
    gradient.addColorStop(0.5, '#2A5298');
    gradient.addColorStop(1, '#1E3C72');
    
    ctx.fillStyle = gradient;
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    ctx.fillStyle = 'rgba(255, 255, 255, 0.05)';
    ctx.fillRect(0, 0, canvas.width, 80);
    
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.2)';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(0, 80);
    ctx.lineTo(canvas.width, 80);
    ctx.stroke();
    
    try {
        const avatarURL = member.user.displayAvatarURL({ extension: 'png', size: 128 });
        const avatarResponse = await request(avatarURL);
        const avatarBuffer = await avatarResponse.body.arrayBuffer();
        const avatar = await loadImage(Buffer.from(avatarBuffer));
        
        ctx.save();
        ctx.shadowColor = 'rgba(0, 0, 0, 0.3)';
        ctx.shadowBlur = 10;
        ctx.shadowOffsetX = 2;
        ctx.shadowOffsetY = 2;
        ctx.drawImage(avatar, 50, 95, 70, 70);
        ctx.restore();
        
    } catch (error) {
        console.error('❌ Erro ao carregar avatar:', error);
    }
    
    ctx.shadowColor = 'rgba(0, 0, 0, 0.3)';
    ctx.shadowBlur = 5;
    ctx.shadowOffsetX = 2;
    ctx.shadowOffsetY = 2;
    
    ctx.font = 'bold 24px "Segoe UI", "Arial", sans-serif';
    ctx.fillStyle = '#FFFFFF';
    ctx.fillText('Entrou no servidor', 140, 125);
    
    ctx.font = 'bold 36px "Segoe UI", "Arial", sans-serif';
    ctx.fillStyle = '#FFFFFF';
    ctx.fillText(member.user.username, 140, 175);
    
    ctx.font = '18px "Segoe UI", "Arial", sans-serif';
    ctx.fillStyle = 'rgba(255, 255, 255, 0.8)';
    ctx.fillText(`@${member.user.username.toLowerCase()}`, 140, 205);
    
    ctx.font = 'bold 48px "Segoe UI", "Arial", sans-serif';
    ctx.fillStyle = 'rgba(255, 255, 255, 0.2)';
    ctx.fillText(`#${member.guild.memberCount}`, 550, 70);
    
    return canvas.toBuffer('image/png');
}

module.exports = {
    async handleWelcome(member) {
        try {
            const channel = member.guild.channels.cache.get(WELCOME_CHANNEL_ID);
            if (!channel) return;

            const imageBuffer = await createWelcomeImage(member);
            const attachment = new AttachmentBuilder(imageBuffer, { 
                name: `welcome-${member.user.id}.png` 
            });

            // CONTAINER ÚNICO (SEM CAMPO CONTENT)
            const welcomeContainer = new ContainerBuilder()
                .setAccentColor(0x1E3C72)
                
                // Mensagem de boas-vindas (dentro do container)
                .addTextDisplayComponents(
                    new TextDisplayBuilder().setContent(
                        `👋 **${member.user}** acabou de entrar no servidor!`
                    )
                )
                
                .addSeparatorComponents(
                    new SeparatorBuilder()
                        .setSpacing(SeparatorSpacingSize.Small)
                        .setDivider(true)
                )
                
                .addMediaGalleryComponents(
                    new MediaGalleryBuilder()
                        .addItems(
                            new MediaGalleryItemBuilder()
                                .setURL(`attachment://welcome-${member.user.id}.png`)
                        )
                )
                
                .addSeparatorComponents(
                    new SeparatorBuilder()
                        .setSpacing(SeparatorSpacingSize.Small)
                        .setDivider(true)
                )
                
                .addTextDisplayComponents(
                    new TextDisplayBuilder().setContent(
                        `## 📌 Canais Importantes\n\n` +
                        `📜 **Regras:** <#1473384716680827083>\n` +
                        `📢 **Anúncios:** <#1473384735592943740>\n` +
                        `🎉 **Eventos:** <#1473384791767515267>`
                    )
                )
                
                .addSeparatorComponents(
                    new SeparatorBuilder()
                        .setSpacing(SeparatorSpacingSize.Small)
                        .setDivider(true)
                )
                
                .addTextDisplayComponents(
                    new TextDisplayBuilder().setContent(
                        `## 🚀 Como começar?\n\n` +
                        `1️⃣ Leia as <#1473384716680827083>\n` +
                        `2️⃣ Participe dos <#1473384791767515267>\n` +
                        `3️⃣ Acompanhe os <#1473384735592943740>`
                    )
                )
                
                .addSeparatorComponents(
                    new SeparatorBuilder()
                        .setSpacing(SeparatorSpacingSize.Small)
                        .setDivider(true)
                )
                
                .addTextDisplayComponents(
                    new TextDisplayBuilder().setContent(
                        `### ✨ Bem-vindo à nossa comunidade!\n` +
                        `Você é o **membro #${member.guild.memberCount}**`
                    )
                );

            await channel.send({
                components: [welcomeContainer],
                files: [attachment],
                flags: [MessageFlags.IsComponentsV2]
            });

            console.log(`✅ Boas-vindas enviada para ${member.user.tag}`);

        } catch (error) {
            console.error('❌ Erro no sistema de boas-vindas:', error);
        }
    }
};
