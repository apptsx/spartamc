const { AttachmentBuilder } = require('discord.js');
const { createCanvas, loadImage, registerFont } = require('canvas');
const axios = require('axios');
const path = require('path');
const fs = require('fs');

const LOG_CHANNEL_ID = '1500407875552542871';

const FONT_PATH = path.join(__dirname, '..', 'fonts', 'cloudsoft.otf');
if (fs.existsSync(FONT_PATH)) {
    registerFont(FONT_PATH, { family: 'Cloudsoft' });
}

function wrapLines(ctx, text, maxWidth) {
    if (!text) return ['(sem conteudo)'];
    const paragraphs = String(text).split('\n');
    const lines = [];
    for (let paragraph of paragraphs) {
        let line = '';
        for (const word of paragraph.split(' ')) {
            const test = line ? line + ' ' + word : word;
            if (ctx.measureText(test).width > maxWidth && line) {
                lines.push(line);
                line = word;
            } else {
                line = test;
            }
        }
        if (line) lines.push(line);
    }
    if (!lines.length) lines.push('(sem conteudo)');
    return lines;
}

function hexToRgba(hex, a) {
    const r = parseInt(hex.slice(1, 3), 16);
    const g = parseInt(hex.slice(3, 5), 16);
    const b = parseInt(hex.slice(5, 7), 16);
    return `rgba(${r},${g},${b},${a})`;
}

async function genImage(message, tipo) {
    const displayName = String(message.member?.displayName || message.author?.displayName || message.author?.username || 'Desconhecido');
    const username = String(message.author?.username || 'Desconhecido');
    const channelName = message.channel?.name || 'desconhecido';
    const content = message.cleanContent || message.content || '';
    const hasAtt = message.attachments && message.attachments.size > 0;
    const accent = tipo === 'deleted' ? '#ff6b6b' : '#ffd93d';
    const title = tipo === 'deleted' ? 'Mensagem Apagada' : 'Mensagem Editada';
    const date = new Date().toLocaleString('pt-BR');

    const W = 560;
    const LINE_H = 22;
    const BAR_H = 28;
    const PAD = 76;
    const AV_Y = BAR_H + 8;
    const MAX_TEXT_W = W - PAD - 20;

    const tempCanvas = createCanvas(1, 1);
    const tempCtx = tempCanvas.getContext('2d');
    tempCtx.font = '14px Cloudsoft, sans-serif';

    const lines = wrapLines(tempCtx, content, MAX_TEXT_W);
    const textH = Math.max(lines.length * LINE_H, LINE_H);
    const attachH = hasAtt ? 24 : 0;
    const totalH = AV_Y + 20 + textH + 8 + attachH + 6 + 16 + 8;

    const canvas = createCanvas(W, totalH);
    const ctx = canvas.getContext('2d');

    ctx.textBaseline = 'top';

    ctx.fillStyle = '#2b2d31';
    ctx.beginPath();
    if (typeof ctx.roundRect === 'function') {
        ctx.roundRect(0, 0, W, totalH, 8);
    } else {
        ctx.rect(0, 0, W, totalH);
    }
    ctx.fill();

    ctx.fillStyle = accent;
    ctx.beginPath();
    if (typeof ctx.roundRect === 'function') {
        ctx.roundRect(0, 0, W, BAR_H, { upperLeft: 8, upperRight: 8, lowerLeft: 0, lowerRight: 0 });
    } else {
        ctx.rect(0, 0, W, BAR_H);
    }
    ctx.fill();

    ctx.font = 'bold 13px Cloudsoft, sans-serif';
    ctx.fillStyle = '#ffffff';
    ctx.fillText(title, 16, 7);

    let avatarImg = null;
    try {
        const avUrl = message.author?.displayAvatarURL?.({ extension: 'png', size: 64 });
        if (avUrl) {
            const res = await axios.get(avUrl, { responseType: 'arraybuffer', timeout: 5000 });
            avatarImg = await loadImage(Buffer.from(res.data));
        }
    } catch {}

    const avCX = 44;
    const avCY = AV_Y + 22;

    if (avatarImg) {
        ctx.save();
        ctx.beginPath();
        ctx.arc(avCX, avCY, 22, 0, Math.PI * 2);
        ctx.clip();
        ctx.drawImage(avatarImg, 22, AV_Y, 44, 44);
        ctx.restore();
    } else {
        ctx.fillStyle = '#5865f2';
        ctx.beginPath();
        ctx.arc(avCX, avCY, 22, 0, Math.PI * 2);
        ctx.fill();
    }

    ctx.strokeStyle = '#ffffff';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.arc(avCX, avCY, 22, 0, Math.PI * 2);
    ctx.stroke();

    let roleIconImg = null;
    try {
        const iconRole = message.member?.roles?.icon;
        const iconUrl = iconRole?.iconURL?.({ size: 16 });
        if (iconUrl) {
            const res = await axios.get(iconUrl, { responseType: 'arraybuffer', timeout: 3000 });
            roleIconImg = await loadImage(Buffer.from(res.data));
        }
    } catch {}

    const roleColor = message.member?.displayHexColor || '#ffffff';
    let nameX = PAD;

    if (roleIconImg) {
        ctx.drawImage(roleIconImg, nameX, AV_Y + 3, 16, 16);
        nameX += 20;
    }

    ctx.font = 'bold 16px Cloudsoft, sans-serif';
    ctx.fillStyle = roleColor !== '#000000' ? roleColor : '#ffffff';
    ctx.fillText(displayName, nameX, AV_Y + 2);

    let xOff = nameX + ctx.measureText(displayName).width + 8;

    if (username !== displayName) {
        ctx.font = '11px Cloudsoft, sans-serif';
        ctx.fillStyle = '#949ba4';
        ctx.fillText(`@${username}`, xOff, AV_Y + 5);
        xOff += ctx.measureText(`@${username}`).width + 8;
    }

    ctx.font = '14px Cloudsoft, sans-serif';
    ctx.fillStyle = '#dbdee1';
    let ty = AV_Y + 20;
    for (const l of lines) {
        ctx.fillText(l, PAD, ty);
        ty += LINE_H;
    }

    let fty = ty + 6;
    if (hasAtt) {
        ctx.font = '12px Cloudsoft, sans-serif';
        ctx.fillStyle = accent;
        ctx.fillText(`📎 ${message.attachments.size} anexo(s)`, PAD, fty);
        fty += 22;
    }

    ctx.font = '10px Cloudsoft, sans-serif';
    ctx.fillStyle = '#5a5e64';
    ctx.fillText(`#${channelName} - ${date}`, PAD, fty);

    return canvas.toBuffer('image/png');
}

const deleteDedup = new Map();
const editDedup = new Map();

module.exports = {
    async handleMessageDelete(message) {
        if (message.author?.bot) return;
        if (!message.content && !message.attachments?.size) return;
        const id = String(message.id);
        if (deleteDedup.has(id)) {
            console.log(`[DEDUP] delete bloqueado para msg ${id}`);
            return;
        }
        deleteDedup.set(id, Date.now());
        setTimeout(() => deleteDedup.delete(id), 60000);
        console.log(`[DEDUP] delete passou para msg ${id}`);
        try {
            const ch = await message.guild?.channels.fetch(LOG_CHANNEL_ID).catch(() => null);
            if (!ch) return;
            const buf = await genImage(message, 'deleted');
            await ch.send({
                content: `🗑️ **${message.author.tag}** apagou em <#${message.channel.id}>`,
                files: [new AttachmentBuilder(buf, { name: 'apagada.png' })]
            });
        } catch (e) { console.error('❌ log apagada:', e.message); }
    },
    async handleMessageUpdate(old, neu) {
        if (old.author?.bot) return;
        if (old.content === neu.content) return;
        if (!old.content && !neu.content) return;
        const key = String(old.id) + '_' + String(neu.content || '').substring(0, 100);
        if (editDedup.has(key)) return;
        editDedup.set(key, Date.now());
        setTimeout(() => editDedup.delete(key), 60000);
        try {
            const ch = await old.guild?.channels.fetch(LOG_CHANNEL_ID).catch(() => null);
            if (!ch) return;
            const buf = await genImage(old, 'edited');
            const txt = old.cleanContent || old.content || '';
            await ch.send({
                content: `✏️ **${old.author.tag}** editou em <#${old.channel.id}>\nAntes:\`\`\`${String(txt).substring(0, 1900)}\`\`\``,
                files: [new AttachmentBuilder(buf, { name: 'editada.png' })]
            });
        } catch (e) { console.error('❌ log editada:', e.message); }
    }
};
