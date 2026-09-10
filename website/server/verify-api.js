const express = require('express');
const cors = require('cors');
const crypto = require('crypto');

const app = express();
app.use(cors());
app.use(express.json());

// Armazenamento em memória (em produção use banco de dados)
const verificationCodes = new Map();

// Webhook do Discord para verificar comandos
// Quando o bot recebe o comando, ele chama esta API

app.post('/api/discord/verify', (req, res) => {
    const { userId, code, username } = req.body;
    
    // Gerar código se não existir
    if (!verificationCodes.has(userId)) {
        const newCode = crypto.randomInt(100000, 999999).toString();
        verificationCodes.set(userId, {
            code: newCode,
            username: username,
            createdAt: Date.now(),
            expiresIn: 15 * 60 * 1000 // 15 minutos
        });
        
        return res.json({ 
            success: true, 
            code: newCode,
            message: 'Código gerado! O usuário deve inseri-lo no site.' 
        });
    }
    
    // Verificar código
    const stored = verificationCodes.get(userId);
    
    if (Date.now() > stored.createdAt + stored.expiresIn) {
        verificationCodes.delete(userId);
        return res.json({ success: false, error: 'Código expirado' });
    }
    
    if (stored.code === code) {
        verificationCodes.delete(userId);
        return res.json({ success: true, message: 'Verificação concluída!' });
    }
    
    return res.json({ success: false, error: 'Código incorreto' });
});

// Verificar status da verificação
app.get('/api/discord/check/:userId', (req, res) => {
    const { userId } = req.params;
    
    const stored = verificationCodes.get(userId);
    
    if (!stored) {
        return res.json({ verified: false, hasCode: false });
    }
    
    if (Date.now() > stored.createdAt + stored.expiresIn) {
        verificationCodes.delete(userId);
        return res.json({ verified: false, hasCode: false, expired: true });
    }
    
    return res.json({ 
        verified: false, 
        hasCode: true,
        code: stored.code,
        expiresIn: stored.createdAt + stored.expiresIn - Date.now()
    });
});

// Gerar novo código
app.post('/api/discord/generate', (req, res) => {
    const { userId, username } = req.body;
    
    const code = crypto.randomInt(100000, 999999).toString();
    verificationCodes.set(userId, {
        code: code,
        username: username,
        createdAt: Date.now(),
        expiresIn: 15 * 60 * 1000
    });
    
    res.json({ success: true, code: code });
});

// Rota para o bot do Discord chamar quando alguém usar o comando
app.post('/api/discord/command', (req, res) => {
    const { userId, action, code } = req.body;
    
    if (action === 'verify') {
        const stored = verificationCodes.get(userId);
        
        if (!stored) {
            return res.json({ success: false, message: 'Nenhum código pendente' });
        }
        
        if (stored.code !== code) {
            return res.json({ success: false, message: 'Código incorreto' });
        }
        
        // Código correto! Marcar como verificado
        verificationCodes.set(userId + '_verified', {
            verifiedAt: Date.now(),
            username: stored.username
        });
        verificationCodes.delete(userId);
        
        return res.json({ success: true, message: 'Verificação concluída!' });
    }
    
    res.json({ success: false });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Servidor de verificação rodando na porta ${PORT}`);
});

module.exports = app;
