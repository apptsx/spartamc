// rank_mappings.js - VERSÃO ATUALIZADA
// MAPEAMENTO DE RANKS REAIS (códigos -> nomes)
const RANK_CODES = {
    // ADMINISTRAÇÃO
    'erv58': 'ADMIN',
    
    // MODERAÇÃO
    'qpyem': 'MODPLUS',
    'if76n': 'MOD',
    '3fmfl': 'TRIAL',
    'hlp21': 'HELPER',
    
    // CRIADORES
    'std21': 'STUDIO',
    
    // PARTNERS
    'my2ec': 'PARTNER_PLUS',
    '3gxcg': 'PARTNER',
    
    // DESTAQUES
    'ye4o5': 'SPARTA',
    'mvvz3': 'VIP',
    'lgp26': 'MAX_PLUS',
    'hwyr2': 'MEMBER',
    'lgndp': 'MAX',
    '0bxjm': 'BETA',
    'bst26': 'BOOSTER'
};

// MAPEAMENTO DE TAGS (tabela other.tag)
const TAG_CODES = {
    // ADMINISTRAÇÃO
    'IzPLp': 'ADMIN',
    
    // MODERAÇÃO
    'CYrov': 'MODPLUS',
    'dyOYO': 'MOD',
    'XGyAp': 'TRIAL',
    'b3761': 'HELPER',
    
    // STUDIO
    'VvNPg': 'STUDIO',
    
    // PARTNERS
    'vAjST': 'PARTNER_PLUS',
    'prtn1': 'PARTNER',
    
    // DESTAQUES
    'jSBMB': 'SPARTA',
    'yDTiT': 'VIP',
    'lgp26': 'MAX_PLUS',
    'lgd26': 'MAX',
    'EalNl': 'MEMBER',
    'chp26': 'TAG_CHAMPION',
    'DxmFd': 'BETA',
    '21g47': 'TAG_2026',
    'elt26': 'TAG_CARNAVAL',
    'hlw26': 'TAG_HALLOWEEN',
    'fer26': 'TAG_FERIAS',
    'xma26': 'TAG_NATAL'
};

// MAPEAMENTO DE MEDALHAS (códigos -> símbolo, nome e cor)
const MEDAL_CODES = {
    // Medalhas padrão
    'TaAEd': { symbol: '', name: 'NONE', color: '#FFFFFF' },
    
    // TOP Rankings
    'topazul': { symbol: '<:TOP1:1494594808386293840>', name: 'TOP1', color: '#AA0000' },
    'top1': { symbol: '<:TOP1:1494594808386293840>', name: 'TOP1', color: '#AA0000' },
    'top2': { symbol: '<:TOP2:1494594809694781460>', name: 'TOP2', color: '#55FFFF' },
    'top3': { symbol: '<:TOP3:1494594811003408514>', name: 'TOP3', color: '#00AAAA' },
    'top4': { symbol: '<:TOP4:1494594812220018788>', name: 'TOP4', color: '#0000AA' },
    
    // Admin
    'admstr': { symbol: '<:ADMIN_STAR:1494594744339402852>', name: 'ADMIN_STAR', color: '#AA0000' },
    'admin1': { symbol: '<:ADMIN_CROSS:1494594742611083355>', name: 'ADMIN_CROSS', color: '#AA0000' },
    
    // Beta
    'bet26': { symbol: '<:BETA:1494594748332376144>', name: 'BETA', color: '#00AAAA' },
    
    // Staff
    'stfshd': { symbol: '<:STAFF_SHIELD:1494594796256497754>', name: 'STAFF_SHIELD', color: '#5555FF' },
    'stfcrs': { symbol: '<:STAFF_CROSS:1494594794872373300>', name: 'STAFF_CROSS', color: '#5555FF' },
    'stfwsh': { symbol: '<:STAFF_WISH:1494594798236209213>', name: 'STAFF_WISH', color: '#5555FF' },
    
    // Stars
    'star1': { symbol: '<:STAR:1494594800186560612>', name: 'STAR', color: '#FFFF55' },
    'sparkl': { symbol: '<:SPARKLE:1494594793576071188>', name: 'SPARKLE', color: '#FFFF55' },
    
    // Diamond/Crystal
    'diomnd': { symbol: '<:DIAMOND:1494594763301584906>', name: 'DIAMOND', color: '#55FFFF' },
    'crystl': { symbol: '<:CRYSTAL:1494594761951150202>', name: 'CRYSTAL', color: '#55FFFF' },
    
    // Sun/Flower
    'sun26': { symbol: '<:SUN:1494594807031533598>', name: 'SUN', color: '#FFAA00' },
    'flowr1': { symbol: '<:FLOWER:1494594769303896174>', name: 'FLOWER', color: '#55FF55' },
    
    // Rose/Bloom/Clover
    'rose26': { symbol: '<:ROSE:1494594788375265392>', name: 'ROSE', color: '#FF55FF' },
    'bloom1': { symbol: '<:BLOOM:1494594750756425820>', name: 'BLOOM', color: '#55FF55' },
    'clover': { symbol: '<:CLOVER:1494594759132446771>', name: 'CLOVER', color: '#55FF55' },
    
    // Music
    'note26': { symbol: '<:NOTE:1494594780624064583>', name: 'NOTE', color: '#FFFF55' },
    'music1': { symbol: '<:MUSIC:1494594777893834845>', name: 'MUSIC', color: '#FFFF55' },
    'harmny': { symbol: '<:HARMONY:1494594772655018124>', name: 'HARMONY', color: '#FFFF55' },
    'melody1': { symbol: '<:MELODY:1494594776069177394>', name: 'MELODY', color: '#FFAA00' },
    
    // Special
    'snowfl': { symbol: '<:SNOWFLAKE:1494594789847597077>', name: 'SNOWFLAKE', color: '#55FFFF' },
    'heart1': { symbol: '<:HEART:1494594774613622906>', name: 'HEART', color: '#FF5555' },
    'check1': { symbol: '<:CHECK:1494594754879553546>', name: 'CHECK', color: '#55FF55' },
    'arrow1': { symbol: '<:ARROW:1494594745694158868>', name: 'ARROW', color: '#FFAA00' },
    'yin26': { symbol: '<:YIN:1494594816804393043>', name: 'YIN', color: '#FFFFFF' },
    'biohzr': { symbol: '<:BIOHAZARD:1494594749489741894>', name: 'BIOHAZARD', color: '#00AA00' },
    
    // Moon/Sun variations
    'cresnt': { symbol: '<:CRESCENT:1494594761074540636>', name: 'CRESCENT', color: '#FFFF55' },
    'astrisk': { symbol: '<:ASTERISK:1494594746939867207>', name: 'ASTERISK', color: '#FF5555' },
    'burst1': { symbol: '<:BURST:1494594753344438394>', name: 'BURST', color: '#FFAA00' },
    'spark1': { symbol: '<:SPARK:1494594791953006662>', name: 'SPARK', color: '#FFFF55' },
    'flare1': { symbol: '<:FLARE:1494594766178881626>', name: 'FLARE', color: '#FF5555' },
    'ring26': { symbol: '<:RING:1494594785812418631>', name: 'RING', color: '#55FFFF' },
    
    // Tech
    'gear26': { symbol: '<:GEAR:1494594770771640431>', name: 'GEAR', color: '#AAAAAA' },
    'wheel1': { symbol: '<:WHEEL:1494594814086221874>', name: 'WHEEL', color: '#AAAAAA' },
    
    // Flowers
    'petal1': { symbol: '<:PETAL:1494594784436949022>', name: 'PETAL', color: '#FF55FF' },
    'blossm': { symbol: '<:BLOSSOM:1494594752107122698>', name: 'BLOSSOM', color: '#55FF55' },
    'pencil': { symbol: '<:PENCIL:1494594782868275262>', name: 'PENCIL', color: '#FFFFFF' },
    
    // Special 2
    'ohms26': { symbol: '<:OHMS:1494594781991665735>', name: 'OHMS', color: '#FFFF55' },
    'wish26': { symbol: '<:WISH:1494594815474532372>', name: 'WISH', color: '#FFAA00' },
    'felp26': { symbol: '<:FELP:1494594764786499644>', name: 'FELP', color: '#FFFFFF' },
    
    // Florenta/Rosa
    'floren': { symbol: '<:FLORENTA:1494594767718449252>', name: 'FLORENTA', color: '#FF5555' },
    'rosafl': { symbol: '<:ROSA_FLOWER:1494594787184083006>', name: 'ROSA_FLOWER', color: '#AA00AA' },
    
    // Medals
    'strmed': { symbol: '<:STAR_MEDAL:1494594801834790982>', name: 'STAR_MEDAL', color: '#FF55FF' },
    'strtmd': { symbol: '<:START_MEDAL:1494594803340546098>', name: 'START_MEDAL', color: '#00AAAA' },
    
    // Clan
    'clncl2': { symbol: '<:CLAN_CLASH_2:1494594757756977183>', name: 'CLAN_CLASH_2', color: '#FFAA00' },
    'clncl1': { symbol: '<:CLAN_CLASH:1494594756267868160>', name: 'CLAN_CLASH', color: '#FF5555' }
};

// EMOJIS DO SERVIDOR
const EMOJIS = {
    YOUTUBE: '<:Youtube:1494449422061404231>',
    TWITCH: '<:Twitch:1494449425899196583>',
    TIKTOK: '<:Tiktok:1494449424053833859>',
    COR_VERMELHO: '<:Corante_vermelhoH:1494067495613501501>',
    MLG: '<:MLG:1494067497949855917>',
    COR_VERDE: '<:Corante_verdeH:1494067489749995704>',
    BARBARIAN: '<:barbarian:1494067505378099312>',
    CTF_BLUE: '<:ctf_blue:1494067534809534464>',
    CTF_RED: '<:ctf_red:1494067536709288047>',
    BEDWARS: '<:bedwars:1494067512655089905>',
    BLINK: '<:blink:1494067514500452535>',
    LAVA: '<:lava:1494068100096720957>',
    KITMUSH: '<:kitmush:1494068091691339946>',
    SPARTA: '<:sparta:1482175426506784781>',
    CHECK: '<:check:1500966707961921657>',
    CROSS: '<:cross:1500966712525324288>'
};

// Cores dos ranks (nomes de exibição -> cor hexadecimal)
const rankColors = {
    'MEMBER': 0xAAAAAA,
    'BOOSTER': 0xFF55FF,
    'VIP': 0x55FF55,
    'SPARTA': 0xFFAA00,
    'MAX': 0xFF55FF,
    'START': 0x00AAAA,
    'MAX_PLUS': 0xAA00AA,
    'BETA': 0x0000AA,
    'TAG_CARNAVAL': 0xFFAA00,
    'TAG_HALLOWEEN': 0xAA00AA,
    'TAG_FERIAS': 0x00AA00,
    'TAG_NATAL': 0xAA0000,
    'TAG_2026': 0x55FFFF,
    'TAG_CHAMPION': 0xFFAA00,
    'PARTNER': 0x55FFFF,
    'PARTNER_PLUS': 0x55FFFF,
    'GRAMMY': 0xFFAA00,
    'BUILDER': 0x00AA00,
    'STUDIO': 0x00AA00,
    'HELPER': 0x5555FF,
    'TRIAL': 0xAA00AA,
    'MOD': 0xAA00AA,
    'MODPLUS': 0xAA00AA,
    'ADMIN': 0xAA0000,
    'CHEFE': 0xAA0000
};

// Ordem de prioridade dos ranks (do maior para o menor)
const RANK_PRIORITY = [
    'ADMIN',
    'CHEFE',
    'MODPLUS',
    'MOD',
    'TRIAL',
    'HELPER',
    'STUDIO',
    'BUILDER',
    'GRAMMY',
    'PARTNER_PLUS',
    'PARTNER',
    'TAG_CHAMPION',
    'BETA',
    'SPARTA',
    'MAX_PLUS',
    'MAX',
    'VIP',
    'BOOSTER',
    'START',
    'TAG_CARNAVAL',
    'TAG_HALLOWEEN',
    'TAG_FERIAS',
    'TAG_NATAL',
    'TAG_2026',
    'MEMBER'
];

// Função para encontrar o MAIOR rank
function getHighestRank(ranksArray) {
    if (!Array.isArray(ranksArray) || ranksArray.length === 0) return 'MEMBER';
    
    const rankNomes = ranksArray.map(r => {
        const codigo = r.rank || r;
        return RANK_CODES[codigo] || codigo;
    });
    
    for (const priority of RANK_PRIORITY) {
        if (rankNomes.includes(priority)) {
            return priority;
        }
    }
    
    return 'MEMBER';
}

// Função para normalizar tag
function normalizeTag(tagCode) {
    if (!tagCode) return 'NONE';
    return TAG_CODES[tagCode] || tagCode;
}

// Função para normalizar medalha
function normalizeMedal(medalCode) {
    if (!medalCode) return 'NONE';
    
    const medal = MEDAL_CODES[medalCode];
    if (medal) {
        return `${medal.symbol} ${medal.name}`;
    }
    
    return medalCode;
}

// Função para obter a cor da medalha
function getMedalColor(medalCode) {
    if (!medalCode) return null;
    
    const medal = MEDAL_CODES[medalCode];
    if (!medal) return null;
    
    const hexColor = medal.color.replace('#', '');
    return parseInt(hexColor, 16);
}

// Função para obter emoji do rank
function getRankEmoji(rankName) {
    const emojiMap = {
        'SPARTA': EMOJIS.SPARTA,
        'YOUTUBE': EMOJIS.YOUTUBE,
        'TWITCH': EMOJIS.TWITCH,
        'TIKTOK': EMOJIS.TIKTOK
    };
    return emojiMap[rankName] || '';
}

module.exports = {
    RANK_CODES,
    TAG_CODES,
    MEDAL_CODES,
    EMOJIS,
    rankColors,
    RANK_PRIORITY,
    getHighestRank,
    normalizeTag,
    normalizeMedal,
    getMedalColor,
    getRankEmoji
};
