# Sparta MC — Bot do Discord

Bot do Discord da rede Sparta MC (tickets, loja, sistema de "creator", cargos de
equipe, rádio, verificação, punições e integração com o MySQL/loja do site).

## Como rodar

Requer **Node.js 18+** e **MySQL**.

```bash
cd discord-bot
npm install

# crie os arquivos de configuração a partir dos exemplos:
cp config.example.json config.json
cp database.example.json database.json
cp creator_config.example.json creator_config.json
cp .env.example .env

node Index.js
```

## Configuração

| Arquivo | O que define |
|---|---|
| `config.json` | Token do bot, Client ID e Guild ID do Discord |
| `database.json` | Conexão MySQL (host, porta, usuário, senha, banco) |
| `creator_config.json` | Canais e cargo do sistema "creator" (por servidor) |
| `.env` | Tokens/segredos: `DISCORD_TOKEN`, `DB_*`, `MP_*`, `SSH_*` |
| `Database/Server/canais.json` | IDs dos canais (logs, vendas, tickets, etc.) |
| `Database/Vendas/*.json` | Produtos e configuração da loja |

> **IDs de canais e cargos** (ex.: `Database/Server/canais.json`) são as
> configurações específicas do servidor Discord da rede. Ajuste os IDs para a
> sua comunidade. Os arquivos `config.json`, `database.json`, `creator_*.json`
> e `data/` são locais e não são versionados (veja `.gitignore`).

## Comandos

| Comando | Descrição |
|---|---|
| `/ticket` | Abrir ticket de suporte |
| `/criar` (creator) | Sistema de solicitação de cargo "creator" |
| `/shop` | Painel da loja com pagamento |
| `/verificar` | Verificar vínculo com o site |
| `/revogar` | Revogar acesso/punição |
| `/stafflog` | Registro de ações da staff |
| Eventos | Boas-vindas, logs de mensagem, voice, rádio, XP |

## Deploy

`deploy.sh` envia a pasta para um servidor remoto via SSH. Preencha
`SSH_HOST`, `SSH_USER`, `SSH_DEST` e `SSH_PASSWORD` por variáveis de ambiente
ou `.env` (ou use chaves SSH).

## Segurança

- Tokens, senhas de banco e endereços de servidor foram removidos do repositório
  (segredos ficam em `.env` + arquivos locais não versionados).
- Não suba `node_modules`, `bot.log`, `data/` ou `database.json` reais.

## Não versionado (runtime/local)

`node_modules/`, `.env`, `config.json`, `database.json`, `creator_*.json`,
`data/`, `bot.log`, `Database/Vendas/pagamentos.json`, `Database/Vendas/entregas/`,
`Database/Vendas/temp/`, `nnyko_data.json`.