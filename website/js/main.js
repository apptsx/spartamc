/*SMC-MAIN-v3 - MySQL API*/
(function() {
    var API = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
        ? 'http://localhost:3000/api'
        : '/api';

    var CURRENT_KEY = 'sparta_current_user';

    // ── Helpers ───────────────────────────────────────────────────────────────
    function getUser() {
        try {
            var s = localStorage.getItem(CURRENT_KEY);
            if (!s) return null;
            return typeof CryptoUtils !== 'undefined' ? CryptoUtils.decrypt(s) : JSON.parse(s);
        } catch (e) { return null; }
    }

    function saveUser(user) {
        var data = typeof CryptoUtils !== 'undefined' ? CryptoUtils.encrypt(user) : JSON.stringify(user);
        localStorage.setItem(CURRENT_KEY, data);
    }

    function showNotify(msg, type) {
        var ex = document.querySelector('.site-notification');
        if (ex) ex.remove();
        var n = document.createElement('div');
        n.className = 'site-notification ' + type;
        n.textContent = msg;
        n.style.cssText = 'position:fixed;top:20px;right:20px;padding:15px 20px;border-radius:10px;color:#fff;font-weight:600;z-index:10000;max-width:300px;background:' +
            (type === 'success' ? 'rgba(34,197,94,0.95)' : 'rgba(239,68,68,0.95)');
        document.body.appendChild(n);
        setTimeout(function() { n.remove(); }, 4000);
    }

    function updateAuthUI(user) {
        var ab = document.getElementById('authButtons');
        var up = document.getElementById('userProfile');
        if (ab) ab.style.display = 'none';
        if (up) up.style.display = 'flex';
        var un = document.getElementById('userNickname');
        var uh = document.getElementById('userSkinHead');
        if (un && user) un.textContent = user.nickname;
        if (uh && user) uh.src = 'https://mc-heads.net/avatar/' + user.nickname;
    }

    function setLoading(btn, loading) {
        if (!btn) return;
        btn.disabled = loading;
        btn.style.opacity = loading ? '0.6' : '1';
    }

    // ── Modal helpers ─────────────────────────────────────────────────────────
    window.closeAllModals = function() {
        document.querySelectorAll('.auth-overlay').forEach(function(m) { m.classList.remove('active'); });
        var cm = document.getElementById('cartModal');   if (cm) cm.classList.remove('active');
        var sm = document.getElementById('sideMenu');    if (sm) sm.classList.remove('active');
        var mo = document.getElementById('menuOverlay'); if (mo) mo.classList.remove('active');
    };
    window.openLogin = function() {
        window.closeAllModals();
        var o = document.getElementById('loginOverlay');
        if (o) o.classList.add('active');
    };
    window.openRegister = function() {
        window.closeAllModals();
        var o = document.getElementById('registerOverlay');
        if (o) o.classList.add('active');
    };
    window.openCart = function() {
        var cm = document.getElementById('cartModal');
        var co = document.getElementById('cartOverlay');
        if (cm) cm.classList.add('active');
        if (co) co.classList.add('active');
    };

    // ── Init on DOM ready ─────────────────────────────────────────────────────
    document.addEventListener('DOMContentLoaded', function() {

        // Mostrar usuário logado se existir
        var cu = getUser();
        if (cu) updateAuthUI(cu);

        // ── Registro ──────────────────────────────────────────────────────────
        var regF = document.getElementById('registerForm');
        if (regF) {
            regF.onsubmit = async function(e) {
                e.preventDefault();
                var nick  = document.getElementById('regNickname').value.trim();
                var email = document.getElementById('regEmail').value.trim();
                var pass  = document.getElementById('regPassword').value;
                var conf  = document.getElementById('regConfirmPassword').value;

                if (pass !== conf)   { showNotify('Senhas diferentes', 'error'); return; }
                if (pass.length < 4) { showNotify('Senha muito curta', 'error'); return; }

                var btn = regF.querySelector('button[type=submit]');
                setLoading(btn, true);

                try {
                    var res  = await fetch(API + '/register', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ nickname: nick, email: email, password: pass })
                    });
                    var data = await res.json();

                    if (data.error) { showNotify(data.error, 'error'); return; }

                    var userData = Object.assign({}, data.user, { token: data.token });
                    saveUser(userData);
                    updateAuthUI(userData);
                    window.closeAllModals();
                    showNotify('Bem-vindo, ' + nick + '!', 'success');
                    setTimeout(function() { location.reload(); }, 500);
                } catch (err) {
                    showNotify('Erro ao conectar com o servidor.', 'error');
                } finally {
                    setLoading(btn, false);
                }
            };
        }

        // ── Login ─────────────────────────────────────────────────────────────
        var logF = document.getElementById('loginForm');
        if (logF) {
            logF.onsubmit = async function(e) {
                e.preventDefault();
                var em = document.getElementById('loginEmail').value.trim();
                var pw = document.getElementById('loginPassword').value;

                var btn = logF.querySelector('button[type=submit]');
                setLoading(btn, true);

                try {
                    var res  = await fetch(API + '/login', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ email: em, password: pw })
                    });
                    var data = await res.json();

                    if (data.error) { showNotify(data.error, 'error'); return; }

                    var userData = Object.assign({}, data.user, { token: data.token });
                    saveUser(userData);
                    updateAuthUI(userData);
                    window.closeAllModals();
                    showNotify('Login OK!', 'success');
                    setTimeout(function() { location.reload(); }, 500);
                } catch (err) {
                    showNotify('Erro ao conectar com o servidor.', 'error');
                } finally {
                    setLoading(btn, false);
                }
            };
        }

        // ── Login com Discord ─────────────────────────────────────────────────
        var dBtn = document.getElementById('discordLoginBtn');
        if (dBtn) {
            dBtn.onclick = function() {
                var state = Math.random().toString(36).substring(2, 15);
                sessionStorage.setItem('discord_oauth_state', state);
                sessionStorage.setItem('discord_oauth_type', 'login');
                var redirectUri = window.location.origin + '/settings';
                (typeof discordOAuth !== 'undefined' ? discordOAuth.loadConfig() : Promise.resolve()).then(function() {
                    var clientId = (typeof discordOAuth !== 'undefined') ? discordOAuth.clientId : 'SEU_CLIENT_ID';
                    var url = 'https://discord.com/api/oauth2/authorize?response_type=code&client_id=' + clientId + '&redirect_uri=' +
                        encodeURIComponent(redirectUri) + '&scope=identify&state=' + state;
                    window.location.href = url;
                });
            };
        }

        // ── Trocar modais ─────────────────────────────────────────────────────
        var swReg = document.getElementById('switchToRegister');
        if (swReg) swReg.onclick = function(e) { e.preventDefault(); window.openRegister(); };

        var swLog = document.getElementById('switchToLogin');
        if (swLog) swLog.onclick = function(e) { e.preventDefault(); window.openLogin(); };
    });
})();
