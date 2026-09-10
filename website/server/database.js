require('dotenv').config();
const mysql = require('mysql2/promise');
const bcrypt = require('bcryptjs');

const dbConfig = {
    host:     process.env.DB_HOST || 'localhost',
    port:     parseInt(process.env.DB_PORT) || 3306,
    database: process.env.DB_NAME || 'sparta',
    user:     process.env.DB_USER || 'root',
    password: process.env.DB_PASS || '',
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0,
    charset: 'utf8mb4'
};

// Usar socket quando for localhost (MariaDB unix_socket auth)
if (dbConfig.host === 'localhost' || dbConfig.host === '127.0.0.1') {
    dbConfig.socketPath = '/run/mysqld/mysqld.sock';
    delete dbConfig.host;
    delete dbConfig.port;
}

let pool = null;

async function initDatabase() {
    try {
        // Primeiro conecta sem database para criar se não existir
        var tempOpts = {
            user: dbConfig.user,
            password: dbConfig.password
        };
        if (dbConfig.socketPath) {
            tempOpts.socketPath = '/run/mysqld/mysqld.sock';
        } else {
            tempOpts.host = dbConfig.host;
            tempOpts.port = dbConfig.port;
        }
        const tempConn = await mysql.createConnection(tempOpts);
        await tempConn.query('CREATE DATABASE IF NOT EXISTS `' + dbConfig.database + '` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci');
        await tempConn.end();

        pool = mysql.createPool(dbConfig);

        // Testar conexão
        const conn = await pool.getConnection();
        conn.release();
        console.log('✅ MySQL conectado em ' + dbConfig.host + '/' + dbConfig.database);

        await pool.query(`
            CREATE TABLE IF NOT EXISTS users (
                id          INT AUTO_INCREMENT PRIMARY KEY,
                nickname    VARCHAR(100) NOT NULL,
                email       VARCHAR(255) NOT NULL,
                password    VARCHAR(255) NOT NULL,
                discord_id  VARCHAR(50)  DEFAULT NULL,
                \`rank\`    VARCHAR(50)  DEFAULT NULL,
                created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY uq_email    (email),
                UNIQUE KEY uq_nickname (nickname)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        `);

        await pool.query(`
            CREATE TABLE IF NOT EXISTS forum_topics (
                id           INT AUTO_INCREMENT PRIMARY KEY,
                category     VARCHAR(50)  NOT NULL,
                title        VARCHAR(255) NOT NULL,
                content      TEXT         NOT NULL,
                author       VARCHAR(100) NOT NULL,
                pinned       TINYINT(1)   DEFAULT 0,
                locked       TINYINT(1)   DEFAULT 0,
                created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_category (category),
                INDEX idx_author   (author)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        `);

        // Adicionar colunas novas se não existirem
        await pool.query(`ALTER TABLE forum_topics ADD COLUMN IF NOT EXISTS locked TINYINT(1) DEFAULT 0`).catch(()=>{});

        await pool.query(`
            CREATE TABLE IF NOT EXISTS forum_categories (
                id          INT AUTO_INCREMENT PRIMARY KEY,
                slug        VARCHAR(50)  NOT NULL UNIQUE,
                title       VARCHAR(100) NOT NULL,
                description VARCHAR(255) DEFAULT '',
                icon        VARCHAR(50)  DEFAULT 'fa-comments',
                can_create  TINYINT(1)   DEFAULT 0,
                locked      TINYINT(1)   DEFAULT 0,
                pinned      TINYINT(1)   DEFAULT 0,
                sort_order  INT          DEFAULT 0,
                created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        `);

        await pool.query(`
            CREATE TABLE IF NOT EXISTS forum_replies (
                id         INT AUTO_INCREMENT PRIMARY KEY,
                topic_id   INT          NOT NULL,
                author     VARCHAR(100) NOT NULL,
                content    TEXT         NOT NULL,
                created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (topic_id) REFERENCES forum_topics(id) ON DELETE CASCADE,
                INDEX idx_topic (topic_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        `);

        // Garantir coluna rank (caso tabela já exista sem ela)
        await pool.query(`
            ALTER TABLE users ADD COLUMN IF NOT EXISTS \`rank\` VARCHAR(50) DEFAULT NULL
        `).catch(() => {}); // ignora se já existir

        // Inserir categorias padrão se não existirem
        const defaultCats = [
            ['anuncios',     'Anúncios',              'Novidades e atualizações oficiais.',    'fa-bullhorn',   0, 0, 1, 1],
            ['regras',       'Regras',                'Diretrizes da comunidade.',             'fa-gavel',      0, 0, 0, 2],
            ['estatisticas', 'Estatísticas da Equipe','Desempenho da nossa staff.',            'fa-chart-line', 0, 0, 0, 3],
            ['revisao',      'Revisão de Punição',    'Solicite uma revisão aqui.',            'fa-undo',       0, 0, 0, 4],
            ['atendimento',  'Atendimento',           'Dúvidas gerais e suporte técnico.',     'fa-headset',    1, 0, 0, 5],
            ['denuncias',    'Denúncias',             'Reporte infrações aqui.',               'fa-shield-alt', 0, 0, 0, 6],
            ['bugs',         'Bugs',                  'Reporte erros técnicos.',               'fa-bug',        0, 0, 0, 7],
            ['criador',      'Criador de Conteúdo',   'Solicitações de parceria.',             'fa-video',      1, 0, 0, 8],
        ];
        for (const [slug, title, desc, icon, can_create, locked, pinned, sort] of defaultCats) {
            await pool.query(
                'INSERT IGNORE INTO forum_categories (slug, title, description, icon, can_create, locked, pinned, sort_order) VALUES (?,?,?,?,?,?,?,?)',
                [slug, title, desc, icon, can_create, locked, pinned, sort]
            );
        }

        console.log('✅ Tabelas verificadas');
    } catch (err) {
        console.error('❌ Falha no MySQL:', err.message);
        pool = null;
    }
    return pool;
}

function getPool() { return pool; }

module.exports = { initDatabase, getPool };
