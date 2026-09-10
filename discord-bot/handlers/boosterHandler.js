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

const BOOSTER_CHANNEL_ID = '1474305774875443272';

async function createBoosterImage(member) {
    const canvas = createCanvas(700, 250);
    const ctx = canvas.getContext('2d');
    
    const gradient = ctx.createLinearGradient(0, 0, canvas.width, 0);
    gradient.addColorStop(0, '#6B2E8C');
    gradient.addColorStop(0.5, '#9B4D96');
    gradient.addColorStop(1, '#6B2E8C');
    
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
    ctx.fillText('💎 Impulsionou o servidor', 140, 125);
    
    ctx.font = 'bold 36px "Segoe UI", "Arial", sans-serif';
    ctx.fillStyle = '#FFFFFF';
    ctx.fillText(member.user.username, 140, 175);
    
    ctx.font = '18px "Segoe UI", "Arial", sans-serif';
    ctx.fillStyle = 'rgba(255, 255, 255, 0.8)';
    ctx.fillText(`@${member.user.username.toLowerCase()}`, 140, 205);
    
    const boostLevel = member.premiumSubscription?.tier || 1;
    ctx.font = 'bold 48px "Segoe UI", "Arial", sans-serif';
    ctx.fillStyle = 'rgba(255, 255, 255, 0.2)';
    ctx.fillText(`Nível ${boostLevel}`, 520, 70);
    
    return canvas.toBuffer('image/png');
}

module.exports = {
    async handleBooster(member) {
        try {
            const channel = member.guild.channels.cache.get(BOOSTER_CHANNEL_ID);
            if (!channel) return;

            const imageBuffer = await createBoosterImage(member);
            const attachment = new AttachmentBuilder(imageBuffer, { 
                name: `booster-${member.user.id}.png` 
            });

            // CONTAINER ÚNICO (SEM CAMPO CONTENT)
            const boosterContainer = new ContainerBuilder()
                .setAccentColor(0x9B4D96)
                
                // Mensagem de agradecimento (dentro do container)
                .addTextDisplayComponents(
                    new TextDisplayBuilder().setContent(
                        `🎉 **${member.user}** acabou de impulsionar o servidor! Muito obrigado! 🎉`
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
                                .setURL(`attachment://booster-${member.user.id}.png`)
                        )
                )
                
                .addSeparatorComponents(
                    new SeparatorBuilder()
                        .setSpacing(SeparatorSpacingSize.Small)
                        .setDivider(true)
                )
                
                .addTextDisplayComponents(
                    new TextDisplayBuilder().setContent(
                        `## ✨ Benefícios do Boost\n\n` +
                        `• Cargo exclusivo de booster\n` +
                        `• Acesso a canais VIP\n` +
                        `• Mais qualidade de áudio\n` +
                        `• Emojis animados extras\n` +
                        `• Sua foto no canal de boosters!`
                    )
                )
                
                .addSeparatorComponents(
                    new SeparatorBuilder()
                        .setSpacing(SeparatorSpacingSize.Small)
                        .setDivider(true)
                )
                
                .addTextDisplayComponents(
                    new TextDisplayBuilder().setContent(
                        `### 🌟 Obrigado por fazer parte da nossa comunidade!\n` +
                        `Com seu boost, o servidor agora está no **Nível ${member.guild.premiumTier}** ` +
                        `com **${member.guild.premiumSubscriptionCount}** boosts!`
                    )
                );

            await channel.send({
                components: [boosterContainer],
                files: [attachment],
                flags: [MessageFlags.IsComponentsV2]
            });

            console.log(`✅ Boost registrado para ${member.user.tag}`);

        } catch (error) {
            console.error('❌ Erro no sistema de booster:', error);
        }
    }
};
