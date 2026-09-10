const bcrypt = require('bcryptjs');
const { getPool } = require('./database');

class SpartaAPI {

    // ============ USUÁRIOS ============

    static async register(nickname, email, password) {
        const pool = getPool();
        if (!pool) return { error: 'Banco de dados indisponível' };

        const [emailRows] = await pool.query('SELECT id FROM users WHERE email = ?', [email]);
        if (emailRows.length > 0) return { error: 'E-mail já cadastrado' };

        const [nickRows] = await pool.query('SELECT id FROM users WHERE LOWER(nickname) = LOWER(?)', [nickname]);
        if (nickRows.length > 0) return { error: 'Nickname já cadastrado' };

        const hash = await bcrypt.hash(password, 10);
        const [result] = await pool.query(
            'INSERT INTO users (nickname, email, password) VALUES (?, ?, ?)',
            [nickname, email, hash]
        );

        const userId = result.insertId;
        const token = Buffer.from(JSON.stringify({ id: userId })).toString('base64');

        return {
            success: true,
            user: { id: userId, nickname, email },
            token
        };
    }

    static async login(email, password) {
        const pool = getPool();
        if (!pool) return { error: 'Banco de dados indisponível' };

        const [rows] = await pool.query('SELECT * FROM users WHERE email = ?', [email]);
        if (rows.length === 0) return { error: 'E-mail ou senha incorretos' };

        const user = rows[0];
        const valid = await bcrypt.compare(password, user.password);
        if (!valid) return { error: 'E-mail ou senha incorretos' };

        const token = Buffer.from(JSON.stringify({ id: user.id })).toString('base64');

        return {
            success: true,
            user: { id: user.id, nickname: user.nickname, email: user.email, rank: user.rank, discord_id: user.discord_id },
            token
        };
    }

    static async getUser(userId) {
        const pool = getPool();
        if (!pool) return { error: 'Banco de dados indisponível' };

        const [rows] = await pool.query('SELECT id, nickname, email, `rank`, discord_id, created_at FROM users WHERE id = ?', [userId]);
        if (rows.length === 0) return { error: 'Usuário não encontrado' };
        return { user: rows[0] };
    }

    static async updateProfile(userId, data) {
        const pool = getPool();
        if (!pool) return { error: 'Banco de dados indisponível' };

        const fields = [];
        const values = [];

        if (data.nickname) {
            const [rows] = await pool.query('SELECT id FROM users WHERE LOWER(nickname) = LOWER(?) AND id != ?', [data.nickname, userId]);
            if (rows.length > 0) return { error: 'Nickname já em uso' };
            fields.push('nickname = ?'); values.push(data.nickname);
        }
        if (data.email) {
            const [rows] = await pool.query('SELECT id FROM users WHERE email = ? AND id != ?', [data.email, userId]);
            if (rows.length > 0) return { error: 'E-mail já em uso' };
            fields.push('email = ?'); values.push(data.email);
        }
        if (data.password) {
            fields.push('password = ?'); values.push(await bcrypt.hash(data.password, 10));
        }
        if (data.discord_id !== undefined) {
            fields.push('discord_id = ?'); values.push(data.discord_id);
        }

        if (fields.length === 0) return { error: 'Nenhum campo para atualizar' };

        values.push(userId);
        await pool.query(`UPDATE users SET ${fields.join(', ')} WHERE id = ?`, values);

        return this.getUser(userId);
    }

    // ============ FÓRUM ============

    static async getTopics(category = null) {
        const pool = getPool();
        if (!pool) return [];

        let sql = `
            SELECT t.*, COUNT(r.id) AS replies_count
            FROM forum_topics t
            LEFT JOIN forum_replies r ON r.topic_id = t.id
        `;
        const params = [];

        if (category) {
            sql += ' WHERE t.category = ?';
            params.push(category);
        }

        sql += ' GROUP BY t.id ORDER BY t.pinned DESC, t.created_at DESC';

        const [rows] = await pool.query(sql, params);
        return rows;
    }

    static async getTopicById(topicId) {
        const pool = getPool();
        if (!pool) return null;

        const [topics] = await pool.query('SELECT * FROM forum_topics WHERE id = ?', [topicId]);
        if (topics.length === 0) return null;

        const [replies] = await pool.query(
            'SELECT * FROM forum_replies WHERE topic_id = ? ORDER BY created_at ASC',
            [topicId]
        );

        return { topic: topics[0], replies };
    }

    static async createTopic(userId, category, title, content, pinned = false) {
        const pool = getPool();
        if (!pool) return { error: 'Banco de dados indisponível' };

        const [userRows] = await pool.query('SELECT nickname, `rank` FROM users WHERE id = ?', [userId]);
        if (userRows.length === 0) return { error: 'Usuário não encontrado' };

        const author = userRows[0].nickname;
        const canPin = ['admin', 'mod+'].includes(userRows[0].rank);

        const [result] = await pool.query(
            'INSERT INTO forum_topics (category, title, content, author, pinned) VALUES (?, ?, ?, ?, ?)',
            [category, title, content, author, pinned && canPin ? 1 : 0]
        );

        return { success: true, topic: { id: result.insertId, category, title, content, author } };
    }

    static async replyToTopic(userId, topicId, content) {
        const pool = getPool();
        if (!pool) return { error: 'Banco de dados indisponível' };

        const [topics] = await pool.query('SELECT id, locked FROM forum_topics WHERE id = ?', [topicId]);
        if (topics.length === 0) return { error: 'Tópico não encontrado' };
        if (topics[0].locked) return { error: 'Este tópico está trancado.' };

        const [userRows] = await pool.query('SELECT nickname FROM users WHERE id = ?', [userId]);
        if (userRows.length === 0) return { error: 'Usuário não encontrado' };

        const author = userRows[0].nickname;
        const [result] = await pool.query(
            'INSERT INTO forum_replies (topic_id, author, content) VALUES (?, ?, ?)',
            [topicId, author, content]
        );

        return { success: true, reply: { id: result.insertId, topic_id: topicId, author, content } };
    }

    static async deleteTopic(topicId, userId) {
        const pool = getPool();
        if (!pool) return { error: 'Banco de dados indisponível' };

        const [topics] = await pool.query('SELECT author FROM forum_topics WHERE id = ?', [topicId]);
        if (topics.length === 0) return { error: 'Tópico não encontrado' };

        const [userRows] = await pool.query('SELECT nickname, `rank` FROM users WHERE id = ?', [userId]);
        if (userRows.length === 0) return { error: 'Usuário não encontrado' };

        const user = userRows[0];
        const isAdmin = ['admin', 'mod+', 'mod'].includes(user.rank);
        if (topics[0].author !== user.nickname && !isAdmin) return { error: 'Sem permissão' };

        await pool.query('DELETE FROM forum_topics WHERE id = ?', [topicId]);
        return { success: true };
    }

    static async deleteReply(replyId, userId) {
        const pool = getPool();
        if (!pool) return { error: 'Banco de dados indisponível' };

        const [replies] = await pool.query('SELECT author FROM forum_replies WHERE id = ?', [replyId]);
        if (replies.length === 0) return { error: 'Resposta não encontrada' };

        const [userRows] = await pool.query('SELECT nickname, `rank` FROM users WHERE id = ?', [userId]);
        if (userRows.length === 0) return { error: 'Usuário não encontrado' };

        const user = userRows[0];
        const isAdmin = ['admin', 'mod+', 'mod'].includes(user.rank);
        if (replies[0].author !== user.nickname && !isAdmin) return { error: 'Sem permissão' };

        await pool.query('DELETE FROM forum_replies WHERE id = ?', [replyId]);
        return { success: true };
    }

    static async pinTopic(topicId, pinned, userId) {
        const pool = getPool();
        if (!pool) return { error: 'Banco de dados indisponível' };

        const [userRows] = await pool.query('SELECT `rank` FROM users WHERE id = ?', [userId]);
        if (userRows.length === 0) return { error: 'Usuário não encontrado' };

        if (!['laranja', 'admin', 'mod+'].includes(userRows[0].rank)) return { error: 'Sem permissão' };

        await pool.query('UPDATE forum_topics SET pinned = ? WHERE id = ?', [pinned ? 1 : 0, topicId]);
        return { success: true };
    }

    static async getForumStats() {
        const pool = getPool();
        if (!pool) return {};

        const [rows] = await pool.query(
            'SELECT category, COUNT(*) AS total FROM forum_topics GROUP BY category'
        );

        const stats = {};
        rows.forEach(r => { stats[r.category] = r.total; });
        return stats;
    }

    // ============ PEDIDOS ============

    static async createOrder(userId, data) {
        const pool = getPool();
        if (!pool) return { error: 'Banco de dados indisponível' };

        // Pedidos ainda não têm tabela — retorna sucesso simples por ora
        return { success: true, order: { userId, ...data, status: 'pending', createdAt: new Date() } };
    }

    static async getOrders(userId) {
        return [];
    }
}

module.exports = SpartaAPI;
