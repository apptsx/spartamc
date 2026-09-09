#!/bin/bash

echo "=============================="
echo "  SpartaMC - Iniciando Servidor"
echo "=============================="

# Verifica se node está instalado
if ! command -v node &> /dev/null; then
    echo "Erro: Node.js não encontrado. Instale com: pkg install nodejs"
    exit 1
fi

# Verifica se screen está instalado
if ! command -v screen &> /dev/null; then
    echo "Erro: screen não encontrado. Instale com: apt install screen"
    exit 1
fi

# Inicia MySQL se não estiver rodando
if ! mysqladmin ping --silent 2>/dev/null; then
    echo "Iniciando MySQL..."
    mkdir -p /run/mysqld
    chown mysql:mysql /run/mysqld 2>/dev/null
    screen -dmS mysql bash -c "mariadbd --user=mysql --datadir=/var/lib/mysql"
    sleep 3
    mysql -u root -e "CREATE DATABASE IF NOT EXISTS sparta CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci" 2>/dev/null
    echo "MySQL pronto"
fi

# Mata sessão anterior do servidor
screen -S spartamc -X quit 2>/dev/null
sleep 1

# Inicia o servidor
echo "Iniciando SpartaMC..."
cd "$(dirname "$0")/server"
screen -dmS spartamc node server.js
sleep 3

# Verifica se subiu
if curl -s http://localhost:3000/api/health | grep -q '"ok"'; then
    echo "✅ Servidor rodando em http://localhost:3000"
    echo ""
    echo "Comandos:"
    echo "  Ver logs:   screen -r spartamc"
    echo "  Sair:       Ctrl+A depois D"
    echo "  Parar:      screen -S spartamc -X quit"
else
    echo "⚠️  Servidor pode não ter iniciado corretamente."
    echo "   Verifique com: screen -r spartamc"
fi
