require('dotenv').config();
const express = require('express');
const cors = require('cors');
const path = require('path');
const fetch = (...args) => import('node-fetch').then(m => m.default(...args));
const { initDatabase } = require('./database');
const SpartaAPI = require('./api');

const app = express();
app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, '..')));

// Rotas sem .html — ex: /forum → /forum.html
app.get('/:page', (req, res, next) => {
    const page = req.params.page;
    if (page.includes('.') || page.startsWith('api')) return next();
    const file = path.join(__dirname, '..', page + '.html');
    res.sendFile(file, err => { if (err) next(); });
});

// ============ AUTH MIDDLEWARE ============

function authenticate(req, res, next) {
    const authHeader = req.headers.authorization;
    if (!authHeader) return res.status(401).json({ error: 'Não autorizado' });
    try {
        const token = authHeader.split(' ')[1];
        const decoded = JSON.parse(Buffer.from(token, 'base64').toString());
        req.userId = decoded.id;
        next();
    } catch (e) {
        return res.status(401).json({ error: 'Token inválido' });
    }
}

// ============ USUÁRIOS ============

app.post('/api/register', async (req, res) => {
    const { nickname, email, password } = req.body;
    if (!nickname || !email || !password)
        return res.status(400).json({ error: 'Preencha todos os campos' });
    if (password.length < 4)
        return res.status(400).json({ error: 'Senha deve ter pelo menos 4 caracteres' });

    const result = await SpartaAPI.register(nickname, email, password);
    if (result.error) return res.status(400).json(result);
    res.json(result);
});

app.post('/api/login', async (req, res) => {
    const { email, password } = req.body;
    const result = await SpartaAPI.login(email, password);
    if (result.error) return res.status(401).json(result);
    res.json(result);
});

app.get('/api/user/me', authenticate, async (req, res) => {
    const result = await SpartaAPI.getUser(req.userId);
    if (result.error) return res.status(404).json(result);
    res.json(result);
});

app.put('/api/user/profile', authenticate, async (req, res) => {
    const result = await SpartaAPI.updateProfile(req.userId, req.body);
    if (result.error) return res.status(400).json(result);
    res.json(result);
});

// ============ FÓRUM - CATEGORIAS ============

const STAFF_RANKS = ['laranja', 'admin', 'mod+', 'mod', 'trial', 'helper'];
function isStaff(rank) { return STAFF_RANKS.includes(rank); }
function isLaranja(rank) { return ['laranja', 'admin'].includes(rank); }

// Listar categorias
app.get('/api/forum/categories', async (req, res) => {
    const pool = require('./database').getPool();
    if (!pool) return res.json([]);
    const [rows] = await pool.query('SELECT * FROM forum_categories ORDER BY pinned DESC, sort_order ASC');
    res.json(rows);
});

// Criar categoria
app.post('/api/forum/categories', authenticate, async (req, res) => {
    const pool = require('./database').getPool();
    const [u] = await pool.query('SELECT `rank` FROM users WHERE id = ?', [req.userId]);
    if (!u.length || !isLaranja(u[0].rank)) return res.status(403).json({ error: 'Sem permissão' });

    const { slug, title, description, icon, can_create } = req.body;
    if (!slug || !title) return res.status(400).json({ error: 'slug e title obrigatórios' });

    const [r] = await pool.query(
        'INSERT INTO forum_categories (slug, title, description, icon, can_create) VALUES (?,?,?,?,?)',
        [slug, title, description || '', icon || 'fa-comments', can_create ? 1 : 0]
    );
    res.json({ success: true, id: r.insertId });
});

// Editar categoria
app.put('/api/forum/categories/:slug', authenticate, async (req, res) => {
    const pool = require('./database').getPool();
    const [u] = await pool.query('SELECT `rank` FROM users WHERE id = ?', [req.userId]);
    if (!u.length || !isLaranja(u[0].rank)) return res.status(403).json({ error: 'Sem permissão' });

    const { title, description, icon, can_create, locked, pinned, sort_order } = req.body;
    const fields = [], vals = [];
    if (title       !== undefined) { fields.push('title=?');       vals.push(title); }
    if (description !== undefined) { fields.push('description=?'); vals.push(description); }
    if (icon        !== undefined) { fields.push('icon=?');        vals.push(icon); }
    if (can_create  !== undefined) { fields.push('can_create=?');  vals.push(can_create ? 1 : 0); }
    if (locked      !== undefined) { fields.push('locked=?');      vals.push(locked ? 1 : 0); }
    if (pinned      !== undefined) { fields.push('pinned=?');      vals.push(pinned ? 1 : 0); }
    if (sort_order  !== undefined) { fields.push('sort_order=?');  vals.push(sort_order); }
    if (!fields.length) return res.status(400).json({ error: 'Nada para atualizar' });

    vals.push(req.params.slug);
    await pool.query(`UPDATE forum_categories SET ${fields.join(',')} WHERE slug=?`, vals);
    res.json({ success: true });
});

// Excluir categoria
app.delete('/api/forum/categories/:slug', authenticate, async (req, res) => {
    const pool = require('./database').getPool();
    const [u] = await pool.query('SELECT `rank` FROM users WHERE id = ?', [req.userId]);
    if (!u.length || !isLaranja(u[0].rank)) return res.status(403).json({ error: 'Sem permissão' });

    await pool.query('DELETE FROM forum_topics WHERE category = ?', [req.params.slug]);
    await pool.query('DELETE FROM forum_categories WHERE slug = ?', [req.params.slug]);
    res.json({ success: true });
});

// ============ FÓRUM - TÓPICOS ============

// Listar tópicos (com filtro opcional por categoria)
app.get('/api/topics', async (req, res) => {
    const topics = await SpartaAPI.getTopics(req.query.category || null);
    res.json(topics);
});

// Buscar tópico + respostas
app.get('/api/topics/:id', async (req, res) => {
    const data = await SpartaAPI.getTopicById(req.params.id);
    if (!data) return res.status(404).json({ error: 'Tópico não encontrado' });
    res.json(data);
});

// Criar tópico
app.post('/api/topics', authenticate, async (req, res) => {
    const { category, title, content, pinned } = req.body;
    if (!category || !title || !content)
        return res.status(400).json({ error: 'Preencha todos os campos' });

    const result = await SpartaAPI.createTopic(req.userId, category, title, content, !!pinned);
    if (result.error) return res.status(400).json(result);
    res.json(result);
});

// Excluir tópico
app.delete('/api/topics/:id', authenticate, async (req, res) => {
    const result = await SpartaAPI.deleteTopic(req.params.id, req.userId);
    if (result.error) return res.status(403).json(result);
    res.json(result);
});

// Fixar/desafixar tópico
app.patch('/api/topics/:id/pin', authenticate, async (req, res) => {
    const result = await SpartaAPI.pinTopic(req.params.id, req.body.pinned, req.userId);
    if (result.error) return res.status(403).json(result);
    res.json(result);
});

// Trancar/destrancar tópico
app.patch('/api/topics/:id/lock', authenticate, async (req, res) => {
    const pool = require('./database').getPool();
    const [u] = await pool.query('SELECT `rank` FROM users WHERE id = ?', [req.userId]);
    if (!u.length || !isStaff(u[0].rank)) return res.status(403).json({ error: 'Sem permissão' });

    await pool.query('UPDATE forum_topics SET locked = ? WHERE id = ?', [req.body.locked ? 1 : 0, req.params.id]);
    res.json({ success: true });
});

// Editar tópico
app.put('/api/topics/:id', authenticate, async (req, res) => {
    const pool = require('./database').getPool();
    const [topics] = await pool.query('SELECT author FROM forum_topics WHERE id = ?', [req.params.id]);
    if (!topics.length) return res.status(404).json({ error: 'Tópico não encontrado' });

    const [u] = await pool.query('SELECT nickname, `rank` FROM users WHERE id = ?', [req.userId]);
    if (!u.length) return res.status(403).json({ error: 'Sem permissão' });

    const canEdit = isStaff(u[0].rank) || topics[0].author === u[0].nickname;
    if (!canEdit) return res.status(403).json({ error: 'Sem permissão' });

    const { title, content } = req.body;
    if (!title && !content) return res.status(400).json({ error: 'Nada para atualizar' });

    const fields = [], vals = [];
    if (title)   { fields.push('title=?');   vals.push(title); }
    if (content) { fields.push('content=?'); vals.push(content); }
    vals.push(req.params.id);
    await pool.query(`UPDATE forum_topics SET ${fields.join(',')} WHERE id = ?`, vals);
    res.json({ success: true });
});

// Responder tópico
app.post('/api/topics/:id/reply', authenticate, async (req, res) => {
    const { content } = req.body;
    if (!content) return res.status(400).json({ error: 'Conteúdo obrigatório' });

    const result = await SpartaAPI.replyToTopic(req.userId, req.params.id, content);
    if (result.error) return res.status(404).json(result);
    res.json(result);
});

// Excluir resposta
app.delete('/api/replies/:id', authenticate, async (req, res) => {
    const result = await SpartaAPI.deleteReply(req.params.id, req.userId);
    if (result.error) return res.status(403).json(result);
    res.json(result);
});

// Estatísticas do fórum
app.get('/api/forum/stats', async (req, res) => {
    const stats = await SpartaAPI.getForumStats();
    res.json(stats);
});

// ============ PEDIDOS ============

app.post('/api/orders', authenticate, async (req, res) => {
    const { items, total, discord, nickname } = req.body;
    if (!items || !total) return res.status(400).json({ error: 'Dados inválidos' });
    const result = await SpartaAPI.createOrder(req.userId, { items, total, discord, nickname });
    res.json(result);
});

app.get('/api/orders', authenticate, async (req, res) => {
    const orders = await SpartaAPI.getOrders(req.userId);
    res.json(orders);
});

// ============ DISCORD OAUTH2 ============

const DISCORD_CLIENT_ID     = process.env.DISCORD_CLIENT_ID     || '';
const DISCORD_CLIENT_SECRET = process.env.DISCORD_CLIENT_SECRET || '';

function getRedirectUri(req, type = 'settings') {
    const origin = req.headers.origin || req.headers.referer || '';
    if (origin.includes('localhost') || origin.includes('127.0.0.1')) {
        return type === 'login'
            ? 'http://localhost:3000/index.html'
            : (process.env.DISCORD_REDIRECT_URI || 'http://localhost:3000/settings.html');
    }
    return type === 'login'
        ? (process.env.DISCORD_REDIRECT_URI_PROD_LOGIN || 'https://seu-site.com/index.html')
        : (process.env.DISCORD_REDIRECT_URI_PROD      || 'https://seu-site.com/settings.html');
}

app.get('/api/discord/auth', (req, res) => {
    const state = Math.random().toString(36).substring(2, 15);
    const type  = req.query.type || 'settings';
    const redirectUri = getRedirectUri(req, type);
    const url = `https://discord.com/api/oauth2/authorize?client_id=${DISCORD_CLIENT_ID}&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code&scope=identify&state=${state}`;
    res.json({ url, state });
});

app.post('/api/discord/callback', async (req, res) => {
    const { code, type } = req.body;
    if (!code) return res.status(400).json({ error: 'Código não fornecido' });

    const redirectUri = getRedirectUri(req, type || 'settings');

    try {
        const tokenRes = await fetch('https://discord.com/api/v10/oauth2/token', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({
                client_id: DISCORD_CLIENT_ID,
                client_secret: DISCORD_CLIENT_SECRET,
                grant_type: 'authorization_code',
                code,
                redirect_uri: redirectUri
            })
        });
        const tokenData = await tokenRes.json();
        if (!tokenData.access_token) return res.status(400).json({ error: 'Falha ao obter token' });

        const userRes = await fetch('https://discord.com/api/v10/users/@me', {
            headers: { 'Authorization': `Bearer ${tokenData.access_token}` }
        });
        const discordUser = await userRes.json();

        res.json({
            success: true,
            discord: { id: discordUser.id, username: discordUser.username, avatar: discordUser.avatar }
        });
    } catch (err) {
        console.error('Discord OAuth error:', err);
        res.status(500).json({ error: 'Erro ao verificar Discord' });
    }
});

// Vincular Discord à conta logada
app.post('/api/discord/link', authenticate, async (req, res) => {
    const { code } = req.body;
    if (!code) return res.status(400).json({ error: 'Código não fornecido' });

    const redirectUri = getRedirectUri(req, 'settings');

    try {
        const tokenRes = await fetch('https://discord.com/api/v10/oauth2/token', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({
                client_id: DISCORD_CLIENT_ID,
                client_secret: DISCORD_CLIENT_SECRET,
                grant_type: 'authorization_code',
                code,
                redirect_uri: redirectUri
            })
        });
        const tokenData = await tokenRes.json();
        if (!tokenData.access_token) return res.status(400).json({ error: 'Falha ao obter token Discord' });

        const userRes = await fetch('https://discord.com/api/v10/users/@me', {
            headers: { 'Authorization': `Bearer ${tokenData.access_token}` }
        });
        const d = await userRes.json();

        // Salvar discord_id no MySQL
        const result = await SpartaAPI.updateProfile(req.userId, { discord_id: d.id });
        if (result.error) return res.status(400).json(result);

        res.json({
            success: true,
            discord: {
                id: d.id,
                username: d.username,
                global_name: d.global_name || d.username,
                avatar: d.avatar,
                banner: d.banner,
                banner_color: d.banner_color,
                flags: d.flags || 0,
                premium_type: d.premium_type || 0
            }
        });
    } catch (err) {
        console.error('Discord link error:', err);
        res.status(500).json({ error: 'Erro ao vincular Discord' });
    }
});

// Login via Discord (conta já vinculada)
app.post('/api/discord/login', async (req, res) => {
    const { code } = req.body;
    if (!code) return res.status(400).json({ error: 'Código não fornecido' });

    const redirectUri = getRedirectUri(req, 'login');

    try {
        const tokenRes = await fetch('https://discord.com/api/v10/oauth2/token', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({
                client_id: DISCORD_CLIENT_ID,
                client_secret: DISCORD_CLIENT_SECRET,
                grant_type: 'authorization_code',
                code,
                redirect_uri: redirectUri
            })
        });
        const tokenData = await tokenRes.json();
        if (!tokenData.access_token) return res.status(400).json({ error: 'Falha ao obter token' });

        const userRes = await fetch('https://discord.com/api/v10/users/@me', {
            headers: { 'Authorization': `Bearer ${tokenData.access_token}` }
        });
        const discordUser = await userRes.json();

        const pool = require('./database').getPool();
        if (!pool) return res.status(500).json({ error: 'Banco indisponível' });

        const [rows] = await pool.query('SELECT * FROM users WHERE discord_id = ?', [discordUser.id]);
        if (rows.length === 0) {
            return res.status(404).json({ error: 'Nenhuma conta vinculada a este Discord. Faça login e vincule em Configurações.' });
        }

        const user = rows[0];
        const token = Buffer.from(JSON.stringify({ id: user.id })).toString('base64');

        res.json({
            success: true,
            user: { id: user.id, nickname: user.nickname, email: user.email, rank: user.rank, discord_id: user.discord_id },
            token
        });
    } catch (err) {
        console.error('Discord login error:', err);
        res.status(500).json({ error: 'Erro ao fazer login com Discord' });
    }
});

// Buscar usuários por nickname (search)
app.get('/api/users/search', async (req, res) => {
    const q = req.query.q || '';
    if (q.length < 2) return res.json([]);

    const pool = require('./database').getPool();
    if (!pool) return res.json([]);

    const [rows] = await pool.query(
        'SELECT id, nickname, `rank`, discord_id FROM users WHERE nickname LIKE ? LIMIT 10',
        ['%' + q + '%']
    );
    res.json(rows);
});

// Listar ranks disponíveis
const RANKS = ['laranja', 'helper', 'trial', 'mod', 'mod+', 'admin'];

// Setar rank de um usuário (chamado pelo bot via token interno)
app.post('/api/admin/setrank', async (req, res) => {
    const { bot_secret, nickname, rank } = req.body;

    if (bot_secret !== (process.env.BOT_SECRET || 'troque_este_secret')) {
        return res.status(403).json({ error: 'Não autorizado' });
    }
    if (!nickname || !rank) {
        return res.status(400).json({ error: 'nickname e rank são obrigatórios' });
    }
    if (!RANKS.includes(rank.toLowerCase()) && rank !== 'none') {
        return res.status(400).json({ error: `Rank inválido. Use: ${RANKS.join(', ')} ou none` });
    }

    const pool = require('./database').getPool();
    if (!pool) return res.status(500).json({ error: 'Banco indisponível' });

    const finalRank = rank === 'none' ? null : rank.toLowerCase();
    const [result] = await pool.query(
        'UPDATE users SET `rank` = ? WHERE LOWER(nickname) = LOWER(?)',
        [finalRank, nickname]
    );

    if (result.affectedRows === 0) {
        return res.status(404).json({ error: `Usuário "${nickname}" não encontrado` });
    }

    res.json({ success: true, nickname, rank: finalRank });
});

// Buscar perfil público de um usuário (por id ou nickname)
app.get('/api/users/:identifier', async (req, res) => {
    const pool = require('./database').getPool();
    if (!pool) return res.status(500).json({ error: 'Banco indisponível' });

    const id = req.params.identifier;
    const [rows] = await pool.query(
        'SELECT id, nickname, `rank`, discord_id, created_at FROM users WHERE id = ? OR LOWER(nickname) = LOWER(?)',
        [isNaN(id) ? -1 : parseInt(id), id]
    );
    if (rows.length === 0) return res.status(404).json({ error: 'Usuário não encontrado' });
    res.json(rows[0]);
});

app.get('/api/health', (req, res) => res.json({ status: 'ok', db: !!require('./database').getPool() }));

// ============ MERCADO PAGO ============

const { criarPagamentoPIX, verificarPagamento } = require('./mercado-pago');

app.post('/api/mercado-pago/create-pix', async (req, res) => {
    const { valor, descricao, externalRef, email } = req.body;
    if (!valor || !descricao) {
        return res.status(400).json({ error: 'Valor e descrição obrigatórios' });
    }
    try {
        const payment = await criarPagamentoPIX(valor, descricao, externalRef || 'SPARTA-' + Date.now(), email);
        if (payment.error) {
            return res.status(400).json({ error: payment.error });
        }
        res.json({
            success: true,
            paymentId: payment.id,
            status: payment.status,
            qrCode: payment.point_of_interaction?.transaction_data?.qr_code || null,
            qrCodeBase64: payment.point_of_interaction?.transaction_data?.qr_code_base64 || null,
            ticketUrl: payment.point_of_interaction?.transaction_data?.ticket_url || null,
            copyCode: payment.point_of_interaction?.transaction_data?.qr_code || null,
            transactionAmount: payment.transaction_amount
        });
    } catch (err) {
        console.error('MP create error:', err);
        res.status(500).json({ error: 'Erro ao criar pagamento: ' + err.message });
    }
});

app.get('/api/mercado-pago/check/:paymentId', async (req, res) => {
    try {
        const payment = await verificarPagamento(req.params.paymentId);
        if (!payment) return res.status(404).json({ error: 'Pagamento não encontrado' });
        res.json({
            success: true,
            paymentId: payment.id,
            status: payment.status,
            statusDetail: payment.status_detail,
            transactionAmount: payment.transaction_amount
        });
    } catch (err) {
        res.status(500).json({ error: 'Erro ao verificar pagamento' });
    }
});

// Webhook para notificações do Mercado Pago
app.post('/api/mercado-pago/notify', async (req, res) => {
    const { type, data } = req.body;
    if (type === 'payment' && data?.id) {
        try {
            const payment = await verificarPagamento(data.id);
            if (payment && payment.status === 'approved') {
                console.log('✅ Pagamento aprovado:', data.id, '| Ref:', payment.external_reference);
                // Aqui você pode integrar com a entrega automática
            }
        } catch (err) {
            console.error('Webhook MP error:', err);
        }
    }
    res.status(200).send('OK');
});

// ============ START ============

const PORT = process.env.PORT || 3000;

initDatabase().then(() => {
    // Iniciar bot do Discord como processo separado
    try {
        const { spawn } = require('child_process');
        const bot = spawn(process.execPath, ['discord-bot.js'], {
            cwd: __dirname,
            env: process.env,
            stdio: 'inherit',
            detached: false
        });
        bot.on('error', e => console.warn('⚠️  Bot erro:', e.message));
        bot.on('exit', code => { if (code !== 0) console.warn('⚠️  Bot encerrado com código', code); });
        console.log('🤖 Bot Discord iniciado (PID ' + bot.pid + ')');
    } catch (e) {
        console.warn('⚠️  Bot Discord não iniciado:', e.message);
    }

    app.listen(PORT, () => console.log(`🚀 Servidor rodando na porta ${PORT}`));
});

module.exports = app;
