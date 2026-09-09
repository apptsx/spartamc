/* ============================================================
    SpartaMC - Staff/Ranks Management
    ============================================================ */

class StaffManager {
    constructor() {
        this.staffDB = new SecureDB('sparta_staff_secure');
    }
    
    // Obter todos os membros da equipe
    getStaffMembers() {
        return this.staffDB.get('members') || [];
    }
    
    // Adicionar membro à equipe
    addMember(nickname, rankId) {
        const members = this.getStaffMembers();
        
        // Verificar se já existe
        if (members.find(m => m.nickname.toLowerCase() === nickname.toLowerCase())) {
            return { success: false, error: 'Este jogador já está na equipe!' };
        }
        
        const rank = CryptoUtils.getRankById(rankId);
        if (!rank) {
            return { success: false, error: 'Cargo inválido!' };
        }
        
        const newMember = {
            id: Date.now().toString(),
            nickname,
            rank: rankId,
            addedAt: new Date().toISOString()
        };
        
        members.push(newMember);
        this.staffDB.set('members', members);
        
        return { success: true, member: newMember };
    }
    
    // Remover membro da equipe
    removeMember(nickname) {
        const members = this.getStaffMembers();
        const filtered = members.filter(m => m.nickname.toLowerCase() !== nickname.toLowerCase());
        
        if (filtered.length === members.length) {
            return { success: false, error: 'Membro não encontrado!' };
        }
        
        this.staffDB.set('members', filtered);
        return { success: true };
    }
    
    // Atualizar cargo de membro
    updateMemberRank(nickname, newRankId) {
        const members = this.getStaffMembers();
        const memberIndex = members.findIndex(m => m.nickname.toLowerCase() === nickname.toLowerCase());
        
        if (memberIndex === -1) {
            return { success: false, error: 'Membro não encontrado!' };
        }
        
        const rank = CryptoUtils.getRankById(newRankId);
        if (!rank) {
            return { success: false, error: 'Cargo inválido!' };
        }
        
        members[memberIndex].rank = newRankId;
        this.staffDB.set('members', members);
        
        return { success: true };
    }
    
    // Verificar se usuário é staff
    isStaff(nickname) {
        const members = this.getStaffMembers();
        return members.find(m => m.nickname.toLowerCase() === nickname.toLowerCase());
    }
    
    // Obter cargo do usuário
    getUserRank(nickname) {
        const member = this.isStaff(nickname);
        if (!member) return null;
        return CryptoUtils.getRankById(member.rank);
    }
    
    // Verificar se usuário pode executar ação admin
    canManageForum(nickname) {
        const rank = this.getUserRank(nickname);
        if (!rank) return false;
        return rank.power >= 3; // Moderador ou superior
    }
    
    // Obter membros por cargo
    getMembersByRank(rankId) {
        return this.getStaffMembers().filter(m => m.rank === rankId);
    }
    
    // Inicializar staff padrão
    initDefaultStaff() {
        const members = this.getStaffMembers();
        if (members.length === 0) {
            // Adicionar uNyko como fundador
            this.addMember('uNyko', 'fundador');
        }
    }
}

// Instância global
const staffManager = new StaffManager();
staffManager.initDefaultStaff();

// Funções globais para templates
window.getStaffMembers = () => staffManager.getStaffMembers();
window.getUserRank = (nickname) => staffManager.getUserRank(nickname);
window.isStaff = (nickname) => staffManager.isStaff(nickname);
window.canManageForum = (nickname) => staffManager.canManageForum(nickname);
