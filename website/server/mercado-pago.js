const MP_CLIENT_ID = process.env.MP_CLIENT_ID || '';
const MP_CLIENT_SECRET = process.env.MP_CLIENT_SECRET || '';

let mpAccessToken = null;
let tokenExpiresAt = 0;

async function getAccessToken() {
    if (mpAccessToken && Date.now() < tokenExpiresAt) return mpAccessToken;

    const response = await fetch('https://api.mercadopago.com/oauth/token', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            client_id: MP_CLIENT_ID,
            client_secret: MP_CLIENT_SECRET,
            grant_type: 'client_credentials'
        })
    });

    if (!response.ok) {
        const err = await response.text();
        throw new Error('Erro autenticando no MP: ' + err);
    }

    const data = await response.json();
    mpAccessToken = data.access_token;
    tokenExpiresAt = Date.now() + (data.expires_in * 1000) - 60000;
    return mpAccessToken;
}

async function criarPagamentoPIX(valor, descricao, externalRef, email) {
    const token = await getAccessToken();
    const crypto = require('crypto');
    const idempotencyKey = crypto.randomUUID();

    const response = await fetch('https://api.mercadopago.com/v1/payments', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json',
            'X-Idempotency-Key': idempotencyKey
        },
        body: JSON.stringify({
            transaction_amount: parseFloat(valor),
            description: descricao,
            payment_method_id: 'pix',
            payer: { email: email || 'comprador@spartamc.com.br' },
            external_reference: externalRef,
            notification_url: process.env.MP_NOTIFICATION_URL || null
        })
    });

    if (!response.ok) {
        const err = await response.text();
        throw new Error('Erro ao criar pagamento PIX: ' + err);
    }

    return await response.json();
}

async function verificarPagamento(paymentId) {
    const token = await getAccessToken();
    const response = await fetch('https://api.mercadopago.com/v1/payments/' + paymentId, {
        headers: { 'Authorization': 'Bearer ' + token }
    });

    if (!response.ok) return null;
    return await response.json();
}

module.exports = { criarPagamentoPIX, verificarPagamento, getAccessToken };
