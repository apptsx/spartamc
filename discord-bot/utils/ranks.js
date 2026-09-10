// ranks.js - VERSÃO ATUALIZADA
// Mapeamento completo de ranks (nomes da database + nomes de exibição)
const RANK_MAPPING = {
    // ADMINISTRAÇÃO
    'ADMIN': 'Admin',
    'CHEFE': 'Dono',
    
    // MODERAÇÃO
    'MODPLUS': 'Mod+',
    'MOD': 'Mod',
    'TRIAL': 'Trial',
    'HELPER': 'Helper',
    
    // CRIADORES
    'STUDIO': 'Studio',
    'BUILDER': 'Builder',
    'GRAMMY': 'Grammy',
    
    // PARTNERS
    'PARTNER_PLUS': 'Partner+',
    'PARTNER': 'Partner',
    
    // DESTAQUES
    'SPARTA': 'Sparta',
    'VIP': 'Vip',
    'MAX_PLUS': 'Max+',
    'MAX': 'Max',
    'BOOSTER': 'Booster',
    'START': 'Start',
    'BETA': 'Beta',
    'TAG_CHAMPION': 'Champion',
    
    // TAGS TEMPORÁRIAS
    'TAG_CARNAVAL': 'Carnaval',
    'TAG_HALLOWEEN': 'Halloween',
    'TAG_FERIAS': 'Férias',
    'TAG_NATAL': 'Natal',
    'TAG_2026': '2026',
    
    // MEMBRO
    'MEMBER': 'Membro',
    
    // Apelidos alternativos
    'admin': 'Admin',
    'administrator': 'Admin',
    'chef': 'Dono',
    'mod+': 'Mod+',
    'moderator+': 'Mod+',
    'mod': 'Mod',
    'moderator': 'Mod',
    'trial': 'Trial',
    'trialmoderator': 'Trial',
    'helper': 'Helper',
    'studio': 'Studio',
    'builder': 'Builder',
    'grammy': 'Grammy',
    'partner+': 'Partner+',
    'partnerplus': 'Partner+',
    'partner': 'Partner',
    'sparta': 'Sparta',
    'vip': 'Vip',
    'max+': 'Max+',
    'maxplus': 'Max+',
    'max': 'Max',
    'booster': 'Booster',
    'start': 'Start',
    'beta': 'Beta',
    'champion': 'Champion',
    'carnaval': 'Carnaval',
    'halloween': 'Halloween',
    'ferias': 'Férias',
    'natal': 'Natal',
    '2026': '2026',
    'membro': 'Membro',
    'member': 'Membro',
    'normal': 'Membro',
    'default': 'Membro'
};

// Cores dos ranks (nome de exibição -> cor hexadecimal)
const rankColors = {
    'Admin': 0xAA0000,
    'Dono': 0xAA0000,
    'Mod+': 0xAA00AA,
    'Mod': 0xAA00AA,
    'Trial': 0xAA00AA,
    'Helper': 0x5555FF,
    'Studio': 0x00AA00,
    'Builder': 0x00AA00,
    'Grammy': 0xFFAA00,
    'Partner+': 0x55FFFF,
    'Partner': 0x55FFFF,
    'Champion': 0xFFAA00,
    'Beta': 0x0000AA,
    'Sparta': 0xFFAA00,
    'Vip': 0x55FF55,
    'Max+': 0xAA00AA,
    'Max': 0xFF55FF,
    'Booster': 0xFF55FF,
    'Start': 0x00AAAA,
    'Carnaval': 0xFFAA00,
    'Halloween': 0xAA00AA,
    'Férias': 0x00AA00,
    'Natal': 0xAA0000,
    '2026': 0x55FFFF,
    'Membro': 0xAAAAAA
};

// Ordem de prioridade dos ranks (do maior para o menor)
const RANK_PRIORITY = [
    'Admin',
    'Dono',
    'Mod+',
    'Mod',
    'Trial',
    'Helper',
    'Studio',
    'Builder',
    'Grammy',
    'Partner+',
    'Partner',
    'Champion',
    'Beta',
    'Sparta',
    'Max+',
    'Max',
    'Vip',
    'Booster',
    'Start',
    'Carnaval',
    'Halloween',
    'Férias',
    'Natal',
    '2026',
    'Membro'
];

// Função para normalizar um rank
function normalizeRank(rank) {
    if (!rank) return 'Membro';
    
    if (rankColors[rank]) return rank;
    
    const normalized = RANK_MAPPING[rank];
    if (normalized) return normalized;
    
    const upperRank = rank.toUpperCase();
    if (RANK_MAPPING[upperRank]) return RANK_MAPPING[upperRank];
    
    const lowerRank = rank.toLowerCase();
    if (RANK_MAPPING[lowerRank]) return RANK_MAPPING[lowerRank];
    
    return rank;
}

// Função para encontrar o rank principal
function getMainRank(ranksList) {
    if (!Array.isArray(ranksList) || ranksList.length === 0) return 'Membro';
    
    const normalizedRanks = ranksList.map(r => normalizeRank(r));
    
    for (const priority of RANK_PRIORITY) {
        if (normalizedRanks.includes(priority)) {
            return priority;
        }
    }
    
    return 'Membro';
}

module.exports = {
    RANK_MAPPING,
    rankColors,
    RANK_PRIORITY,
    normalizeRank,
    getMainRank
};
