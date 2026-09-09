/* SpartaMC - Ranks */
var RANK_CONFIG = {
    'laranja': { label: 'Laranja', color: '#f97316', bg: 'rgba(249,115,22,0.2)',  border: '#f97316' },
    'admin':   { label: 'Admin',   color: '#cc2222', bg: 'rgba(139,0,0,0.25)',    border: '#cc2222' },
    'mod+':    { label: 'Mod+',    color: '#7b2fbe', bg: 'rgba(75,0,130,0.25)',   border: '#7b2fbe' },
    'mod':     { label: 'Mod',     color: '#a855f7', bg: 'rgba(138,43,226,0.2)',  border: '#a855f7' },
    'trial':   { label: 'Trial',   color: '#ec4899', bg: 'rgba(236,72,153,0.2)',  border: '#ec4899' },
    'helper':  { label: 'Helper',  color: '#06b6d4', bg: 'rgba(6,182,212,0.2)',   border: '#06b6d4' }
};

function getRankConfig(rank) {
    if (!rank) return null;
    return RANK_CONFIG[rank.toLowerCase()] || null;
}

function applyRankBadge(el, rank) {
    if (!el) return;
    var cfg = getRankConfig(rank);
    if (cfg) {
        el.textContent = cfg.label;
        el.style.color      = cfg.color;
        el.style.background = cfg.bg;
        el.style.border     = '1px solid ' + cfg.border;
        el.style.display    = 'inline-block';
    } else {
        el.textContent = 'Membro';
        el.style.color      = '#888';
        el.style.background = 'rgba(255,255,255,0.07)';
        el.style.border     = '1px solid rgba(255,255,255,0.15)';
        el.style.display    = 'inline-block';
    }
}
