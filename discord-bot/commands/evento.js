const {
  SlashCommandBuilder,
  PermissionFlagsBits,
  ModalBuilder,
  TextInputBuilder,
  TextInputStyle,
  ActionRowBuilder,
  AttachmentBuilder
} = require("discord.js");

const { createCanvas, loadImage } = require("canvas");

module.exports = {
  data: new SlashCommandBuilder()
    .setName("evento")
    .setDescription("Sistema de eventos")
    .setDefaultMemberPermissions(PermissionFlagsBits.Administrator)
    .addSubcommand(sub =>
      sub.setName("criar").setDescription("Criar imagem de evento")
    ),

  async execute(interaction) {
    if (!interaction.member.permissions.has(PermissionFlagsBits.Administrator)) {
      return interaction.reply({ content: "❌ Apenas administradores podem usar este comando!", ephemeral: true });
    }

    if (interaction.options.getSubcommand() === "criar") {
      const modal = new ModalBuilder()
        .setCustomId("modal_evento_criar")
        .setTitle("Criar Evento");

      const nome = new TextInputBuilder()
        .setCustomId("nome_evento")
        .setLabel("Nome do evento")
        .setStyle(TextInputStyle.Short)
        .setRequired(true);

      const horas = new TextInputBuilder()
        .setCustomId("horas_evento")
        .setLabel("Horário, exemplo: 20:00")
        .setStyle(TextInputStyle.Short)
        .setRequired(true);

      const comoJogar = new TextInputBuilder()
        .setCustomId("como_jogar")
        .setLabel("Como jogar?")
        .setStyle(TextInputStyle.Paragraph)
        .setRequired(true);

      const item = new TextInputBuilder()
        .setCustomId("item_evento")
        .setLabel("Item: SOPA, CAMA ou ESPADA")
        .setStyle(TextInputStyle.Short)
        .setRequired(true);

      modal.addComponents(
        new ActionRowBuilder().addComponents(nome),
        new ActionRowBuilder().addComponents(horas),
        new ActionRowBuilder().addComponents(comoJogar),
        new ActionRowBuilder().addComponents(item)
      );

      return interaction.showModal(modal);
    }
  },

  async modal(interaction) {
    if (interaction.customId !== "modal_evento_criar") return;

    const nomeEvento = interaction.fields.getTextInputValue("nome_evento");
    const horas = interaction.fields.getTextInputValue("horas_evento");
    const comoJogar = interaction.fields.getTextInputValue("como_jogar");
    const itemEscolhido = interaction.fields.getTextInputValue("item_evento").toUpperCase();

    const canvas = createCanvas(1366, 768);
    const ctx = canvas.getContext("2d");

    const grad = ctx.createLinearGradient(0, 0, 680, 768);
    grad.addColorStop(0, "#ffcf00");
    grad.addColorStop(0.25, "#ff9700");
    grad.addColorStop(1, "#ff8500");

    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, 683, 768);

    ctx.fillStyle = "#ffffff";
    ctx.fillRect(683, 0, 683, 768);

    ctx.fillStyle = "#ffd900";
    ctx.beginPath();
    ctx.arc(-20, -20, 150, 0, Math.PI * 2);
    ctx.fill();

    ctx.beginPath();
    ctx.arc(1380, 760, 160, 0, Math.PI * 2);
    ctx.fill();

    function textoSombra(text, x, y, size, align = "center") {
      ctx.font = `bold ${size}px Arial`;
      ctx.textAlign = align;
      ctx.fillStyle = "#000000";
      ctx.fillText(text, x + 4, y + 5);
      ctx.fillStyle = "#ffffff";
      ctx.fillText(text, x, y);
    }

    textoSombra(horas, 341, 130, 58);
    textoSombra("EVENTO", 341, 260, 105);
    textoSombra(nomeEvento.toUpperCase(), 341, 390, 95);

    await drawItem(ctx, itemEscolhido, 340, 530);

    await drawIcon(ctx, 683, 700, 110);

    ctx.fillStyle = "#000000";
    ctx.font = "bold 38px Arial";
    ctx.textAlign = "left";
    ctx.fillText("Como jogar?", 700, 48);

    ctx.font = "32px Arial";
    wrapText(ctx, comoJogar, 700, 90, 610, 43);

    roundRect(ctx, 850, 660, 300, 72, 12, "#ff8c00");
    ctx.font = "bold 42px Arial";
    ctx.textAlign = "center";
    ctx.fillStyle = "#000000";
    ctx.fillText("/EVENTO", 1000 + 3, 708 + 4);
    ctx.fillStyle = "#ffffff";
    ctx.fillText("/EVENTO", 1000, 708);

    const attachment = new AttachmentBuilder(canvas.toBuffer("image/png"), {
      name: "evento.png"
    });

    const channel = await interaction.client.channels.fetch("1500407754442150040");
    await channel.send({
      content: "<@&1508520504397398137>",
      files: [attachment]
    });

    await interaction.reply({
      content: "✅ Imagem enviada no canal <#1500407754442150040>!",
      ephemeral: true
    });
  }
};

function wrapText(ctx, text, x, y, maxWidth, lineHeight) {
  const words = text.split(" ");
  let line = "";

  for (const word of words) {
    const testLine = line + word + " ";
    const metrics = ctx.measureText(testLine);

    if (metrics.width > maxWidth && line !== "") {
      ctx.fillText(line, x, y);
      line = word + " ";
      y += lineHeight;
    } else {
      line = testLine;
    }
  }

  ctx.fillText(line, x, y);
}

function roundRect(ctx, x, y, w, h, r, color) {
  ctx.fillStyle = color;
  ctx.beginPath();
  ctx.roundRect(x, y, w, h, r);
  ctx.fill();
}

const ITEMS = {
  CAMA: "https://cdn.discordapp.com/attachments/1478862197345222768/1515422312730726562/250.png?ex=6a2ef282&is=6a2da102&hm=ac53a57b38eb69926a7557f0b828b4d6e6fb2725fe70a9def849afaa97d1e143",
  ESPADA: "https://cdn.discordapp.com/attachments/1478862197345222768/1515422358402371625/Diamond_Sword_JE3_BE3.png?ex=6a2ef28d&is=6a2da10d&hm=61f6b41d65fc2f36106b36fcea77ebe929766b297e4891739274000976bf0b23",
  SOPA: "https://cdn.discordapp.com/attachments/1478862197345222768/1515422270665785454/Mushroom_Stew_JE2_BE2.png?ex=6a2ef278&is=6a2da0f8&hm=8c4139166888c66cdf71ee709d868b421cb558878c688ef419e4bf1b14e36af1"
};

const ICON_URL = "https://cdn.discordapp.com/attachments/1460470527566282793/1515421204272517250/d02faba2dd19bb774778015f8e9f5f33.png?ex=6a2ef17a&is=6a2d9ffa&hm=9acc784eb0f5657e58e71f0965fc3ee3864a88ad37d60b9130269be064374dc5";

async function drawIcon(ctx, x, y, size) {
  try {
    const res = await fetch(ICON_URL, { headers: { "User-Agent": "DiscordBot" } });
    const buf = Buffer.from(await res.arrayBuffer());
    const img = await loadImage(buf);
    ctx.save();
    ctx.beginPath();
    ctx.arc(x, y, size / 2, 0, Math.PI * 2);
    ctx.closePath();
    ctx.clip();
    ctx.drawImage(img, x - size / 2, y - size / 2, size, size);
    ctx.restore();
  } catch {
    roundRect(ctx, x - size / 2, y - size / 2, size, size, size / 4, "#ccc");
    ctx.fillStyle = "#666";
    ctx.font = `bold ${size / 2}px Arial`;
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText("S", x, y);
  }
}

async function drawItem(ctx, item, x, y) {
  const url = item.includes("CAMA") ? ITEMS.CAMA
    : item.includes("ESPADA") ? ITEMS.ESPADA
    : ITEMS.SOPA;

  try {
    const res = await fetch(url, { headers: { "User-Agent": "DiscordBot" } });
    const buf = Buffer.from(await res.arrayBuffer());
    const img = await loadImage(buf);
    const size = item.includes("ESPADA") ? 200 : 240;
    ctx.drawImage(img, x - size / 2, y - size / 2, size, size);
  } catch {
    ctx.fillStyle = "#999";
    ctx.font = "bold 40px Arial";
    ctx.textAlign = "center";
    ctx.fillText("?", x, y + 15);
  }
}
