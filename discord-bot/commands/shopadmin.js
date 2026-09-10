const { SlashCommandBuilder, PermissionFlagsBits, EmbedBuilder, ActionRowBuilder, ButtonBuilder, ButtonStyle, StringSelectMenuBuilder, StringSelectMenuOptionBuilder, MessageFlags } = require('discord.js');
const shop = require('../handlers/shopHandler');
const fs = require('fs');
const path = require('path');

const self = module.exports;

self.data = new SlashCommandBuilder()
    .setName('shopadmin')
    .setDescription('Sparta » Painel administrativo da loja')
    .setDefaultMemberPermissions(PermissionFlagsBits.Administrator);

self.execute = async function(interaction) {
    if (!interaction.member.permissions.has(PermissionFlagsBits.Administrator)) {
      return interaction.reply({ content: '❌ Apenas administradores podem usar este comando!', flags: MessageFlags.Ephemeral });
    }
    await interaction.deferReply({ flags: MessageFlags.Ephemeral });
    const embed = new EmbedBuilder()
      .setColor(0x00FFFF)
      .setTitle('⚙️ Painel Administrativo — Loja')
      .setDescription('Selecione uma categoria para gerenciar:')
      .setTimestamp();
    const select = new StringSelectMenuBuilder()
      .setCustomId('shop_admin_menu')
      .setPlaceholder('Selecione uma categoria')
      .addOptions(
        new StringSelectMenuOptionBuilder().setLabel('📦 Produtos').setValue('admin_produtos').setDescription('Gerenciar produtos e variantes'),
        new StringSelectMenuOptionBuilder().setLabel('📊 Estoque').setValue('admin_estoque').setDescription('Gerenciar estoque dos produtos'),
        new StringSelectMenuOptionBuilder().setLabel('💳 Pagamentos').setValue('admin_pagamentos').setDescription('Configurar métodos de pagamento'),
        new StringSelectMenuOptionBuilder().setLabel('🎫 Cupons').setValue('admin_cupons').setDescription('Gerenciar cupons de desconto'),
        new StringSelectMenuOptionBuilder().setLabel('📈 Estatísticas').setValue('admin_estatisticas').setDescription('Relatório de vendas'),
        new StringSelectMenuOptionBuilder().setLabel('🎨 Marca').setValue('admin_marca').setDescription('Personalizar cores e logo da loja')
      );
    const row = new ActionRowBuilder().addComponents(select);
    await interaction.editReply({ embeds: [embed], components: [row] });
};

self.handleAdminMenu = async function(interaction) {
    if (interaction.customId !== 'shop_admin_menu') return;
    const value = interaction.values[0];
    await interaction.deferUpdate();
    switch (value) {
      case 'admin_produtos': return self.mostrarProdutos(interaction);
      case 'admin_estoque': return self.mostrarEstoque(interaction);
      case 'admin_pagamentos': return self.mostrarPagamentos(interaction);
      case 'admin_cupons': return self.mostrarCupons(interaction);
      case 'admin_estatisticas': return self.mostrarEstatisticas(interaction);
      case 'admin_marca': return self.mostrarMarca(interaction);
    }
};

self.mostrarProdutos = async function(interaction) {
    const produtos = shop.getProdutos();
    const entries = Object.entries(produtos);
    const embed = new EmbedBuilder()
      .setColor(0x00FFFF)
      .setTitle('📦 Produtos')
      .setDescription(entries.length === 0 ? 'Nenhum produto cadastrado.' : entries.map(([id, p], i) =>
        `**${i + 1}.** ${p.nome} — ${Object.keys(p.campos).length} variante(s)\n` +
        `🆔 \`${id}\``
      ).join('\n'))
      .setTimestamp();
    const rows = [];
    if (entries.length > 0) {
      const select = new StringSelectMenuBuilder()
        .setCustomId('shop_admin_produto')
        .setPlaceholder('Selecionar produto para editar')
        .addOptions(entries.map(([id, p]) =>
          new StringSelectMenuOptionBuilder().setLabel(p.nome).setValue(id).setDescription(`${Object.keys(p.campos).length} variante(s)`)
        ));
      rows.push(new ActionRowBuilder().addComponents(select));
    }
    rows.push(new ActionRowBuilder().addComponents(
      new ButtonBuilder().setCustomId('shop_admin_criar_produto').setLabel('Criar Produto').setEmoji('➕').setStyle(ButtonStyle.Success)
    ));
    await interaction.editReply({ embeds: [embed], components: rows });
};

self.handleAdminProduto = async function(interaction) {
    if (interaction.customId !== 'shop_admin_produto') return;
    const produtoId = interaction.values[0];
    const produto = shop.getProduto(produtoId);
    if (!produto) return interaction.reply({ content: '❌ Produto não encontrado.', flags: MessageFlags.Ephemeral });
    await interaction.deferUpdate();
    const embed = new EmbedBuilder()
      .setColor(parseInt(produto.assets?.hex || '00FFFF', 16))
      .setTitle(`📦 ${produto.nome}`)
      .setDescription(
        `**Descrição:** ${produto.desc || 'Sem descrição'}\n` +
        `**Entrega automática:** ${produto.entrega ? '✅ Sim' : '❌ Não'}\n` +
        `**Variantes:** ${Object.keys(produto.campos).length}\n` +
        `**Criado em:** <t:${produto.criadoEm}:R>`
      )
      .setTimestamp();
    const campoSelect = new StringSelectMenuBuilder()
      .setCustomId(`shop_admin_campo_${produtoId}`)
      .setPlaceholder('Gerenciar variantes')
      .addOptions(
        ...Object.entries(produto.campos).map(([id, c]) =>
          new StringSelectMenuOptionBuilder().setLabel(c.nome).setValue(id).setDescription(`R$ ${parseFloat(c.preco).toFixed(2)} — ${getEstoqueCountDisplay(produtoId, id)} em estoque`)
        )
      );
    const row1 = new ActionRowBuilder().addComponents(campoSelect);
    const row2 = new ActionRowBuilder().addComponents(
      new ButtonBuilder().setCustomId(`shop_admin_add_campo_${produtoId}`).setLabel('Adicionar Variante').setEmoji('➕').setStyle(ButtonStyle.Success),
      new ButtonBuilder().setCustomId(`shop_admin_deletar_produto_${produtoId}`).setLabel('Deletar Produto').setEmoji('🗑️').setStyle(ButtonStyle.Danger)
    );
    await interaction.editReply({ embeds: [embed], components: [row1, row2] });

    function getEstoqueCountDisplay(pid, cid) {
      const count = shop.getEstoqueCount(pid, cid);
      return `${count} item(ns)`;
    }
};

self.handleAdminCampo = async function(interaction) {
    if (!interaction.customId.startsWith('shop_admin_campo_')) return;
    const produtoId = interaction.customId.replace('shop_admin_campo_', '');
    const campoId = interaction.values[0];
    const produto = shop.getProduto(produtoId);
    if (!produto || !produto.campos[campoId]) return interaction.reply({ content: '❌ Campo não encontrado.', flags: MessageFlags.Ephemeral });
    await interaction.deferUpdate();
    const campo = produto.campos[campoId];
    const embed = new EmbedBuilder()
      .setColor(parseInt(produto.assets?.hex || '00FFFF', 16))
      .setTitle(`📦 ${produto.nome} → ${campo.nome}`)
      .setDescription(
        `**Descrição:** ${campo.desc || 'Sem descrição'}\n` +
        `**Preço:** R$ ${parseFloat(campo.preco).toFixed(2)}\n` +
        `**Estoque:** ${shop.getEstoqueCount(produtoId, campoId)} item(ns)\n` +
        `**Cargos (add):** ${campo.cargos?.add?.length || 0}\n` +
        `**Cargos (rem):** ${campo.cargos?.rem?.length || 0}`
      )
      .setTimestamp();
    const rows = [
      new ActionRowBuilder().addComponents(
        new ButtonBuilder().setCustomId(`shop_admin_edit_campo_${campoId}`).setLabel('Editar').setEmoji('✏️').setStyle(ButtonStyle.Primary),
        new ButtonBuilder().setCustomId(`shop_admin_add_estoque_${produtoId}_${campoId}`).setLabel('Adicionar Estoque').setEmoji('📦').setStyle(ButtonStyle.Success),
        new ButtonBuilder().setCustomId(`shop_admin_delete_campo_${produtoId}_${campoId}`).setLabel('Deletar').setEmoji('🗑️').setStyle(ButtonStyle.Danger)
      )
    ];
    await interaction.editReply({ embeds: [embed], components: rows });
};

self.handleAdminButtons = async function(interaction) {
    const customId = interaction.customId;

    if (customId === 'shop_admin_criar_produto') {
      return interaction.showModal({
        title: 'Criar Produto',
        customId: 'shop_modal_criar_produto',
        components: [
          { type: 1, components: [{ type: 4, customId: 'produto_nome', label: 'Nome do Produto', style: 1, placeholder: 'Ex: VIP', required: true, maxLength: 100 }] },
          { type: 1, components: [{ type: 4, customId: 'produto_desc', label: 'Descrição', style: 2, placeholder: 'Descrição do produto', required: true, maxLength: 400 }] },
          { type: 1, components: [{ type: 4, customId: 'produto_entrega', label: 'Entrega automática? (sim/nao)', style: 1, placeholder: 'sim', required: true, maxLength: 3 }] },
          { type: 1, components: [{ type: 4, customId: 'produto_icon', label: 'URL do Ícone (opcional)', style: 1, placeholder: 'https://...', required: false, maxLength: 300 }] },
          { type: 1, components: [{ type: 4, customId: 'produto_hex', label: 'Cor em HEX (opcional)', style: 1, placeholder: '00FFFF', required: false, maxLength: 6 }] }
        ]
      });
    }

    if (customId.startsWith('shop_admin_add_campo_')) {
      const produtoId = customId.replace('shop_admin_add_campo_', '');
      return interaction.showModal({
        title: 'Adicionar Variante',
        customId: 'shop_modal_criar_campo',
        components: [
          { type: 1, components: [{ type: 4, customId: 'campo_produto_id', label: 'ID do Produto', style: 1, value: produtoId, required: true }] },
          { type: 1, components: [{ type: 4, customId: 'campo_nome', label: 'Nome da Variante', style: 1, placeholder: 'Ex: VIP Mensal', required: true, maxLength: 100 }] },
          { type: 1, components: [{ type: 4, customId: 'campo_desc', label: 'Descrição', style: 2, placeholder: 'Descrição da variante', required: false, maxLength: 400 }] },
          { type: 1, components: [{ type: 4, customId: 'campo_preco', label: 'Preço', style: 1, placeholder: '29.90', required: true, maxLength: 10 }] }
        ]
      });
    }

    if (customId.startsWith('shop_admin_edit_campo_')) {
      const campoId = customId.replace('shop_admin_edit_campo_', '');
      return interaction.showModal({
        title: 'Editar Variante',
        customId: 'shop_modal_editar_campo',
        components: [
          { type: 1, components: [{ type: 4, customId: 'edit_campo_id', label: 'ID da Variante', style: 1, value: campoId, required: true }] },
          { type: 1, components: [{ type: 4, customId: 'edit_campo_nome', label: 'Nome', style: 1, placeholder: 'Novo nome', required: true, maxLength: 100 }] },
          { type: 1, components: [{ type: 4, customId: 'edit_campo_desc', label: 'Descrição', style: 2, placeholder: 'Nova descrição', required: false, maxLength: 400 }] },
          { type: 1, components: [{ type: 4, customId: 'edit_campo_preco', label: 'Preço', style: 1, placeholder: '39.90', required: true, maxLength: 10 }] }
        ]
      });
    }

    if (customId.startsWith('shop_admin_deletar_produto_')) {
      const produtoId = customId.replace('shop_admin_deletar_produto_', '');
      shop.deletarProduto(produtoId);
      await interaction.reply({ content: '✅ Produto deletado.', flags: MessageFlags.Ephemeral });
      return self.mostrarProdutos(interaction);
    }

    if (customId.startsWith('shop_admin_delete_campo_')) {
      const rest = customId.replace('shop_admin_delete_campo_', '');
      const [produtoId, campoId] = rest.split('_');
      shop.deletarCampo(produtoId, campoId);
      await interaction.reply({ content: '✅ Variante deletada.', flags: MessageFlags.Ephemeral });
      return self.mostrarProdutos(interaction);
    }

    if (customId.startsWith('shop_admin_add_estoque_')) {
      const rest = customId.replace('shop_admin_add_estoque_', '');
      const [produtoId, campoId] = rest.split('_');
      return interaction.showModal({
        title: 'Adicionar Estoque',
        customId: `shop_modal_estoque_${produtoId}_${campoId}`,
        components: [
          { type: 1, components: [{ type: 4, customId: 'estoque_itens', label: 'Itens (um por linha)', style: 2, placeholder: 'item1\nitem2\nitem3', required: true, maxLength: 2000 }] }
        ]
      });
    }

    // ---- PAGAMENTOS ----
    if (customId === 'shop_admin_config_mp') {
      return interaction.showModal({
        title: 'Configurar Mercado Pago',
        customId: 'shop_modal_mp',
        components: [
          { type: 1, components: [{ type: 4, customId: 'mp_access_token', label: 'Access Token', style: 1, placeholder: 'APP_USR-...', required: true, maxLength: 200 }] }
        ]
      });
    }

    if (customId.startsWith('shop_admin_toggle_mp')) {
      const pagamentos = JSON.parse(fs.readFileSync(path.join(__dirname, '..', 'Database', 'Vendas', 'pagamentos.json'), 'utf8'));
      pagamentos.mercadopago.habilitado = !pagamentos.mercadopago.habilitado;
      fs.writeFileSync(path.join(__dirname, '..', 'Database', 'Vendas', 'pagamentos.json'), JSON.stringify(pagamentos, null, 2));
      await interaction.reply({ content: `✅ Mercado Pago ${pagamentos.mercadopago.habilitado ? 'habilitado' : 'desabilitado'}.`, flags: MessageFlags.Ephemeral });
      return self.mostrarPagamentos(interaction);
    }

    // ---- CUPONS ----
    if (customId.startsWith('shop_admin_cupom_produto_')) {
      const produtoId = customId.replace('shop_admin_cupom_produto_', '');
      return interaction.showModal({
        title: 'Criar Cupom',
        customId: `shop_modal_cupom_produto_${produtoId}`,
        components: [
          { type: 1, components: [{ type: 4, customId: 'cupom_codigo', label: 'Código do Cupom', style: 1, placeholder: 'DESCONTO10', required: true, maxLength: 30 }] },
          { type: 1, components: [{ type: 4, customId: 'cupom_porcentagem', label: 'Desconto (%)', style: 1, placeholder: '10', required: true, maxLength: 3 }] },
          { type: 1, components: [{ type: 4, customId: 'cupom_minvalue', label: 'Valor mínimo (opcional)', style: 1, placeholder: '50', required: false, maxLength: 10 }] },
          { type: 1, components: [{ type: 4, customId: 'cupom_maxuses', label: 'Usos máximos (opcional)', style: 1, placeholder: '100', required: false, maxLength: 5 }] }
        ]
      });
    }

    // ---- MARCA ----
    if (customId === 'shop_admin_salvar_marca') {
      return interaction.showModal({
        title: 'Personalizar Marca',
        customId: 'shop_modal_marca',
        components: [
          { type: 1, components: [{ type: 4, customId: 'marca_cor_principal', label: 'Cor Principal (HEX)', style: 1, placeholder: '00FFFF', required: true, maxLength: 6 }] },
          { type: 1, components: [{ type: 4, customId: 'marca_cor_sec', label: 'Cor Secundária (HEX)', style: 1, placeholder: '000000', required: true, maxLength: 6 }] },
          { type: 1, components: [{ type: 4, customId: 'marca_url', label: 'URL da Logo', style: 1, placeholder: 'https://...', required: false, maxLength: 300 }] },
          { type: 1, components: [{ type: 4, customId: 'marca_instrucoes', label: 'Instruções pós-compra', style: 2, placeholder: 'Obrigado pela compra!', required: false, maxLength: 500 }] }
        ]
      });
    }
};

self.handleAdminModal = async function(interaction) {
    const customId = interaction.customId;

    if (customId.startsWith('shop_modal_estoque_')) {
      const rest = customId.replace('shop_modal_estoque_', '');
      const [produtoId, campoId] = rest.split('_');
      const texto = interaction.fields.getTextInputValue('estoque_itens');
      const itens = texto.split('\n').map(s => s.trim()).filter(s => s);
      shop.adicionarEstoque(produtoId, campoId, itens);
      return interaction.reply({ content: `✅ ${itens.length} item(ns) adicionados ao estoque.`, flags: MessageFlags.Ephemeral });
    }

    if (customId === 'shop_modal_mp') {
      const token = interaction.fields.getTextInputValue('mp_access_token');
      const pagamentos = JSON.parse(fs.readFileSync(path.join(__dirname, '..', 'Database', 'Vendas', 'pagamentos.json'), 'utf8'));
      pagamentos.mercadopago.access_token = token;
      pagamentos.mercadopago.configurado = true;
      pagamentos.mercadopago.habilitado = true;
      fs.writeFileSync(path.join(__dirname, '..', 'Database', 'Vendas', 'pagamentos.json'), JSON.stringify(pagamentos, null, 2));
      return interaction.reply({ content: '✅ Mercado Pago configurado!', flags: MessageFlags.Ephemeral });
    }

    if (customId.startsWith('shop_modal_cupom_produto_')) {
      const produtoId = customId.replace('shop_modal_cupom_produto_', '');
      const codigo = interaction.fields.getTextInputValue('cupom_codigo').toUpperCase();
      const porcentagem = parseFloat(interaction.fields.getTextInputValue('cupom_porcentagem'));
      const minvalue = interaction.fields.getTextInputValue('cupom_minvalue') || '';
      const maxuses = interaction.fields.getTextInputValue('cupom_maxuses') || '';
      if (isNaN(porcentagem) || porcentagem <= 0 || porcentagem > 100) {
        return interaction.reply({ content: '❌ Percentual inválido (1-100).', flags: MessageFlags.Ephemeral });
      }
      const produto = shop.getProduto(produtoId);
      if (!produto) return interaction.reply({ content: '❌ Produto não encontrado.', flags: MessageFlags.Ephemeral });
      if (!produto.cupons) produto.cupons = {};
      produto.cupons[codigo] = {
        nome: codigo,
        porcentagem,
        usage: {
          minvalue,
          maxuses
        },
        estatistics: { uses: 0, economizado: 0 }
      };
      shop.salvarProduto(produtoId, produto);
      return interaction.reply({ content: `✅ Cupom **${codigo}** criado (${porcentagem}% de desconto).`, flags: MessageFlags.Ephemeral });
    }

    if (customId === 'shop_modal_marca') {
      const corPrincipal = interaction.fields.getTextInputValue('marca_cor_principal').replace('#', '');
      const corSec = interaction.fields.getTextInputValue('marca_cor_sec').replace('#', '');
      const url = interaction.fields.getTextInputValue('marca_url');
      const instrucoes = interaction.fields.getTextInputValue('marca_instrucoes');
      const vendas = JSON.parse(fs.readFileSync(path.join(__dirname, '..', 'Database', 'Vendas', 'vendas.json'), 'utf8'));
      vendas.marca.cores.principal = corPrincipal;
      vendas.marca.cores.sec = corSec;
      if (url) vendas.marca.url = url;
      if (instrucoes) vendas.instrucoes = instrucoes;
      fs.writeFileSync(path.join(__dirname, '..', 'Database', 'Vendas', 'vendas.json'), JSON.stringify(vendas, null, 2));
      return interaction.reply({ content: '✅ Marca atualizada!', flags: MessageFlags.Ephemeral });
    }
};

self.mostrarEstoque = async function(interaction) {
    const produtos = shop.getProdutos();
    const embed = new EmbedBuilder()
      .setColor(0x00FFFF)
      .setTitle('📊 Estoque')
      .setDescription(Object.entries(produtos).map(([id, p]) =>
        `**${p.nome}**:\n` + Object.entries(p.campos).map(([cid, c]) =>
          `└ ${c.nome}: ${shop.getEstoqueCount(id, cid)} item(ns)`
        ).join('\n')
      ).join('\n') || 'Nenhum produto.')
      .setTimestamp();
    await interaction.editReply({ embeds: [embed] });
};

self.mostrarPagamentos = async function(interaction) {
    const pagamentos = JSON.parse(fs.readFileSync(path.join(__dirname, '..', 'Database', 'Vendas', 'pagamentos.json'), 'utf8'));
    const mp = pagamentos.mercadopago;
    const embed = new EmbedBuilder()
      .setColor(0x00FFFF)
      .setTitle('💳 Pagamentos')
      .setDescription(
        `**Mercado Pago**\n` +
        `Status: ${mp.habilitado ? '✅ Habilitado' : '❌ Desabilitado'}\n` +
        `Configurado: ${mp.configurado ? '✅ Sim' : '❌ Não'}\n` +
        `Token: ${mp.access_token ? '✅ Configurado' : '❌ Não configurado'}`
      )
      .setTimestamp();
    const rows = [
      new ActionRowBuilder().addComponents(
        new ButtonBuilder().setCustomId('shop_admin_config_mp').setLabel('Configurar MP').setEmoji('⚙️').setStyle(ButtonStyle.Primary),
        new ButtonBuilder().setCustomId('shop_admin_toggle_mp').setLabel(mp.habilitado ? 'Desabilitar MP' : 'Habilitar MP').setEmoji('🔄').setStyle(ButtonStyle.Secondary)
      )
    ];
    await interaction.editReply({ embeds: [embed], components: rows });
};

self.mostrarCupons = async function(interaction) {
    const produtos = shop.getProdutos();
    const embed = new EmbedBuilder()
      .setColor(0x00FFFF)
      .setTitle('🎫 Cupons')
      .setTimestamp();
    let desc = '';
    for (const [id, p] of Object.entries(produtos)) {
      if (p.cupons && Object.keys(p.cupons).length > 0) {
        desc += `**${p.nome}**:\n`;
        for (const [codigo, c] of Object.entries(p.cupons)) {
          desc += `└ **${codigo}** — ${c.porcentagem}% | Usos: ${c.estatistics?.uses || 0}`;
          if (c.usage?.maxuses) desc += `/${c.usage.maxuses}`;
          desc += '\n';
        }
      }
    }
    if (!desc) desc = 'Nenhum cupom cadastrado.';
    embed.setDescription(desc);
    const select = new StringSelectMenuBuilder()
      .setCustomId('shop_admin_cupom_produto_select')
      .setPlaceholder('Criar cupom em um produto')
      .addOptions(Object.entries(produtos).map(([id, p]) =>
        new StringSelectMenuOptionBuilder().setLabel(p.nome).setValue(id).setDescription(`Criar cupom para ${p.nome}`)
      ));
    await interaction.editReply({ embeds: [embed], components: [new ActionRowBuilder().addComponents(select)] });
};

self.handleCupomProdutoSelect = async function(interaction) {
    if (interaction.customId !== 'shop_admin_cupom_produto_select') return;
    const produtoId = interaction.values[0];
    await interaction.deferUpdate();
    return interaction.showModal({
      title: 'Criar Cupom',
      customId: `shop_modal_cupom_produto_${produtoId}`,
      components: [
        { type: 1, components: [{ type: 4, customId: 'cupom_codigo', label: 'Código do Cupom', style: 1, placeholder: 'DESCONTO10', required: true, maxLength: 30 }] },
        { type: 1, components: [{ type: 4, customId: 'cupom_porcentagem', label: 'Desconto (%)', style: 1, placeholder: '10', required: true, maxLength: 3 }] },
        { type: 1, components: [{ type: 4, customId: 'cupom_minvalue', label: 'Valor mínimo (opcional)', style: 1, placeholder: '50', required: false, maxLength: 10 }] },
        { type: 1, components: [{ type: 4, customId: 'cupom_maxuses', label: 'Usos máximos (opcional)', style: 1, placeholder: '100', required: false, maxLength: 5 }] }
      ]
    });
};

self.mostrarEstatisticas = async function(interaction) {
    const hoje = shop.calcularRendimentos('hoje');
    const seteDias = shop.calcularRendimentos('7d');
    const mes = shop.calcularRendimentos('mes');
    const total = shop.calcularRendimentos('total');
    const embed = new EmbedBuilder()
      .setColor(0x00FFFF)
      .setTitle('📈 Estatísticas de Vendas')
      .setDescription(
        `**Hoje**\n` +
        `Vendas: ${hoje.totalVendas} | Ganho: R$ ${hoje.totalGanho.toFixed(2)} | Itens: ${hoje.totalProdutosEntregues}\n\n` +
        `**Últimos 7 Dias**\n` +
        `Vendas: ${seteDias.totalVendas} | Ganho: R$ ${seteDias.totalGanho.toFixed(2)} | Itens: ${seteDias.totalProdutosEntregues}\n\n` +
        `**Último Mês**\n` +
        `Vendas: ${mes.totalVendas} | Ganho: R$ ${mes.totalGanho.toFixed(2)} | Itens: ${mes.totalProdutosEntregues}\n\n` +
        `**Total**\n` +
        `Vendas: ${total.totalVendas} | Ganho: R$ ${total.totalGanho.toFixed(2)} | Itens: ${total.totalProdutosEntregues}`
      )
      .setTimestamp();
    await interaction.editReply({ embeds: [embed] });
};

self.mostrarMarca = async function(interaction) {
    const vendas = JSON.parse(fs.readFileSync(path.join(__dirname, '..', 'Database', 'Vendas', 'vendas.json'), 'utf8'));
    const embed = new EmbedBuilder()
      .setColor(parseInt(vendas.marca.cores.principal || '00FFFF', 16))
      .setTitle('🎨 Marca da Loja')
      .setDescription(
        `**Cor Principal:** #${vendas.marca.cores.principal || '00FFFF'}\n` +
        `**Cor Secundária:** #${vendas.marca.cores.sec || '000000'}\n` +
        `**Logo:** ${vendas.marca.url ? '[Link](' + vendas.marca.url + ')' : 'Não configurada'}\n` +
        `**Instruções:** ${vendas.instrucoes || 'Não configuradas'}`
      )
      .setTimestamp();
    if (vendas.marca.url) embed.setThumbnail(vendas.marca.url);
    const rows = [
      new ActionRowBuilder().addComponents(
        new ButtonBuilder().setCustomId('shop_admin_salvar_marca').setLabel('Editar Marca').setEmoji('✏️').setStyle(ButtonStyle.Primary)
      )
    ];
    await interaction.editReply({ embeds: [embed], components: [rows] });
};
