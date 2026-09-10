# Como configurar o MongoDB para o SpartaMC

## 1. Criar cluster no MongoDB Atlas

1. Acesse: https://www.mongodb.com/cloud/atlas
2. Crie uma conta gratuita
3. Clique em "Create a Free Cluster"
4. Escolha:
   - Provider: **AWS**
   - Region: **São Paulo (sa-east-1)** ou outra próxima
   - Cluster Tier: **Free (M0)**
5. Clique em "Create Cluster"

## 2. Configurar acesso ao banco

1. No menu lateral: **Database Access**
2. Clique em "Add New Database User"
3. Preencha:
   - Username: `Vercel-Admin-Lands`
   - Password: **Ger uma senha** ou use a que você já tem
4. Clique em "Add User"

## 3. Configurar rede (Importante!)

1. No menu lateral: **Network Access**
2. Clique em "Add IP Address"
3. Selecione **Allow Access from Anywhere (0.0.0.0/0)**
4. Clique em "Confirm"

## 4. Obter string de conexão

1. No menu lateral: **Database**
2. Clique em "Connect" no seu cluster
3. Selecione "Connect your application"
4. Copie a string de conexão:
```
mongodb+srv://<username>:<password>@<cluster>.mongodb.net/
```

## 5. Adicionar ao Vercel

1. Acesse seu projeto no Vercel
2. Vá em **Settings** → **Environment Variables**
3. Adicione:
   - Nome: `MONGODB_URI`
   - Valor: Sua string de conexão completa
   - Exemplo: `mongodb+srv://Vercel-Admin-Lands:minhasenha@lands.nboja2t.mongodb.net/`

## 6. Deploy

```bash
cd server
npm install
vercel deploy
```

## 7. Testar

Acesse: `seudominio.com/api/health`
Deve retornar: `{ "status": "ok" }`

## 📋 Variáveis de ambiente

No Vercel, adicione:
- `MONGODB_URI` - String de conexão do MongoDB

## 🗄️ Coleções criadas automaticamente

O sistema cria automaticamente:
- `users` - Usuários cadastrados
- `topics` - Tópicos do fórum  
- `orders` - Pedidos da loja
