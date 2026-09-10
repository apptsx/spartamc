# Configuração do Bot Discord - SpartaMC

## 🚀 Como rodar

### 1. Instalar dependências
```bash
cd server
npm install
```

### 2. Configurar o `.env`
Copie o `.env.example` para `.env` e preencha:
```
DISCORD_TOKEN=SEU_BOT_TOKEN
DISCORD_CLIENT_ID=SEU_CLIENT_ID
DISCORD_GUILD_ID=SEU_GUILD_ID
```

### 3. Rodar o bot
```bash
npm start
```

## 📝 Variáveis de Ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `DISCORD_TOKEN` | Token do bot (Discord Developer Portal → Bot) | (obrigatório) |
| `DISCORD_CLIENT_ID` | ID da aplicação Discord | (obrigatório) |
| `DISCORD_GUILD_ID` | ID do servidor Discord | (obrigatório) |
| `BOT_SECRET` | Segredo interno bot→servidor | troque_este_secret |
| `API_URL` | URL da API do site | http://localhost:3000 |
| `PORT` | Porta do servidor | 3000 |

## 🎯 Comandos do Bot

O bot tem o comando `/verificar` que:
1. Gera um código de 6 dígitos
2. Envia via DM para o usuário
3. O usuário insere o código no site

## 🔧 Adicionar o bot ao seu servidor

Use o link de invite padrão do Discord substituindo o `client_id` pelo seu:
```
https://discord.com/oauth2/authorize?client_id=SEU_CLIENT_ID&permissions=68608&scope=bot%20applications.commands
```

## ⚠️ Notas

- O bot precisa de permissões: `Send Messages`, `Manage Messages`, `Read Messages`
- O comando `/verificar` é registrado automaticamente quando o bot inicia
- O bot armazena códigos em memória (reinicia se o bot reiniciar)