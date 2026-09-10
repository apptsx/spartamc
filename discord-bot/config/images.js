module.exports = {
    // URL para avatar/busto do jogador (usada no menu e outros lugares)
    avatar: (nick) => `https://starlightskins.lunareclipse.studio/render/dungeons/${nick}/bust?borderHighlight=true&borderHighlightRadius=5`,
    
    // URL para cabeça 2D (usada em alguns lugares)
    head: (nick) => `https://starlightskins.lunareclipse.studio/render/dungeons/${nick}/head?borderHighlight=true&borderHighlightRadius=5`,
    
    // URL para corpo completo
    body: (nick) => `https://starlightskins.lunareclipse.studio/render/dungeons/${nick}/full?borderHighlight=true&borderHighlightRadius=5`,
    
    // URL para download da skin
    skin: (nick) => `https://mc-heads.net/download/${nick}`,
    
    // URL antiga do mc-heads (caso precise como fallback)
    mcHeadsAvatar: (nick) => `https://mc-heads.net/avatar/${nick}`,
    mcHeadsHead: (nick) => `https://mc-heads.net/head/${nick}`
};
