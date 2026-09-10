# Como configurar o Firebase para o SpartaMC

## 1. Criar projeto no Firebase

1. Acesse: https://console.firebase.google.com/
2. Clique em "Criar projeto"
3. Dê um nome: **SpartaMC**
4. Ative Google Analytics (opcional)
5. Clique em "Criar projeto"

## 2. Configurar Firestore (Banco de dados)

1. No painel do Firebase, clique em **Firestore Database**
2. Clique em "Criar banco de dados"
3. Escolha o modo: **Teste** (permite leitura/escrita sem regras)
4. Selecione uma região próxima (ex: us-central1)
5. Clique em "Habilitar"

## 3. Configurar autenticação (opcional - já temos a própria)

1. Clique em **Authentication**
2. Clique em "Começar"
3. O sistema já usa email/senha próprio, então pode deixar assim

## 4. Obter chave de serviço (para o backend)

1. Clique em **Configurações do projeto** (ícone de engrenagem)
2. Clique em "Contas de serviço"
3. Clique em "Gerar nova chave privada"
4. Um arquivo `.json` será baixado
5. **IMPORTANTE:** Renomeie o arquivo para `firebase-key.json`

## 5. Adicionar a chave ao Vercel

1. Acesse seu projeto no Vercel
2. Vá em **Settings** → **Environment Variables**
3. Clique em "Add"
4. Nome: `FIREBASE_SERVICE_KEY`
5. Valor: Cole o conteúdo inteiro do arquivo `firebase-key.json`
6. Clique em "Save"

## 6. Estrutura das coleções no Firestore

O código cria automaticamente as coleções:
- `users` - Usuários cadastrados
- `topics` - Tópicos do fórum
- `orders` - Pedidos da loja

## 🔒 Regras de segurança (para produção)

Quando quiser colocar em produção, altere as regras do Firestore:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Usuários só manipulam seus próprios dados
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Tópicos públicos para leitura
    match /topics/{topicId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    
    // Pedidos só do próprio usuário
    match /orders/{orderId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
    }
  }
}
```

## 🚀 Após configurar

1. Faça deploy novamente para o Vercel
2. Teste a API em: `seudominio.com/api/health`
3. Deve retornar: `{ "status": "ok" }`

## 📝 Variáveis de ambiente no Vercel

Adicione estas variáveis:
- `FIREBASE_SERVICE_KEY` - O conteúdo do arquivo JSON da chave
