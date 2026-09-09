// js/auth.js
class AuthAPI {
    constructor() {
        this.apiUrl = window.location.hostname === 'localhost' 
            ? 'http://localhost:3000/api'
            : '/api';
        this.useLocalStorage = false;
    }
    
    async register(nickname, email, password) {
        try {
            const response = await fetch(`${this.apiUrl}/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ nickname, email, password })
            });
            const data = await response.json();
            
            if (data.success || data.useLocalStorage) {
                if (data.useLocalStorage || !data.success) {
                    return this.localRegister(nickname, email, password);
                }
                return data;
            }
            return data;
        } catch (error) {
            console.error('API error, using localStorage fallback');
            return this.localRegister(nickname, email, password);
        }
    }
    
    async login(email, password) {
        try {
            const response = await fetch(`${this.apiUrl}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });
            const data = await response.json();
            
            if (data.success || data.useLocalStorage) {
                if (data.useLocalStorage || !data.success) {
                    return this.localLogin(email, password);
                }
                return data;
            }
            return data;
        } catch (error) {
            console.error('API error, using localStorage fallback');
            return this.localLogin(email, password);
        }
    }
    
    localRegister(nickname, email, password) {
        const users = JSON.parse(localStorage.getItem('sparta_db_users')) || [];
        
        // Verificar email duplicado
        if (users.find(u => u.email === email)) {
            return { success: false, message: 'Este e-mail já está cadastrado!' };
        }
        
        // Verificar nickname duplicado
        if (users.find(u => u.nickname === nickname)) {
            return { success: false, message: 'Este nickname já está em uso!' };
        }
        
        const newUser = { 
            id: Date.now().toString(),
            nickname, 
            email, 
            password, 
            createdAt: new Date().toISOString() 
        };
        users.push(newUser);
        localStorage.setItem('sparta_db_users', JSON.stringify(users));
        
        return { success: true, user: { id: newUser.id, nickname, email } };
    }
    
    localLogin(email, password) {
        const users = JSON.parse(localStorage.getItem('sparta_db_users')) || [];
        const user = users.find(u => u.email === email && u.password === password);
        
        if (user) {
            return { success: true, user: { id: user.id, nickname: user.nickname, email: user.email } };
        }
        
        return { success: false, message: 'E-mail ou senha incorretos!' };
    }
}

const authAPI = new AuthAPI();