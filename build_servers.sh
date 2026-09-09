#!/bin/bash

echo "========================================"
echo "  Compilando Duzy Server"
echo "========================================"
echo ""

cd "$(dirname "$0")"

# Dar permissão ao gradlew se necessário
chmod +x gradlew

echo "[1/4] Compilando modulos..."
bash gradlew :core-bungee:build :core-bukkit:build :lobby:build :pvp:build :bedwars:build :duels:build :auth:build --no-daemon

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERRO] Falha na compilacao!"
    exit 1
fi

echo ""
echo "[2/4] Copiando arquivos para /root/servers/..."

SERVERS_DIR="/sdcard/Sparta/server"

# Criar pastas se não existirem
for server in hub hub-bedwars hub-duels hub-pvp hub-aquatic bedwars-1 bedwars-2 duels pvp aquatic auth bungee; do
    mkdir -p "$SERVERS_DIR/$server/plugins"
done

# Copiar lobby para hubs
cp -f lobby/build/libs/lobby.jar "$SERVERS_DIR/hub/plugins/" && echo "    - lobby.jar -> hub/plugins"
cp -f lobby/build/libs/lobby.jar "$SERVERS_DIR/hub-bedwars/plugins/" && echo "    - lobby.jar -> hub-bedwars/plugins"
cp -f lobby/build/libs/lobby.jar "$SERVERS_DIR/hub-duels/plugins/" && echo "    - lobby.jar -> hub-duels/plugins"
cp -f lobby/build/libs/lobby.jar "$SERVERS_DIR/hub-pvp/plugins/" && echo "    - lobby.jar -> hub-pvp/plugins"

# Copiar bedwars para bedwars servers
cp -f bedwars/build/libs/bedwars.jar "$SERVERS_DIR/bedwars-1/plugins/" && echo "    - bedwars.jar -> bedwars-1/plugins"
cp -f bedwars/build/libs/bedwars.jar "$SERVERS_DIR/bedwars-2/plugins/" && echo "    - bedwars.jar -> bedwars-2/plugins"

# Copiar outros plugins
cp -f duels/build/libs/duels.jar "$SERVERS_DIR/duels/plugins/" && echo "    - duels.jar -> duels/plugins"
cp -f pvp/build/libs/pvp.jar "$SERVERS_DIR/pvp/plugins/" && echo "    - pvp.jar -> pvp/plugins"
cp -f auth/build/libs/auth.jar "$SERVERS_DIR/auth/plugins/" && echo "    - auth.jar -> auth/plugins"

# Copiar core-bungee para bungee
cp -f core-bungee/build/libs/core-bungee.jar "$SERVERS_DIR/bungee/plugins/" && echo "    - core-bungee.jar -> bungee/plugins"

echo ""
echo "[2.5/4] Copiando plugins para /sdcard/Sparta/plugins/..."

PLUGINS_DIR="/sdcard/Sparta/plugins"
mkdir -p "$PLUGINS_DIR"

cp -f lobby/build/libs/lobby.jar "$PLUGINS_DIR/" && echo "    - lobby.jar"
cp -f bedwars/build/libs/bedwars.jar "$PLUGINS_DIR/" && echo "    - bedwars.jar"
cp -f duels/build/libs/duels.jar "$PLUGINS_DIR/" && echo "    - duels.jar"
cp -f pvp/build/libs/pvp.jar "$PLUGINS_DIR/" && echo "    - pvp.jar"
cp -f auth/build/libs/auth.jar "$PLUGINS_DIR/" && echo "    - auth.jar"
cp -f core-bungee/build/libs/core-bungee.jar "$PLUGINS_DIR/" && echo "    - core-bungee.jar"
cp -f core-bukkit/build/libs/core-bukkit.jar "$PLUGINS_DIR/" && echo "    - core-bukkit.jar"

echo ""
echo "[3/4] Listando arquivos..."

echo "    Hub plugins:"
ls -la "$SERVERS_DIR/hub/plugins/" 2>/dev/null | grep -E "\.jar$"

echo ""
echo "[4/4] Build completo!"
echo "========================================"
