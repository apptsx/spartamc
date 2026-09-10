const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { EmbedBuilder, ActionRowBuilder, ButtonBuilder, ButtonStyle, StringSelectMenuBuilder, StringSelectMenuOptionBuilder, MessageFlags, AttachmentBuilder } = require('discord.js');

const DB_BASE = path.join(__dirname, '..', 'Database', 'Vendas');
const TEMP_DIR = path.join(DB_BASE, 'temp');
if (!fs.existsSync(TEMP_DIR)) fs.mkdirSync(TEMP_DIR, { recursive: true });
const CATEGORIA_CARRINHOS = '1502368884295073843';

let mpAccessToken = null;
let tokenExpiresAt = 0;

function dbPath(file) {
  return path.join(DB_BASE, file);
}

function readJSON(file) {
  const fullPath = dbPath(file);
  if (!fs.existsSync(fullPath)) return null;
  try {
    const content = fs.readFileSync(fullPath, 'utf8');
    return JSON.parse(content);
  } catch {
    console.error(`Arquivo corrompido: ${fullPath}`);
    return null;
  }
}

function writeJSON(file, data) {
  fs.writeFileSync(dbPath(file), JSON.stringify(data, null, 2), 'utf8');
}

function generateID(length = 13) {
  const chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

function formatDate() {
  const d = new Date();
  return `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

function hexToInt(hex) {
  return parseInt(hex.replace('#', ''), 16);
}

// ─── MERCADO PAGO ─────────────────────────────────────────

async function getAccessToken() {
  if (mpAccessToken && Date.now() < tokenExpiresAt) return mpAccessToken;
  const pagamentos = readJSON('pagamentos.json');
  if (pagamentos?.mercadopago?.access_token) {
    mpAccessToken = pagamentos.mercadopago.access_token;
    tokenExpiresAt = Date.now() + 86400000;
    return mpAccessToken;
  }
  const mpClientId = process.env.MP_CLIENT_ID;
  const mpClientSecret = process.env.MP_CLIENT_SECRET;
  if (!mpClientId || !mpClientSecret) {
    throw new Error('MP_CLIENT_ID e MP_CLIENT_SECRET não configurados no .env');
  }
  const response = await fetch('https://api.mercadopago.com/oauth/token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      client_id: mpClientId,
      client_secret: mpClientSecret,
      grant_type: 'client_credentials'
    })
  });
  if (!response.ok) {
    const err = await response.text();
    throw new Error(`Erro ao autenticar no Mercado Pago: ${err}`);
  }
  const data = await response.json();
  mpAccessToken = data.access_token;
  tokenExpiresAt = Date.now() + (data.expires_in * 1000) - 60000;
  const pag = readJSON('pagamentos.json') || { mercadopago: {} };
  if (!pag.mercadopago) pag.mercadopago = {};
  pag.mercadopago.access_token = data.access_token;
  writeJSON('pagamentos.json', pag);
  return mpAccessToken;
}

async function criarPagamentoMP(valor, descricao, externalRef) {
  const token = await getAccessToken();
  const idempotencyKey = crypto.createHash('md5').update(`${valor}_${descricao}_${externalRef}_${Date.now()}`).digest('hex');
  const response = await fetch('https://api.mercadopago.com/v1/payments', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      'X-Idempotency-Key': idempotencyKey
    },
    body: JSON.stringify({
      transaction_amount: parseFloat(valor),
      description: descricao,
      payment_method_id: 'pix',
      payer: { email: 'comprador@spartamc.com.br' },
      external_reference: externalRef,
      notification_url: 'https://spartamc.com.br/api/mercado-pago/notify'
    })
  });
  if (!response.ok) {
    const err = await response.text();
    throw new Error(`Erro ao criar pagamento PIX: ${err}`);
  }
  return await response.json();
}

async function verificarPagamentoMP(paymentId) {
  const token = await getAccessToken();
  const response = await fetch(`https://api.mercadopago.com/v1/payments/${paymentId}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) return null;
  return await response.json();
}

async function reembolsarPagamentoMP(paymentId) {
  const token = await getAccessToken();
  const response = await fetch(`https://api.mercadopago.com/v1/payments/${paymentId}/refunds`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  return response.ok;
}

// ─── PRODUTOS ─────────────────────────────────────────────

function getProdutos() {
  return readJSON('produtos.json');
}

function getProduto(produtoId) {
  const produtos = getProdutos();
  return produtos[produtoId] || null;
}

function salvarProduto(produtoId, data) {
  const produtos = getProdutos();
  produtos[produtoId] = data;
  writeJSON('produtos.json', produtos);
}

function deletarProduto(produtoId) {
  const produtos = getProdutos();
  delete produtos[produtoId];
  writeJSON('produtos.json', produtos);
}

function criarProduto(nome, desc, entrega, icon, banner, hex) {
  const produtoId = generateID();
  const produto = {
    nome,
    desc,
    entrega: !!entrega,
    assets: {
      icon: icon || null,
      banner: banner || null,
      hex: hex || '00FFFF'
    },
    campos: {},
    criadoEm: Math.floor(Date.now() / 1000),
    cupons: {},
    ids: []
  };
  salvarProduto(produtoId, produto);
  return produtoId;
}

function criarCampo(produtoId, nome, desc, preco) {
  const produto = getProduto(produtoId);
  if (!produto) return null;
  const campoId = generateID();
  produto.campos[campoId] = {
    nome,
    desc: desc || '',
    preco: preco.toString(),
    condicoes: {
      valorMin: null,
      valorMax: null,
      quantidadeMin: null,
      quantidadeMax: null,
      idCargo: []
    },
    estoque: [],
    estoqueinfo: {
      last: null,
      alertas: []
    },
    cargos: {
      add: [],
      rem: []
    }
  };
  salvarProduto(produtoId, produto);
  return campoId;
}

function atualizarCampo(produtoId, campoId, data) {
  const produto = getProduto(produtoId);
  if (!produto || !produto.campos[campoId]) return false;
  Object.assign(produto.campos[campoId], data);
  salvarProduto(produtoId, produto);
  return true;
}

function deletarCampo(produtoId, campoId) {
  const produto = getProduto(produtoId);
  if (!produto || !produto.campos[campoId]) return false;
  delete produto.campos[campoId];
  salvarProduto(produtoId, produto);
  return true;
}

function adicionarEstoque(produtoId, campoId, itens) {
  const produto = getProduto(produtoId);
  if (!produto || !produto.campos[campoId]) return false;
  const campo = produto.campos[campoId];
  if (!Array.isArray(campo.estoque)) campo.estoque = [];
  campo.estoque.push(...itens.filter(i => i.trim()));
  campo.estoqueinfo.last = Date.now();
  salvarProduto(produtoId, produto);
  return true;
}

function removerEstoque(produtoId, campoId, quantidade) {
  const produto = getProduto(produtoId);
  if (!produto || !produto.campos[campoId]) return false;
  const campo = produto.campos[campoId];
  if (!Array.isArray(campo.estoque)) return false;
  if (campo.estoque.length < quantidade) return false;
  const removidos = campo.estoque.splice(0, quantidade);
  salvarProduto(produtoId, produto);
  return removidos;
}

function getEstoqueCount(produtoId, campoId) {
  const produto = getProduto(produtoId);
  if (!produto || !produto.campos[campoId]) return 0;
  const campo = produto.campos[campoId];
  return Array.isArray(campo.estoque) ? campo.estoque.length : 0;
}

// ─── CUPONS ───────────────────────────────────────────────

function getCuponsGlobais() {
  return readJSON('cupons.json');
}

function validarCupom(codigo, produtoId, valorAtual) {
  const produto = getProduto(produtoId);
  if (!produto) return { valido: false, motivo: 'Produto não encontrado' };
  const cupom = produto.cupons?.[codigo];
  if (!cupom) return { valido: false, motivo: 'Cupom inválido' };
  if (cupom.usage?.maxuses) {
    const max = parseInt(cupom.usage.maxuses);
    if (cupom.estatistics?.uses >= max) return { valido: false, motivo: 'Cupom esgotado' };
  }
  if (cupom.usage?.minvalue) {
    const min = parseFloat(cupom.usage.minvalue);
    if (valorAtual < min) return { valido: false, motivo: `Valor mínimo: R$ ${min.toFixed(2)}` };
  }
  const desconto = (cupom.porcentagem / 100) * valorAtual;
  const valorFinal = valorAtual - desconto;
  return { valido: true, desconto, valorFinal, cupom };
}

function usarCupom(produtoId, codigo, valorEconomizado) {
  const produto = getProduto(produtoId);
  if (!produto || !produto.cupons?.[codigo]) return;
  const cupom = produto.cupons[codigo];
  if (!cupom.estatistics) cupom.estatistics = { uses: 0, economizado: 0 };
  cupom.estatistics.uses = (cupom.estatistics.uses || 0) + 1;
  cupom.estatistics.economizado = (cupom.estatistics.economizado || 0) + valorEconomizado;
  salvarProduto(produtoId, produto);
}

// ─── CARRINHO ─────────────────────────────────────────────

function getCarrinhos() {
  return readJSON('carrinhos.json');
}

function getCarrinho(carrinhoId) {
  const carrinhos = getCarrinhos();
  return carrinhos[carrinhoId] || null;
}

function getCarrinhoByUser(userId) {
  const carrinhos = getCarrinhos();
  return Object.values(carrinhos).find(c => c.server?.usuario === userId && !c.finalizado);
}

function salvarCarrinho(carrinhoId, data) {
  const carrinhos = getCarrinhos();
  carrinhos[carrinhoId] = data;
  writeJSON('carrinhos.json', carrinhos);
}

function deletarCarrinho(carrinhoId) {
  const carrinhos = getCarrinhos();
  delete carrinhos[carrinhoId];
  writeJSON('carrinhos.json', carrinhos);
}

async function criarCarrinho(interaction, produtoId, campoId, nickname, email, nome, quantidade = 1) {
  const produto = getProduto(produtoId);
  if (!produto) return { erro: 'Produto não encontrado' };
  const campo = produto.campos[campoId];
  if (!campo) return { erro: 'Variante não encontrada' };
  const carrinhoId = generateID();
  const preco = parseFloat(campo.preco) * quantidade;
  const guild = interaction.guild;
  const canal = await guild.channels.create({
    name: `🛒・${nickname}`,
    type: 0,
    parent: CATEGORIA_CARRINHOS,
    permissionOverwrites: [
      { id: guild.id, deny: ['ViewChannel'] },
      { id: interaction.user.id, allow: ['ViewChannel', 'SendMessages', 'ReadMessageHistory', 'AttachFiles', 'EmbedLinks'] }
    ],
    reason: `Carrinho de ${interaction.user.tag}`
  });
  const carrinho = {
    produtoID: produtoId,
    campoID: campoId,
    carrinhoID: carrinhoId,
    server: {
      produtoURL: interaction.channel.id,
      carrinho: canal.id,
      usuario: interaction.user.id
    },
    info: {
      nickname,
      email,
      nome,
      quantidade,
      valorFinal: preco.toFixed(2),
      pagamento: {
        copiacola: null,
        txid: null,
        url: null,
        paymentID: null
      },
      cupom: {
        usado: null,
        valorAntes: null
      }
    },
    criadoEm: Date.now(),
    finalizado: false
  };
  salvarCarrinho(carrinhoId, carrinho);
  await enviarEmbedCarrinho(canal, carrinho, produto);
  setTimeout(() => expirarCarrinho(carrinhoId, canal), 600000);
  return { carrinhoId, canal };
}

async function expirarCarrinho(carrinhoId, canal) {
  const carrinho = getCarrinho(carrinhoId);
  if (!carrinho || carrinho.finalizado) return;
  deletarCarrinho(carrinhoId);
  try {
    await canal.send('⏰ **Tempo de pagamento expirado!** Carrinho cancelado automaticamente.');
    setTimeout(() => canal.delete().catch(() => {}), 10000);
  } catch {}
}

async function enviarEmbedCarrinho(canal, carrinho, produto) {
  const campo = produto.campos[carrinho.campoID];
  const vendas = readJSON('vendas.json');
  const cor = vendas.marca?.cores?.principal || '00FFFF';
  const embed = new EmbedBuilder()
    .setColor(hexToInt(cor))
    .setTitle(`🛒 ${produto.nome}`)
    .setDescription(
      `═══════════════════════════════\n` +
      `**💎 Produto:** ${produto.nome}\n` +
      `**📦 Variante:** ${campo.nome}\n` +
      `**👤 Nickname:** ${carrinho.info.nickname}\n` +
      `**📧 Email:** ${carrinho.info.email}\n` +
      `**💰 Valor:** R$ ${parseFloat(carrinho.info.valorFinal).toFixed(2)}\n` +
      (carrinho.info.cupom.usado ? `**🎫 Cupom:** ${carrinho.info.cupom.usado} (-${((1 - parseFloat(carrinho.info.valorFinal) / parseFloat(carrinho.info.cupom.valorAntes)) * 100).toFixed(0)}%)\n` : '') +
      `═══════════════════════════════`
    )
    .addFields(
      { name: '📋 Instruções', value: 'Clique em **Pagar Agora** para gerar o PIX.\nApós o pagamento, a entrega é automática!', inline: false },
      { name: '⏰ Prazo', value: '10 minutos', inline: true },
      { name: '📦 Estoque', value: 'Ilimitado', inline: true }
    )
    .setFooter({ text: 'Sparta MC • Loja', iconURL: 'https://cdn.discordapp.com/attachments/1492296145165353150/1494094525889777845/logo.png' })
    .setTimestamp();
  const rows = [
    new ActionRowBuilder().addComponents(
      new ButtonBuilder().setCustomId(`shop_pagar_${carrinho.carrinhoID}`).setLabel('Pagar Agora').setEmoji('💳').setStyle(ButtonStyle.Success),
      new ButtonBuilder().setCustomId(`shop_cupom_${carrinho.carrinhoID}`).setLabel('Cupom').setEmoji('🎫').setStyle(ButtonStyle.Secondary),
      new ButtonBuilder().setCustomId(`shop_cancelar_${carrinho.carrinhoID}`).setLabel('Cancelar').setEmoji('🗑️').setStyle(ButtonStyle.Danger)
    )
  ];
  await canal.send({ embeds: [embed], components: rows });
}

// ─── HISTORICO ─────────────────────────────────────────────

function registrarHistorico(carrinho) {
  const historicos = readJSON('historicos.json');
  const entry = JSON.parse(JSON.stringify(carrinho));
  entry.info.timestamp = formatDate();
  entry.info.reembolso = false;
  entry.finalizado = true;
  historicos[carrinho.carrinhoID] = entry;
  writeJSON('historicos.json', historicos);
}

function getHistorico() {
  return readJSON('historicos.json');
}

// ─── LOGS ──────────────────────────────────────────────────

async function sendLog(client, tipo, data) {
  const canais = readJSON(path.join(DB_BASE, '..', 'Server', 'canais.json'));
  const vendasConfig = readJSON('vendas.json');
  const cor = vendasConfig.marca?.cores?.principal || '00FFFF';
  const embed = new EmbedBuilder().setColor(hexToInt(cor)).setTimestamp();
  switch (tipo) {
    case 'pedido':
      embed.setTitle('📦 Novo Pedido').setDescription(`**Usuário:** <@${data.userId}>\n**Produto:** ${data.produto}\n**Valor:** R$ ${data.valor}`);
      break;
    case 'pagamento_criado':
      embed.setTitle('💳 Pagamento Criado').setDescription(`**Pagamento:** ${data.paymentId}\n**Valor:** R$ ${data.valor}\n**Link:** ${data.url}`);
      break;
    case 'pagamento_aprovado':
      embed.setTitle('✅ Pagamento Aprovado').setDescription(`**Pagamento:** ${data.paymentId}\n**Usuário:** <@${data.userId}>\n**Valor:** R$ ${data.valor}`).setColor(0x00FF00);
      break;
    case 'entrega':
      embed.setTitle('📬 Entrega Realizada').setDescription(`**Usuário:** <@${data.userId}>\n**Produto:** ${data.produto}\n**Itens:** ${data.itens}`).setColor(0x00FF00);
      break;
    case 'reembolso':
      embed.setTitle('🔄 Reembolso').setDescription(`**Pagamento:** ${data.paymentId}\n**Usuário:** <@${data.userId}>\n**Motivo:** ${data.motivo}`).setColor(0xFF0000);
      break;
    case 'fraude':
      embed.setTitle('🚨 Tentativa de Fraude').setDescription(`**Usuário:** <@${data.userId}>\n**Banco:** ${data.banco}\n**Pagamento:** ${data.paymentId}`).setColor(0xFF0000);
      break;
  }
  if (canais.logs && embed.data.title) {
    try {
      const ch = await client.channels.fetch(canais.logs);
      if (ch) await ch.send({ embeds: [embed] });
    } catch {}
  }
}

async function sendCompraLog(client, data) {
  const canais = readJSON(path.join(DB_BASE, '..', 'Server', 'canais.json'));
  if (!canais.vendas) return;
  try {
    const ch = await client.channels.fetch(canais.vendas);
    if (!ch) return;
    const embed = new EmbedBuilder()
      .setColor(0x00FF00)
      .setTitle('🛒 Nova Compra')
      .setDescription(
        `═══════════════════════════════\n` +
        `**👤 Nickname:** ${data.nickname}\n` +
        `**🎖️ Cargo:** <@&${data.cargoId}>\n` +
        `**🛒 Produto:** ${data.produto}\n` +
        `**💵 Valor:** R$ ${data.valor}\n` +
        `**📅 Data:** <t:${Math.floor(Date.now() / 1000)}:F>\n` +
        `═══════════════════════════════`
      )
      .setThumbnail(data.userAvatar || null)
      .setFooter({ text: 'Sparta MC • Compras', iconURL: 'https://cdn.discordapp.com/attachments/1492296145165353150/1494094525889777845/logo.png' })
      .setTimestamp();
    await ch.send({ embeds: [embed] });
  } catch {}
}

// ─── ENTREGA ────────────────────────────────────────────────

async function entregarPedido(client, carrinhoId) {
  const carrinho = getCarrinho(carrinhoId);
  if (!carrinho) return { erro: 'Carrinho não encontrado' };
  const produto = getProduto(carrinho.produtoID);
  if (!produto) return { erro: 'Produto não encontrado' };
  const campo = produto.campos[carrinho.campoID];
  if (!campo) return { erro: 'Variante não encontrada' };
  const userId = carrinho.server.usuario;
  const quantidade = carrinho.info.quantidade;
  try {
    const user = await client.users.fetch(userId);
    if (produto.entrega) {
      const tempFile = path.join(TEMP_DIR, `${carrinhoId}.txt`);
      const conteudo = `Pedido: ${produto.nome} (${carrinho.info.nickname})`;
      fs.writeFileSync(tempFile, conteudo, 'utf8');
      try {
        await user.send({ content: `✅ **${produto.nome}** adquirido com sucesso!\nNickname: ${carrinho.info.nickname}\nEmail: ${carrinho.info.email}` });
      } catch {
        const canal = await client.channels.fetch(carrinho.server.carrinho).catch(() => null);
        if (canal) {
          await canal.send({ content: `📦 **${produto.nome}** — Nickname: ${carrinho.info.nickname}` });
        }
      }
      try { fs.unlinkSync(tempFile); } catch {}
    }
    if (campo.cargos?.add?.length > 0 || campo.cargos?.rem?.length > 0) {
      const guild = client.guilds.cache.first();
      if (guild) {
        const member = await guild.members.fetch(userId).catch(() => null);
        if (member) {
          for (const roleId of campo.cargos.rem || []) {
            const role = guild.roles.cache.get(roleId);
            if (role) await member.roles.remove(role).catch(() => {});
          }
          for (const roleId of campo.cargos.add || []) {
            const role = guild.roles.cache.get(roleId);
            if (role) await member.roles.add(role).catch(() => {});
          }
          const vendasConfig = readJSON('vendas.json');
          if (vendasConfig.cargoCliente) {
            const cargoCliente = guild.roles.cache.get(vendasConfig.cargoCliente);
            if (cargoCliente) await member.roles.add(cargoCliente).catch(() => {});
          }
        }
      }
    }
    const vendas = readJSON('vendas.json');
    if (vendas.instrucoes) {
      try {
        await user.send({ content: `📝 **Instruções:** ${vendas.instrucoes}` });
      } catch {}
    }
    if (carrinho.info.cupom.usado) {
      usarCupom(carrinho.produtoID, carrinho.info.cupom.usado, parseFloat(carrinho.info.cupom.valorAntes) - parseFloat(carrinho.info.valorFinal));
    }
    registrarHistorico(carrinho);
    deletarCarrinho(carrinhoId);
    await sendLog(client, 'entrega', {
      userId,
      produto: produto.nome,
      itens: quantidade.toString()
    });
    const cargoAdd = campo.cargos?.add || [];
    await sendCompraLog(client, {
      nickname: carrinho.info.nickname,
      cargoId: cargoAdd[0] || 'N/A',
      produto: produto.nome,
      valor: carrinho.info.valorFinal,
      userAvatar: user.displayAvatarURL()
    });
    return { sucesso: true, produto: produto.nome };
  } catch (error) {
    return { erro: error.message };
  }
}

// ─── HANDLERS DE INTERACAO ───────────────────────────────

async function handleProductSelect(interaction) {
  if (interaction.customId !== 'shop_selecionar_produto') return;
  const produtoKey = interaction.values[0];
  const produtos = getProdutos();
  const produto = produtos[produtoKey];
  if (!produto) {
    return interaction.reply({ content: '❌ Produto inválido!', flags: MessageFlags.Ephemeral });
  }
  const campoIds = Object.keys(produto.campos);
  if (campoIds.length === 0) {
    return interaction.reply({ content: '❌ Produto sem variantes disponíveis.', flags: MessageFlags.Ephemeral });
  }
  if (campoIds.length === 1) {
    return mostrarModalCompra(interaction, produtoKey, campoIds[0]);
  }
  const select = new StringSelectMenuBuilder()
    .setCustomId(`shop_variante_${produtoKey}`)
    .setPlaceholder('Selecione a variante desejada')
    .addOptions(
      campoIds.map(id => {
        const campo = produto.campos[id];
        return new StringSelectMenuOptionBuilder()
          .setLabel(campo.nome)
          .setValue(id)
          .setDescription(`R$ ${parseFloat(campo.preco).toFixed(2)} — ${campo.desc || produto.nome}`);
      })
    );
  const row = new ActionRowBuilder().addComponents(select);
  await interaction.reply({
    content: `**${produto.nome}** — Selecione a variante:`,
    components: [row],
    flags: MessageFlags.Ephemeral
  });
}

async function handleVariantSelect(interaction) {
  if (!interaction.customId.startsWith('shop_variante_')) return;
  const produtoId = interaction.customId.replace('shop_variante_', '');
  const campoId = interaction.values[0];
  return mostrarModalCompra(interaction, produtoId, campoId);
}

async function mostrarModalCompra(interaction, produtoId, campoId) {
  try {
    const produto = getProduto(produtoId);
    await interaction.showModal({
      title: `🛒 ${produto.nome}`,
      customId: `shop_comprar_modal_${produtoId}_${campoId}`,
      components: [
        { type: 1, components: [{ type: 4, customId: 'compra_nick', label: 'Nickname do Jogador', style: 1, placeholder: 'Ex: Spartan_2025', required: true, minLength: 2, maxLength: 30 }] },
        { type: 1, components: [{ type: 4, customId: 'compra_email', label: 'Email', style: 1, placeholder: 'seu@email.com', required: true, minLength: 5, maxLength: 100 }] },
        { type: 1, components: [{ type: 4, customId: 'compra_nome', label: 'Nome Completo', style: 1, placeholder: 'Seu nome', required: true, minLength: 3, maxLength: 100 }] }
      ]
    });
  } catch (error) {
    console.error('❌ Erro ao mostrar modal de compra:', error);
    if (interaction.replied || interaction.deferred) {
      await interaction.editReply({ content: `❌ Erro: ${error.message}` }).catch(() => {});
    } else {
      await interaction.reply({ content: `❌ Erro: ${error.message}`, flags: MessageFlags.Ephemeral }).catch(() => {});
    }
  }
}

async function handleCompraSubmit(interaction) {
  if (!interaction.customId.startsWith('shop_comprar_modal_')) return;
  try {
    await interaction.deferReply({ flags: MessageFlags.Ephemeral });
    const rest = interaction.customId.replace('shop_comprar_modal_', '');
    const sep = rest.lastIndexOf('_');
    const produtoId = rest.substring(0, sep);
    const campoId = rest.substring(sep + 1);
    const nickname = interaction.fields.getTextInputValue('compra_nick').trim();
    const email = interaction.fields.getTextInputValue('compra_email').trim();
    const nome = interaction.fields.getTextInputValue('compra_nome').trim();
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      return interaction.editReply({ content: '❌ Email inválido. Digite um email válido.' });
    }
    const result = await criarCarrinho(interaction, produtoId, campoId, nickname, email, nome, 1);
    if (result.erro) {
      return interaction.editReply({ content: `❌ ${result.erro}` });
    }
    await interaction.editReply({ content: `✅ Carrinho criado! Vá para o canal <#${result.canal.id}> para finalizar a compra.` });
  } catch (error) {
    console.error('❌ Erro handleCompraSubmit:', error);
    if (interaction.deferred || interaction.replied) {
      await interaction.editReply({ content: `❌ Erro: ${error.message}` }).catch(() => {});
    } else {
      await interaction.reply({ content: `❌ Erro: ${error.message}`, flags: MessageFlags.Ephemeral }).catch(() => {});
    }
  }
}

async function handlePagar(interaction) {
  if (!interaction.customId.startsWith('shop_pagar_')) return;
  const carrinhoId = interaction.customId.replace('shop_pagar_', '');
  const carrinho = getCarrinho(carrinhoId);
  if (!carrinho) {
    return interaction.reply({ content: '❌ Carrinho não encontrado ou já expirou.', flags: MessageFlags.Ephemeral });
  }
  if (carrinho.server.usuario !== interaction.user.id) {
    return interaction.reply({ content: '❌ Este carrinho não pertence a você.', flags: MessageFlags.Ephemeral });
  }
  try {
    await interaction.deferReply({ flags: MessageFlags.Ephemeral });
    const produto = getProduto(carrinho.produtoID);
    const payment = await criarPagamentoMP(
      parseFloat(carrinho.info.valorFinal),
      `${produto.nome} — ${interaction.user.tag}`,
      `${interaction.user.id}|${carrinho.carrinhoID}`
    );
    carrinho.info.pagamento.paymentID = payment.id.toString();
    carrinho.info.pagamento.copiacola = payment.point_of_interaction?.transaction_data?.qr_code || null;
    carrinho.info.pagamento.url = payment.point_of_interaction?.transaction_data?.ticket_url || null;
    carrinho.info.pagamento.txid = payment.id.toString();
    salvarCarrinho(carrinhoId, carrinho);
    const vendas = readJSON('vendas.json');
    const cor = vendas.marca?.cores?.principal || '00FFFF';
    const pixCode = payment.point_of_interaction?.transaction_data?.qr_code || 'Código PIX indisponível';
    const qrCodeBase64 = payment.point_of_interaction?.transaction_data?.qr_code_base64 || null;
    const ticketUrl = payment.point_of_interaction?.transaction_data?.ticket_url || null;
    const embed = new EmbedBuilder()
      .setColor(hexToInt(cor))
      .setTitle('💳 Pagamento PIX')
      .setDescription(
        `═══════════════════════════════\n` +
        `**🛒 Produto:** ${produto.nome}\n` +
        `**💰 Valor:** R$ ${parseFloat(carrinho.info.valorFinal).toFixed(2)}\n` +
        `**👤 Comprador:** ${interaction.user}\n` +
        `═══════════════════════════════\n\n` +
        `**📋 PIX Copia e Cola:**\n\`\`\`${pixCode}\`\`\`\n` +
        `> *Pague o PIX acima usando o app do seu banco.*\n` +
        `> *Após o pagamento, a entrega será feita automaticamente!*`
      )
      .setFooter({ text: 'Aguardando pagamento... • Sparta MC', iconURL: 'https://cdn.discordapp.com/attachments/1492296145165353150/1494094525889777845/logo.png' })
      .setTimestamp();
    const files = [];
    if (qrCodeBase64) {
      const buffer = Buffer.from(qrCodeBase64, 'base64');
      const attachment = new AttachmentBuilder(buffer, { name: 'qrcode.png' });
      embed.setImage('attachment://qrcode.png');
      files.push(attachment);
    }
    const rows = [
      new ActionRowBuilder().addComponents(
        new ButtonBuilder().setCustomId(`shop_copiarpix_${carrinho.carrinhoID}`).setLabel('Copiar Código PIX').setEmoji('📋').setStyle(ButtonStyle.Primary),
        ...(ticketUrl ? [new ButtonBuilder().setLabel('Abrir no App').setURL(ticketUrl).setEmoji('🔗').setStyle(ButtonStyle.Link)] : [])
      )
    ];
    await interaction.editReply({ embeds: [embed], components: rows, files });
    await sendLog(interaction.client, 'pagamento_criado', {
      paymentId: payment.id,
      valor: carrinho.info.valorFinal,
      url: payment.point_of_interaction?.transaction_data?.ticket_url
    });
    verificarPagamentoLoop(interaction.client, carrinhoId, payment.id.toString(), interaction.user.id);
  } catch (error) {
    console.error('❌ Erro no pagamento:', error);
    if (interaction.deferred || interaction.replied) {
      await interaction.editReply({ content: `❌ Erro ao processar pagamento: ${error.message}` }).catch(() => {});
    } else {
      await interaction.reply({ content: `❌ Erro ao processar pagamento: ${error.message}`, flags: MessageFlags.Ephemeral }).catch(() => {});
    }
  }
}

async function verificarPagamentoLoop(client, carrinhoId, paymentId, userId) {
  const maxTentativas = 120;
  let tentativas = 0;
  const check = async () => {
    const carrinho = getCarrinho(carrinhoId);
    if (!carrinho || carrinho.finalizado) return;
    if (tentativas >= maxTentativas) {
      const canal = await client.channels.fetch(carrinho.server.carrinho).catch(() => null);
      if (canal) {
        await canal.send('⏰ **Tempo de pagamento expirado!**');
        setTimeout(() => canal.delete().catch(() => {}), 10000);
      }
      deletarCarrinho(carrinhoId);
      return;
    }
    tentativas++;
    try {
      const payment = await verificarPagamentoMP(paymentId);
      if (!payment) {
        setTimeout(check, 5000);
        return;
      }
      if (payment.status === 'approved') {
        await sendLog(client, 'pagamento_aprovado', { paymentId, userId, valor: carrinho.info.valorFinal });
        const entrega = await entregarPedido(client, carrinhoId);
        const canal = await client.channels.fetch(carrinho.server.carrinho).catch(() => null);
        if (canal) {
          if (entrega.sucesso) {
            await canal.send({ content: '✅ **Pagamento aprovado e entrega realizada com sucesso!**' });
          } else {
            await canal.send({ content: `✅ **Pagamento aprovado!** ${entrega.erro || 'Aguardando entrega manual.'}` });
          }
          setTimeout(() => canal.delete().catch(() => {}), 120000);
        }
        return;
      }
      if (payment.status === 'rejected' || payment.status === 'cancelled') {
        const canal = await client.channels.fetch(carrinho.server.carrinho).catch(() => null);
        if (canal) {
          await canal.send('❌ **Pagamento rejeitado ou cancelado.**');
        }
        deletarCarrinho(carrinhoId);
        return;
      }
    } catch {}
    setTimeout(check, 5000);
  };
  setTimeout(check, 5000);
}

async function handleCupom(interaction) {
  if (!interaction.customId.startsWith('shop_cupom_')) return;
  const carrinhoId = interaction.customId.replace('shop_cupom_', '');
  const carrinho = getCarrinho(carrinhoId);
  if (!carrinho) {
    return interaction.reply({ content: '❌ Carrinho não encontrado.', flags: MessageFlags.Ephemeral });
  }
  if (carrinho.server.usuario !== interaction.user.id) {
    return interaction.reply({ content: '❌ Este carrinho não pertence a você.', flags: MessageFlags.Ephemeral });
  }
  await interaction.showModal({
    title: '🎫 Aplicar Cupom',
    customId: `shop_modal_cupom_${carrinhoId}`,
    components: [{
      type: 1,
      components: [{
        type: 4,
        customId: 'cupom_codigo',
        label: 'Código do Cupom',
        style: 1,
        placeholder: 'Digite o código do cupom',
        required: true,
        minLength: 1,
        maxLength: 50
      }]
    }]
  });
}

async function handleCupomSubmit(interaction) {
  if (!interaction.customId.startsWith('shop_modal_cupom_')) return;
  const carrinhoId = interaction.customId.replace('shop_modal_cupom_', '');
  const codigo = interaction.fields.getTextInputValue('cupom_codigo').trim();
  const carrinho = getCarrinho(carrinhoId);
  if (!carrinho) {
    return interaction.reply({ content: '❌ Carrinho não encontrado.', flags: MessageFlags.Ephemeral });
  }
  const valorAtual = carrinho.info.cupom.valorAntes
    ? parseFloat(carrinho.info.cupom.valorAntes)
    : parseFloat(carrinho.info.valorFinal);
  const result = validarCupom(codigo, carrinho.produtoID, valorAtual);
  if (!result.valido) {
    return interaction.reply({ content: `❌ Cupom inválido: ${result.motivo}`, flags: MessageFlags.Ephemeral });
  }
  carrinho.info.cupom.usado = codigo;
  carrinho.info.cupom.valorAntes = valorAtual.toFixed(2);
  carrinho.info.valorFinal = result.valorFinal.toFixed(2);
  salvarCarrinho(carrinhoId, carrinho);
  const produto = getProduto(carrinho.produtoID);
  const canal = await interaction.client.channels.fetch(carrinho.server.carrinho).catch(() => null);
  if (canal) {
    await enviarEmbedCarrinho(canal, carrinho, produto);
  }
  await interaction.reply({ content: `✅ Cupom **${codigo}** aplicado! Desconto: ${result.cupom.porcentagem}% | Novo valor: R$ ${result.valorFinal.toFixed(2)}`, flags: MessageFlags.Ephemeral });
}

async function handleCopiarPix(interaction) {
  if (!interaction.customId.startsWith('shop_copiarpix_')) return;
  const carrinhoId = interaction.customId.replace('shop_copiarpix_', '');
  const carrinho = getCarrinho(carrinhoId);
  if (!carrinho || !carrinho.info.pagamento.copiacola) {
    return interaction.reply({ content: '❌ Código PIX não encontrado.', flags: MessageFlags.Ephemeral });
  }
  await interaction.reply({
    content: `📋 **Código PIX Copia e Cola:**\n\`\`\`${carrinho.info.pagamento.copiacola}\`\`\``,
    flags: MessageFlags.Ephemeral
  });
}

async function handleCancelar(interaction) {
  if (!interaction.customId.startsWith('shop_cancelar_')) return;
  const carrinhoId = interaction.customId.replace('shop_cancelar_', '');
  const carrinho = getCarrinho(carrinhoId);
  if (!carrinho) {
    return interaction.reply({ content: '❌ Carrinho não encontrado.', flags: MessageFlags.Ephemeral });
  }
  if (carrinho.server.usuario !== interaction.user.id) {
    return interaction.reply({ content: '❌ Este carrinho não pertence a você.', flags: MessageFlags.Ephemeral });
  }
  deletarCarrinho(carrinhoId);
  try {
    const canal = await interaction.client.channels.fetch(carrinho.server.carrinho).catch(() => null);
    if (canal) {
      await canal.send('🗑️ **Carrinho cancelado pelo usuário.**');
      setTimeout(() => canal.delete().catch(() => {}), 5000);
    }
  } catch {}
  await interaction.reply({ content: '✅ Carrinho cancelado.', flags: MessageFlags.Ephemeral });
}

// ─── COMANDO DELIVER ──────────────────────────────────────

async function deliverCommand(interaction, campoId, quantidade, usuario) {
  const produtos = getProdutos();
  for (const [produtoId, produto] of Object.entries(produtos)) {
    if (produto.campos[campoId]) {
      const itens = removerEstoque(produtoId, campoId, quantidade);
      if (!itens) {
        return interaction.reply({ content: '❌ Estoque insuficiente.', flags: MessageFlags.Ephemeral });
      }
      const conteudo = itens.join('\n');
      try {
        await usuario.send({ content: `📦 **${produto.nome}** — Itens entregues manualmente:\n\`\`\`${conteudo}\`\`\`` });
      } catch {
        return interaction.reply({ content: '❌ Não foi possível enviar DM para o usuário.', flags: MessageFlags.Ephemeral });
      }
      await sendLog(interaction.client, 'entrega', {
        userId: usuario.id,
        produto: produto.nome,
        itens: quantidade.toString()
      });
      return interaction.reply({ content: `✅ Entregue ${quantidade} item(ns) de **${produto.nome}** para ${usuario.tag}.`, flags: MessageFlags.Ephemeral });
    }
  }
  return interaction.reply({ content: '❌ Variante não encontrada em nenhum produto.', flags: MessageFlags.Ephemeral });
}

async function deliverAutocomplete(interaction) {
  const focused = interaction.options.getFocused();
  const produtos = getProdutos();
  const choices = [];
  for (const [, produto] of Object.entries(produtos)) {
    for (const [campoId, campo] of Object.entries(produto.campos)) {
      choices.push({ name: `${produto.nome} → ${campo.nome}`, value: campoId });
    }
  }
  const filtered = choices.filter(c => c.name.toLowerCase().includes(focused.toLowerCase())).slice(0, 25);
  await interaction.respond(filtered);
}

// ─── MODAL ADMIN ──────────────────────────────────────────

async function handleAdminModal(interaction) {
  const customId = interaction.customId;
  if (customId === 'shop_modal_criar_produto') {
    const nome = interaction.fields.getTextInputValue('produto_nome');
    const desc = interaction.fields.getTextInputValue('produto_desc');
    const entrega = interaction.fields.getTextInputValue('produto_entrega') === 'sim';
    const icon = interaction.fields.getTextInputValue('produto_icon') || null;
    const hex = interaction.fields.getTextInputValue('produto_hex') || '00FFFF';
    criarProduto(nome, desc, entrega, icon, null, hex);
    return interaction.reply({ content: `✅ Produto **${nome}** criado!`, flags: MessageFlags.Ephemeral });
  }
  if (customId === 'shop_modal_criar_campo') {
    const produtoId = interaction.fields.getTextInputValue('campo_produto_id');
    const nome = interaction.fields.getTextInputValue('campo_nome');
    const desc = interaction.fields.getTextInputValue('campo_desc');
    const preco = interaction.fields.getTextInputValue('campo_preco');
    const campoId = criarCampo(produtoId, nome, desc, preco);
    if (!campoId) return interaction.reply({ content: '❌ Produto não encontrado.', flags: MessageFlags.Ephemeral });
    return interaction.reply({ content: `✅ Campo **${nome}** criado no produto!`, flags: MessageFlags.Ephemeral });
  }
  if (customId === 'shop_modal_editar_campo') {
    const campoId = interaction.fields.getTextInputValue('edit_campo_id');
    const nome = interaction.fields.getTextInputValue('edit_campo_nome');
    const desc = interaction.fields.getTextInputValue('edit_campo_desc');
    const preco = interaction.fields.getTextInputValue('edit_campo_preco');
    const produtos = getProdutos();
    for (const [pid, p] of Object.entries(produtos)) {
      if (p.campos[campoId]) {
        atualizarCampo(pid, campoId, { nome, desc, preco });
        return interaction.reply({ content: `✅ Campo **${nome}** atualizado.`, flags: MessageFlags.Ephemeral });
      }
    }
    return interaction.reply({ content: '❌ Campo não encontrado.', flags: MessageFlags.Ephemeral });
  }
}

// ─── RENDIMENTOS ────────────────────────────────────────────

function calcularRendimentos(periodo) {
  const historicos = getHistorico();
  const vendas = Object.values(historicos).filter(h => h.finalizado && !h.info?.reembolso);
  const agora = Date.now();
  const periodos = {
    hoje: () => 86400000,
    '7d': () => 604800000,
    mes: () => 2592000000,
    total: () => Infinity
  };
  const limite = (periodos[periodo] || periodos.total)();
  const filtradas = vendas.filter(v => agora - (v.criadoEm || 0) < limite);
  const totalVendas = filtradas.length;
  const totalGanho = filtradas.reduce((acc, v) => acc + (parseFloat(v.info?.valorFinal || 0) * (v.info?.quantidade || 1)), 0);
  const totalProdutosEntregues = filtradas.reduce((acc, v) => acc + (v.info?.quantidade || 0), 0);
  return { totalVendas, totalGanho, totalProdutosEntregues };
}

module.exports = {
  getProdutos,
  getProduto,
  salvarProduto,
  deletarProduto,
  criarProduto,
  criarCampo,
  atualizarCampo,
  deletarCampo,
  adicionarEstoque,
  removerEstoque,
  getEstoqueCount,
  getCarrinhos,
  getCarrinho,
  getCarrinhoByUser,
  salvarCarrinho,
  deletarCarrinho,
  criarCarrinho,
  getCuponsGlobais,
  validarCupom,
  getHistorico,
  sendLog,
  sendCompraLog,
  handleProductSelect,
  handleVariantSelect,
  handleCompraSubmit,
  handlePagar,
  handleCupom,
  handleCupomSubmit,
  handleCancelar,
  handleCopiarPix,
  handleAdminModal,
  deliverCommand,
  deliverAutocomplete,
  calcularRendimentos,
  getAccessToken,
  criarPagamentoMP,
  verificarPagamentoMP,
  reembolsarPagamentoMP
};
