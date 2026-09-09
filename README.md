# Sparta — Rede de Servidores Minecraft

Conjunto de plugins Java/Kotlin que formam uma **rede de minigames** para Minecraft (proxy BungeeCord/Waterfall + servidores Spigot 1.8–1.21).

> **Open-source e configurável.** Todas as marcas (nome, site, loja, Discord, TikTok, e-mail) e segredos (banco de dados, bot do Discord, Mercado Pago) foram removidos do código e centralizados para edição — veja [Configuração](#configuração).

## Módulos

| Módulo | Tipo | Descrição |
|---|---|---|
| `core` | Biblioteca compartilhada | Contas, ranks, punições, fines, coleta de dados, cosméticos, MySQL/Redis |
| `core-bukkit` | Plugin (Spigot) | Hologramas, NPCs, menus, sidebars, leaderboards, ferramenta de worldedit (`/spartaedit`) |
| `core-bungee` | Plugin (BungeeCord) | Proxy, sincronização de punições/ranks, Discord (JDA), staff chat, votação |
| `lobby` | Plugin (Spigot) | Spawn/hub com NPCs, loja (PIX via Mercado Pago), cosméticos, parkour |
| `auth` | Plugin (Spigot) | Login/autenticação de contas |
| `bedwars` | Plugin (Spigot) | Minigame Bedwars |
| `duels` | Plugin (Spigot) | Minigame Duels |
| `pvp` | Plugin (Spigot) | Minigame PvP (arenas, lava, FPS) |

## Website

A pasta [`website/`](website/) contém o site da rede (HTML/CSS/JS + backend Node.js/Express).

- Front-end: páginas estáticas (`index.html`, `shop.html`, `forum.html`, `equipe.html`, etc.)
- Backend: `website/server/` (Express + MySQL; bot do Discord, integração Mercado Pago e OAuth2 via variáveis de ambiente)
- Configuração central: [**`website/config.json`**](website/config.json) (nome, webhooks do Discord, IDs de OAuth)
- Para rodar:
  ```bash
  cd website/server
  npm install
  cp .env.example .env   # preencha as credenciais
  npm start
  ```

## Requisitos

- **Java 21**+
- Gradle (wrapper incluso: `./gradlew`)
- **MySQL** (contas, punições, loja) e **Redis** (sincronização entre servidores)
- Jars proprietários em `./libs/` (não publicados aqui): servidor/fork **Zartema/Panda (Spigot 1.8.8)**, **SlimeWorldManager**, **ProtocolLib**, `authlib` da Mojang
- Opcional: bot do **Discord** (JDA), credenciais do **Mercado Pago** para gerar PIX

## Compilando

```bash
./gradlew clean build --no-daemon
```

Ou somente os módulos:

```bash
./gradlew :core-bungee:build :core-bukkit:build :lobby:build :pvp:build :bedwars:build :duels:build :auth:build --no-daemon
```

Os JARs são gerados em `build/libs/` de cada módulo. O script `build_servers.sh` copia cada plugin para as pastas de servidores da rede.

## Configuração

### 1. Marca do servidor — `Constant.java`

**Tudo que é visível ao jogador fica no `Constant.java`** (`core/src/main/java/com/minecraft/core/Constant.java`):
nome do servidor, cor, domínio (site), loja, Discord, TikTok, domínio de e-mail, ID dos desenvolvedores/owner.

> Edite os valores padrão direto no arquivo **ou** sobrescreva com variáveis de ambiente (precedência do ambiente é maior).

### 2. Banco de dados — `config.json`

O caminho do `config.json` é resolvido nesta ordem:
1. `-Dsparta.config.path=<caminho>` (system property)
2. `<server>/SpartaConfig/config.json` (diretório compartilhado da rede)
3. Classepath (`core/src/main/resources/config.json` — template com valores neutros)

```json
{
  "mysql": {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "",
    "database": "sparta"
  },
  "redis": {
    "host": "127.0.0.1",
    "port": 6379,
    "password": ""
  }
}
```

### 3. Variáveis de ambiente

| Variável | Descrição |
|---|---|
| `SPARTA_SERVER_NAME` | Nome do servidor exibido (padrão `Sparta`) |
| `SPARTA_SERVER_COLOR` | Cor do nome (padrão `§6§l`) |
| `SPARTA_SERVER_DOMAIN` | Domínio do site (padrão `spartamc.com.br`) |
| `SPARTA_SERVER_STORE` | URL da loja (padrão `www.<domain>/loja`) |
| `SPARTA_SERVER_DISCORD` | Convite do Discord (padrão `https://dsc.gg/...`) |
| `SPARTA_SERVER_DISCORD_GG` | Convite curto `discord.gg/...` |
| `SPARTA_SERVER_TIKTOK` | Usuário do TikTok |
| `SPARTA_SERVER_EMAIL_DOMAIN` | Domínio de e-mail (faturamento/loja) |
| `SPARTA_CONFIG_DIR` | Pasta de config compartilhada (padrão `SpartaConfig`) |
| `SPARTA_OWNER_UUID` / `SPARTA_OWNER_NAME` | Identidade do owner |
| `SPARTA_DEVELOPER_UUIDS` | UUIDs dos devs (separados por vírgula) |
| `SPARTA_DISCORD_TOKEN` | Token do bot do Discord |
| `SPARTA_DISCORD_SERVER_ID` | ID do servidor (guild) do Discord |
| `SPARTA_DISCORD_*_CHANNEL` | IDs dos canais do Discord (punishments, ranks, staff-chat, shop, reports, hub, bedwars, pvp, hungergames) |
| `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_USER` / `MYSQL_PASSWORD` / `MYSQL_DATABASE` | Conexão MySQL (fallback quando não há `config.json`) |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Conexão Redis (fallback quando não há `config.json`) |
| `MERCADO_PAGO_CLIENT_ID` / `MERCADO_PAGO_CLIENT_SECRET` | Credenciais de aplicação do Mercado Pago (PIX) |

## Estrutura do repositório

```
├── core/            # Biblioteca compartilhada
├── core-bukkit/     # Utilidades Spigot
├── core-bungee/     # Proxy BungeeCord + Discord
├── lobby/           # Hub/loja/cosméticos
├── auth/ bedwars/ duels/ pvp/  # Modos de jogo
├── libs/            # Jars de terceiros (preencha localmente)
├── gradle/ gradlew  # Build
└── Constant.java    # Edite aqui toda a marca do servidor
```

## Notas de segurança / abertura

- **Nenhum secret é publicado**: senhas de MySQL/Redis, tokens de Discord, credenciais de Mercado Pago e IPs de produção foram removidos. Você precisa configurar os seus.
- **`server/`, `worlds/` e `libs/*.jar` não são versionados** (dados de runtime, mundos e binários proprietários de terceiros).
- Identificadores internos (pacotes, comandos como `/spartaedit`, chaves de Redis, nomes de tabelas) fazem parte do código e não precisam ser alterados — apenas o conteúdo visível em `Constant.java` e nas variáveis de ambiente.

## Licença

Código disponibilizado para fins de estudo e uso da rede. Os jars de terceiros em `libs/` pertencem aos seus respectivos donos e não estão inclusos.