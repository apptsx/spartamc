/* ============================================================
    SpartaMC - Forum API Client (MySQL via REST)
    ============================================================ */

class ForumAPI {
    constructor() {
        this.base = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
            ? 'http://localhost:3000/api'
            : '/api';
    }

    _token() {
        try {
            const stored = localStorage.getItem('sparta_current_user');
            if (!stored) return null;
            const user = typeof CryptoUtils !== 'undefined' ? CryptoUtils.decrypt(stored) : JSON.parse(stored);
            return user?.token || null;
        } catch { return null; }
    }

    _headers(auth = false) {
        const h = { 'Content-Type': 'application/json' };
        if (auth) { const t = this._token(); if (t) h['Authorization'] = `Bearer ${t}`; }
        return h;
    }

    // ── Categorias ────────────────────────────────────────────
    async getCategories() {
        const res = await fetch(`${this.base}/forum/categories`);
        return res.ok ? res.json() : [];
    }

    async createCategory(data) {
        const res = await fetch(`${this.base}/forum/categories`, { method: 'POST', headers: this._headers(true), body: JSON.stringify(data) });
        return res.json();
    }

    async updateCategory(slug, data) {
        const res = await fetch(`${this.base}/forum/categories/${slug}`, { method: 'PUT', headers: this._headers(true), body: JSON.stringify(data) });
        return res.json();
    }

    async deleteCategory(slug) {
        const res = await fetch(`${this.base}/forum/categories/${slug}`, { method: 'DELETE', headers: this._headers(true) });
        return res.json();
    }

    // ── Tópicos ───────────────────────────────────────────────
    async getTopics(category = null) {
        const url = category ? `${this.base}/topics?category=${category}` : `${this.base}/topics`;
        const res = await fetch(url);
        return res.ok ? res.json() : [];
    }

    async getTopicById(id) {
        const res = await fetch(`${this.base}/topics/${id}`);
        return res.ok ? res.json() : null;
    }

    async createTopic(category, title, content, pinned = false) {
        const res = await fetch(`${this.base}/topics`, { method: 'POST', headers: this._headers(true), body: JSON.stringify({ category, title, content, pinned }) });
        return res.json();
    }

    async editTopic(topicId, title, content) {
        const res = await fetch(`${this.base}/topics/${topicId}`, { method: 'PUT', headers: this._headers(true), body: JSON.stringify({ title, content }) });
        return res.json();
    }

    async deleteTopic(topicId) {
        const res = await fetch(`${this.base}/topics/${topicId}`, { method: 'DELETE', headers: this._headers(true) });
        return res.json();
    }

    async pinTopic(topicId, pinned) {
        const res = await fetch(`${this.base}/topics/${topicId}/pin`, { method: 'PATCH', headers: this._headers(true), body: JSON.stringify({ pinned }) });
        return res.json();
    }

    async lockTopic(topicId, locked) {
        const res = await fetch(`${this.base}/topics/${topicId}/lock`, { method: 'PATCH', headers: this._headers(true), body: JSON.stringify({ locked }) });
        return res.json();
    }

    // ── Respostas ─────────────────────────────────────────────
    async addReply(topicId, content) {
        const res = await fetch(`${this.base}/topics/${topicId}/reply`, { method: 'POST', headers: this._headers(true), body: JSON.stringify({ content }) });
        return res.json();
    }

    async deleteReply(replyId) {
        const res = await fetch(`${this.base}/replies/${replyId}`, { method: 'DELETE', headers: this._headers(true) });
        return res.json();
    }

    async getStats() {
        const res = await fetch(`${this.base}/forum/stats`);
        return res.ok ? res.json() : {};
    }
}

const forumAPI = new ForumAPI();
