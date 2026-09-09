/* ============================================================
    SpartaMC - API Client (Conexão com Backend)
    ============================================================ */

const API_URL = window.location.hostname === 'localhost' 
    ? 'http://localhost:3000/api' 
    : '/api';

class SpartaAPI {
    constructor() {
        this.token = localStorage.getItem('sparta_token');
    }

    // ============ AUTENTICAÇÃO ============
    
    async register(nickname, email, password) {
        const response = await fetch(`${API_URL}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nickname, email, password })
        });
        
        const data = await response.json();
        
        if (data.token) {
            this.token = data.token;
            localStorage.setItem('sparta_token', data.token);
            localStorage.setItem('sparta_current_user', JSON.stringify(data.user));
        }
        
        return data;
    }

    async login(email, password) {
        const response = await fetch(`${API_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        
        const data = await response.json();
        
        if (data.token) {
            this.token = data.token;
            localStorage.setItem('sparta_token', data.token);
            localStorage.setItem('sparta_current_user', JSON.stringify(data.user));
        }
        
        return data;
    }

    logout() {
        this.token = null;
        localStorage.removeItem('sparta_token');
        localStorage.removeItem('sparta_current_user');
        location.reload();
    }

    getAuthHeader() {
        return this.token ? { 'Authorization': `Bearer ${this.token}` } : {};
    }

    // ============ USUÁRIO ============
    
    async getUser() {
        const response = await fetch(`${API_URL}/user/me`, {
            headers: this.getAuthHeader()
        });
        return response.json();
    }

    async updateProfile(data) {
        const response = await fetch(`${API_URL}/user/profile`, {
            method: 'PUT',
            headers: { 
                'Content-Type': 'application/json',
                ...this.getAuthHeader()
            },
            body: JSON.stringify(data)
        });
        return response.json();
    }

    // ============ FÓRUM ============
    
    async getTopics(category = null) {
        const url = category 
            ? `${API_URL}/topics?category=${category}` 
            : `${API_URL}/topics`;
        const response = await fetch(url);
        return response.json();
    }

    async createTopic(category, title, content) {
        const response = await fetch(`${API_URL}/topics`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                ...this.getAuthHeader()
            },
            body: JSON.stringify({ category, title, content })
        });
        return response.json();
    }

    async replyToTopic(topicId, content) {
        const response = await fetch(`${API_URL}/topics/${topicId}/reply`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                ...this.getAuthHeader()
            },
            body: JSON.stringify({ content })
        });
        return response.json();
    }

    async getForumStats() {
        const response = await fetch(`${API_URL}/forum/stats`);
        return response.json();
    }

    // ============ PEDIDOS ============
    
    async createOrder(items, total, discord, nickname) {
        const response = await fetch(`${API_URL}/orders`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                ...this.getAuthHeader()
            },
            body: JSON.stringify({ items, total, discord, nickname })
        });
        return response.json();
    }

    async getOrders() {
        const response = await fetch(`${API_URL}/orders`, {
            headers: this.getAuthHeader()
        });
        return response.json();
    }

    // ============ CHECK ============
    
    isLoggedIn() {
        return !!this.token;
    }

    getCurrentUser() {
        return JSON.parse(localStorage.getItem('sparta_current_user'));
    }
}

const api = new SpartaAPI();
