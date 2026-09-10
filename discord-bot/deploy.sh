#!/bin/bash
# Deploy botzao para o servidor via SSH

SRC="$(cd "$(dirname "$0")" && pwd)"
HOST="${SSH_HOST:-seu-servidor.com}"
USER="${SSH_USER:-root}"
DEST="${SSH_DEST:-/home/}"
SSH_OPTS="-o StrictHostKeyChecking=no"
SSH_PASSWORD="${SSH_PASSWORD:-}"

if ! command -v sshpass >/dev/null 2>&1; then
    echo "[!] sshpass nao instalado. Instale com: apt install sshpass"
    exit 1
fi

echo "[*] Enviando pasta para $HOST:${DEST}botzao/ ..."

sshpass -p "$SSH_PASSWORD" ssh $SSH_OPTS "$USER@$HOST" \
    "mkdir -p ${DEST}botzao" || exit 1

cd "$SRC"

tar cz \
    --exclude='node_modules' \
    --exclude='.npm' \
    --exclude='bot.zip' \
    . | sshpass -p "$SSH_PASSWORD" ssh $SSH_OPTS "$USER@$HOST" \
    "tar xz -C ${DEST}botzao"

if [ $? -ne 0 ]; then
    echo "[!] Erro ao enviar. Verifique a conexao."
    exit 1
fi

echo "[+] Envio concluido com sucesso!"
echo "[*] Atualizando dependencias no servidor..."

sshpass -p "$SSH_PASSWORD" ssh $SSH_OPTS "$USER@$HOST" \
    "export NVM_DIR=\"\$HOME/.nvm\" && [ -s \"\$NVM_DIR/nvm.sh\" ] && . \"\$NVM_DIR/nvm.sh\" && nvm use 18 && cd ${DEST}botzao && npm install"

if [ $? -eq 0 ]; then
    echo "[+] Dependencias atualizadas com sucesso!"
    echo "[+] Deploy concluido!"
else
    echo "[!] Erro ao atualizar dependencias."
fi
