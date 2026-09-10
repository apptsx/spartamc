const mysql = require('mysql2/promise');
const fs = require('fs');
const path = require('path');

let pool = null;

function loadConfig() {
    try {
        const configPath = path.join(__dirname, '..', 'database.json');
        if (fs.existsSync(configPath)) {
            return JSON.parse(fs.readFileSync(configPath, 'utf8'));
        }
    } catch (error) {
        console.error('Erro ao carregar database.json:', error);
    }
    return null;
}

function createPool() {
    if (pool) return pool;
    
    const config = loadConfig();
    if (!config) {
        throw new Error('Configuração do banco não encontrada');
    }
    
    pool = mysql.createPool({
        host: config.host,
        port: config.port,
        user: config.user,
        password: config.password,
        database: config.database,
        waitForConnections: true,
        connectionLimit: 10,
        queueLimit: 0
    });
    
    console.log('✅ Pool MySQL criado');
    return pool;
}

async function query(sql, params = []) {
    const poolInstance = createPool();
    try {
        const [rows] = await poolInstance.execute(sql, params);
        return rows;
    } catch (error) {
        console.error('Erro na query:', error.message);
        throw error;
    }
}

async function queryOne(sql, params = []) {
    const rows = await query(sql, params);
    return rows.length > 0 ? rows[0] : null;
}

async function closePool() {
    if (pool) {
        await pool.end();
        pool = null;
        console.log('Pool MySQL fechado');
    }
}

async function createConnection() {
    return createPool();
}

module.exports = {
    createPool,
    createConnection,
    query,
    queryOne,
    closePool
};
