const {
  SlashCommandBuilder,
  PermissionFlagsBits,
  AttachmentBuilder
} = require("discord.js");
const { createCanvas, loadImage } = require("canvas");

module.exports = {
  data: new SlashCommandBuilder()
    .setName("stafflog")
    .setDescription("Gera uma imagem de log da staff")
    .setDefaultMemberPermissions(PermissionFlagsBits.Administrator)
    .addStringOption(option =>
      option
        .setName("acao")
        .setDescription("Ação do log")
        .setRequired(true)
        .addChoices(
          { name: "Entrou", value: "entrou" },
          { name: "Saiu", value: "saiu" },
          { name: "Removido", value: "removido" },
          { name: "Promovido", value: "promovido" }
        )
    )
    .addStringOption(option =>
      option
        .setName("nick")
        .setDescription("Nick do jogador")
        .setRequired(true)
    )
    .addRoleOption(option =>
      option
        .setName("cargo")
        .setDescription("Cargo da staff")
        .setRequired(true)
    ),

  async execute(interaction) {
    if (!interaction.member.permissions.has(PermissionFlagsBits.Administrator)) {
      return interaction.reply({ content: "❌ Apenas administradores podem usar este comando!", ephemeral: true });
    }

    await interaction.deferReply();

    const acao = interaction.options.getString("acao");
    const nick = interaction.options.getString("nick");
    const cargo = interaction.options.getRole("cargo");

    function abreviar(nome) {
      if (nome === "Moderador Secundário") return "Moderador";
      if (nome === "Moderador Primário") return "Moderador+";
      return nome;
    }

    const nomeCargo = abreviar(cargo.name);

    const canvas = createCanvas(2048, 320);
    const ctx = canvas.getContext("2d");

    const roleColor = cargo.hexColor === "#000000" ? "#0d7cff" : cargo.hexColor;

    const isRed = acao === "saiu" || acao === "removido";
    const mainColor = isRed ? "#ff2b2b" : "#ffc400";
    const mainColorDark = isRed ? "#b80000" : "#ffb000";

    const titles = {
      entrou: "ENTROU",
      saiu: "SAIU",
      removido: "REMOVIDO",
      promovido: "PROMOVIDO"
    };

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    ctx.beginPath();
    ctx.arc(160, 160, 138, 0, Math.PI * 2);
    ctx.fillStyle = "#151515";
    ctx.fill();

    ctx.beginPath();
    ctx.moveTo(250, 78);
    ctx.lineTo(1940, 78);
    ctx.quadraticCurveTo(1980, 160, 1940, 242);
    ctx.lineTo(250, 242);
    ctx.closePath();
    ctx.fillStyle = "#161616";
    ctx.fill();

    const bgGradient = ctx.createLinearGradient(250, 78, 1940, 242);
    bgGradient.addColorStop(0, "#111111");
    bgGradient.addColorStop(0.5, "#1b1b1b");
    bgGradient.addColorStop(1, "#111111");
    ctx.fillStyle = bgGradient;
    ctx.fillRect(250, 78, 1690, 164);

    ctx.beginPath();
    ctx.moveTo(1940, 78);
    ctx.lineTo(2040, 160);
    ctx.lineTo(1940, 242);
    ctx.quadraticCurveTo(1980, 160, 1940, 78);
    ctx.closePath();

    const arrowGradient = ctx.createLinearGradient(1940, 78, 2040, 242);
    arrowGradient.addColorStop(0, mainColor);
    arrowGradient.addColorStop(0.5, mainColorDark);
    arrowGradient.addColorStop(1, mainColor);
    ctx.fillStyle = arrowGradient;
    ctx.fill();

    ctx.beginPath();
    ctx.roundRect(820, 45, 430, 62, 35);
    const badgeGradient = ctx.createLinearGradient(820, 45, 1250, 107);
    badgeGradient.addColorStop(0, mainColor);
    badgeGradient.addColorStop(1, mainColorDark);
    ctx.fillStyle = badgeGradient;
    ctx.fill();

    ctx.font = "bold 44px Arial";
    ctx.fillStyle = "#ffffff";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText(titles[acao], 1035, 77);

    try {
      const head = await loadImage(`https://mc-heads.net/head/${encodeURIComponent(nick)}/180`);

      ctx.save();
      ctx.beginPath();
      ctx.arc(160, 160, 95, 0, Math.PI * 2);
      ctx.clip();

      ctx.imageSmoothingEnabled = false;
      ctx.drawImage(head, 65, 65, 190, 190);

      ctx.restore();
    } catch {
      ctx.font = "bold 120px Arial";
      ctx.fillStyle = "#ffffff";
      ctx.textAlign = "center";
      ctx.fillText("?", 160, 165);
    }

    ctx.textBaseline = "middle";
    const bodyCenter = 1095;
    const maxWidth = 1420;
    const y = 165;

    function autoSize(fullText) {
      let size = 58;
      ctx.font = `bold ${size}px Arial`;
      while (ctx.measureText(fullText).width > maxWidth && size > 20) {
        size -= 2;
        ctx.font = `bold ${size}px Arial`;
      }
    }

    if (acao === "entrou") {
      const full = `${nick} entrou como ${nomeCargo}`;
      autoSize(full);
      let x = 480;
      ctx.textAlign = "left";
      ctx.fillStyle = roleColor;
      ctx.fillText(nick, x, y);
      x += ctx.measureText(nick).width;
      ctx.fillStyle = "#ffffff";
      ctx.fillText(" entrou como ", x, y);
      x += ctx.measureText(" entrou como ").width;
      ctx.fillStyle = roleColor;
      ctx.fillText(nomeCargo, x, y);
    }

    if (acao === "promovido") {
      const full = `${nick} foi promovido para ${nomeCargo}`;
      autoSize(full);
      let x = 480;
      ctx.textAlign = "left";
      ctx.fillStyle = roleColor;
      ctx.fillText(nick, x, y);
      x += ctx.measureText(nick).width;
      ctx.fillStyle = "#ffffff";
      ctx.fillText(" foi promovido para ", x, y);
      x += ctx.measureText(" foi promovido para ").width;
      ctx.fillStyle = roleColor;
      ctx.fillText(nomeCargo, x, y);
    }

    if (acao === "removido" || acao === "saiu") {
      const texto = acao === "removido"
        ? `${nick} foi removido da equipe.`
        : `${nick} saiu da equipe.`;
      autoSize(texto);
      const fullWidth = ctx.measureText(texto).width;
      const parts = [
        { text: nick, color: "#ff2b2b" },
        { text: acao === "removido" ? " foi removido da equipe." : " saiu da equipe.", color: "#ffffff" }
      ];
      let startX = bodyCenter - fullWidth / 2;
      ctx.textAlign = "left";
      for (const p of parts) {
        ctx.fillStyle = p.color;
        ctx.fillText(p.text, startX, y);
        startX += ctx.measureText(p.text).width;
      }
    }

    const attachment = new AttachmentBuilder(canvas.toBuffer("image/png"), {
      name: "stafflog.png"
    });

    const channel = await interaction.client.channels.fetch("1500407796368408607");

    const mensagens = {
      entrou: "### <:blink:1500966698566815895> NOVO INTEGRANTE",
      promovido: "### ✨ **INTEGRANTE PROMOVIDO**",
      saiu: "📤 **INTEGRANTE SAIU**",
      removido: "📤 **INTEGRANTE SAIU**"
    };

    await channel.send({ content: mensagens[acao], files: [attachment] });

    await interaction.editReply({
      content: "✅ Log enviado no canal <#1500407796368408607>!"
    });
  }
};
