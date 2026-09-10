const { Client, GatewayIntentBits, Collection, ActivityType } = require('discord.js');
const { joinVoiceChannel, createAudioPlayer, createAudioResource, AudioPlayerStatus, VoiceConnectionStatus, entersState } = require('@discordjs/voice');
const fs = require('fs');
const path = require('path');

// Carregar .env manualmente (sem dependência externa)
try {
    const envPath = path.join(__dirname, '.env');
    if (fs.existsSync(envPath)) {
        const envContent = fs.readFileSync(envPath, 'utf8');
        for (const line of envContent.split('\n')) {
            const trimmed = line.trim();
            if (trimmed && !trimmed.startsWith('#')) {
                const eqIdx = trimmed.indexOf('=');
                if (eqIdx > 0) {
                    const key = trimmed.substring(0, eqIdx).trim();
                    let val = trimmed.substring(eqIdx + 1).trim();
                    if ((val.startsWith('"') && val.endsWith('"')) || (val.startsWith("'") && val.endsWith("'"))) {
                        val = val.slice(1, -1);
                    }
                    if (!process.env[key]) process.env[key] = val;
                }
            }
        }
    }
} catch (e) {}

const config = require('./config.json');
config.token = config.token || process.env.DISCORD_TOKEN;

// ============================================
// CONFIGURAÇÕES INICIAIS
// ============================================

const client = new Client({ 
    intents: [
        GatewayIntentBits.Guilds,
        GatewayIntentBits.GuildMessages,
        GatewayIntentBits.MessageContent,
        GatewayIntentBits.GuildMembers,
        GatewayIntentBits.GuildWebhooks,
        GatewayIntentBits.GuildVoiceStates
    ] 
});

client.commands = new Collection();

// ID da call de rádio
const RADIO_CHANNEL_ID = '1500407881848197283';

let ultimoNumJogadores = 0;
let servidorOnline = false;

// Configurações da rádio
let connection = null;
let player = null;
let currentRadioIndex = 0;
let isPlaying = false;
let reconnectAttempts = 0;
let connectionMethod = 0;
const MAX_RECONNECT_ATTEMPTS = 5;

const radioStations = [
    { name: 'Rádio 1', url: 'http://streaming.jovempan.com.br:80/stream.mp3' },
    { name: 'Rádio 2', url: 'http://antena1.newradio.it:80/stream' }
];

// ============================================
// FUNÇÕES DA RÁDIO
// ============================================

async function entrarNaCall() {
    try {
        const channel = await client.channels.fetch(RADIO_CHANNEL_ID);
        if (!channel?.isVoiceBased()) {
            console.log('❌ Canal de rádio não é um canal de voz!');
            return false;
        }

        if (connection) {
            try { connection.destroy(); } catch (e) {}
        }

        console.log(`🎵 Conectando ao canal: ${channel.name}...`);

        const connectionOptions = {
            channelId: RADIO_CHANNEL_ID,
            guildId: channel.guild.id,
            adapterCreator: channel.guild.voiceAdapterCreator,
            selfDeaf: false,
            selfMute: false
        };

        connection = joinVoiceChannel(connectionOptions);

        connection.on(VoiceConnectionStatus.Ready, () => {
            console.log('✅ Conexão de voz estabelecida!');
            setTimeout(() => tocarRadio(), 2000);
        });

        connection.on(VoiceConnectionStatus.Disconnected, async () => {
            reconnectAttempts++;
            if (reconnectAttempts > MAX_RECONNECT_ATTEMPTS) {
                console.log('❌ Máximo de tentativas de reconexão atingido. Parando rádio.');
                return;
            }
            const delay = Math.min(5000 * reconnectAttempts, 60000);
            console.log(`⚠️ Conexão perdida, reconectando... (tentativa ${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS}, delay: ${delay}ms)`);
            setTimeout(entrarNaCall, delay);
        });

        return true;
    } catch (error) {
        console.error('❌ Erro:', error.message);
        return false;
    }
}

async function tocarRadio() {
    if (!connection || connection.state.status !== VoiceConnectionStatus.Ready) return;

    try {
        if (player) player.stop();

        player = createAudioPlayer();
        const station = radioStations[currentRadioIndex];
        currentRadioIndex = (currentRadioIndex + 1) % radioStations.length;

        console.log(`📻 Tocando: ${station.name}`);

        const resource = createAudioResource(station.url);
        player.play(resource);
        connection.subscribe(player);

        player.on(AudioPlayerStatus.Playing, () => {
            isPlaying = true;
            client.user.setActivity(`🎵 Rádio`, { type: ActivityType.Listening });
        });

        player.on(AudioPlayerStatus.Idle, () => setTimeout(() => tocarRadio(), 3000));
        player.on('error', () => setTimeout(tocarRadio, 5000));

    } catch (error) {
        console.error('❌ Erro:', error.message);
        setTimeout(tocarRadio, 10000);
    }
}

async function pararRadio() {
    if (player) { player.stop(); player = null; }
    if (connection) { connection.destroy(); connection = null; }
    isPlaying = false;
    console.log('⏹️ Rádio parada');
}

// ============================================
// CARREGAR COMANDOS
// ============================================

const commandsPath = path.join(__dirname, 'commands');
const commandFiles = fs.readdirSync(commandsPath).filter(file => file.endsWith('.js'));

const commands = [];

for (const file of commandFiles) {
    const filePath = path.join(commandsPath, file);
    const command = require(filePath);
    
    if ('data' in command && 'execute' in command) {
        client.commands.set(command.data.name, command);
        commands.push(command.data.toJSON());
        console.log(`✅ Comando carregado: ${command.data.name}`);
    } else {
        console.log(`⚠️ Comando ${file} está faltando propriedades obrigatórias.`);
    }
}

// ============================================
// FUNÇÕES DE STATUS
// ============================================

async function buscarNumJogadores() {
    try {
        console.log('🔍 Buscando status...');
        const response = await fetch('https://api.mcsrvstat.us/2/spartamc.com.br');
        const data = await response.json();
        
        if (data.online) {
            servidorOnline = true;
            return data.players?.online || 0;
        } else {
            servidorOnline = false;
            return 0;
        }
    } catch (error) {
        servidorOnline = false;
        return null;
    }
}

async function atualizarStatus() {
    if (isPlaying) return;
    
    const jogadores = await buscarNumJogadores();
    if (jogadores !== null) {
        ultimoNumJogadores = jogadores;
        let texto = !servidorOnline ? '0 Jogadores online!' : 
                   jogadores === 1 ? '1 Jogador online!' : `${jogadores} Jogadores online!`;
        
        client.user.setActivity(texto, { type: ActivityType.Watching });
        console.log(`✅ Status: ${texto}`);
    } else {
        client.user.setActivity('🔴 Offline', { type: ActivityType.Watching });
    }
}

// ============================================
// EVENTO READY
// ============================================

client.once('ready', async () => {
    console.log(`✅ Bot online como ${client.user.tag}`);
    
    await atualizarStatus();
    setTimeout(async () => {
        console.log('🎵 Iniciando rádio...');
        await entrarNaCall();
    }, 5000);
    
    setInterval(atualizarStatus, 30000);
});

// ============================================
// CARREGAR EVENTOS
// ============================================

const eventsPath = path.join(__dirname, 'events');
const eventFiles = fs.readdirSync(eventsPath).filter(file => file.endsWith('.js'));

for (const file of eventFiles) {
    const filePath = path.join(eventsPath, file);
    const event = require(filePath);
    if (event.once) {
        client.once(event.name, (...args) => event.execute(...args, client));
    } else {
        client.on(event.name, (...args) => event.execute(...args, client));
    }
    console.log(`✅ Evento carregado: ${event.name}`);
}

// ============================================
// TRATAMENTO DE ENCERRAMENTO
// ============================================

process.on('SIGINT', async () => {
    console.log('\n🛑 Encerrando bot...');
    await pararRadio();
    client.destroy();
    process.exit(0);
});

process.on('SIGTERM', async () => {
    console.log('\n🛑 Encerrando bot...');
    await pararRadio();
    client.destroy();
    process.exit(0);
});

// Nota: process.removeAllListeners('warning') foi removido pois escondia avisos importantes do Node.js

// ============================================
// TRATAMENTO DE ERROS GLOBAIS
// ============================================

process.on('unhandledRejection', (reason, promise) => {
    console.error('❌ Unhandled Rejection:', reason?.message || reason);
});

process.on('uncaughtException', (err) => {
    console.error('❌ Uncaught Exception:', err?.message || err);
});

// ============================================
// LOGIN DO BOT
// ============================================

if (!config.token) {
    console.error('❌ Token do bot não configurado! Defina DISCORD_TOKEN no .env ou config.json');
    process.exit(1);
}
client.login(config.token);
