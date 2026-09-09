// Widget Discord Completa - SpartaMC

async function getDiscordData() {
    try {
        const [membersRes, voiceRes, guildRes, rolesRes, channelsRes] = await Promise.all([
            fetch('/api/discord/members').then(r => r.json()).catch(() => []),
            fetch('/api/discord/voice').then(r => r.json()).catch(() => []),
            fetch('/api/discord/guild').then(r => r.json()).catch(() => ({})),
            fetch('/api/discord/roles').then(r => r.json()).catch(() => []),
            fetch('/api/discord/channels').then(r => r.json()).catch(() => [])
        ]);
        
        return { members: membersRes, voice: voiceRes, guild: guildRes, roles: rolesRes, channels: channelsRes };
    } catch (e) {
        console.error('Erro ao buscar dados do Discord:', e);
        return null;
    }
}

function getAvatarUrl(userId, avatar, discriminator) {
    if (avatar) {
        return `https://cdn.discordapp.com/avatars/${userId}/${avatar}.png?size=128`;
    }
    return `https://cdn.discordapp.com/embed/avatars/${parseInt(discriminator) % 5}.png`;
}

function getStatusColor(status) {
    switch(status) {
        case 'online': return '#23a55a';
        case 'idle': return '#f0b232';
        case 'dnd': return '#f23f43';
        default: return '#80848e';
    }
}

function formatActivity(activity) {
    if (!activity) return null;
    if (activity.type === 0) return { icon: 'fas fa-gamepad', name: activity.name, detail: activity.details || activity.state };
    if (activity.type === 2) return { icon: 'fas fa-music', name: activity.name, detail: activity.state };
    if (activity.type === 1) return { icon: 'fas fa-video', name: 'Watching', detail: activity.name };
    return { icon: 'fas fa-circle', name: activity.name, detail: activity.state };
}

function getRoleColor(roleId, roles) {
    const role = roles.find(r => r.id === roleId);
    return role ? `#${role.color.toString(16).padStart(6, '0')}` : null;
}

function getRoleName(roleId, roles) {
    const role = roles.find(r => r.id === roleId);
    return role ? role.name : null;
}

async function renderDiscordWidget() {
    const membersGrid = document.getElementById('membersGrid');
    if (!membersGrid) return;
    
    const data = await getDiscordData();
    if (!data) {
        membersGrid.innerHTML = `
            <div class="empty-state">
                <i class="fab fa-discord"></i>
                <p>Erro ao conectar</p>
                <small>Tente novamente mais tarde</small>
            </div>`;
        return;
    }
    
    const { members, voice, guild, roles, channels } = data;
    
    // Filtrar membros online (não bots)
    const onlineMembers = members.filter(m => m.user_id && !m.bot);
    
    // Atualizar stats
    const totalEl = document.getElementById('totalMembers');
    const onlineEl = document.getElementById('onlineCount');
    const voiceEl = document.getElementById('voiceCount');
    const liveText = document.getElementById('liveText');
    const discordLive = document.getElementById('discordLive');
    
    if (totalEl) totalEl.textContent = guild.approximate_member_count || members.length || '0';
    if (onlineEl) onlineEl.textContent = onlineMembers.length;
    if (voiceEl) voiceEl.textContent = voice.length || '0';
    
    // Atualizar indicador live
    if (discordLive && liveText) {
        if (onlineMembers.length > 0) {
            discordLive.classList.remove('offline');
            liveText.textContent = `${onlineMembers.length} Online`;
        } else {
            discordLive.classList.add('offline');
            liveText.textContent = 'Offline';
        }
    }
    
    // Renderizar membros
    if (membersGrid) {
        if (onlineMembers.length > 0) {
            membersGrid.innerHTML = onlineMembers.map(member => {
                const activity = formatActivity(member.activities?.[0]);
                const avatarUrl = getAvatarUrl(member.user.id, member.user.avatar, member.user.discriminator);
                const status = member.client_status?.web || member.client_status?.desktop || member.client_status?.mobile || 'offline';
                const roleColor = member.roles?.[0] ? getRoleColor(member.roles[0], roles) : null;
                const roleName = member.roles?.[0] ? getRoleName(member.roles[0], roles) : null;
                
                return `
                    <div class="member-card" title="${member.nick || member.user.username}">
                        <div class="member-card-avatar">
                            <img src="${avatarUrl}" alt="${member.user.username}">
                            <div class="status-dot ${status}" style="background: ${getStatusColor(status)}"></div>
                        </div>
                        <div class="member-card-name" style="${roleColor ? `color: ${roleColor}` : ''}">${member.nick || member.user.username}</div>
                        ${roleName ? `<div class="member-card-role" style="background: ${roleColor}20; color: ${roleColor}">${roleName}</div>` : ''}
                        ${activity ? `<div class="member-card-activity"><i class="${activity.icon}"></i> ${activity.name}</div>` : ''}
                    </div>`;
            }).join('');
        } else {
            membersGrid.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-user-slash"></i>
                    <p>Nenhum membro online</p>
                    <small>Seja o primeiro a entrar!</small>
                </div>`;
        }
    }
    
    // Renderizar canais de voz
    const voiceChannels = document.getElementById('voiceChannels');
    if (voiceChannels) {
        const voiceChannelsGrouped = voice.reduce((acc, v) => {
            if (v.channel_id) {
                if (!acc[v.channel_id]) acc[v.channel_id] = [];
                acc[v.channel_id].push(v);
            }
            return acc;
        }, {});
        
        if (Object.keys(voiceChannelsGrouped).length > 0) {
            voiceChannels.innerHTML = Object.entries(voiceChannelsGrouped).map(([channelId, users]) => {
                const channel = channels.find(c => c.id === channelId);
                const channelName = channel?.name || 'Canal de Voz';
                return `
                    <div class="voice-channel">
                        <div class="voice-channel-header">
                            <i class="fas fa-volume-up"></i>
                            <span>${channelName}</span>
                            <span class="channel-count">${users.length}</span>
                        </div>
                        <div class="voice-users">
                            ${users.map(v => {
                                const vUser = members.find(m => m.user.id === v.user_id);
                                const vAvatar = vUser ? getAvatarUrl(vUser.user.id, vUser.user.avatar, vUser.user.discriminator) : '';
                                return `
                                    <div class="voice-user ${v.mute ? 'muted' : ''}">
                                        <img src="${vAvatar}" alt="${vUser?.user.username || 'User'}">
                                        <span>${vUser?.nick || vUser?.user.username || 'User'}</span>
                                        ${v.mute ? '<i class="fas fa-microphone-slash" style="margin-left: 4px;"></i>' : ''}
                                    </div>`;
                            }).join('')}
                        </div>
                    </div>`;
            }).join('');
        } else {
            voiceChannels.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-microphone-slash"></i>
                    <p>Nenhum usuário em canal de voz</p>
                </div>`;
        }
    }
    
    // Renderizar atividades (rich presence)
    const activityList = document.getElementById('activityList');
    if (activityList) {
        const membersWithActivity = members.filter(m => m.activities && m.activities.length > 0);
        
        if (membersWithActivity.length > 0) {
            const activitiesGrouped = membersWithActivity.flatMap(m => 
                m.activities.map(a => ({ ...a, member: m }))
            ).filter(a => a.type === 0 || a.type === 2);
            
            activityList.innerHTML = activitiesGrouped.slice(0, 10).map(activity => {
                const member = activity.member;
                const avatarUrl = getAvatarUrl(member.user.id, member.user.avatar, member.user.discriminator);
                const icon = activity.type === 2 ? 'fas fa-music' : 'fab fa-spotify';
                return `
                    <div class="activity-card">
                        <img src="${activity.assets?.[0]?.image ? `https://cdn.discordapp.com/activity-assets/${activity.application_id}/${activity.assets[0].image}.png` : `https://cdn.discordapp.com/embed/app-icon/${activity.application_id}/icon.png`}" alt="${activity.name}" onerror="this.src='https://cdn.discordapp.com/embed/app-icon/${activity.application_id}/icon.png'">
                        <div class="activity-info">
                            <div class="activity-name">${activity.name}</div>
                            <div class="activity-detail">${activity.details || activity.state || 'Jogando...'}</div>
                        </div>
                        <div class="activity-users">
                            <img src="${avatarUrl}" alt="${member.user.username}" title="${member.nick || member.user.username}">
                        </div>
                    </div>`;
            }).join('');
        } else {
            activityList.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-gamepad"></i>
                    <p>Nenhuma atividade</p>
                    <small>Jogue algo para aparecer aqui!</small>
                </div>`;
        }
    }
}

// Tabs
function initDiscordTabs() {
    document.querySelectorAll('.discord-tab').forEach(tab => {
        tab.addEventListener('click', () => {
            document.querySelectorAll('.discord-tab').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.discord-panel').forEach(p => p.classList.add('hidden'));
            tab.classList.add('active');
            const panel = document.getElementById(tab.dataset.tab + 'Panel');
            if (panel) panel.classList.remove('hidden');
        });
    });
}

// Init
document.addEventListener('DOMContentLoaded', () => {
    renderDiscordWidget();
    initDiscordTabs();
    setInterval(renderDiscordWidget, 30000);
});