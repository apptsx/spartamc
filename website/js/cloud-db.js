/* ============================================================
    SpartaMC - Cloud Database (JSONBin.io)
    ============================================================ */

class CloudDB {
    constructor() {
        this.apiKey = localStorage.getItem('clouddb_apikey') || '';
        this.binId = localStorage.getItem('clouddb_binid') || '';
        this.isConnected = false;
    }

    async init(apiKey, binId = null) {
        this.apiKey = apiKey;
        
        if (!binId) {
            try {
                const response = await fetch('https://api.jsonbin.io/v3/b', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-Master-Key': apiKey
                    },
                    body: JSON.stringify({
                        users: [],
                        topics: [],
                        orders: []
                    })
                });
                const data = await response.json();
                this.binId = data.metadata.id;
            } catch (e) {
                console.error('Erro ao criar bin:', e);
                return false;
            }
        } else {
            this.binId = binId;
        }

        localStorage.setItem('clouddb_apikey', this.apiKey);
        localStorage.setItem('clouddb_binid', this.binId);
        this.isConnected = true;
        return true;
    }

    async getData() {
        if (!this.isConnected) return this.getLocalData();

        try {
            const response = await fetch(`https://api.jsonbin.io/v3/b/${this.binId}/latest`, {
                headers: { 'X-Master-Key': this.apiKey }
            });
            return await response.json();
        } catch (e) {
            console.error('Erro ao buscar dados da cloud:', e);
            return this.getLocalData();
        }
    }

    async saveData(data) {
        if (!this.isConnected) {
            localStorage.setItem('sparta_cloud_cache', JSON.stringify(data));
            return true;
        }

        try {
            await fetch(`https://api.jsonbin.io/v3/b/${this.binId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Master-Key': this.apiKey
                },
                body: JSON.stringify(data)
            });
            return true;
        } catch (e) {
            console.error('Erro ao salvar na cloud:', e);
            return false;
        }
    }

    getLocalData() {
        return {
            users: JSON.parse(localStorage.getItem('sparta_db_users')) || [],
            topics: JSON.parse(localStorage.getItem('sparta_forum_topics')) || [],
            orders: JSON.parse(localStorage.getItem('sparta_orders')) || [],
            cart: JSON.parse(localStorage.getItem('sparta_cart')) || []
        };
    }

    isConfigured() {
        return this.apiKey && this.binId;
    }

    disconnect() {
        this.apiKey = '';
        this.binId = '';
        this.isConnected = false;
        localStorage.removeItem('clouddb_apikey');
        localStorage.removeItem('clouddb_binid');
    }
}

const cloudDB = new CloudDB();

// Sync Functions
async function syncToCloud() {
    if (!cloudDB.isConfigured()) {
        alert('Configure a API Key na página de configurações primeiro!');
        return;
    }

    const data = {
        users: JSON.parse(localStorage.getItem('sparta_db_users')) || [],
        topics: JSON.parse(localStorage.getItem('sparta_forum_topics')) || [],
        orders: JSON.parse(localStorage.getItem('sparta_orders')) || []
    };

    const success = await cloudDB.saveData(data);
    if (success) {
        alert('Dados sincronizados com sucesso!');
    } else {
        alert('Erro ao sincronizar dados.');
    }
}

async function syncFromCloud() {
    if (!cloudDB.isConfigured()) {
        alert('Configure a API Key na página de configurações primeiro!');
        return;
    }

    const data = await cloudDB.getData();
    if (data && data.record) {
        localStorage.setItem('sparta_db_users', JSON.stringify(data.record.users || []));
        localStorage.setItem('sparta_forum_topics', JSON.stringify(data.record.topics || []));
        localStorage.setItem('sparta_orders', JSON.stringify(data.record.orders || []));
        alert('Dados baixados da cloud com sucesso!');
        location.reload();
    }
}
