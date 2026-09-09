/* ============================================================
    SpartaMC - Profile Dropdown Module
    ============================================================ */

document.addEventListener('DOMContentLoaded', function() {
    initDropdown();
});

function initDropdown() {
    const userProfile = document.getElementById('userProfile');
    const profileDropdown = document.getElementById('profileDropdown');
    
    if (!userProfile) {
        console.log('[Dropdown] userProfile não encontrado');
        return;
    }
    
    // Ler usuário
    let currentUser = null;
    try {
        const stored = localStorage.getItem('sparta_current_user');
        if (stored) {
            currentUser = CryptoUtils.decrypt(stored);
            if (!currentUser) currentUser = JSON.parse(stored);
        }
    } catch (e) {
        console.log('[Dropdown] Erro ao ler usuário:', e);
    }
    
    if (!currentUser) {
        console.log('[Dropdown] Nenhum usuário logado');
        return;
    }
    
    console.log('[Dropdown] Usuário:', currentUser.nickname);
    
    // MOSTRAR PERFIL
    userProfile.style.display = 'flex';
    
    // Atualizar dados do header e dropdown
    const elements = {
        userSkinHead: document.getElementById('userSkinHead'),
        userNickname: document.getElementById('userNickname'),
        dropdownSkinHead: document.getElementById('dropdownSkinHead'),
        dropdownUserName: document.getElementById('dropdownUserName'),
        dropdownUserEmail: document.getElementById('dropdownUserEmail'),
        adminStaffBtn: document.getElementById('adminStaffBtn')
    };
    
    if (elements.userSkinHead) elements.userSkinHead.src = `https://mc-heads.net/avatar/${currentUser.nickname}`;
    if (elements.userNickname) elements.userNickname.textContent = currentUser.nickname;
    if (elements.dropdownSkinHead) elements.dropdownSkinHead.src = `https://mc-heads.net/avatar/${currentUser.nickname}`;
    if (elements.dropdownUserName) elements.dropdownUserName.textContent = currentUser.nickname;
    if (elements.dropdownUserEmail) elements.dropdownUserEmail.textContent = currentUser.email;

    // Mostrar rank colorido no dropdown
    const rankEl = document.getElementById('dropdownUserRank');
    if (rankEl) {
        if (currentUser.rank && typeof applyRankBadge !== 'undefined') {
            applyRankBadge(rankEl, currentUser.rank);
            rankEl.style.display = 'inline-block';
        } else if (currentUser.rank) {
            rankEl.textContent = currentUser.rank;
            rankEl.style.display = 'inline-block';
        }
    }
    
    // Verificar se é admin e mostrar botão de gerenciar staff
    if (elements.adminStaffBtn) {
        try {
            const isAdmin = typeof staffManager !== 'undefined' && staffManager.canManageForum(currentUser.nickname);
            elements.adminStaffBtn.style.display = isAdmin ? 'block' : 'none';
        } catch (e) {
            elements.adminStaffBtn.style.display = 'none';
        }
    }
    
    // Evento de clique no perfil - abre/fecha dropdown
    userProfile.addEventListener('click', function(e) {
        e.stopPropagation();
        const isOpen = userProfile.classList.contains('open');
        
        if (isOpen) {
            userProfile.classList.remove('open');
            if (profileDropdown) profileDropdown.classList.remove('show');
        } else {
            userProfile.classList.add('open');
            if (profileDropdown) profileDropdown.classList.add('show');
        }
    });
    
    // Fechar ao clicar fora
    document.addEventListener('click', function(e) {
        if (!userProfile.contains(e.target)) {
            userProfile.classList.remove('open');
            if (profileDropdown) profileDropdown.classList.remove('show');
        }
    });
    
    // Botão Logout
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            localStorage.removeItem('sparta_current_user');
            location.reload();
        });
    }
    
    // Links do dropdown agora são <a> tags com href correto
    
    console.log('[Dropdown] Inicializado com sucesso!');
}
