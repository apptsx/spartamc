const { MongoClient, ObjectId } = require('mongodb');
const bcrypt = require('bcryptjs');

// Fetch polyfill for Vercel Node.js
const http = require('http');
const https = require('https');

function fetch(url, options = {}) {
  return new Promise((resolve, reject) => {
    const protocol = url.startsWith('https') ? https : http;
    const urlObj = new URL(url);
    
    const opts = {
      hostname: urlObj.hostname,
      port: urlObj.port || (url.startsWith('https') ? 443 : 80),
      path: urlObj.pathname + urlObj.search,
      method: options.method || 'GET',
      headers: options.headers || {}
    };
    
    const req = protocol.request(opts, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        resolve({
          ok: res.statusCode >= 200 && res.statusCode < 300,
          status: res.statusCode,
          json: () => Promise.resolve(JSON.parse(data))
        });
      });
    });
    
    req.on('error', reject);
    if (options.body) req.write(options.body);
    req.end();
  });
}

let cachedClient = null;
let cachedDb = null;

async function connectDB() {
  if (cachedDb) {
    return cachedDb;
  }

  const client = new MongoClient(process.env.MONGODB_URI);
  await client.connect();
  cachedClient = client;
  cachedDb = client.db();
  
  console.log('Conectado ao MongoDB');
  return cachedDb;
}

class SpartaAPI {
  
  static async register(nickname, email, password) {
    const db = await connectDB();
    const users = db.collection('users');
    
    const emailExists = await users.findOne({ email });
    if (emailExists) {
      return { error: 'E-mail já cadastrado' };
    }
    
    const nickExists = await users.findOne({ nicknameLower: nickname.toLowerCase() });
    if (nickExists) {
      return { error: 'Nickname já cadastrado' };
    }
    
    const hashedPassword = await bcrypt.hash(password, 10);
    
    const userData = {
      id: Date.now().toString(),
      nickname,
      nicknameLower: nickname.toLowerCase(),
      email,
      password: hashedPassword,
      discord: '',
      createdAt: new Date()
    };
    
    await users.insertOne(userData);
    
    const token = Buffer.from(JSON.stringify({ id: userData.id })).toString('base64');
    
    return {
      success: true,
      user: { id: userData.id, nickname: userData.nickname, email: userData.email },
      token
    };
  }
  
  static async login(email, password) {
    const db = await connectDB();
    const users = db.collection('users');
    
    const user = await users.findOne({ email });
    
    if (!user) {
      return { error: 'E-mail ou senha incorretos' };
    }
    
    const validPassword = await bcrypt.compare(password, user.password);
    if (!validPassword) {
      return { error: 'E-mail ou senha incorretos' };
    }
    
    const token = Buffer.from(JSON.stringify({ id: user.id })).toString('base64');
    
    return {
      success: true,
      user: { id: user.id, nickname: user.nickname, email: user.email, discord: user.discord || '' },
      token
    };
  }
  
  static async getUser(userId) {
    const db = await connectDB();
    const users = db.collection('users');
    
    const user = await users.findOne({ id: userId });
    if (!user) {
      return { error: 'Usuário não encontrado' };
    }
    return { user };
  }
  
  static async updateProfile(userId, data) {
    const db = await connectDB();
    const users = db.collection('users');
    
    const user = await users.findOne({ id: userId });
    if (!user) {
      return { error: 'Usuário não encontrado' };
    }
    
    const updateData = {};
    
    if (data.nickname) {
      const nickExists = await users.findOne({ 
        nicknameLower: data.nickname.toLowerCase(),
        id: { $ne: userId }
      });
      if (nickExists) {
        return { error: 'Nickname já em uso' };
      }
      updateData.nickname = data.nickname;
      updateData.nicknameLower = data.nickname.toLowerCase();
    }
    
    if (data.email) {
      const emailExists = await users.findOne({ 
        email: data.email,
        id: { $ne: userId }
      });
      if (emailExists) {
        return { error: 'E-mail já em uso' };
      }
      updateData.email = data.email;
    }
    
    if (data.discord !== undefined) {
      updateData.discord = data.discord;
    }
    
    if (data.password) {
      updateData.password = await bcrypt.hash(data.password, 10);
    }
    
    await users.updateOne({ id: userId }, { $set: updateData });
    
    const updated = await users.findOne({ id: userId });
    return { success: true, user: updated };
  }
  
  static async getTopics(category = null) {
    const db = await connectDB();
    const topics = db.collection('topics');
    
    let query = topics.find({});
    const allTopics = await query.sort({ createdAt: -1 }).toArray();
    
    if (category) {
      return allTopics.filter(t => t.category === category);
    }
    
    return allTopics.sort((a, b) => {
      if (a.pinned && !b.pinned) return -1;
      if (!a.pinned && b.pinned) return 1;
      return 0;
    });
  }
  
  static async createTopic(userId, category, title, content) {
    const db = await connectDB();
    const topics = db.collection('topics');
    const users = db.collection('users');
    
    const user = await users.findOne({ id: userId });
    if (!user) {
      return { error: 'Usuário não encontrado' };
    }
    
    const topicData = {
      id: Date.now().toString(),
      category,
      title,
      content,
      author: user.nickname,
      authorId: userId,
      createdAt: new Date(),
      pinned: false,
      repliesCount: 0,
      replies: []
    };
    
    await topics.insertOne(topicData);
    
    return { success: true, topic: topicData };
  }
  
  static async replyToTopic(userId, topicId, content) {
    const db = await connectDB();
    const topics = db.collection('topics');
    const users = db.collection('users');
    
    const topic = await topics.findOne({ id: topicId });
    if (!topic) {
      return { error: 'Tópico não encontrado' };
    }
    
    const user = await users.findOne({ id: userId });
    if (!user) {
      return { error: 'Usuário não encontrado' };
    }
    
    const reply = {
      id: Date.now().toString(),
      content,
      author: user.nickname,
      authorId: userId,
      createdAt: new Date()
    };
    
    await topics.updateOne(
      { id: topicId },
      { 
        $push: { replies: reply },
        $inc: { repliesCount: 1 }
      }
    );
    
    return { success: true, reply };
  }
  
  static async getForumStats() {
    const db = await connectDB();
    const topics = db.collection('topics');
    
    const allTopics = await topics.find({}).toArray();
    
    const stats = {};
    const categories = ['anuncios', 'regras', 'estatisticas', 'revisao', 'atendimento', 'denuncias', 'bugs', 'criador'];
    
    categories.forEach(cat => {
      stats[cat] = allTopics.filter(t => t.category === cat).length;
    });
    
    return stats;
  }
  
  static async createOrder(userId, data) {
    const db = await connectDB();
    const orders = db.collection('orders');
    const users = db.collection('users');
    
    const user = await users.findOne({ id: userId });
    if (!user) {
      return { error: 'Usuário não encontrado' };
    }
    
    const orderData = {
      id: Date.now().toString(),
      userId,
      discord: data.discord || user.discord || '',
      nickname: data.nickname || user.nickname,
      items: data.items,
      total: data.total,
      status: 'pending',
      paymentMethod: 'pix',
      createdAt: new Date()
    };
    
    await orders.insertOne(orderData);
    
    return { success: true, order: orderData };
  }
  
  static async getOrders(userId) {
    const db = await connectDB();
    const orders = db.collection('orders');
    
    return await orders.find({ userId }).sort({ createdAt: -1 }).toArray();
  }
}

// Express app
const express = require('express');
const cors = require('cors');
const app = express();

app.use(cors());
app.use(express.json());

function authenticate(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader) {
    return res.status(401).json({ error: 'Não autorizado' });
  }
  
  try {
    const token = authHeader.split(' ')[1];
    const decoded = JSON.parse(Buffer.from(token, 'base64').toString());
    req.userId = decoded.id;
    next();
  } catch (e) {
    return res.status(401).json({ error: 'Token inválido' });
  }
}

app.post('/api/register', async (req, res) => {
  const { nickname, email, password } = req.body;
  if (!nickname || !email || !password) {
    return res.status(400).json({ error: 'Preencha todos os campos' });
  }
  if (password.length < 4) {
    return res.status(400).json({ error: 'Senha deve ter pelo menos 4 caracteres' });
  }
  
  const result = await SpartaAPI.register(nickname, email, password);
  
  if (result.error) {
    return res.status(400).json(result);
  }
  res.json(result);
});

app.post('/api/login', async (req, res) => {
  const { email, password } = req.body;
  const result = await SpartaAPI.login(email, password);
  
  if (result.error) {
    return res.status(401).json(result);
  }
  res.json(result);
});

app.put('/api/user/profile', authenticate, async (req, res) => {
  const result = await SpartaAPI.updateProfile(req.userId, req.body);
  
  if (result.error) {
    return res.status(400).json(result);
  }
  res.json(result);
});

app.get('/api/user/me', authenticate, async (req, res) => {
  const result = await SpartaAPI.getUser(req.userId);
  
  if (result.error) {
    return res.status(404).json(result);
  }
  res.json(result);
});

app.get('/api/topics', async (req, res) => {
  const topics = await SpartaAPI.getTopics(req.query.category);
  res.json(topics);
});

app.post('/api/topics', authenticate, async (req, res) => {
  const { category, title, content } = req.body;
  
  if (!category || !title || !content) {
    return res.status(400).json({ error: 'Preencha todos os campos' });
  }
  
  const result = await SpartaAPI.createTopic(req.userId, category, title, content);
  res.json(result);
});

app.post('/api/topics/:id/reply', authenticate, async (req, res) => {
  const { content } = req.body;
  
  if (!content) {
    return res.status(400).json({ error: 'Conteúdo obrigatório' });
  }
  
  const result = await SpartaAPI.replyToTopic(req.userId, req.params.id, content);
  
  if (result.error) {
    return res.status(404).json(result);
  }
  res.json(result);
});

app.get('/api/forum/stats', async (req, res) => {
  const stats = await SpartaAPI.getForumStats();
  res.json(stats);
});

app.post('/api/orders', authenticate, async (req, res) => {
  const { items, total, discord, nickname } = req.body;
  
  if (!items || !total) {
    return res.status(400).json({ error: 'Dados inválidos' });
  }
  
  const result = await SpartaAPI.createOrder(req.userId, { items, total, discord, nickname });
  res.json(result);
});

app.get('/api/orders', authenticate, async (req, res) => {
  const orders = await SpartaAPI.getOrders(req.userId);
  res.json(orders);
});

app.get('/api/health', (req, res) => {
  res.json({ status: 'ok' });
});

// ============ DISCORD OAUTH2 ============

const DISCORD_CLIENT_ID = process.env.DISCORD_CLIENT_ID || '';
const DISCORD_CLIENT_SECRET = process.env.DISCORD_CLIENT_SECRET || '';
const DISCORD_REDIRECT_URI_PROD = process.env.DISCORD_REDIRECT_URI_PROD || 'https://seu-site.com/discord-callback.html';
const DISCORD_REDIRECT_URI_LOGIN = process.env.DISCORD_REDIRECT_URI_LOGIN || 'https://seu-site.com/index.html';

console.log('=== Server Starting ===');
console.log('CLIENT_ID:', DISCORD_CLIENT_ID ? 'set' : 'missing');
console.log('CLIENT_SECRET:', DISCORD_CLIENT_SECRET ? 'set' : 'missing');

function getRedirectUri(req, type = 'settings') {
  const origin = req.headers.origin || '';
  if (origin.includes('localhost')) {
    return type === 'login' ? 'http://localhost:3000/index.html' : 'http://localhost:3000/settings.html';
  }
  return type === 'login' ? DISCORD_REDIRECT_URI_LOGIN : DISCORD_REDIRECT_URI_PROD;
}

app.get('/api/discord/auth', (req, res) => {
  const state = Math.random().toString(36).substring(2, 15);
  const scopes = ['identify'];
  const type = req.query.type || 'settings';
  const redirectUri = getRedirectUri(req, type);
  
  const url = `https://discord.com/api/oauth2/authorize?client_id=${DISCORD_CLIENT_ID}&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code&scope=${encodeURIComponent(scopes.join(' '))}&state=${state}`;
  
  res.json({ url, state });
});

app.post('/api/discord/callback', async (req, res) => {
  const { code, type } = req.body;
  
  console.log('=== DISCORD CALLBACK ===');
  console.log('Code received:', !!code);
  
  if (!code) {
    return res.status(400).json({ error: 'Código não fornecido' });
  }
  
  const redirectUri = getRedirectUri(req, type || 'settings');
  console.log('Using redirect URI:', redirectUri);
  
  try {
    // Step 1: Exchange authorization code for access token
    const tokenResponse = await fetch('https://discord.com/api/v10/oauth2/token', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': 'Basic ' + Buffer.from(DISCORD_CLIENT_ID + ':' + DISCORD_CLIENT_SECRET).toString('base64')
      },
      body: new URLSearchParams({
        'grant_type': 'authorization_code',
        'code': code,
        'redirect_uri': redirectUri
      })
    });
    
    const tokenData = await tokenResponse.json();
    console.log('Token response:', tokenData);
    
    if (!tokenData.access_token) {
      console.log('No access token received');
      return res.status(400).json({ error: 'Falha ao obter token', details: tokenData });
    }
    
    // Step 2: Get user info from Discord
    const userResponse = await fetch('https://discord.com/api/v10/users/@me', {
      headers: {
        'Authorization': 'Bearer ' + tokenData.access_token
      }
    });
    
    const discordUser = await userResponse.json();
    console.log('Discord user data:', JSON.stringify(discordUser));
    
    // Get username - check global_name first (new Discord accounts), then username
    const username = discordUser.global_name || discordUser.username || 'Usuario';
    const discriminator = discordUser.discriminator || '0';
    const avatar = discordUser.avatar;
    const id = discordUser.id;
    
    console.log('Final values - ID:', id, 'Username:', username, 'Discrim:', discriminator);
    
    res.json({
      success: true,
      discord: {
        id: id,
        username: username,
        discriminator: discriminator,
        avatar: avatar
      }
    });
    
  } catch (error) {
    console.error('OAuth Error:', error);
    res.status(500).json({ error: 'Erro ao processar OAuth', details: error.message });
  }
});

app.post('/api/discord/login', async (req, res) => {
  const { code } = req.body;
  
  if (!code) {
    return res.status(400).json({ error: 'Código não fornecido' });
  }
  
  const redirectUri = getRedirectUri(req, 'login');
  
  try {
    const tokenResponse = await fetch('https://discord.com/api/v10/oauth2/token', {
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
    
    const tokenData = await tokenResponse.json();
    
    if (!tokenData.access_token) {
      return res.status(400).json({ error: 'Falha ao obter token' });
    }
    
    const userResponse = await fetch('https://discord.com/api/v10/users/@me', {
      headers: { 'Authorization': `Bearer ${tokenData.access_token}` }
    });
    
    const discordUser = await userResponse.json();
    
    const db = await connectDB();
    const users = db.collection('users');
    const spartaUser = await users.findOne({ discordId: discordUser.id });
    
    if (!spartaUser) {
      return res.status(404).json({ error: 'Nenhuma conta vinculada a este Discord. Faça login primeiro e vincule sua conta.' });
    }
    
    const token = Buffer.from(JSON.stringify({ id: spartaUser.id })).toString('base64');
    
    res.json({
      success: true,
      user: { id: spartaUser.id, nickname: spartaUser.nickname, email: spartaUser.email },
      token
    });
    
  } catch (error) {
    console.error('Discord login error:', error);
    res.status(500).json({ error: 'Erro ao fazer login com Discord' });
  }
});

module.exports = app;
