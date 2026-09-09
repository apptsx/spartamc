# SpartaMC — Estrutura do Projeto

Este projeto foi organizado separando o código original em arquivos independentes por responsabilidade.

## Estrutura de Pastas

```
spartamc/
├── index.html          ← HTML estrutural (sem CSS ou JS inline)
├── css/
│   └── style.css       ← Todos os estilos do site
├── js/
│   └── main.js         ← Todos os scripts (busca, carrinho, CTAs)
└── images/
    └── README.md       ← Guia de imagens a serem adicionadas
```

## Como usar

1. Abra o arquivo `index.html` em qualquer navegador moderno.
2. Os arquivos `css/style.css` e `js/main.js` são carregados automaticamente.
3. Para adicionar imagens reais, coloque-as na pasta `images/` e atualize os atributos `src` no `index.html` conforme indicado nos comentários.

## Dependências externas

| Recurso         | URL                                                              |
|-----------------|------------------------------------------------------------------|
| Font Awesome 6  | `https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/...` |
| Imagens (placeholder) | `https://placehold.co` (substituir por imagens reais)      |
